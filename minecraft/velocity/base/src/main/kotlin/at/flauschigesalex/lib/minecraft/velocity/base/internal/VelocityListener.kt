package at.flauschigesalex.lib.minecraft.velocity.base.internal

import com.velocitypowered.api.proxy.ProxyServer


@Suppress("unused")
abstract class VelocityListener(val autoRegister: Boolean = true) : VelocityReflect {

    fun register(server: ProxyServer, plugin: Any) {
        server.eventManager.register(plugin, this)
    }

    fun unregister(server: ProxyServer, plugin: Any) {
        server.eventManager.register(plugin, this)
    }
}