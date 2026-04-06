package at.flauschigesalex.lib.minecraft.paper.base.events

import at.flauschigesalex.lib.minecraft.paper.base.InternalPluginData
import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Called after a plugin is properly initialized within the library.
 */
@Suppress("unused")
class PaperReflectFinishEvent internal constructor(val data: InternalPluginData) : Event(Bukkit.isPrimaryThread().not()) {
    companion object {
        @JvmField
        val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = HANDLERS
    }
    override fun getHandlers(): HandlerList = HANDLERS
}