package at.flauschigesalex.lib.minecraft.paper.base.command

import at.flauschigesalex.lib.minecraft.brigadier.CommandArgument
import at.flauschigesalex.lib.minecraft.brigadier.CommandArgumentData
import at.flauschigesalex.lib.minecraft.brigadier.CommandArgumentDataList
import at.flauschigesalex.lib.minecraft.brigadier.CommandBase
import at.flauschigesalex.lib.minecraft.brigadier.CommandBuilder
import at.flauschigesalex.lib.minecraft.brigadier.CommandContext
import at.flauschigesalex.lib.minecraft.brigadier.CommandInternal
import at.flauschigesalex.lib.minecraft.brigadier.GreedyCommandArgumentData
import at.flauschigesalex.lib.minecraft.brigadier.OptionalArgumentMode
import at.flauschigesalex.lib.minecraft.brigadier.isOptional
import at.flauschigesalex.lib.minecraft.brigadier.shouldSuggest
import at.flauschigesalex.lib.minecraft.brigadier.types.internal.GreedyArgumentType
import at.flauschigesalex.lib.minecraft.brigadier.types.primitive.number.NumberArgumentType
import at.flauschigesalex.lib.minecraft.brigadier.types.primitive.number.isSuppressRangeWarning
import at.flauschigesalex.lib.minecraft.paper.base.FlauschigeLibraryPaper
import com.mojang.brigadier.arguments.StringArgumentType as MojangStringArgumentType
import com.mojang.brigadier.context.CommandContext as BrigadierContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.audience.Audience
import org.bukkit.command.CommandSender
import org.bukkit.command.ProxiedCommandSender
import org.bukkit.entity.Player
import org.bukkit.permissions.Permissible
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.CoroutineContext

@OptIn(CommandInternal::class)
internal object CommandConfigurator {

    internal val registeredCommands = mutableSetOf<CommandBuilder>()

    init {
        CommandBase.Internal.COMMAND_REGISTRAR = { commandBuilder ->
            val instance = BrigadierCommand(commandBuilder.command, FlauschigeLibraryPaper.activeData.last().plugin) {
                requires { stack -> canUse(stack.sender, commandBuilder.permission) }
                executes { context ->
                    executeAsync(context.source, commandBuilder, context.input, emptyArray(), commandBuilder.dispatcher)
                    return@executes 1
                }

                if (commandBuilder.arguments.isNotEmpty()) {
                    argument("args", MojangStringArgumentType.greedyString()) {
                        suggests { context, builder ->
                            suggest(commandBuilder, context, builder)
                        }
                        executes { context ->
                            val args = context.getArgument<String>("args").split(' ').toTypedArray()
                            executeAsync(context.source, commandBuilder, context.input, args, commandBuilder.dispatcher)
                            return@executes 1
                        }
                    }
                }
            }

            instance.aliases.addAll(commandBuilder.aliases)
            this.registeredCommands.add(commandBuilder)
        }

        @Suppress("DEPRECATION")
        CommandBase.Internal.COMMAND_CAN_USE = use@{ sender, commandArgument, fullCommand, data, args ->
            val permission = commandArgument.permission ?: return@use true
            val context = CommandContext(sender, (sender as? ProxiedCommandSender)?.callee ?: sender, fullCommand, data, args)

            val require = commandArgument.argInternal.requirements.all {
                it(context)
            }
            if (require.not()) return@use false

            if (sender !is Permissible) return@use true
            return@use sender.hasPermission(permission)
        }

        CommandBase.Internal.COMMAND_HAS_PERMISSION = permission@{ sender, commandArgument ->
            if (sender !is Permissible) return@permission true

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

    private fun executeAsync(
        source: CommandSourceStack,
        commandBuilder: CommandBuilder,
        fullCommand: String,
        stringArgs: Array<out String>,
        context: CoroutineContext?
    ) {
        if (context != null) {
            CoroutineScope(context).launch {
                executeAsync(source, commandBuilder, fullCommand, stringArgs, null)
            }
            return
        }

        val sender = source.sender
        val executor = (source.executor as? Audience) ?: sender as Audience
        val paperArgs = stringArgs.filter { it.isNotEmpty() }.toTypedArray()

        var current: CommandBase = commandBuilder
        val resolvedCommand = fullCommand.ifBlank {
            if (paperArgs.isEmpty()) commandBuilder.command
            else "${commandBuilder.command} ${paperArgs.joinToString(" ")}"
        }

        val dataList = CommandArgumentDataList()
        val argList = mutableSetOf<CommandArgument<*>>()

        paperArgs.forEachIndexed paperArgs@{ index, string ->
            val success = current.eligibleArguments.filterNot {
                argList.contains(it.second)
            }.any required@{ (_, any) ->
                val type = any.type
                val value = runBlocking { type.parse(string, sender) } ?: return@required false

                if (sender is Player && !any.canUse(executor, resolvedCommand, dataList, paperArgs))
                    return@required false

                if (type is NumberArgumentType<*>) {
                    if (value !is Number)
                        throw IllegalStateException()

                    if (type.allowRange(executor, value, any.isSuppressRangeWarning.not()) != true)
                        return@required false
                }

                if (type is GreedyArgumentType<*, *>) {
                    val values = paperArgs.copyOfRange(index, paperArgs.size).map { arg ->
                        runBlocking { type.parse(arg, sender) }
                    }
                    if (values.any { it == null })
                        return@required false

                    val data = GreedyCommandArgumentData(index, values, any, type)
                    dataList.add(data)
                    argList.add(any)

                    val invocation = any.commandSuccess ?: any.commandFail
                    val commandContext = CommandContext(sender, executor, resolvedCommand, dataList, paperArgs)
                    invocation.invoke(commandContext)
                    return
                } else {
                    val data = CommandArgumentData(index, value, any, type)
                    dataList.add(data)
                    argList.add(any)
                }

                if (any.isOptional && commandBuilder.optionalArgumentMode != OptionalArgumentMode.ORDERED)
                    return@required true

                current = any
                return@required true
            }

            if (success.not()) {
                val commandContext = CommandContext(sender, executor, resolvedCommand, dataList, paperArgs)
                return current.commandFail(commandContext)
            }
        }

        val providedExecutor = argList.toMutableList().apply {
            while (this.count { it.isOptional.not() } > 1)
                this.removeFirst()
        }.reversed().firstOrNull { it.commandSuccess != null }?.commandSuccess

        var optionalExecutor = current
        while (true) {
            val arguments = current.arguments
            val preferred = arguments.filter {
                if (sender is Player)
                    it.canUse(executor, resolvedCommand, dataList, paperArgs)
                else true
            }.find { it.isOptional } ?: break

            if (preferred.commandSuccess != null)
                optionalExecutor = preferred

            current = preferred
        }

        val invocation = providedExecutor ?: optionalExecutor.commandSuccess ?: optionalExecutor.commandFail

        val commandContext = CommandContext(sender, executor, resolvedCommand, dataList, paperArgs)
        invocation.invoke(commandContext)
    }

    private fun suggest(
        commandBuilder: CommandBuilder,
        context: BrigadierContext<CommandSourceStack>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val sender = context.source.sender
        val strings = context.input.split(' ')
        val paperArgs = strings.drop(1).toTypedArray()
        val currentArg = paperArgs.lastOrNull().orEmpty()
        val fullCommand = context.input

        var current: CommandBase = commandBuilder
        val dataList = CommandArgumentDataList()
        val argList = mutableSetOf<CommandArgument<*>>()

        fun complete() = builder.buildFuture()

        fun sendSuggestions() {
            val possibleArguments = if (commandBuilder.optionalArgumentMode == OptionalArgumentMode.UNORDERED) {
                current.eligibleArguments
                    .asSequence()
                    .map { it.second }
                    .filterNot(argList::contains)
                    .distinct()
                    .toList()
            } else {
                argList.lastOrNull()?.arguments ?: commandBuilder.arguments
            }

            possibleArguments.filter { argument ->
                if (sender is Player && !argument.canUse(sender, fullCommand, dataList, paperArgs))
                    return@filter false

                if (argument.shouldSuggest.not())
                    return@filter false

                argument.type.suggestType(currentArg, sender)
            }.flatMap {
                it.type.defaultChatSuggestions(currentArg, sender)
            }.filter {
                it.startsWith(currentArg, true)
            }.forEach(builder::suggest)
        }

        if (paperArgs.size > 1) {
            val mustBeCorrect = paperArgs.dropLast(1).toTypedArray()

            mustBeCorrect.forEachIndexed paperArgs@{ index, string ->
                val success = current.eligibleArguments.filterNot {
                    argList.contains(it.second)
                }.any required@{ (_, any) ->
                    val type = any.type
                    val value = runBlocking { type.parse(string, sender) } ?: return@required false

                    if (sender is Player && !any.canUse(sender, fullCommand, dataList, mustBeCorrect))
                        return@required false

                    if (type is NumberArgumentType<*>) {
                        if (value !is Number)
                            throw IllegalStateException()

                        if (type.allowRange(sender, value, any.isSuppressRangeWarning.not()) != true)
                            return@required false
                    }

                    if (type is GreedyArgumentType<*, *>) {
                        val values = mustBeCorrect.copyOfRange(index, mustBeCorrect.size).map { arg ->
                            runBlocking { type.parse(arg, sender) }
                        }
                        if (values.any { it == null })
                            return@required false

                        val data = GreedyCommandArgumentData(index, values, any, type)
                        dataList.add(data)
                        argList.add(any)

                        sendSuggestions()
                        return complete()
                    } else {
                        val data = CommandArgumentData(index, value, any, type)
                        dataList.add(data)
                        argList.add(any)
                    }

                    if (any.isOptional && commandBuilder.optionalArgumentMode != OptionalArgumentMode.ORDERED)
                        return@required true

                    current = any
                    return@required true
                }

                if (success.not())
                    return complete()
            }
        }

        sendSuggestions()
        return complete()
    }

    private fun canUse(sender: CommandSender, permission: String?): Boolean {
        permission ?: return true
        return sender.hasPermission(permission)
    }
}
