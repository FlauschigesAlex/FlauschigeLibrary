package at.flauschigesalex.lib.minecraft.velocity.base.command.types

import at.flauschigesalex.lib.minecraft.brigadier.CommandArgumentType
import at.flauschigesalex.lib.minecraft.velocity.base.FlauschigeLibraryVelocity
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import net.kyori.adventure.audience.Audience

@Suppress("unused")
class PlayerArgumentType private constructor() : CommandArgumentType<Player>() {

    companion object {
        fun player() = PlayerArgumentType()
    }

    private fun server(): ProxyServer = FlauschigeLibraryVelocity.server

    override fun suggestType(value: String, sender: Audience): Boolean {
        return server().allPlayers.any { it.username.startsWith(value, true) }
    }

    override suspend fun parse(value: String, sender: Audience): Player? {
        return server().getPlayer(value).orElse(null)
    }

    override fun defaultChatSuggestions(provided: String, sender: Audience): List<String> = server().allPlayers.map { it.username }
}