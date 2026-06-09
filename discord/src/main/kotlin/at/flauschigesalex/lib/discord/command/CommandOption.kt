package at.flauschigesalex.lib.discord.command

import net.dv8tion.jda.api.interactions.commands.OptionType

data class CommandOption(val type: OptionType, val name: String, val description: String = name, val required: Boolean = false, val autoComplete: Boolean = false)