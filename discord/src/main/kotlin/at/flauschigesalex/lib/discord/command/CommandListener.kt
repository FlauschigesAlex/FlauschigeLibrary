package at.flauschigesalex.lib.discord.command

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

@Suppress("unused")
internal object CommandListener : ListenerAdapter() {
    
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        val rawCommand = event.name
        val command = CommandBuilder.find { it.name.equals(rawCommand, true) } ?: return
        
        command(event)
    }
}