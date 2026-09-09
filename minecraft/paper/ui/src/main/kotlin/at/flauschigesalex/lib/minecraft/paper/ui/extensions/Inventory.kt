@file:Suppress("unused")

package at.flauschigesalex.lib.minecraft.paper.ui.extensions

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

operator fun Inventory.get(index: Int): ItemStack? {
    return this.getItem(index)
}
operator fun Inventory.set(index: Int, item: ItemStack?) {
    this.setItem(index, item)
}
operator fun Inventory.set(index: IntRange, item: ItemStack?) {
    index.forEach { this[it] = item }
}

fun Inventory.fill(item: ItemStack) {
    this[0 until this.size] = item
}