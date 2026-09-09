@file:Suppress("unused")

package at.flauschigesalex.lib.minecraft.paper.ui.extensions

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.CustomModelData
import org.bukkit.inventory.ItemStack

fun ItemStack.customModelData(supplier: () -> CustomModelData.Builder) {
    setData(DataComponentTypes.CUSTOM_MODEL_DATA, supplier().build())
}