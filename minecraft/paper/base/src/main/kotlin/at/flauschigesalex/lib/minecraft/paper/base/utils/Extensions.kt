@file:Suppress("UnstableApiUsage", "unused")

package at.flauschigesalex.lib.minecraft.paper.base.utils

import com.destroystokyo.paper.profile.PlayerProfile
import com.destroystokyo.paper.profile.ProfileProperty
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Material
import org.bukkit.block.Container
import org.bukkit.inventory.meta.ColorableArmorMeta

val Material.isColorable : Boolean
    get() = this.asItemType()?.typed()?.itemMetaClass?.isAssignableFrom(ColorableArmorMeta::class.java) ?: false

val Material.isContainer : Boolean
    get() = asBlockType()?.createBlockData()?.createBlockState() is Container

fun PlayerProfile.texture(texture: String): PlayerProfile {
    this.setProperty(ProfileProperty("textures", texture, null))
    return this
}

fun Audience.sendRichMessage(message: String, vararg args: Pair<String, Any>) =
    this.sendRichMessage(message, args.map { Placeholder.unparsed(it.first, it.second.toString()) }.toTypedArray())
fun Audience.sendRichMessage(message: String, args: Array<TagResolver>) =
    this.sendMessage(MiniMessage.miniMessage().deserialize(message, *args))
fun Throwable.toRichString(prefix: String = "<red>", hover: (String) -> String = { "<red>$it" }) = "$prefix<hover:\"show_text\":\"${ hover(this.message.toString()) + "\n" + hover(this.stackTrace.toList().take(7).joinToString("\n") { hover(it.toString()).substringAfterLast("//") }) }\">${this::class.simpleName}</hover>"