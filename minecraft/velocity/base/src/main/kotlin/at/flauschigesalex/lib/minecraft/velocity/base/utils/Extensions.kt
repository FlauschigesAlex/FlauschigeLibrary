@file:Suppress("unused")

package at.flauschigesalex.lib.minecraft.velocity.base.utils

import at.flauschigesalex.lib.minecraft.velocity.base.FlauschigeLibraryVelocity
import com.velocitypowered.api.command.CommandSource
import java.util.concurrent.CompletableFuture

fun CommandSource.performCommand(command: String): CompletableFuture<Boolean> =
    FlauschigeLibraryVelocity.server.commandManager.executeAsync(this, command)