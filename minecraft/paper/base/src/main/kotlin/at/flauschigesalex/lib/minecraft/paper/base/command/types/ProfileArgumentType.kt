package at.flauschigesalex.lib.minecraft.paper.base.command.types

import at.flauschigesalex.lib.minecraft.api.MojangAPI
import at.flauschigesalex.lib.minecraft.api.MojangProfile
import at.flauschigesalex.lib.minecraft.brigadier.CommandArgumentType
import net.kyori.adventure.audience.Audience
import org.bukkit.Bukkit
import java.util.UUID

@Suppress("unused")
class ProfileArgumentType private constructor() : CommandArgumentType<MojangProfile>() {

    companion object {
        fun profile() = ProfileArgumentType()
    }

    override fun suggestType(value: String, sender: Audience): Boolean {
        return true
    }

    override suspend fun parse(value: String, sender: Audience): MojangProfile? {
        try {
            val uuid = UUID.fromString(value)
            if (uuid != null) return MojangAPI.profile(uuid)
        } catch (_: Exception) {}

        return MojangAPI.profile(value)
    }

    override fun defaultChatSuggestions(provided: String, sender: Audience): List<String> = Bukkit.getOnlinePlayers().map { it.name }
}