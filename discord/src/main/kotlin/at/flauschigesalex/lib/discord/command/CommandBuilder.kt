package at.flauschigesalex.lib.discord.command

import at.flauschigesalex.lib.discord.FlauschigeLibraryDiscord
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions

@Suppress("unused")
class CommandBuilder(val name: String, invocation: CommandBuilder.() -> Unit) {
    
    internal companion object : Iterable<CommandBuilder> {
        var JDA: JDA = FlauschigeLibraryDiscord.JDA

        private val entries = mutableSetOf<CommandBuilder>()
        override fun iterator(): Iterator<CommandBuilder> = entries.iterator()
    }
    
    private var isRegistered: Boolean = false
    
    var description: String = this.name
    var permission: Permission? = null
    internal val options = mutableSetOf<CommandOption>()
    
    fun addOption(option: CommandOption) = this.options.add(option)
    fun addOptions(options: Collection<CommandOption>) = this.options.addAll(options)

    private var executor: (SlashCommandInteractionEvent) -> Unit = {
        throw NotImplementedError()
    }
    fun execute(executor: (SlashCommandInteractionEvent) -> Unit) {
        this.executor = executor
    }
    
    init {
        entries += this
        
        invocation(this)
        this.registerInternally()
    }
    
    internal operator fun invoke(event: SlashCommandInteractionEvent) = this.executor(event)
    
    private fun registerInternally() {
        if (this.isRegistered) return
        this.isRegistered = true
        
        JDA.upsertCommand(name, description).also { command ->
            options.forEach { option ->
                command.addOption(option.type, option.name, option.description, option.required, option.autoComplete)
            }
            permission?.let { permission -> command.setDefaultPermissions(DefaultMemberPermissions.enabledFor(permission)) }
            command.queue()
        }
    }

    override fun equals(other: Any?): Boolean = other is CommandBuilder && other.name.equals(this.name, true)
    override fun hashCode(): Int = this.name.hashCode()
}