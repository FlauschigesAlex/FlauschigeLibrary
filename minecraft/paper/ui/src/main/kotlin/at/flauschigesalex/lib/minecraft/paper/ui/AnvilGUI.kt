@file:Suppress("DeprecatedCallableAddReplaceWith", "UnstableApiUsage", "unused", "MemberVisibilityCanBePrivate", "DEPRECATION")
@file:OptIn(ExperimentalStdlibApi::class)

package at.flauschigesalex.lib.minecraft.paper.ui

import at.flauschigesalex.lib.base.general.Validator
import at.flauschigesalex.lib.minecraft.paper.base.internal.PaperListener
import at.flauschigesalex.lib.minecraft.paper.ui.PluginGUI.Companion.getOpenGUI
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.view.AnvilView
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.annotations.Range
import java.util.*
import java.util.concurrent.CompletableFuture

private val anvilTypingControllers = HashMap<Player, UUID>()

/**
 * @since 1.6.0
 */

@ExperimentalStdlibApi
abstract class AnvilGUI(
    plugin: JavaPlugin,
    autoUpdateTickDelay: @Range(from = 1, to = Long.MAX_VALUE) Int = 0,
    val legacyTitle: String = " ",
) : PluginGUI(plugin, 9, Component.text(legacyTitle), autoUpdateTickDelay) {

    companion object {
        @JvmStatic
        protected fun String.toLegacyColored(): String = LegacyComponentSerializer.legacy('§').serialize(MiniMessage.miniMessage().deserialize(this))

        val defaultInputItem: ItemStack = ItemCreator(Material.LIGHT_GRAY_STAINED_GLASS_PANE).item {
            this.isHideTooltip = true
            this.richName("")
        }
    }

    @Deprecated("", level = DeprecationLevel.HIDDEN)
    final override val size: Int = 0

    @Deprecated("", level = DeprecationLevel.HIDDEN)
    final override fun createGUI(player: Player): Inventory = throw IllegalAccessException()

    /**
     * Called before running [AnvilGUI.onTyping] to check if the provided input is valid.
     * @return null if the string is valid, else item to display.
     */
    open fun catchInvalidInput(player: Player, inputString: String): ItemStack? = null
    internal fun isValidInput(player: Player, inputString: String): Boolean {
        return catchInvalidInput(player, inputString) == null
    }

    open fun onTyping(player: Player, inventory: AnvilInventory, input: Validator<String>): ItemStack? = null

    @Deprecated("", level = DeprecationLevel.HIDDEN)
    final override fun onClick(event: PluginGUIClick): Boolean {
        val anvilView = event.player.openInventory as AnvilView
        val renameText = anvilView.renameText ?: ""

        val s = CompletableFuture<Validator<String>>().completeAsync {
            Validator(renameText, {
                this.isValidInput(event.player, it)
            })
        }

        return onClick(event, s.join())
    }
    protected open fun onClick(event: PluginGUIClick, input: Validator<String>): Boolean {
        event.isCancelled = true
        return false
    }

    @Deprecated("Unused", level = DeprecationLevel.HIDDEN)
    final override fun designGUI(player: Player, inventory: Inventory) = Unit

    @Deprecated("", level = DeprecationLevel.HIDDEN)
    final override fun loadGUI(player: Player, inventory: Inventory) {
        val anvilView = player.openInventory as AnvilView
        this.loadGUI(player, inventory as AnvilInventory, Validator(anvilView.renameText ?: "", {
            this.isValidInput(player, it)
        }))
    }
    protected open fun loadGUI(player: Player, inventory: AnvilInventory, input: Validator<String>) {
        inventory.firstItem = defaultInputItem
    }

    @Deprecated("", level = DeprecationLevel.HIDDEN)
    final override fun loadLiveGUI(player: Player, inventory: Inventory) {
        try {
            val anvilView = player.openInventory as AnvilView
            this.loadLiveGUI(player, inventory as AnvilInventory, Validator(anvilView.renameText ?: "", {
                this.isValidInput(player, it)
            }))
        } catch (ignore: Exception) {}
    }
    protected open fun loadLiveGUI(player: Player, inventory: AnvilInventory, input: Validator<String>) {
    }

    @Deprecated("", level = DeprecationLevel.HIDDEN)
    final override fun onOpen(player: Player, inventory: Inventory): Boolean {
        return this.onOpen(player, inventory as AnvilInventory)
    }
    open fun onOpen(player: Player, inventory: AnvilInventory): Boolean {
        return false
    }

    @Deprecated("", level = DeprecationLevel.HIDDEN)
    final override fun onClose(player: Player, inventory: Inventory): Boolean {
        return this.onClose(player, inventory as AnvilInventory)
    }
    open fun onClose(player: Player, inventory: AnvilInventory): Boolean {
        inventory.clear()
        player.setItemOnCursor(null)
        return false
    }

    @Deprecated("", level = DeprecationLevel.HIDDEN)
    final override fun reload(player: Player, loadBackground: Boolean): Boolean {
        return this.reload(player)
    }
    open fun reload(player: Player): Boolean {
        if (player.getOpenGUI() != this)
            return false

        val view = player.openInventory
        if (view !is AnvilView) return false

        val inventory = view.topInventory

        this.loadGUI(player, inventory, Validator(view.renameText ?: "", {
            this.isValidInput(player, it)
        }))
        return true
    }

    @Deprecated("", level = DeprecationLevel.HIDDEN)
    final override fun reloadForAllViewers(loadBackground: Boolean) {
    }

    open fun reloadForAllViewers() {
        super.reloadForAllViewers(false)
    }

    protected open fun anvilView(player: Player, view: AnvilView, inventory: AnvilInventory) {
        view.title = legacyTitle.toLegacyColored()
        view.repairCost = 0
    }

    override fun open(player: Player) {
        if (player.getOpenGUI() == this) {
            this.reload(player)
            return
        }

        player.getOpenGUI()?.onClose(player, player.openInventory.topInventory)

        val view = player.openAnvil(null, true)
            ?: return

        openGUIs[player.uniqueId] = this

        val inventory = view.topInventory
        if (inventory !is AnvilInventory || view !is AnvilView)
            return

        this.anvilView(player, view, inventory)
        this.loadGUI(player, inventory, Validator(view.renameText ?: "", {
            this.isValidInput(player, it)
        }))

        this.onOpen(player, inventory)
        if (autoUpdateTickDelay > 0)
            this.liveInventory(player, inventory)
    }
}

/**
 * @since 1.6.0
 */
private class AnvilListener private constructor(): PaperListener() {

    @EventHandler
    private fun onTyping(event: PrepareAnvilEvent) {
        val inventory = event.inventory
        val player = event.viewers.firstOrNull() as? Player ?: return

        val gui = player.getOpenGUI() ?: return
        if (gui !is AnvilGUI) return

        inventory.result = null
        event.view.repairCost = 0

        val renameText = event.view.renameText?.trim() ?: ""

        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, Runnable {
            val invalidInput = gui.catchInvalidInput(player, renameText)

            if (invalidInput != null)
                inventory.result = invalidInput

            val result = gui.onTyping(player, inventory, Validator(renameText, invalidInput == null))

            if (result != null && invalidInput == null)
                inventory.result = result
        }, 1)
    }
}