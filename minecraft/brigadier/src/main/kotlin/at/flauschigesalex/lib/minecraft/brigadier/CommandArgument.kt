@file:Suppress("UNCHECKED_CAST", "unused")

package at.flauschigesalex.lib.minecraft.brigadier

import at.flauschigesalex.lib.minecraft.brigadier.types.internal.LiteralArgumentType
import net.kyori.adventure.audience.Audience
import org.jetbrains.annotations.Range
import org.jetbrains.annotations.Unmodifiable

data class CommandArgument<T: CommandArgumentType<*>>(val name: String, val type: T) : CommandBase(name) {

    @CommandInternal
    lateinit var parent: CommandBase
        private set
    
    @OptIn(CommandInternal::class)
    internal fun parent(base: CommandBase) {
        this.parent = base
    }

    fun depth(): Int {
        var depth = 0
        var current: CommandBase? = this
        while (current != null) {
            depth++
            current = current.arguments.firstOrNull()
        }
        return depth
    }
    
    internal val meta = mutableMapOf<String, Any>()
    @CommandInternal
    fun getMeta(key: String): Any? = meta[key]
    
    fun optional() {
        meta["optional"] = true
    }

    fun suggest(suggest: Boolean) {
        meta["suggest"] = suggest
    }

    @CommandInternal
    val argInternal = InternalCommandArgumentMeta()

    @OptIn(CommandInternal::class)
    fun require(consumer: CommandRequirement) {
        this.argInternal.requirements.add(consumer)
    }
    
    @OptIn(CommandInternal::class)
    fun canUse(sender: Audience, fullCommand: String, data: CommandArgumentDataList, args: Array<out String>): Boolean {
        return Internal.COMMAND_CAN_USE(sender, this, fullCommand, data, args) && hasPermission(sender)
    }

    @OptIn(CommandInternal::class)
    fun hasPermission(sender: Audience): Boolean {
        return Internal.COMMAND_HAS_PERMISSION(sender, this)
    }
}
data class CommandArgumentDataList(private val _arguments: MutableSet<AbstractCommandArgumentData<*>> = mutableSetOf()): Iterable<AbstractCommandArgumentData<*>> {
    val arguments get(): @Unmodifiable Set<AbstractCommandArgumentData<*>> = _arguments.toSet()
    
    fun add(data: AbstractCommandArgumentData<*>) = _arguments.add(data)
    override fun iterator(): Iterator<AbstractCommandArgumentData<*>> = _arguments.iterator()
    val size: Int = _arguments.size

    fun byName(name: String): CommandArgumentData<*>? {
        return arguments
            .filterIsInstance(CommandArgumentData::class.java)
            .find { it.base.command.equals(name, true) }
    }

    inline fun <reified T> byType(name: String? = null): CommandArgumentData<T>? {
        return arguments
            .filterIsInstance(CommandArgumentData::class.java)
            .filter { name == null || it.base.command.equals(name, true) }
            .filter { it.type !is LiteralArgumentType }
            .firstOrNull { it.value is T } as? CommandArgumentData<T>
    }
    fun byIndex(index: @Range(from = 0, to = Int.MAX_VALUE.toLong()) Int): CommandArgumentData<*>? {
        return arguments
            .filterIsInstance(CommandArgumentData::class.java)
            .find { it.index == index}
    }

    fun greedyByName(name: String): GreedyCommandArgumentData<*, *>? {
        return arguments
            .filterIsInstance(GreedyCommandArgumentData::class.java)
            .find { it.base.command.equals(name, true) }
    }
    fun <T> greedyByType(name: String? = null): GreedyCommandArgumentData<T, Collection<T>>? {
        return arguments
            .filterIsInstance(GreedyCommandArgumentData::class.java)
            .filter { name == null || it.base.command.equals(name, true) }
            .filter { it.type !is LiteralArgumentType }
            .firstNotNullOfOrNull { it as? GreedyCommandArgumentData<T, Collection<T>> }
    }
    fun greedyByIndex(index: @Range(from = 0, to = Int.MAX_VALUE.toLong()) Int): GreedyCommandArgumentData<*, *>? {
        return arguments
            .filterIsInstance(GreedyCommandArgumentData::class.java)
            .find { it.index == index }
    }
}

@CommandInternal
class InternalCommandArgumentMeta {
    val requirements = mutableListOf<CommandRequirement>()
}

val CommandArgument<*>.isOptional: Boolean
    get() = meta["optional"] as? Boolean ?: false
val CommandArgument<*>.shouldSuggest: Boolean
    get() = meta["suggest"] as? Boolean ?: true