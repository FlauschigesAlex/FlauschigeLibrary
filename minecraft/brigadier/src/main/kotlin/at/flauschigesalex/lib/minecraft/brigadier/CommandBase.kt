@file:Suppress("unused")
@file:OptIn(CommandInternal::class)

package at.flauschigesalex.lib.minecraft.brigadier

import at.flauschigesalex.lib.minecraft.brigadier.CommandBase.Companion.DEFAULT_COMMAND_FAIL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import net.kyori.adventure.audience.Audience
import kotlin.coroutines.CoroutineContext

abstract class CommandBase protected constructor(val command: String) {

    companion object {

        @JvmStatic
        var DEFAULT_COMMAND_FAIL: CommandExecutor = { context ->
            context.sender.sendIncompleteCommand(context.fullCommand)
        }

        @JvmStatic
        var DEFAULT_OPTIONAL_ARGUMENT_MODE: OptionalArgumentMode = OptionalArgumentMode.ORDERED
        
        @JvmStatic
        var DEFAULT_COROUTINE_SCOPE: CoroutineContext = SupervisorJob() + Dispatchers.Default
    }

    @CommandInternal object Internal {
        @JvmStatic
        var COMMAND_REGISTRAR: (CommandBuilder) -> Unit = { throw NotImplementedError() }

        @JvmStatic
        var COMMAND_CAN_USE: (Audience, CommandArgument<*>, String, CommandArgumentDataList, Array<out String>) -> Boolean = { _, _, _, _, _ ->
            throw NotImplementedError()
        }

        @JvmStatic
        var COMMAND_HAS_PERMISSION: (Audience, CommandArgument<*>) -> Boolean = { _, _ ->
            throw NotImplementedError()
        }
    }
    
    @CommandInternal
    val commandInternal = InternalCommandBaseMeta()
    
    internal open lateinit var base: CommandBuilder

    fun permission(permission: String?) {
        permission?.let { permission ->
            this.commandInternal.meta["permission"] = permission
        }
    }

    open fun argument(name: String, type: CommandArgumentType<*>, invocation: CommandArgument<*>.() -> Unit) {
        val argument = CommandArgument(name, type)
        
        argument.type.source(argument)
        argument.parent(this)
        argument.invocation()

        this.commandInternal.arguments.add(argument)
    }

    open fun fail(fail: CommandExecutor) {
        this.commandInternal.commandFail = fail
    }

    open fun execute(execute: CommandExecutor) {
        this.commandInternal.coroutineContext = null
        this.commandInternal.commandSuccess = execute
    }
    open fun executeAsync(context: CoroutineContext? = null, execute: CommandExecutor) {
        this.commandInternal.coroutineContext = context ?: DEFAULT_COROUTINE_SCOPE
        this.commandInternal.commandSuccess = execute
    }

    override fun toString(): String = "${this::class.java.simpleName}(command=\"${command}\")"
}

@CommandInternal
class InternalCommandBaseMeta {
    val arguments = mutableListOf<CommandArgument<*>>()
    val meta = mutableMapOf<String, Any>()
    var coroutineContext: CoroutineContext? = null
    
    var overrideSuggestions: ((CommandContext) -> Set<String>)? = null

    @CommandInternal
    val eligibleArguments: List<Pair<CommandArgument<*>, CommandArgument<*>>>
        get() {
            fun CommandArgument<*>.optionalTree(): List<CommandArgument<*>> {
                val list = mutableListOf<CommandArgument<*>>()
                val arguments = this.commandInternal.arguments.filter { it.isOptional }

                list.addAll(arguments)
                arguments.forEach {
                    list.addAll(it.optionalTree())
                }

                return list
            }
            fun CommandArgument<*>.skipTree(): List<CommandArgument<*>> {
                val list = mutableListOf<CommandArgument<*>>()

                list.addAll(this.commandInternal.arguments)
                this.commandInternal.arguments.filter { it.isOptional }.forEach {
                    list.addAll(it.skipTree())
                }

                return list
            }

            val set = mutableSetOf<Pair<CommandArgument<*>, CommandArgument<*>>>()

            // Adds all non-optional arguments to the list
            set.addAll(this.arguments.map { it to it })

            // Adds all optional (sub-) arguments to the list
            val optional = this.arguments.filter { it.isOptional }.flatMap { base -> base.optionalTree().map { base to it } }
            set.addAll(optional)

            // Adds all skippable 
            val skip = this.arguments.filter { it.isOptional }.flatMap { base -> base.skipTree().map { base to it } }
            set.addAll(skip)

            return set.sortedByDescending { it.second.type.priority }
        }

    var commandFail: CommandExecutor = DEFAULT_COMMAND_FAIL
    var commandSuccess: CommandExecutor? = null
}

@CommandInternal
val CommandBase.permission: String?
    get() = this.commandInternal.meta["permission"] as? String