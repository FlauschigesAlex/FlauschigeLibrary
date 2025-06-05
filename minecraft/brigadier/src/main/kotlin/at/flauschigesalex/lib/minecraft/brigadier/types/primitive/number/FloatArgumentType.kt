package at.flauschigesalex.lib.minecraft.brigadier.types.primitive.number

import at.flauschigesalex.lib.minecraft.brigadier.CommandArgumentType
import net.kyori.adventure.audience.Audience

@Suppress("unused")
class FloatArgumentType private constructor(override val min: Float? = null, override val max: Float? = null) : CommandArgumentType<Float>(),
    NumberArgumentType<Float> {

    companion object {
        fun float() = FloatArgumentType()

        fun positive() = FloatArgumentType(min = .00000001f)
        fun negative() = FloatArgumentType(max = -.99999999f)

        fun range(min: Float, max: Float) = FloatArgumentType(min, max)
    }

    override fun suggestType(value: String, sender: Audience): Boolean {
        return value.toFloatOrNull() != null
    }

    override suspend fun parse(value: String, sender: Audience): Float? {
        return value.toFloatOrNull()
    }
}