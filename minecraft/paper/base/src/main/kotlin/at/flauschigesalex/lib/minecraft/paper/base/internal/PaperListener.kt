package at.flauschigesalex.lib.minecraft.paper.base.internal

import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

@Suppress("unused", "DEPRECATION")
abstract class PaperListener(val autoRegister: Boolean = true) : Listener, PaperReflect {

    @Deprecated("")
    protected lateinit var plugin: JavaPlugin
    
    var isRegistered = false
        private set

    fun register(plugin: JavaPlugin) {
        if (isRegistered)
            return
        
        this.plugin = plugin
        plugin.logger.info("Registered PaperListener: ${this::class.java.name}")
        
        try {       
            plugin.server.pluginManager.registerEvents(this, plugin)
            isRegistered = true
        } catch (_: Exception) {}
    }

    fun unregister() {
        if (!isRegistered)
            return
        
        plugin.logger.info("Unregistered PaperListener: ${this::class.java.name}")
        
        try {
            HandlerList.unregisterAll(this)
            isRegistered = false
        } catch (_: Exception) {}
    }
}