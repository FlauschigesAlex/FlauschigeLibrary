@file:Suppress("unused", "UNUSED_EXPRESSION")

package at.flauschigesalex.lib.minecraft.paper.base

import at.flauschigesalex.lib.base.general.Reflector
import at.flauschigesalex.lib.minecraft.api.CacheableMojangProfile
import at.flauschigesalex.lib.minecraft.api.MojangAPI
import at.flauschigesalex.lib.minecraft.api.MojangProfile
import at.flauschigesalex.lib.minecraft.api.MojangProfileTexture
import at.flauschigesalex.lib.minecraft.paper.base.command.BrigadierCommand
import at.flauschigesalex.lib.minecraft.paper.base.command.CommandConfigurator
import at.flauschigesalex.lib.minecraft.paper.base.command.TabCompleteListener
import at.flauschigesalex.lib.minecraft.paper.base.events.PaperReflectFinishEvent
import at.flauschigesalex.lib.minecraft.paper.base.internal.PaperCommand
import at.flauschigesalex.lib.minecraft.paper.base.internal.PaperListener
import at.flauschigesalex.lib.minecraft.paper.base.internal.PaperReflect
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import java.lang.reflect.Modifier

@Deprecated("") typealias FlauschigeLibrary = FlauschigeLibraryPaper

@Suppress("unused", "MemberVisibilityCanBePrivate")
object FlauschigeLibraryPaper {

    private val _activeData = mutableSetOf<InternalPluginData>()
    val activeData: Set<InternalPluginData>
        get() = _activeData.toSet()
    
    fun init(plugin: JavaPlugin, packages: String? = null): InternalPluginData {
        val packageName = packages ?: plugin.javaClass.packageName
        val data = InternalPluginData(plugin, packageName)

        if (_activeData.any { (it.plugin == plugin) })
            return data

        this.firstInit(plugin)
        _activeData.add(data)
        this.reflectPaper(data)
        return data
    }

    private fun firstInit(plugin: JavaPlugin) {
        if (_activeData.isNotEmpty())
            return

        CommandConfigurator // ENABLES COMMAND REGISTRATION

        MojangAPI.addNameLookup(MojangAPI.LookupCall.BEFORE) { uuid ->
            val player = Bukkit.getPlayer(uuid) ?: return@addNameLookup null
            val textures = player.playerProfile.properties.firstOrNull { it.name.equals("textures", true) }?.let { it.value to it.signature!! }
            val profile = MojangProfile(player.name, player.uniqueId, textures?.let { MojangProfileTexture(it.first, it.second) })
            return@addNameLookup CacheableMojangProfile(profile)
        }
        MojangAPI.addUuidLookup(MojangAPI.LookupCall.BEFORE) { name ->
            val player = Bukkit.getPlayerExact(name) ?: return@addUuidLookup null
            val textures = player.playerProfile.properties.firstOrNull { it.name.equals("textures", true) }?.let { it.value to it.signature!! }
            val profile = MojangProfile(player.name, player.uniqueId, textures?.let { MojangProfileTexture(it.first, it.second) })
            return@addUuidLookup CacheableMojangProfile(profile)
        }
    }

    private fun reflectPaper(data: InternalPluginData) {
        val (plugin, packageName) = data

        TabCompleteListener.register(plugin)
        
        Reflector.reflect(plugin.javaClass.classLoader, packageName).getSubTypes(PaperReflect::class.java).filter {
            !Modifier.isAbstract(it.modifiers) && !it.isAnonymousClass
        }.forEach {
            try {
                var instance = it.kotlin.objectInstance
                if (instance == null) {
                    val constructor = it.getDeclaredConstructor()
                    constructor.isAccessible = true
                    instance = constructor.newInstance()
                }

                @Suppress("DEPRECATION")
                when (instance) {
                    is PaperCommand -> instance.register(Bukkit.getCommandMap())
                    is PaperListener -> {
                        if (!instance.autoRegister)
                            return@forEach
                        instance.register(plugin)
                    }

                    else -> println("Useless reflection found for ${PaperReflect::class.java.simpleName}: ${it.name}")
                }

            } catch (fail: Exception) {
                fail.printStackTrace()
            }
        }
        
        if (plugin is Listener)
            Bukkit.getPluginManager().registerEvents(plugin, plugin)
        
        plugin.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            val registrar = event.registrar()

            BrigadierCommand.brigadier.forEach { (command, node) ->
                plugin.logger.info { "Registered new brigadier command: ${command.command}" }
                registrar.register(node, command.description, command.aliases)
            }
        }

        PaperReflectFinishEvent(data).callEvent()
    }
}

data class InternalPluginData(val plugin: JavaPlugin, val packageName: String)