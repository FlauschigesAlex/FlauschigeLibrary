@file:Suppress("UNCHECKED_CAST", "unused", "MemberVisibilityCanBePrivate")

package at.flauschigesalex.lib.minecraft.brigadier

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import kotlin.coroutines.CoroutineContext

/**
 * @param sender The sender of the command
 * @param fullCommand The full command
 * @param arguments The data arguments of the command
 * @param strings The string arguments of the command
 */
data class CommandContext(val sender: Audience,
                          val fullCommand: String,
                          val arguments: CommandArgumentDataList,
                          val strings: Array<out String>
) {
    override fun hashCode(): Int = fullCommand.hashCode()
    override fun equals(other: Any?): Boolean = other is CommandContext && other.fullCommand == fullCommand
}

typealias CommandExecutor = (context: CommandContext) -> Unit
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
    
    @Deprecated("", ReplaceWith("CommandBuilder(command) {\nTODO(\"Unused CommandBuilder implementation\")\n}"), DeprecationLevel.ERROR)
    constructor(command: String): this(command, throw IllegalArgumentException())

    override val aliases: MutableList<String> = mutableListOf()
    internal var isRegistered = false

    @CommandInternal
    var label: String = DEFAULT_COMMAND_LABEL?.lowercase() ?: command.lowercase()
        private set

    @OptIn(CommandInternal::class)
    fun label(label: String) {
        this.label = label.lowercase()
    }
    
    @CommandInternal
    var optionalArgumentMode: OptionalArgumentMode = DEFAULT_OPTIONAL_ARGUMENT_MODE
        private set

    @OptIn(CommandInternal::class)
    fun optionalMode(mode: OptionalArgumentMode) {
        this.optionalArgumentMode = mode
    }

    @CommandInternal
    var dispatcher: CoroutineContext? = null
        private set

    @Deprecated("Async command parsing behavior is declared deprecated by Mojang brigadier.")
    @OptIn(CommandInternal::class)
    fun dispatcher(dispatcher: CoroutineContext) {
        this.dispatcher = dispatcher
    }
    
    init {
        consumer.invoke(this)
        this.registerInternally()
    }

    @Deprecated("Replaced with consuming constructor", level = DeprecationLevel.ERROR)
    fun register() {
        throw IllegalArgumentException("Use consuming constructor instead")
    }
    
    @OptIn(CommandInternal::class)
    private fun registerInternally() {
        if (isRegistered)
            return

        COMMAND_REGISTRAR.invoke(this)
        isRegistered = true
    }
}