package at.flauschigesalex.lib.minecraft.brigadier.types.primitive

import at.flauschigesalex.lib.minecraft.brigadier.CommandArgumentType
import net.kyori.adventure.audience.Audience

@Suppress("unused")
class EnumArgumentType<T: Enum<T>> private constructor(clazz: Class<T>) : CommandArgumentType<Enum<T>>() {

    companion object {
        fun <T: Enum<T>> enum(clazz: Class<T>): EnumArgumentType<T> = EnumArgumentType(clazz)
    }
    val entries: Array<T> = clazz.enumConstants

    override fun suggestType(value: String, sender: Audience): Boolean {
        return entries.any { it.name.startsWith(value, ignoreCase = true) }
    }

    override suspend fun parse(value: String, sender: Audience): Enum<T>? {
        return entries.find { it.name.equals(value, ignoreCase = true) }
    }

    override fun defaultChatSuggestions(provided: String, sender: Audience): List<String> {
        return entries.map { it.name }
    }
}