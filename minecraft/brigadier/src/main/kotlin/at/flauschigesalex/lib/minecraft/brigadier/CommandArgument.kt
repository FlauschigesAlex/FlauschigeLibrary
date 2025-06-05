@file:Suppress("UNCHECKED_CAST", "unused")

package at.flauschigesalex.lib.minecraft.brigadier

import at.flauschigesalex.lib.minecraft.brigadier.types.internal.LiteralArgumentType
import org.jetbrains.annotations.Range
import java.util.*

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

    @CommandInternal
    var optional = false
        private set
    
    @OptIn(CommandInternal::class)
    fun optional() {
        this.optional = true
    }

    @CommandInternal
    val requirements = mutableListOf<CommandRequirement>()
    @OptIn(CommandInternal::class)
    fun require(consumer: CommandRequirement) {
        this.requirements.add(consumer)
    }
    
    @OptIn(CommandInternal::class)
    fun canUse(sender: UUID, fullCommand: String, data: Collection<CommandArgument<*>>, args: Array<out String>): Boolean {
        return COMMAND_CAN_USE(sender, this, fullCommand, data, args) && hasPermission(sender)
    }

    @OptIn(CommandInternal::class)
    fun hasPermission(sender: UUID): Boolean {
        return COMMAND_HAS_PERMISSION(sender, this)
    }
}
data class CommandArgumentDataList(val arguments: MutableSet<AbstractCommandArgumentData<*>> = mutableSetOf()): Iterable<AbstractCommandArgumentData<*>> {

    fun add(data: AbstractCommandArgumentData<*>) = arguments.add(data)
    private val entries get(): Set<AbstractCommandArgumentData<*>> = arguments.toSet()
    override fun iterator(): Iterator<AbstractCommandArgumentData<*>> = entries.iterator()
    val size: Int
        get() = entries.size

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