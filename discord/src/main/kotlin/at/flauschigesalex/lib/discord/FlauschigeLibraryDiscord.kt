package at.flauschigesalex.lib.discord

import at.flauschigesalex.lib.base.general.Reflector
import at.flauschigesalex.lib.discord.command.CommandListener
import at.flauschigesalex.lib.discord.listener.DiscordListener
import net.dv8tion.jda.api.JDA
import java.lang.reflect.Modifier

object FlauschigeLibraryDiscord {
    
    private var isRegistered = false
    
    internal lateinit var JDA: JDA
        private set
    
    fun init(jda: JDA, supervisorClass: Class<*>) = this.init(jda, supervisorClass.packageName, supervisorClass.classLoader)
    fun init(jda: JDA, packages: String, loader: ClassLoader) {
        if (this.isRegistered) return
        this.isRegistered = true

        JDA = jda.awaitReady()
        
        JDA.addEventListener(CommandListener)

        Reflector.reflect(loader, packages).getSubTypes(DiscordListener::class.java)
            .filterNot {
                Modifier.isAbstract(it.modifiers)
            }.forEach { listenerClass ->
                runCatching {
                    listenerClass.kotlin.objectInstance?.let { listener ->
                        JDA.addEventListener(listener)
                    }
                    
                    val constructor = listenerClass.getDeclaredConstructor()
                    constructor.isAccessible = true
                    val listener = constructor.newInstance()
                    
                    JDA.addEventListener(listener)
                }
            }
    }
}