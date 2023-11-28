package at.flauschigesalex.lib.minecraft.brigadier.types.primitive.number

import at.flauschigesalex.lib.minecraft.brigadier.CommandArgumentType
import net.kyori.adventure.audience.Audience

@Suppress("unused")
class LongArgumentType private constructor(override val min: Long? = null, override val max: Long? = null) : CommandArgumentType<Long>(),
    NumberArgumentType<Long> {

    companion object {
        fun long() = LongArgumentType()

        fun positive() = LongArgumentType(min = 1)
        fun negative() = LongArgumentType(max = -1)

        fun range(min: Long, max: Long) = LongArgumentType(min, max)
    }

    override fun suggestType(value: String, sender: Audience): Boolean {
        return value.toLongOrNull() != null
    }

    override suspend fun parse(value: String, sender: Audience): Long? {
        return value.toLongOrNull()
    }
}