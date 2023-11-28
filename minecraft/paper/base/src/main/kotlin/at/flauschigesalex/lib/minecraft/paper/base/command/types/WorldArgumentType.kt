package at.flauschigesalex.lib.minecraft.paper.base.command.types

import at.flauschigesalex.lib.minecraft.brigadier.CommandArgumentType
import net.kyori.adventure.audience.Audience
import org.bukkit.Bukkit
import org.bukkit.World

private typealias WorldSupplier = () -> (List<World>)

@Suppress("unused")
class WorldArgumentType private constructor(private val supplier: WorldSupplier) : CommandArgumentType<World>() {

    companion object {
        fun all() = WorldArgumentType { Bukkit.getWorlds() }
        fun of(provider: WorldSupplier) = WorldArgumentType(provider)
        fun of(worlds: List<World>) = WorldArgumentType { worlds }
    }

    override fun suggestType(value: String, sender: Audience): Boolean = supplier().any { it.name.startsWith(value, true) }
    override suspend fun parse(value: String, sender: Audience): World? = supplier().find { it.name.equals(value, true) }
    
    override fun defaultChatSuggestions(provided: String, sender: Audience): List<String> = supplier().map { it.name }
}