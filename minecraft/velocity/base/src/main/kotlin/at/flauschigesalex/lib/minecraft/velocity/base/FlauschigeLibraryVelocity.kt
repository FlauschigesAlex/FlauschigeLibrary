package at.flauschigesalex.lib.minecraft.velocity.base

import at.flauschigesalex.lib.base.general.Reflector
import at.flauschigesalex.lib.minecraft.velocity.base.command.CommandConfigurator
import at.flauschigesalex.lib.minecraft.velocity.base.internal.VelocityListener
import at.flauschigesalex.lib.minecraft.velocity.base.internal.VelocityReflect
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import java.lang.reflect.Modifier


@Suppress("unused", "UNUSED_EXPRESSION", "MemberVisibilityCanBePrivate")
object FlauschigeLibraryVelocity {

    lateinit var server: ProxyServer
        private set

    private val _activeData = mutableSetOf<InternalPluginData>()
    val activeData: Set<InternalPluginData>
        get() = _activeData.toSet()

    fun init(plugin: Any, server: ProxyServer, packages: String? = null) {
        val packageName = packages ?: plugin.javaClass.packageName
        val data = InternalPluginData(plugin, packageName)

        if (_activeData.any { (it.plugin == plugin) })
            return

        firstInit(plugin, server)
        _activeData.add(data)
        reflectPaper(plugin, packageName, server)
    }

    private fun firstInit(plugin: Any, server: ProxyServer) {
        if (_activeData.isNotEmpty())
            return

        CommandConfigurator // ENABLES COMMAND REGISTRATION
        this.server = server
    }

    private fun reflectPaper(plugin: Any, packageName: String, server: ProxyServer) {

        Reflector.reflect(plugin.javaClass.classLoader, packageName).getSubTypes(VelocityReflect::class.java).filter {
            !Modifier.isAbstract(it.modifiers) && !it.isAnonymousClass
        }.forEach {
            try {
                var instance = it.kotlin.objectInstance
                if (instance == null) {
                    val constructor = it.getDeclaredConstructor()
                    constructor.isAccessible = true
                    instance = constructor.newInstance()
                }

                if (instance is VelocityListener) {
                    if (!instance.autoRegister)
                        return@forEach
                    instance.register(server, plugin)
                } else println("Useless reflection found for ${VelocityReflect::class.java.simpleName}: ${it.name}")

            } catch (fail: Exception) {
                fail.printStackTrace()
            }
        }
    }
}

data class InternalPluginData(val plugin: Any, val packageName: String)