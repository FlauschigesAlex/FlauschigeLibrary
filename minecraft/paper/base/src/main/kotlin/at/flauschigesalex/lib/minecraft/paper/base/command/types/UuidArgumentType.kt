package at.flauschigesalex.lib.minecraft.paper.base.command.types

import at.flauschigesalex.lib.minecraft.brigadier.CommandArgumentType
import net.kyori.adventure.audience.Audience
import kotlin.uuid.Uuid

@Suppress("unused")
class UuidArgumentType private constructor() : CommandArgumentType<Uuid>() {

    companion object {
        fun uuid() = UuidArgumentType()
    }

    override fun suggestType(value: String, sender: Audience): Boolean = Uuid.parseOrNull(value) != null
    override suspend fun parse(value: String, sender: Audience): Uuid? = Uuid.parseOrNull(value)
}