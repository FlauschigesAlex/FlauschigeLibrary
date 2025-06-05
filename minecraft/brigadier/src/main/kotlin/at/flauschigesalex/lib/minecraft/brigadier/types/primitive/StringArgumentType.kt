package at.flauschigesalex.lib.minecraft.brigadier.types.primitive

import at.flauschigesalex.lib.minecraft.brigadier.CommandArgumentType
import net.kyori.adventure.audience.Audience

@Suppress("unused")
class StringArgumentType private constructor(val regex: Regex?) : CommandArgumentType<String>() {

    companion object {

        fun string() = StringArgumentType(null)
        fun string(regex: Regex?) = StringArgumentType(regex)
        fun word() = StringArgumentType(Regex("^[a-zA-Z0-9_]*"))

    }
    override fun suggestType(value: String, sender: Audience): Boolean {
        regex ?: return true
        return regex.matches(value)
    }

    override suspend fun parse(value: String, sender: Audience): String = value

    override val priority: Int = super.priority -1
}