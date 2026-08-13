@file:Suppress("DEPRECATION")

package at.flauschigesalex.lib.minecraft.paper.base.utils

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.ChatColor

@Deprecated("Class is marked for removal")
object Paper {

    @Deprecated("Legacy function", ReplaceWith("NamedTextColor.NAMES.values()"))
    fun getNamedTextColorValues(): List<NamedTextColor> {
        return NamedTextColor.NAMES.values().toList()
    }
}

val NamedTextColor.name: String
    get() = this.toString().uppercase()

val NamedTextColor.char: Char
    get() = ChatColor.valueOf(this.name).char

val TextDecoration.char: Char
    get() {
        val name = when (this.name) { // fuck paper
            "OBFUSCATED" -> "MAGIC"
            "UNDERLINED" -> "UNDERLINE"

            else -> this.name
        }

        return ChatColor.valueOf(name).char
    }