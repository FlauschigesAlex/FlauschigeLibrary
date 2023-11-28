package at.flauschigesalex.lib.minecraft.brigadier.types.primitive

import at.flauschigesalex.lib.minecraft.brigadier.CommandArgumentType
import net.kyori.adventure.audience.Audience

@Suppress("unused")
class ByteArgumentType private constructor() : CommandArgumentType<Byte>() {

    companion object {
        fun byte() = ByteArgumentType()
    }

    override fun suggestType(value: String, sender: Audience): Boolean {
        return value.toByteOrNull() != null
    }

    override suspend fun parse(value: String, sender: Audience): Byte? {
        return value.toByteOrNull()
    }
}