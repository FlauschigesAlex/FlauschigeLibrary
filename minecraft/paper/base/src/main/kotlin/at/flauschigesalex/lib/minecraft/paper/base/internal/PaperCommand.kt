@file:Suppress("DeprecatedCallableAddReplaceWith", "MemberVisibilityCanBePrivate", "UNUSED_PARAMETER", "CanBeParameter")

package at.flauschigesalex.lib.minecraft.paper.base.internal

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import java.util.*
import java.util.concurrent.Executors

@Suppress("unused", "DEPRECATION", "RedundantModalityModifier")
@Deprecated("", ReplaceWith("BrigadierCommand(command)"))
abstract class PaperCommand protected constructor(val command: String, description: String, usage: String, aliases: ArrayList<String?>)
    : Command(command, description, usage, aliases), PaperReflect {

        companion object {
            var defaultCommandPermission: String? = null
        }

    var async: Boolean = false

    var pluginPrefix: String
        private set

    protected constructor(command: String) : this(command, "", "/$command")
    protected constructor(command: String, description: String = "") : this(command, "", "/$command")

    protected constructor(command: String, description: String, usage: String) : this(
        command, "",
        "/$command", ArrayList<String?>()
    )

    init {
        this.pluginPrefix = command
        this.permission = this.permission
    }

    @Deprecated("", ReplaceWith("this.command")) override fun setName(name: String): Boolean {
        return super.setName(name)
    }

    @Deprecated("", ReplaceWith("this.permissible(permissible)"), level = DeprecationLevel.ERROR)
    override fun testPermission(permissible: CommandSender): Boolean {
        return this.permissible(permissible)
    }

    @Deprecated("", ReplaceWith("this.permissible(permissible)"), level = DeprecationLevel.ERROR)
    override fun testPermissionSilent(target: CommandSender): Boolean {
        return super.testPermissionSilent(target)
    }

    override fun getLabel(): String {
        return pluginPrefix
    }

    @Deprecated("", level = DeprecationLevel.HIDDEN) override fun getPermissionMessage(): String? {
        return super.getPermissionMessage()
    }

    @Deprecated("", level = DeprecationLevel.HIDDEN) override fun setPermissionMessage(permissionMessage: String?): Command {
        return super.setPermissionMessage(permissionMessage)
    }

    open override fun getPermission(): String? {
        return defaultCommandPermission?.replace("%command%", command)
    }

    override fun setAliases(aliases: List<String>): Command {
        return super.setAliases(aliases)
    }

    @Deprecated("", level = DeprecationLevel.HIDDEN) override fun execute(commandSender: CommandSender, s: String, strings: Array<String>): Boolean {
        if (async) {
            Executors.newCachedThreadPool().execute { this.executeCommand(commandSender, s, strings) }
            return true
        }

        this.executeCommand(commandSender, s, strings)
        return true
    }

    protected abstract fun executeCommand(sender: CommandSender, fullCommand: String, args: Array<String>)

    fun permissible(permissible: CommandSender): Boolean {
        return super.testPermission(permissible)
    }

    fun setPluginPrefix(pluginPrefix: String): PaperCommand {
        this.pluginPrefix = pluginPrefix
        return this
    }
}
