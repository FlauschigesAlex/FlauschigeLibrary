package at.flauschigesalex.lib.minecraft.brigadier.types.primitive.number

import at.flauschigesalex.lib.minecraft.brigadier.CommandArgumentType
import net.kyori.adventure.audience.Audience

@Suppress("unused")
class IntegerArgumentType private constructor(override val min: Int? = null, override val max: Int? = null) : CommandArgumentType<Int>(),
    NumberArgumentType<Int> {

    companion object {
        fun int() = IntegerArgumentType()

        fun positive() = IntegerArgumentType(min = 1)
        fun negative() = IntegerArgumentType(max = -1)

        fun range(min: Int, max: Int) = IntegerArgumentType(min, max)
    }

    override fun suggestType(value: String, sender: Audience): Boolean {
        return value.toIntOrNull() != null
    }

    override suspend fun parse(value: String, sender: Audience): Int? {
        return value.toIntOrNull()
    }
}