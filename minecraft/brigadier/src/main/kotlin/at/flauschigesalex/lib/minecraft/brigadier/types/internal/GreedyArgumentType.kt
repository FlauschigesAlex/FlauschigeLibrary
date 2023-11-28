package at.flauschigesalex.lib.minecraft.brigadier.types.internal

import at.flauschigesalex.lib.minecraft.brigadier.CommandArgumentType
import net.kyori.adventure.audience.Audience

@Suppress("unused")
class GreedyArgumentType<A, T: CommandArgumentType<A>> private constructor(val any: T) : CommandArgumentType<A>() {

    companion object {
        fun <A, T: CommandArgumentType<A>> greedy(any: T) = GreedyArgumentType(any)
    }

    override fun suggestType(value: String, sender: Audience): Boolean {
        return any.suggestType(value, sender)
    }

    override suspend fun parse(value: String, sender: Audience): A? {
        return any.parse(value, sender)
    }

    override fun defaultChatSuggestions(provided: String, sender: Audience): List<String> {
        return any.defaultChatSuggestions(provided, sender)
    }

    override val priority: Int = super.priority -99
}