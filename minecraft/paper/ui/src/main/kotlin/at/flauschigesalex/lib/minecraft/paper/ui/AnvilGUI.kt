@file:Suppress("DeprecatedCallableAddReplaceWith", "UnstableApiUsage", "unused", "MemberVisibilityCanBePrivate")

package at.flauschigesalex.lib.minecraft.paper.ui

import at.flauschigesalex.lib.minecraft.paper.base.internal.PaperListener
import at.flauschigesalex.lib.minecraft.paper.base.utils.PersistentData
import at.flauschigesalex.lib.minecraft.paper.ui.extensions.persistentData
import at.flauschigesalex.lib.minecraft.paper.ui.extensions.richName
import at.flauschigesalex.lib.minecraft.paper.ui.extensions.texture
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.MenuType
import org.bukkit.inventory.view.AnvilView
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import javax.naming.OperationNotSupportedException

private val anvilTypingControllers = HashMap<Player, UUID>()

/**
 * @since 1.6.0
 */

abstract class AnvilGUI(
    plugin: JavaPlugin,
) : PaperGUI(plugin, 3) {

    companion object {
        var inputItem: (Player, AnvilGUI) -> ItemStack = { player, gui ->
            ItemCreator(Material.BARRIER).item {
                this.isHideTooltip = true
                this.texture(Material.AIR)
                this.richName(" ")
                
                this.persistentData(gui.plugin) {
                    this["plugin_stack"] = "input"
                }
            }
        }
        
        var loadingItem: (Player, AnvilGUI) -> ItemStack = { player, gui ->
            ItemCreator(Material.GRAY_STAINED_GLASS_PANE).item {
                this.displayName(Component.translatable("narrator.loading", gui.titleConstructor(player)))
                this.persistentData(gui.plugin) {
                    this["plugin_stack"] = "loading"
                }
            }
        }
        
        var failureItem: Throwable.(Player, AnvilGUI) -> ItemStack = throwable@{ player, gui ->
            ItemCreator(Material.RED_STAINED_GLASS_PANE).item {
                this.richName("<red>${this@throwable}</red>")
                this.persistentData(gui.plugin) {
                    this["plugin_stack"] = "failure"
                }
            }
        }
    }
    
    override val titleConstructor: (Player) -> Component = { Component.text(this.javaClass.simpleName) }
    
    @Deprecated("", level = DeprecationLevel.HIDDEN)
    final override val size: Int = 3

    @Deprecated("", level = DeprecationLevel.HIDDEN)
    final override fun createGUI(player: Player): Inventory = throw OperationNotSupportedException()

    /**
     * Called when a player changes the text in the anvil.
     */
    open fun onTyping(player: Player, inventory: AnvilInventory, input: String): Result<ItemStack> = Result.failure(NotImplementedError())
    
    /**
     * Called when #onTyping returns an exception.
     */
    open fun onFailure(error: Throwable, player: Player, inventory: AnvilInventory): ItemStack = failureItem(error, player, this)

    /**
     * Called while the actual result of the typing is pending.
     */
    open fun onLoading(player: Player, inventory: AnvilInventory): ItemStack? = loadingItem(player, this)

    @Deprecated("", level = DeprecationLevel.HIDDEN)
    final override fun onClick(data: PaperGuiClickData): Boolean {
        val anvilView = data.player.openInventory as AnvilView
        val renameText = anvilView.renameText ?: ""
        
        val inventory = data.inventory as AnvilInventory
        
        val result = onTyping(data.player, inventory, renameText).map { renameText }
        val data = AnvilGUIClickData(
            data.player,
            this,
            inventory,
            data.clickedItem,
            data.clickedSlot,
            data.clickType,
            data.cursorItem,
            data.event,
            result
        )
        
        return onClick(data)
    }
    protected open fun onClick(data: AnvilGUIClickData): Boolean {
        data.isCancelled = true
        return false
    }

    @Deprecated("Unused", level = DeprecationLevel.HIDDEN)
    final override fun designGUI(player: Player, inventory: Inventory) = Unit

    @Deprecated("", level = DeprecationLevel.HIDDEN)
    final override fun loadGUI(player: Player, inventory: Inventory) {
        val anvilView = player.openInventory as AnvilView
        val anvilText = anvilView.renameText ?: ""
        
        this.loadGUI(player, inventory as AnvilInventory, Result.success(anvilText))
    }
    protected open fun loadGUI(player: Player, inventory: AnvilInventory, input: Result<String>) {
        inventory.firstItem = inputItem(player, this)
    }

    @Deprecated("", level = DeprecationLevel.HIDDEN)
    final override fun loadLiveGUI(player: Player, inventory: Inventory) = throw OperationNotSupportedException()

    @Deprecated("", level = DeprecationLevel.HIDDEN)
    final override fun onOpen(player: Player, inventory: Inventory): Boolean = this.onOpen(player, inventory as AnvilInventory)
    open fun onOpen(player: Player, inventory: AnvilInventory): Boolean {
        return false
    }

    @Deprecated("", level = DeprecationLevel.HIDDEN)
    final override fun onClose(player: Player, inventory: Inventory): Boolean = this.onClose(player, inventory as AnvilInventory)
    open fun onClose(player: Player, inventory: AnvilInventory): Boolean {
        inventory.clear()
        player.setItemOnCursor(null)
        return false
    }

    final override fun reload(player: Player, loadBackground: Boolean): Boolean {
        if (player.getOpenGUI() != this)
            return false

        val view = player.openInventory
        if (view !is AnvilView) return false

        val inventory = view.topInventory
        val anvilText = view.renameText ?: ""
        
        this.loadGUI(player, inventory, Result.success(anvilText))
        return true
    }

    @Deprecated("", level = DeprecationLevel.HIDDEN)
    final override fun reloadForAllViewers(loadBackground: Boolean) = reloadForAllViewers()
    open fun reloadForAllViewers() {
        super.reloadForAllViewers(false)
    }

    protected open fun anvilView(player: Player, view: AnvilView, inventory: AnvilInventory) {
        view.repairCost = 0
        view.bypassEnchantmentLevelRestriction(true)
    }

    override fun open(player: Player) {
        if (player.getOpenGUI() == this) {
            this.reload(player)
            return
        }

        player.getOpenGUI()?.onClose(player, player.openInventory.topInventory)

        val view = MenuType.ANVIL.create(player, titleConstructor(player))
        player.openInventory(view)

        openGUIs[player.uniqueId] = this

        val inventory = view.topInventory

        this.anvilView(player, view, inventory)
        val anvilText = view.renameText ?: ""
        
        this.loadGUI(player, inventory, Result.success(anvilText))
        this.onOpen(player, inventory)
    }
}

/**
 * @since 1.6.0
 */
@Suppress("DEPRECATION")
internal object AnvilListener : PaperListener() {

    @EventHandler
    private fun onTyping(event: PrepareAnvilEvent) {
        val inventory = event.inventory
        val player = event.view.player as? Player ?: return

        val gui = player.getOpenGUI() ?: return
        if (gui !is AnvilGUI) return

        inventory.result = null
        val view = event.view
        view.repairCost = 0
        view.bypassEnchantmentLevelRestriction(true)

        val renameText = event.view.renameText?.trim() ?: ""
        
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, Runnable {
            gui.onLoading(player, inventory)?.let { 
                inventory.result = it
            }
            
            val stack = gui.onTyping(player, inventory, renameText).getOrElse {
                AnvilGUI.failureItem(it, player, gui)
            }
            
            inventory.result = stack
            val cursor = view.cursor
            if (cursor.isEmpty.not() && cursor.itemMeta != null) {
                val pdc = PersistentData(cursor.itemMeta, plugin)
                if (pdc.contains("plugin_stack")) view.setCursor(null)
            }

            view.repairCost = 0
            view.bypassEnchantmentLevelRestriction(true)
        }, 1)
    }
}

class AnvilGUIClickData(
    player: Player,
    gui: AnvilGUI,
    inventory: AnvilInventory,
    clickedItem: ItemStack?,
    clickedSlot: Int,
    clickType: ClickType,
    cursorItem: ItemStack?,
    event: InventoryClickEvent,
    val result: Result<String>
) : PaperGuiClickData(player, gui, inventory, clickedItem, clickedSlot, clickType, cursorItem, event) {
    val isResultClicked = clickedSlot == 2
    val isFirstClicked = clickedSlot == 0
    val isSecondClicked = clickedSlot == 1
}