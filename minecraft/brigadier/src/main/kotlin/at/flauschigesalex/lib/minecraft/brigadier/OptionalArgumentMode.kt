package at.flauschigesalex.lib.minecraft.brigadier

@Suppress("unused")
enum class OptionalArgumentMode {
    /**
     * Defines optional argument parsing as ordered.<br>
     * This means that optional arguments must be called in the correct order.
     */
    ORDERED,
    /**
     * Defines optional argument parsing as unordered.<br>
     * This means that optional arguments can be called in any order.
     */
    UNORDERED,
    ;
}