package at.flauschigesalex.lib.minecraft.brigadier.types.primitive.number

import at.flauschigesalex.lib.minecraft.brigadier.CommandInternal
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

interface NumberArgumentType<N> where N: Number, N: Comparable<N> {

    val min: N?
    val max: N?

    fun allowRange(sender: Audience, value: Any): Boolean? {

        if (value !is Number)
            return false

        val className = value::class.java.simpleName.lowercase()

        if (min != null && value.check(min!!, false)) {
            sender.sendMessage(Component.translatable("argument.$className.low").color(NamedTextColor.RED).arguments(Component.text(this.min.toString()), Component.text(value.toString())))
            return null
        }
        if (max != null && value.check(max!!, true)) {
            sender.sendMessage(Component.translatable("argument.$className.big").color(NamedTextColor.RED).arguments(Component.text(this.max.toString()), Component.text(value.toString())))
            return null
        }

        return true
    }
    
    private inline fun <reified N: Number> N.check(num: Number, larger: Boolean): Boolean {
        return when (this) {
            is Long -> if (larger) this > num.toLong() else this < num.toLong()
            is Int -> if (larger) this > num.toInt() else this < num.toInt()
            is Short -> if (larger) this > num.toShort() else this < num.toShort()
            is Byte -> if (larger) this > num.toByte() else this < num.toByte()

            is Double -> if (larger) this > num.toDouble() else this < num.toDouble()
            is Float -> if (larger) this > num.toFloat() else this < num.toFloat()
            
            else -> throw IllegalArgumentException()
        }
    }
}

@CommandInternal
fun Number.compareTo(other: Number): Int {
    return this.toDouble().compareTo(other.toDouble())
}