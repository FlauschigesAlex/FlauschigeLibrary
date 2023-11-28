@file:Suppress("unused", "UNUSED_EXPRESSION", "MemberVisibilityCanBePrivate")

package at.flauschigesalex.lib.minecraft.velocity.base

import at.flauschigesalex.lib.base.file.FileManager
import at.flauschigesalex.lib.base.general.Reflector
import at.flauschigesalex.lib.minecraft.api.CacheableMojangProfile
import at.flauschigesalex.lib.minecraft.api.MojangAPI
import at.flauschigesalex.lib.minecraft.api.MojangProfile
import at.flauschigesalex.lib.minecraft.api.MojangProfileTexture
import at.flauschigesalex.lib.minecraft.velocity.base.command.CommandConfigurator
import at.flauschigesalex.lib.minecraft.velocity.base.events.VelocityReflectFinishEvent
import at.flauschigesalex.lib.minecraft.velocity.base.internal.VelocityListener
import at.flauschigesalex.lib.minecraft.velocity.base.internal.VelocityReflect
import com.velocitypowered.api.proxy.ProxyServer
import java.io.File
import java.lang.reflect.Modifier
import kotlin.jvm.optionals.getOrNull


object FlauschigeLibraryVelocity {

    lateinit var server: ProxyServer
        private set

    private val _activeData = mutableSetOf<InternalPluginData>()
    val activeData: Set<InternalPluginData>
        get() = _activeData.toSet()

    fun init(plugin: Any, server: ProxyServer, packages: String? = null, registerSelfListener: Boolean = false): InternalPluginData {
        val packageName = packages ?: plugin.javaClass.packageName
        val data = InternalPluginData(plugin, packageName)

        if (_activeData.any { (it.plugin == plugin) })
            return data

        firstInit(plugin, server)
        _activeData.add(data)
        reflectPaper(data, server, registerSelfListener)
        return data
    }

    private fun firstInit(plugin: Any, server: ProxyServer) {
        if (_activeData.isNotEmpty())
            return

        CommandConfigurator // ENABLES COMMAND REGISTRATION
        this.server = server
        
        MojangAPI.addNameLookup(MojangAPI.LookupCall.BEFORE) { uuid ->
            val player = server.getPlayer(uuid).getOrNull() ?: return@addNameLookup null
            val textures = player.gameProfile.properties.firstOrNull { it.name.equals("textures", true) }?.let { it.value to it.signature!! }
            val profile = MojangProfile(player.username, player.uniqueId, textures?.let { MojangProfileTexture(it.first, it.second) })
            return@addNameLookup CacheableMojangProfile(profile)
        }
        MojangAPI.addUuidLookup(MojangAPI.LookupCall.BEFORE) { name ->
            val player = server.getPlayer(name).getOrNull() ?: return@addUuidLookup null
            val textures = player.gameProfile.properties.firstOrNull { it.name.equals("textures", true) }?.let { it.value to it.signature!! }
            val profile = MojangProfile(player.username, player.uniqueId, textures?.let { MojangProfileTexture(it.first, it.second) })
            return@addUuidLookup CacheableMojangProfile(profile)
        }
    }

    private fun reflectPaper(data: InternalPluginData, server: ProxyServer, registerSelfListener: Boolean) {
        val (plugin, packageName) = data
        
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
        
        if (registerSelfListener && plugin !is VelocityListener)
            server.eventManager.register(plugin, plugin)
        
        server.eventManager.fire(VelocityReflectFinishEvent(data))
    }
}

data class InternalPluginData(internal val plugin: Any, val packageName: String) {
    val name: String
        get() = plugin.javaClass.simpleName
    
    val dataFolder: File
        get() = FileManager("plugins/$name").let { 
            if (!it.exists) it.createDirectory()
            it.file
        }
    
    init {
        dataFolder // CREATE DATA FOLDER
    }
}