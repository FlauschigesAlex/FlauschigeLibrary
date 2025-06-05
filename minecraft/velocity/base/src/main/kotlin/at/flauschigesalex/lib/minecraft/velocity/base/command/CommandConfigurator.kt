package at.flauschigesalex.lib.minecraft.velocity.base.command

import at.flauschigesalex.lib.minecraft.brigadier.*
import at.flauschigesalex.lib.minecraft.brigadier.types.internal.GreedyArgumentType
import at.flauschigesalex.lib.minecraft.brigadier.types.primitive.number.NumberArgumentType
import at.flauschigesalex.lib.minecraft.velocity.base.FlauschigeLibraryVelocity
import at.flauschigesalex.lib.minecraft.velocity.base.internal.CommandData
import at.flauschigesalex.lib.minecraft.velocity.base.internal.VelocityCommand
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.command.RawCommand
import com.velocitypowered.api.proxy.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.CoroutineContext

@OptIn(CommandInternal::class)
object CommandConfigurator {

    internal val registeredCommands = mutableSetOf<CommandBuilder>()

    init {

        CommandBase.COMMAND_REGISTRAR = { commandBuilder ->
            val instance = object : VelocityCommand() {
                override val data: CommandData = CommandData(commandBuilder.command, commandBuilder.aliases)

                override fun executeCommand(sender: CommandSource, fullCommand: String, stringArgs: Array<String>) {
                    executeAsync(sender, fullCommand, stringArgs, commandBuilder.dispatcher)
                }

                private fun executeAsync(sender: CommandSource, fullCommand: String, stringArgs: Array<out String>, context: CoroutineContext?) {
                    if (context != null) {
                        CoroutineScope(context).launch {
                            executeAsync(sender, fullCommand, stringArgs, null)
                        }
                        return
                    }

                    val velocityArgs = stringArgs.filter { it.isNotEmpty() }.toTypedArray()

                    var current: CommandBase = commandBuilder

                    val dataList = CommandArgumentDataList()
                    val argList = mutableSetOf<CommandArgument<*>>()

                    velocityArgs.forEachIndexed paperArgs@{ index, string ->
                        val success = current.eligibleArguments.filterNot {
                            argList.contains(it.second)
                        }.any required@{ (base, any) ->
                            val type = any.type
                            val value = runBlocking { type.parse(string, sender) } ?: return@required false

                            if (sender is Player && !any.canUse(sender.uniqueId, fullCommand, argList, velocityArgs))
                                return@required false

                            if (type is NumberArgumentType<*>) {
                                if (value !is Number)
                                    throw IllegalStateException()

                                if (type.allowRange(sender, value) != true)
                                    return@required false
                            }

                            if (type is GreedyArgumentType<*, *>) {
                                val values = velocityArgs.copyOfRange(index, velocityArgs.size).map { arg -> runBlocking { type.parse(arg, sender) } }
                                if (values.any { it == null })
                                    return@required false

                                val data = GreedyCommandArgumentData(index, values, any, type)
                                dataList.add(data)
                                argList.add(any)
                                
                                val executor = any.commandSuccess ?: any.commandFail
                                return executor.invoke(sender, fullCommand, dataList, velocityArgs)
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
                            return current.commandFail(sender, fullCommand, dataList, velocityArgs)
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
                                it.canUse(sender.uniqueId, fullCommand, argList, velocityArgs)
                            else true
                        }.find { it.optional } ?: break

                        if (preferred.commandSuccess != null)
                            optionalExecutor = preferred

                        current = preferred
                    }

                    val executor = providedExecutor ?: optionalExecutor.commandSuccess ?: optionalExecutor.commandFail

                    return executor.invoke(sender, fullCommand, dataList, velocityArgs)
                }

                override fun suggestAsync(invocation: RawCommand.Invocation): CompletableFuture<List<String>> {
                    val sender = invocation.source()
                    val strings = invocation.arguments().split(" ").toTypedArray()

                    val fullCommand = "${invocation.alias()} ${invocation.arguments()}"
                    val baseCommand = strings.first()

                    val velocityArgs = strings.clone()
                    val currentArg = velocityArgs.lastOrNull().orEmpty()

                    var current: CommandBase = commandBuilder

                    val dataList = CommandArgumentDataList()
                    val argList = mutableSetOf<CommandArgument<*>>()
                    
                    fun sendSuggestions(): CompletableFuture<List<String>> {
                        val possibleArguments = current.arguments

                        val suggestArguments = possibleArguments.filter { argument ->
                            if (sender is Player && !argument.canUse(sender.uniqueId, fullCommand, argList, velocityArgs))
                                return@filter false

                            return@filter argument.type.suggestType(currentArg, sender)
                        }

                        return CompletableFuture.supplyAsync { suggestArguments.flatMap { it.type.defaultChatSuggestions(currentArg, sender) }.filter { it.startsWith(currentArg, true) } }
                    }

                    if (velocityArgs.size > 1) {
                        val mustBeCorrect = velocityArgs.dropLast(1).toTypedArray()
                        println("mustBeCorrect: ${mustBeCorrect.joinToString(" ")}")

                        mustBeCorrect.forEachIndexed paperArgs@{ index, string ->
                            val success = current.eligibleArguments.filterNot {
                                argList.contains(it.second)
                            }.any required@{ (base, any) ->
                                val type = any.type
                                val value = runBlocking { type.parse(string, sender) } ?: return@required false

                                if (sender is Player && !any.canUse(sender.uniqueId, fullCommand, argList, mustBeCorrect))
                                    return@required false

                                if (type is NumberArgumentType<*>) {
                                    if (value !is Number)
                                        throw IllegalStateException()

                                    if (type.allowRange(sender, value) != true)
                                        return@required false
                                }

                                if (type is GreedyArgumentType<*, *>) {
                                    val values = mustBeCorrect.copyOfRange(index, mustBeCorrect.size).map { arg -> runBlocking { type.parse(arg, sender) } }
                                    if (values.any { it == null })
                                        return@required false

                                    val data = GreedyCommandArgumentData(index, values, any, type)
                                    dataList.add(data)
                                    argList.add(any)

                                    return sendSuggestions()
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

                            if (success.not()) {
                                return CompletableFuture.supplyAsync { emptyList() }
                            }
                        }
                    }
                    
                    return sendSuggestions()
                }
            }

            // sets the permission of this command (can be null)
            instance.permission = commandBuilder.permission

            // registers this command to the server
            val plugin = FlauschigeLibraryVelocity.activeData.first().plugin
            val server = FlauschigeLibraryVelocity.server
            
            instance.register(server, plugin)
            this.registeredCommands.add(commandBuilder)
        }

        @Suppress("DEPRECATION")
        CommandBase.COMMAND_CAN_USE = use@{ uuid, commandArgument, fullCommand, data, args ->
            val permission = commandArgument.permission
            val sender = FlauschigeLibraryVelocity.server.getPlayer(uuid).orElseThrow()

            return@use (permission == null || sender.hasPermission(permission)) && commandArgument.requirements.all { it(sender, fullCommand, data, args) }
        }

        CommandBase.COMMAND_HAS_PERMISSION = permission@{ uuid, commandArgument ->
            val sender = FlauschigeLibraryVelocity.server.getPlayer(uuid).orElseThrow()

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