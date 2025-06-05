@file:Suppress("DeprecatedCallableAddReplaceWith", "MemberVisibilityCanBePrivate", "CanBeParameter")

package at.flauschigesalex.lib.minecraft.velocity.base.internal

import com.mojang.brigadier.tree.CommandNode
import com.velocitypowered.api.command.CommandMeta
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.command.RawCommand
import com.velocitypowered.api.proxy.ProxyServer
import java.util.concurrent.Executors

@Suppress("unused")
abstract class VelocityCommand protected constructor(): RawCommand, VelocityReflect {

    companion object {
        var defaultCommandPermission: String? = null
    }

    protected abstract val data: CommandData
    val command: String by lazy { data.command }
    val aliases: List<String> by lazy { data.aliases }
    private val _internalAliases: MutableList<String>
        get() {
            val list = aliases.toMutableList()
            list.addFirst(command)
            return list
        }

    var async: Boolean = false
    var permission: String? = defaultCommandPermission

    private fun toMeta(plugin: Any): CommandMeta {
        return object : CommandMeta {
            override fun getAliases(): MutableCollection<String> = this@VelocityCommand._internalAliases
            override fun getHints(): MutableCollection<CommandNode<CommandSource>> = mutableListOf()
            override fun getPlugin(): Any = plugin
        }
    }

    var isRegistered: Boolean = false
    fun register(server: ProxyServer, plugin: Any) {
        if (isRegistered)
            return

        val meta = toMeta(plugin)
        server.commandManager.register(meta, this)
        this.isRegistered = true
    }

    override fun execute(context: RawCommand.Invocation) {
        val commandSender = context.source()
        val fullCommand = context.alias()+" "+context.arguments()
        val strings = context.arguments().split(" ").toTypedArray()

        if (async) {
            Executors.newCachedThreadPool().execute { this.executeCommand(commandSender, fullCommand, strings) }
            return
        }

        this.executeCommand(commandSender, fullCommand, strings)
        return
    }

    protected abstract fun executeCommand(sender: CommandSource, fullCommand: String, stringArgs: Array<String>)
}
data class CommandData(val command: String, val aliases: List<String>)