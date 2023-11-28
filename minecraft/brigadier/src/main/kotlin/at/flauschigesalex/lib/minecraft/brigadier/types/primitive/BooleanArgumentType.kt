package at.flauschigesalex.lib.minecraft.brigadier.types.primitive

import at.flauschigesalex.lib.minecraft.brigadier.CommandArgumentType
import net.kyori.adventure.audience.Audience

@Suppress("unused")
class BooleanArgumentType private constructor() : CommandArgumentType<Boolean>() {

    companion object {
        fun bool() = BooleanArgumentType()
    }

    override fun suggestType(value: String, sender: Audience): Boolean {
        return listOf("true", "false").any { it.startsWith(value, true) }
    }

    override suspend fun parse(value: String, sender: Audience): Boolean? {
        return if (value.equals("true", true)) true else if (value.equals("false", true)) false else null
    }

    override fun defaultChatSuggestions(provided: String, sender: Audience): List<String> {
        return listOf("true", "false").filter { it.startsWith(provided, true) }
    }
}