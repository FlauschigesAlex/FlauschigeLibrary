@file:Suppress("UnstableApiUsage", "unused")

package at.flauschigesalex.lib.minecraft.paper.base.utils

import at.flauschigesalex.lib.minecraft.api.MojangProfile
import com.destroystokyo.paper.profile.PlayerProfile
import com.destroystokyo.paper.profile.ProfileProperty
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Container
import org.bukkit.block.data.type.CommandBlock
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ColorableArmorMeta
import org.bukkit.inventory.meta.SpawnEggMeta

val Material.isColorable : Boolean
    get() = this.asItemType()?.typed()?.itemMetaClass?.isAssignableFrom(ColorableArmorMeta::class.java) ?: false

val Material.isContainer : Boolean
    get() = asBlockType()?.createBlockData()?.createBlockState() is Container

val Material.isObtainable: Boolean
    get() {
        // BY ITEM
        if (this.isItem.not()) return false
        if (this.isAir) return false

        // SINGLETON
        val unobtainable = setOf(
            Material.BARRIER,
            Material.BEDROCK,
            Material.BUDDING_AMETHYST,
            Material.CHORUS_PLANT,
            Material.COMMAND_BLOCK_MINECART,
            Material.DEBUG_STICK,
            Material.END_PORTAL_FRAME,
            Material.FROGSPAWN,
            Material.JIGSAW,
            Material.KNOWLEDGE_BOOK,
            Material.LIGHT,
            Material.PLAYER_HEAD,
            Material.REINFORCED_DEEPSLATE,
            Material.STRUCTURE_BLOCK,
            Material.STRUCTURE_VOID,
            Material.VAULT,
        )

        if (this in unobtainable) return false

        // BY NAME
        if (this.name.contains("INFESTED_", true)) return false
        if (this.name.contains("UNCRAFTABLE", true)) return false
        if (this.name.contains("TEST", true) && this.name.contains("BLOCK", true)) return false
        if (this.name.contains("SPAWNER", true)) return false

        val item = ItemStack(this)
        val blockData = runCatching { this.createBlockData() }.getOrNull()

        // BY META
        if (item.itemMeta is SpawnEggMeta) return false
        if (blockData is CommandBlock) return false

        return true
    }

fun PlayerProfile.texture(texture: String): PlayerProfile {
    this.setProperty(ProfileProperty("textures", texture, null))
    return this
}

fun Audience.sendRichMessage(message: String, vararg args: Pair<String, Any>) =
    this.sendRichMessage(message, args.map { Placeholder.unparsed(it.first, it.second.toString()) }.toTypedArray())
fun Audience.sendRichMessage(message: String, args: Array<TagResolver>) =
    this.sendMessage(MiniMessage.miniMessage().deserialize(message, *args))
fun Throwable.toRichString(prefix: String = "<red>", hover: (String) -> String = { "<red>$it" }) = "$prefix<hover:\"show_text\":\"${ hover(this.message.toString()) + "\n" + hover(this.stackTrace.toList().take(7).joinToString("\n") { hover(it.toString()).substringAfterLast("//") }) }\">${this::class.simpleName}</hover>"

fun MojangProfile.toPlayerProfile() = Bukkit.createProfileExact(this.uniqueId, this.name)
fun MojangProfile.toOfflinePlayer() = Bukkit.getOfflinePlayer(this.uniqueId)