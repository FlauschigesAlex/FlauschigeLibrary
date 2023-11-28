package at.flauschigesalex.lib.minecraft.paper.base.command

import at.flauschigesalex.lib.minecraft.brigadier.*
import at.flauschigesalex.lib.minecraft.brigadier.types.internal.GreedyArgumentType
import at.flauschigesalex.lib.minecraft.brigadier.types.primitive.number.NumberArgumentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import kotlin.coroutines.CoroutineContext

@OptIn(CommandInternal::class)
object CommandConfigurator {
    
    internal val registeredCommands = mutableSetOf<CommandBuilder>()

    init {

        CommandBase.COMMAND_REGISTRAR = { commandBuilder ->
            val instance = object : Command(commandBuilder.command, "", "", commandBuilder.aliases) {
                override fun execute(sender: CommandSender, command: String, stringArgs: Array<out String>): Boolean {
                    executeAsync(sender, command, stringArgs, commandBuilder.dispatcher)
                    return true
                }

                private fun executeAsync(sender: CommandSender, command: String, stringArgs: Array<out String>, context: CoroutineContext?) {
                    if (context != null) {
                        CoroutineScope(context).launch {
                            executeAsync(sender, command, stringArgs, null)
                        }
                        return
                    }

                    val paperArgs = stringArgs.filter { it.isNotEmpty() }.toTypedArray()

                    var current: CommandBase = commandBuilder
                    val fullCommand = "$command ${paperArgs.joinToString(" ")}"

                    val dataList = CommandArgumentDataList()
                    val argList = mutableSetOf<CommandArgument<*>>()

                    paperArgs.forEachIndexed paperArgs@{ index, string -> 
                        val success = current.eligibleArguments.filterNot { 
                            argList.contains(it.second)
                        }.any required@{ (base, any) ->
                            val type = any.type
                            val value = runBlocking { type.parse(string, sender) } ?: return@required false
                            
                            if (sender is Player && !any.canUse(sender.uniqueId, fullCommand, argList, paperArgs))
                                return@required false
                            
                            if (type is NumberArgumentType<*>) {
                                if (value !is Number)
                                    throw IllegalStateException()

                                if (type.allowRange(sender, value) != true)
                                    return@required false
                            }

                            if (type is GreedyArgumentType<*, *>) {
                                val values = paperArgs.copyOfRange(index, paperArgs.size).map { arg -> runBlocking { type.parse(arg, sender) } }
                                if (values.any { it == null })
                                    return@required false
                                
                                val data = GreedyCommandArgumentData(index, values, any, type)
                                dataList.add(data)
                                argList.add(any)
                                
                                val executor = any.commandSuccess ?: any.commandFail
                                return executor.invoke(sender, fullCommand, dataList, paperArgs)
                            } else {
                                val data = CommandArgumentData(index, value, any, type)
                                dataList.add(data)
                                argList.add(any)
                            }
                            
                            if (any.optional && commandBuilder.optionalArgumentMode != OptionalArgumentMode.ORDERED)
                                return@required true
                            
                            current = any
                            return@required true
                        }
                        
                        if (success.not())
                            return current.commandFail(sender, fullCommand, dataList, paperArgs)
                    }

                    val providedExecutor = argList.toMutableList().apply {
                        while (this.count { it.optional.not() } > 1)
                            this.removeFirst()
                    }.reversed().firstOrNull { it.commandSuccess != null }?.commandSuccess
                    
                    var optionalExecutor = current
                    while (true) {
                        val arguments = current.arguments
                        val preferred = arguments.filter { 
                            if (sender is Player)
                                it.canUse(sender.uniqueId, fullCommand, argList, paperArgs) 
                            else true
                        }.find { it.optional } ?: break
                        
                        if (preferred.commandSuccess != null)
                            optionalExecutor = preferred
                        
                        current = preferred
                    }

                    val executor = providedExecutor ?: optionalExecutor.commandSuccess ?: optionalExecutor.commandFail
                    
                    return executor.invoke(sender, fullCommand, dataList, paperArgs)
                }

                override fun tabComplete(
                    sender: CommandSender,
                    alias: String,
                    paperArgs: Array<out String>,
                    location: Location?
                ): MutableList<String> = mutableListOf()
            }

            // sets the permission of this command (can be null)
            instance.permission = commandBuilder.permission

            // registers this command to the server
            Bukkit.getCommandMap().register(commandBuilder.command, commandBuilder.label, instance)
            this.registeredCommands.add(commandBuilder)
        }

        @Suppress("DEPRECATION")
        CommandBase.COMMAND_CAN_USE = use@{ uuid, commandArgument, fullCommand, data, args ->
            val permission = commandArgument.permission
            val sender = Bukkit.getPlayer(uuid)!!

            return@use (permission == null || sender.hasPermission(permission)) && commandArgument.requirements.all { it(sender, fullCommand, data, args) }
        }

        CommandBase.COMMAND_HAS_PERMISSION = permission@{ uuid, commandArgument ->
            val sender = Bukkit.getPlayer(uuid)!!

            var current: CommandBase? = commandArgument
            while (current != null) {

                val perm = current.permission
                if (perm != null && !sender.hasPermission(perm))
                    return@permission false

                current = if (current is CommandArgument<*>) current.parent
                          else null
            }

            return@permission true
        }
    }
}