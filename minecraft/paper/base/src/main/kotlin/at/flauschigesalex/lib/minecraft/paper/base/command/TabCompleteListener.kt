package at.flauschigesalex.lib.minecraft.paper.base.command

import at.flauschigesalex.lib.minecraft.brigadier.CommandArgument
import at.flauschigesalex.lib.minecraft.brigadier.CommandArgumentData
import at.flauschigesalex.lib.minecraft.brigadier.CommandArgumentDataList
import at.flauschigesalex.lib.minecraft.brigadier.CommandBase
import at.flauschigesalex.lib.minecraft.brigadier.CommandInternal
import at.flauschigesalex.lib.minecraft.brigadier.GreedyCommandArgumentData
import at.flauschigesalex.lib.minecraft.brigadier.OptionalArgumentMode
import at.flauschigesalex.lib.minecraft.brigadier.types.internal.GreedyArgumentType
import at.flauschigesalex.lib.minecraft.brigadier.types.primitive.number.NumberArgumentType
import at.flauschigesalex.lib.minecraft.paper.base.internal.PaperListener
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent
import kotlinx.coroutines.runBlocking
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import kotlin.collections.copyOfRange

internal object TabCompleteListener : PaperListener(false) {
    
    @OptIn(CommandInternal::class)
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private fun event(event: AsyncTabCompleteEvent) {
        if (event.isCommand.not())
            return
        
        val sender = event.sender
        val strings = event.buffer.drop(1).split(' ')
        
        val fullCommand = event.buffer
        val baseCommand = strings.first().substringAfterLast(":")
        
        val paperArgs = strings.drop(1).toTypedArray()
        val currentArg = paperArgs.lastOrNull().orEmpty()
        
        val commandBuilder = CommandConfigurator.registeredCommands.toList().find {
            it.command == baseCommand || it.aliases.any { alias -> alias == baseCommand }
        } ?: return
        
        var current: CommandBase = commandBuilder

        val dataList = CommandArgumentDataList()
        val argList = mutableSetOf<CommandArgument<*>>()
        
        fun sendSuggestions() {
            val possibleArguments = argList.lastOrNull()?.arguments ?: commandBuilder.arguments

            val suggestArguments = possibleArguments.filter { argument ->
                if (sender is Player && !argument.canUse(sender, fullCommand, dataList, paperArgs))
                    return@filter false

                if (argument.suggest.not())
                    return@filter false
                
                return@filter argument.type.suggestType(currentArg, sender)
            }

            event.isHandled = true
            event.completions = emptyList()
            val completions = event.completions()
            completions.clear()

            suggestArguments.flatMap { it.type.defaultChatSuggestions(currentArg, sender) }.filter { it.startsWith(currentArg, true) }.map {
                AsyncTabCompleteEvent.Completion.completion(it, null)
            }.forEach(completions::add)
        }
        
        if (paperArgs.size > 1) {
            val mustBeCorrect = paperArgs.dropLast(1).toTypedArray()

            mustBeCorrect.forEachIndexed paperArgs@{ index, string ->
                val success = current.eligibleArguments.toList().filterNot {
                    argList.contains(it.second)
                }.any required@{ (_, any) ->
                    val type = any.type
                    val value = runBlocking { type.parse(string, sender) } ?: return@required false

                    if (sender is Player && !any.canUse(sender, fullCommand, dataList, mustBeCorrect))
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
                    event.completions = emptyList()
                    return
                }
            }
        }

        sendSuggestions()
    }
    
}