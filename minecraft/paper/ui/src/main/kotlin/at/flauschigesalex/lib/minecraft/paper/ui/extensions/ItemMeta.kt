@file:Suppress("unused")

package at.flauschigesalex.lib.minecraft.paper.ui.extensions

import at.flauschigesalex.lib.minecraft.paper.base.utils.PersistentData
import at.flauschigesalex.lib.minecraft.paper.base.utils.persistentData
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.plugin.java.JavaPlugin

fun ItemMeta.richName(richName: String) = this.customName(MiniMessage.miniMessage().deserialize(richName))
fun ItemMeta.richLore(richLore: List<String>) = this.lore(richLore.map { MiniMessage.miniMessage().deserialize(it) })
fun ItemMeta.richLore(vararg richLore: String) = this.richLore(richLore.toList())
fun ItemMeta.appendRichLore(richLore: List<String>) = this.lore((this.lore()?: mutableListOf()).apply {
    this.addAll(richLore.map { MiniMessage.miniMessage().deserialize(it) })
})
fun ItemMeta.appendRichLore(vararg richLore: String) = this.appendRichLore(richLore.toList())
fun ItemMeta.texture(material: Material) = this.texture(material.key)
fun ItemMeta.texture(key: NamespacedKey) {
    this.itemModel = key
}
fun ItemMeta.persistentData(plugin: JavaPlugin, persistent: PersistentData.() -> Unit) = this.persistentData(plugin, persistent)