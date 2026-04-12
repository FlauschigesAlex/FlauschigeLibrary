@file:Suppress("unused")

package at.flauschigesalex.lib.minecraft.paper.ui

import at.flauschigesalex.lib.minecraft.api.MojangProfileTexture
import at.flauschigesalex.lib.minecraft.paper.base.utils.PersistentData
import at.flauschigesalex.lib.minecraft.paper.base.utils.isColorable
import at.flauschigesalex.lib.minecraft.paper.base.utils.persistentData
import at.flauschigesalex.lib.minecraft.paper.base.utils.texture
import com.destroystokyo.paper.profile.PlayerProfile
import io.papermc.paper.datacomponent.item.CustomModelData
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.OfflinePlayer
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

@Suppress("MemberVisibilityCanBePrivate")
class ItemCreator<out M: ItemMeta> private constructor(internal val item: ItemStack, private val metaClass: Class<M>, private val constructorConsumer: M.() -> Unit = {}, private val overrideConsumer: M.() -> Unit = {}) {

    companion object {
        fun skull(profile: OfflinePlayer, consumer: (SkullMeta) -> Unit = {}): ItemCreator<SkullMeta> {
            return ItemCreator(Material.PLAYER_HEAD, SkullMeta::class.java, consumer, overrideConsumer = {
                this.owningPlayer = profile
            })
        }
        fun skull(profile: PlayerProfile, consumer: (SkullMeta) -> Unit = {}): ItemCreator<SkullMeta> {
            return ItemCreator(Material.PLAYER_HEAD, SkullMeta::class.java, consumer, overrideConsumer = {
                this.playerProfile = profile
            })
        }
        fun skull(uuid: UUID, consumer: (SkullMeta) -> Unit = {}): ItemCreator<SkullMeta> {
            return skull(Bukkit.getOfflinePlayer(uuid), consumer)
        }
        fun skull(texture: MojangProfileTexture, consumer: (SkullMeta) -> Unit = {}): ItemCreator<SkullMeta> {
            return skull(Bukkit.createProfile(UUID.randomUUID()).texture(texture))
        }

        fun colored(material: Material, consumer: (LeatherArmorMeta) -> Unit = {}): ItemCreator<LeatherArmorMeta> {
            if (!material.isColorable)
                throw IllegalArgumentException("Material '$material' is not colorable!")

            return ItemCreator(material, LeatherArmorMeta::class.java, consumer)
        }
        operator fun invoke(material: Material = defaultMaterial): ItemCreator<ItemMeta> {
            return ItemCreator(material, ItemMeta::class.java)
        }

        fun of(item: ItemStack, consumer: (ItemMeta) -> Unit = {}): ItemCreator<ItemMeta> {
            return ItemCreator(item, ItemMeta::class.java, constructorConsumer = consumer)
        }

        var defaultMaterial = Material.PAPER
    }

    private constructor(material: Material, metaClass: Class<M>, consumer: M.() -> Unit = {}, overrideConsumer: M.() -> Unit = {}) : this(
        ItemStack(material), metaClass, consumer, overrideConsumer
    )

    @Suppress("UNCHECKED_CAST")
    fun item(consumer: M.() -> Unit = {}): ItemStack = item.apply {
        this.editMeta {
            if (!metaClass.isInstance(it))
                return@editMeta

            val meta = it as M

            constructorConsumer(meta)
            consumer(meta)
            overrideConsumer(meta)

            it.displayName(it.displayName()?.let { component ->
                component.style { style ->
                    style.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                }
            })
            it.lore(it.lore()?.map { component ->
                component.style { style ->
                    style.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                }
            })
        }
    }
}

// EXTENSIONS

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

@Suppress("UnstableApiUsage")
fun ItemStack.customModelData(supplier: () -> CustomModelData.Builder) {
    supplier.invoke().build()
} 