@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package at.flauschigesalex.lib.minecraft.paper.ui

import at.flauschigesalex.lib.minecraft.paper.base.FlauschigeLibraryPaper
import at.flauschigesalex.lib.minecraft.paper.base.internal.PaperListener
import at.flauschigesalex.lib.minecraft.paper.ui.PaperGUI.Companion.openGUIs
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

operator fun Inventory.get(index: Int): ItemStack? {
    return this.getItem(index)
}
operator fun Inventory.set(index: Int, item: ItemStack?) {
    this.setItem(index, item)
}
operator fun Inventory.set(index: IntRange, item: ItemStack?) {
    index.forEach { this[it] = item }
}

fun HumanEntity.getOpenGUI(): PaperGUI? {
    return openGUIs[this.uniqueId]
}

@Deprecated("Legacy name", ReplaceWith("PaperGUI")) typealias PluginGUI = PaperGUI

/**
 * @since v1.5.0
 */
abstract class PaperGUI protected constructor(
    val plugin: JavaPlugin,
    open val size: Int
) {
    companion object {
        internal val openGUIs = hashMapOf<UUID, PaperGUI>()

        init {
            PaperGUIListener.register(FlauschigeLibraryPaper.activeData.first().plugin)
        }
    }
    
     abstract val titleConstructor: (Player) -> Component

    protected open fun createGUI(player: Player): Inventory {
        return Bukkit.createInventory(player, size, titleConstructor.invoke(player))
    }
    protected open fun designGUI(player: Player, inventory: Inventory) {
        val black = ItemCreator(Material.BLACK_STAINED_GLASS_PANE).item {
            this.isHideTooltip = true
        }
        val gray = ItemCreator(Material.GRAY_STAINED_GLASS_PANE).item {
            this.isHideTooltip = true
        }

        for (slot in 0 until inventory.size) {
            if (slot < 9 || slot > inventory.size -10) inventory[slot] = black
            else inventory[slot] = gray
        }
    }
    protected open fun loadGUI(player: Player, inventory: Inventory) {}
    protected open fun loadLiveGUI(player: Player, inventory: Inventory) {
        return loadGUI(player, inventory)
    }

    open fun onClick(data: PaperGuiClickData): Boolean = false
    open fun onOpen(player: Player, inventory: Inventory): Boolean = false
    open fun onClose(player: Player, inventory: Inventory): Boolean = false

    open fun reloadForAllViewers(loadBackground: Boolean = false) {
        viewers.forEach { reload(it, loadBackground) }
    }
    open fun reload(player: Player, loadBackground: Boolean = false): Boolean {
        if (player.getOpenGUI() != this)
            return false

        val gui = player.openInventory.topInventory
        if (!this.isAnvilGUI && gui.size != this.size) {
            openGUIs.remove(player.uniqueId, this)
            return false
        }

        runCatching {
            if (loadBackground)
                this.designGUI(player, gui)
            
            this.loadGUI(player, gui)
        }.onFailure {
            it.printStackTrace()
            player.closeInventory()
        }
        
        return true
    }

    open fun open(player: Player) {
        if (player.getOpenGUI() == this) {
            this.reload(player)
            return
        }

        player.getOpenGUI()?.onClose(player, player.openInventory.topInventory)
        openGUIs[player.uniqueId] = this
        
        runCatching {
            val inventory = this.createGUI(player)
            this.designGUI(player, inventory)
            this.loadGUI(player, inventory)
            
            player.openInventory(inventory)
            this.onOpen(player, inventory)
        }.onFailure { 
            openGUIs.remove(player.uniqueId, this)
            player.closeInventory()
            it.printStackTrace()
        }
    }

    val isAnvilGUI
        get() = this is AnvilGUI

    val viewers: List<Player> get() {
        return Bukkit.getOnlinePlayers().filter {
            it.getOpenGUI() == this
        }
    }
}

internal object PaperGUIListener : PaperListener() {

    @EventHandler
    private fun inventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as Player
        val gui = player.getOpenGUI() ?: return
        
        val clickedInventory = event.clickedInventory ?: return
        if (clickedInventory != event.view.topInventory)
            return

        runCatching {
            gui.onClick(PaperGuiClickData(
                    player,
                    gui,
                    clickedInventory,
                    event.currentItem,
                    event.slot,
                    event.click,
                    event.cursor.let { if (it.type.isAir) null else it }, event))
        }.onFailure {
            it.printStackTrace()
            player.closeInventory()
        }
    }

    @Suppress("DEPRECATION")
    @EventHandler
    private fun inventoryClose(event: InventoryCloseEvent) {
        val player = event.player as Player
        val gui = player.getOpenGUI() ?: return

        val inventory = event.inventory

        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            if (player.getOpenGUI() != gui)
                return@Runnable

            openGUIs.remove(player.uniqueId, gui)
            runCatching {
                gui.onClose(player, inventory)
            }.onFailure {
                it.printStackTrace()
            }

            inventory.clear()
        }, 1)
    }

    @EventHandler
    private fun onQuit(event: PlayerQuitEvent) {
        val player = event.player
        val gui = player.getOpenGUI() ?: return

        val inventory = player.openInventory.topInventory

        runCatching {
            gui.onClose(player, inventory)
        }.onFailure {
            it.printStackTrace()
        }

        inventory.clear()
        openGUIs.remove(player.uniqueId, gui)
    }
}

@Deprecated("Legacy name", ReplaceWith("PaperGuiClickData")) typealias PluginGUIClick = PaperGuiClickData 
data class PaperGuiClickData(val player: Player,
                             val gui: PaperGUI,
                             val inventory: Inventory,
                             val clickedItem: ItemStack?,
                             val clickedSlot: Int,
                             val clickType: ClickType,
                             val cursorItem: ItemStack?,
                             val event: InventoryClickEvent,
) {
    var isCancelled: Boolean
        get() = event.isCancelled
        set(value) { event.isCancelled = value }
    
    @Deprecated("Legacy name", ReplaceWith("event"))
    val source: InventoryClickEvent = this.event
}