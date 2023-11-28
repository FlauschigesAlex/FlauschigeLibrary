package at.flauschigesalex.lib.minecraft.brigadier

import net.kyori.adventure.audience.Audience

abstract class CommandArgumentType<T> protected constructor() {

    lateinit var source: CommandBase
    internal fun source(base: CommandBase) {
        this.source = base
    }

    abstract fun suggestType(value: String, sender: Audience): Boolean
    abstract suspend fun parse(value: String, sender: Audience): T?

    open fun defaultChatSuggestions(provided: String, sender: Audience): List<String> = listOf()
    open val priority: Int = 0

    override fun toString(): String = this::class.java.simpleName
}