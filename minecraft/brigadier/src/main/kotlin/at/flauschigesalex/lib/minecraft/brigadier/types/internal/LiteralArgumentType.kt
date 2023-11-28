package at.flauschigesalex.lib.minecraft.brigadier.types.internal

import at.flauschigesalex.lib.minecraft.brigadier.CommandAlias
import at.flauschigesalex.lib.minecraft.brigadier.CommandArgumentType
import net.kyori.adventure.audience.Audience

@Suppress("unused")
class LiteralArgumentType private constructor() : CommandArgumentType<String>(), CommandAlias<LiteralArgumentType> {

    companion object {
        fun literal() = LiteralArgumentType()
    }

    override val aliases: MutableList<String> = mutableListOf()

    override fun suggestType(value: String, sender: Audience): Boolean {
        val set = mutableSetOf(source.command)
        set.addAll(aliases)

        return set.any { it.startsWith(value, true) }
    }

    override suspend fun parse(value: String, sender: Audience): String? {
        if (source.command.equals(value, true))
            return value

        return aliases.find { it.equals(value, true) }
    }

    override fun defaultChatSuggestions(provided: String, sender: Audience): List<String> {
        if (source.command.startsWith(provided, true))
            return listOf(source.command)

        return aliases.filter { it.startsWith(provided, true) }
    }

    override val priority: Int = super.priority +1
}