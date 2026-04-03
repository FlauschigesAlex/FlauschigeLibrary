package at.flauschigesalex.lib.minecraft.paper.base.command.types

import at.flauschigesalex.lib.minecraft.brigadier.CommandArgumentType
import net.kyori.adventure.audience.Audience
import org.bukkit.Material

@Suppress("unused")
class MaterialArgumentType private constructor(private val materials: Collection<Material>) : CommandArgumentType<Material>() {

    companion object {
        fun all() = MaterialArgumentType(Material.entries)
        fun filtered(filter: (Material) -> Boolean) = MaterialArgumentType(Material.entries.filter(filter))
        fun items() = this.filtered { it.isItem }
        fun blocks() = this.filtered { it.isBlock }
        fun of(list: Iterable<Material>) = MaterialArgumentType(list.toList())
    }

    override fun suggestType(value: String, sender: Audience): Boolean = materials.any { it.name.startsWith(value, true) }
    override suspend fun parse(value: String, sender: Audience): Material? = materials.find { it.name.equals(value, true) }
    override fun defaultChatSuggestions(provided: String, sender: Audience): List<String> = materials.map { it.name }.filter { it.startsWith(provided, true) }
}