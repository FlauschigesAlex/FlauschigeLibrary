package at.flauschigesalex.lib.minecraft.paper.base.command.types

import at.flauschigesalex.lib.minecraft.brigadier.CommandArgumentType
import net.kyori.adventure.audience.Audience
import org.bukkit.Bukkit
import org.bukkit.entity.Player

@Suppress("unused")
class PlayerArgumentType private constructor() : CommandArgumentType<Player>() {

    companion object {
        fun player() = PlayerArgumentType()
    }

    override fun suggestType(value: String, sender: Audience): Boolean {
        return Bukkit.getOnlinePlayers()
            .filter {
                if (sender !is Player)
                    return@filter true
                sender.canSee(it)
            }.any { it.name.startsWith(value, true) }
    }

    override suspend fun parse(value: String, sender: Audience): Player? {
        return Bukkit.getOnlinePlayers()
            .filter {
                if (sender !is Player)
                    return@filter true
                sender.canSee(it)
            }.find { it.name.equals(value, true) }
    }

    override fun defaultChatSuggestions(provided: String, sender: Audience): List<String> = Bukkit.getOnlinePlayers()
        .filter {
            if (sender !is Player)
                return@filter true
            sender.canSee(it)
        }.map { it.name }
}