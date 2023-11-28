package at.flauschigesalex.lib.minecraft.brigadier.types.primitive

import at.flauschigesalex.lib.minecraft.brigadier.CommandArgumentType
import net.kyori.adventure.audience.Audience

@Suppress("unused")
class CharacterArgumentType private constructor() : CommandArgumentType<Char>() {

    companion object {
        fun char() = CharacterArgumentType()
    }

    override fun suggestType(value: String, sender: Audience): Boolean {
        val chars = value.toCharArray()
        return chars.size == 1
    }

    override suspend fun parse(value: String, sender: Audience): Char? {
        return value.toCharArray().firstOrNull()
    }
}