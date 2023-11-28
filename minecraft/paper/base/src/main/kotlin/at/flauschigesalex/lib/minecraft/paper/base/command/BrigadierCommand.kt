@file:Suppress("unused")

package at.flauschigesalex.lib.minecraft.paper.base.command

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.CustomArgumentType
import org.bukkit.plugin.java.JavaPlugin

abstract class BrigadierArgumentType<T: Any, N: Any> : CustomArgumentType<T, N> {
    final override fun parse(p0: StringReader): T = throw UnsupportedOperationException()
    final override fun <S : Any> parse(reader: StringReader, source: S): T {
        if (source !is CommandSourceStack)
            return super.parse(reader, source)

        return this.parseContext(reader, source)
    }
    
    abstract fun parseContext(reader: StringReader, context: CommandSourceStack): T
}

class BrigadierCommand(val command: String, val plugin: JavaPlugin, private val invocation: LiteralArgumentBuilder<CommandSourceStack>.() -> Unit) {
    
    companion object {
        private val brigadier = mutableMapOf<BrigadierCommand, LiteralCommandNode<CommandSourceStack>>()
        fun entries(plugin: JavaPlugin) = brigadier.toList().filter { it.first.isOwner(plugin) }
    }
    
    val aliases: MutableList<String> = mutableListOf()
    var description: String? = null
    
    init {
        brigadier[this] = this.create()
    }
    
    private fun create() = Commands.literal(command).run {
        this.invocation()
        this.build()
    }

    internal fun isOwner(plugin: JavaPlugin) = this.plugin == plugin

    override fun equals(other: Any?): Boolean = other is BrigadierCommand && other.command == command
    override fun hashCode(): Int = command.hashCode()
}

inline fun <S, B : ArgumentBuilder<S, B>> B.literal(
    name: String,
    block: LiteralArgumentBuilder<S>.() -> Unit
): B = apply {
    val child = LiteralArgumentBuilder.literal<S>(name).apply(block)
    this.then(child)
}
inline fun <S, B : ArgumentBuilder<S, B>, T> B.argument(
    name: String,
    type: ArgumentType<T>,
    block: RequiredArgumentBuilder<S, T>.() -> Unit
): B = apply {
    val child = RequiredArgumentBuilder.argument<S, T>(name, type).apply(block)
    this.then(child)
}
inline fun <reified V: Any> CommandContext<CommandSourceStack>.getArgument(name: String): V = this.getArgument(name, V::class.java)