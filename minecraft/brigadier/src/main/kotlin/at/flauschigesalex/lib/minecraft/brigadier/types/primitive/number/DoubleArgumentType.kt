package at.flauschigesalex.lib.minecraft.brigadier.types.primitive.number

import at.flauschigesalex.lib.minecraft.brigadier.CommandArgumentType
import net.kyori.adventure.audience.Audience

@Suppress("unused")
class DoubleArgumentType private constructor(override val min: Double? = null, override val max: Double? = null) : CommandArgumentType<Double>(),
    NumberArgumentType<Double> {

    companion object {
        fun double() = DoubleArgumentType()

        fun positive() = DoubleArgumentType(min = .00000001)
        fun negative() = DoubleArgumentType(max = -.99999999)

        fun range(min: Double, max: Double) = DoubleArgumentType(min, max)
    }

    override fun suggestType(value: String, sender: Audience): Boolean {
        return value.toDoubleOrNull() != null
    }
    override suspend fun parse(value: String, sender: Audience): Double? {
        return value.toDoubleOrNull()
    }
}