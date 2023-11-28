package at.flauschigesalex.lib.minecraft.velocity.base.events

import at.flauschigesalex.lib.minecraft.velocity.base.InternalPluginData

/**
 * Called after a plugin is properly initialized within the library.
 */
@Suppress("unused")
class VelocityReflectFinishEvent internal constructor(val data: InternalPluginData)