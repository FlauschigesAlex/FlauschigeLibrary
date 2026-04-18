package at.flauschigesalex.lib.minecraft.brigadier

import net.kyori.adventure.audience.Audience

@Suppress("UNCHECKED_CAST", "unused")
abstract class CommandBase protected constructor(val command: String) {

    companion object {

        @JvmStatic
        var DEFAULT_COMMAND_FAIL: CommandExecutor = { context ->
            context.sender.sendIncompleteCommand(context.fullCommand)
        }
        @JvmStatic
        var DEFAULT_COMMAND_LABEL: String? = null

        @JvmStatic
        var DEFAULT_OPTIONAL_ARGUMENT_MODE: OptionalArgumentMode = OptionalArgumentMode.ORDERED
        
        @JvmStatic
        @CommandInternal
        var COMMAND_REGISTRAR: (CommandBuilder) -> Unit = { throw NotImplementedError() }
        
        @JvmStatic
        @CommandInternal
        var COMMAND_CAN_USE: (Audience, CommandArgument<*>, String, CommandArgumentDataList, Array<out String>) -> Boolean = { _, _, _, _, _ ->
            throw NotImplementedError()
        }
        
        @JvmStatic
        @CommandInternal
        var COMMAND_HAS_PERMISSION: (Audience, CommandArgument<*>) -> Boolean = { _, _ ->
            throw NotImplementedError()
        }
    }

    @CommandInternal
    var permission: String? = null
        private set
    
    @OptIn(CommandInternal::class)
    fun permission(permission: String?) {
        this.permission = permission
    }

    /**
     * Returns a list of arguments that can be accessed from this parent argument.
     */
    val arguments = mutableListOf<CommandArgument<*>>()
    
    @CommandInternal
    val eligibleArguments: List<Pair<CommandArgument<*>, CommandArgument<*>>>
        get() {
            fun CommandArgument<*>.optionalTree(): List<CommandArgument<*>> {
                val list = mutableListOf<CommandArgument<*>>()
                val arguments = arguments.filter { it.optional }
                
                list.addAll(arguments)
                arguments.forEach { 
                    list.addAll(it.optionalTree())
                }

                return list
            }
            fun CommandArgument<*>.skipTree(): List<CommandArgument<*>> {
                val list = mutableListOf<CommandArgument<*>>()
                
                list.addAll(arguments)
                arguments.filter { it.optional }.forEach { 
                    list.addAll(it.skipTree())
                }

                return list
            }

            val set = mutableSetOf<Pair<CommandArgument<*>, CommandArgument<*>>>()
            
            // Adds all non-optional arguments to the list
            set.addAll(arguments.map { it to it })
            
            // Adds all optional (sub-) arguments to the list
            val optional = arguments.filter { it.optional }.flatMap { base -> base.optionalTree().map { base to it } }
            set.addAll(optional)
            
            // Adds all skippable 
            val skip = arguments.filter { it.optional }.flatMap { base -> base.skipTree().map { base to it } }
            set.addAll(skip)
            
            return set.sortedByDescending { it.second.type.priority }
        }

    @Suppress("DEPRECATION")
    @Deprecated("Redundant force of declaration", ReplaceWith("argument(argument, TODO(\"ArgumentType\")) {\nTODO(\"Unused CommandArgument implementation\")\n}"), DeprecationLevel.ERROR)
    open fun argument(argument: CommandArgument<*>): Unit = throw IllegalArgumentException()
    
    @Suppress("DEPRECATION")
    open fun argument(name: String, type: CommandArgumentType<*>, invocation: CommandArgument<*>.() -> Unit) {
        val argument = CommandArgument(name, type)
        
        argument.type.source(argument)
        argument.parent(this)
        argument.invocation()

        arguments.add(argument)
    }

    var commandFail: CommandExecutor = DEFAULT_COMMAND_FAIL
    open fun fail(fail: CommandExecutor) {
        this.commandFail = fail
    }

    var commandSuccess: CommandExecutor? = null
    open fun execute(execute: CommandExecutor) {
        this.commandSuccess = execute
    }

    override fun toString(): String = "${this::class.java.simpleName}(command=\"${command}\")"
}