package at.flauschigesalex.lib.minecraft.brigadier

interface AbstractCommandArgumentData<out T> {
    val value: T?
    val index: Int
    val type: CommandArgumentType<*>
}
data class CommandArgumentData<out T>(override val index: Int, override val value: T?, val base: CommandBase, override val type: CommandArgumentType<out T>):
    AbstractCommandArgumentData<T>
data class GreedyCommandArgumentData<out T, out TL : Collection<T>>(override val index: Int, override val value: TL, val base: CommandBase, override val type: CommandArgumentType<out T>): AbstractCommandArgumentData<TL>, Iterable<T> {
    override fun iterator(): Iterator<T> = (value as Collection<T>).iterator()
}