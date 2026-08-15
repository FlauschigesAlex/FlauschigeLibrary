@file:Suppress("unused")

package at.flauschigesalex.lib.minecraft.velocity.base.utils

import at.flauschigesalex.lib.minecraft.api.MojangProfile
import at.flauschigesalex.lib.minecraft.api.MojangProfileTexture
import at.flauschigesalex.lib.minecraft.velocity.base.FlauschigeLibraryVelocity
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.util.GameProfile
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import java.util.concurrent.CompletableFuture

fun CommandSource.performCommand(command: String): CompletableFuture<Boolean> =
    FlauschigeLibraryVelocity.server.commandManager.executeAsync(this, command)

fun GameProfile.texture(texture: MojangProfileTexture): GameProfile {
    val properties = this.properties.toMutableList()
    properties.removeIf { it.name == "textures" }
    
    properties += GameProfile.Property("textures", texture.value, texture.signature)
    this.properties
    
    return this.withProperties(properties)
}

fun MojangProfile.toGameProfile(): GameProfile {
    val list = mutableListOf<GameProfile.Property>()

    this.texture?.let { texture ->
        list.add(GameProfile.Property("textures", texture.value, texture.signature))
    }

    return GameProfile(this.uniqueId, this.name, list)
}
fun GameProfile.toMojangProfile(): MojangProfile {
    val texture = this.properties.find { it.name == "textures" }?.let {
        MojangProfileTexture(it.value, it.signature)
    }
    return MojangProfile(this.name, this.id, texture)
}