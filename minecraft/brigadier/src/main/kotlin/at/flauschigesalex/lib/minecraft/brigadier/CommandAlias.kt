package at.flauschigesalex.lib.minecraft.brigadier

@Suppress("UNCHECKED_CAST", "unused")
interface CommandAlias<B> {

    val aliases: MutableList<String>
    fun alias(vararg alias: String): B = this.alias(alias.toList())
    fun alias(alias: Collection<String>): B {
        this.aliases.addAll(alias)
        return this as B
    }
}