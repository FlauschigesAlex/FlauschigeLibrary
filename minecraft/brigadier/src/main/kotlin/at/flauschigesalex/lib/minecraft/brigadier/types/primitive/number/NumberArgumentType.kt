@file:OptIn(CommandInternal::class)

package at.flauschigesalex.lib.minecraft.brigadier.types.primitive.number

import at.flauschigesalex.lib.minecraft.brigadier.CommandArgument
import at.flauschigesalex.lib.minecraft.brigadier.CommandArgumentType
import at.flauschigesalex.lib.minecraft.brigadier.CommandInternal
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

@Suppress("unused")
interface NumberArgumentType<N> where N: Number, N: Comparable<N> {

    val min: N?
    val max: N?

    @Suppress("UNCHECKED_CAST")
    fun allowRange(sender: Audience, value: Any, sendMessage: Boolean): Boolean? {

        val number = value as? N ?: return false
        val className = value::class.java.simpleName.lowercase()
        
        val (min, max) = this.min to this.max

        if (min != null && number < min) {
            if (sendMessage) sender.sendMessage(Component.translatable("argument.$className.low").color(NamedTextColor.RED).arguments(Component.text(this.min.toString()), Component.text(value.toString())))
            return null
        }
        if (max != null && number > max) {
            if (sendMessage) sender.sendMessage(Component.translatable("argument.$className.big").color(NamedTextColor.RED).arguments(Component.text(this.max.toString()), Component.text(value.toString())))
            return null
        }

        return true
    }
}

@CommandInternal
fun Number.compareTo(other: Number): Int {
    this.toLong().compareTo(other.toLong()).takeIf { it != 0 }?.let { return it }
    return this.toDouble().compareTo(other.toDouble())
}

fun <T> CommandArgument<T>.suppressRangeWarning() where T: CommandArgumentType<*>, T: NumberArgumentType<*> {
    this.commandInternal.meta["suppressRangeWarning"] = true
}
@CommandInternal
val CommandArgument<*>.isSuppressRangeWarning: Boolean
    get() = this.commandInternal.meta["suppressRangeWarning"] as? Boolean ?: false