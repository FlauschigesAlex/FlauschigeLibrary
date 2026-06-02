@file:Suppress("UNCHECKED_CAST", "unused", "MemberVisibilityCanBePrivate")
@file:OptIn(CommandInternal::class)

package at.flauschigesalex.lib.minecraft.brigadier

import at.flauschigesalex.lib.minecraft.brigadier.CommandBase.Companion.DEFAULT_OPTIONAL_ARGUMENT_MODE
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import kotlin.coroutines.CoroutineContext

/**
 * @param sender The sender of the command
 * @param executor The executor of the command (affected by `/execute as` on paper)
 * @param fullCommand The full command
 * @param arguments The data arguments of the command
 * @param strings The string arguments of the command
 */
data class CommandContext(
    val sender: Audience, 
    val executor: Audience, 
    val fullCommand: String,
    val arguments: CommandArgumentDataList,
    val strings: Array<out String>
) {
    override fun hashCode(): Int = fullCommand.hashCode()
    override fun equals(other: Any?): Boolean = other is CommandContext && other.fullCommand == fullCommand
}

typealias CommandExecutor = suspend (context: CommandContext) -> Unit
typealias CommandRequirement = (context: CommandContext) -> Boolean

fun Audience.sendIncompleteCommand(fullCommand: String) {
    this.sendMessage(Component.translatable("command.unknown.command").color(NamedTextColor.RED)
        .append(Component.newline())
        .append(Component.text(fullCommand).decorate(TextDecoration.UNDERLINED))
        .append(Component.translatable("command.context.here").decorate(TextDecoration.ITALIC)))
}

class CommandBuilder private constructor(command: String, consumer: CommandBuilder.() -> Unit): CommandBase(command), CommandAlias<CommandBuilder> {
    
    companion object {
        operator fun invoke(command: String, consumer: CommandBuilder.() -> Unit) {
            CommandBuilder(command, consumer)
        }
    }

    val baseInternal = InternalCommandBuilderMeta()
    override var base: CommandBuilder = this
    
    @CommandInternal
    override val aliases: MutableList<String> = mutableListOf()
    internal var isRegistered = false

    init {
        consumer.invoke(this)
        this.registerInternally()
    }

    fun optionalMode(mode: OptionalArgumentMode) {
        this.baseInternal.optionalArgumentMode = mode
    }

    private fun registerInternally() {
        if (isRegistered)
            return

        Internal.COMMAND_REGISTRAR.invoke(this)
        isRegistered = true
    }

    override fun equals(other: Any?): Boolean = other is CommandBuilder && other.command == command
    override fun hashCode(): Int = command.hashCode()
}

@CommandInternal
class InternalCommandBuilderMeta {
    var optionalArgumentMode: OptionalArgumentMode = DEFAULT_OPTIONAL_ARGUMENT_MODE
}