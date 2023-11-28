@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package at.flauschigesalex.lib.minecraft.paper.ui

import at.flauschigesalex.lib.minecraft.paper.base.FlauschigeLibraryPaper
import at.flauschigesalex.lib.minecraft.paper.base.internal.PaperListener
import at.flauschigesalex.lib.minecraft.paper.ui.PluginGUI.Companion.getOpenGUI
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
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
import org.bukkit.scheduler.BukkitTask
import org.jetbrains.annotations.Range
import java.util.*
import kotlin.math.max

operator fun Inventory.get(index: Int): ItemStack? {
    return this.getItem(index)
}
operator fun Inventory.set(index: Int, item: ItemStack?) {
    this.setItem(index, item)
}
operator fun Inventory.set(index: IntRange, item: ItemStack?) {
    index.forEach { this[it] = item }
}

/**
 * @since v1.5.0
 */
abstract class PluginGUI protected constructor(
    val plugin: JavaPlugin,
    open val size: Int,
    protected val titleConstructor: (Player) -> Component,
    protected val autoUpdateTickDelay: @Range(from = 1, to = Long.MAX_VALUE) Int = 0,
) {

    constructor(
        plugin: JavaPlugin,
        size: Int,
        title: Component = Component.text(" "),
        autoUpdateTickDelay: @Range(from = 1, to = Long.MAX_VALUE) Int = 0,
    ) : this(plugin, size, { _: Player -> title}, autoUpdateTickDelay)

    constructor(
        plugin: JavaPlugin,
        size: Int,
        richTitle: String = " ",
        autoUpdateTickDelay: @Range(from = 1, to = Long.MAX_VALUE) Int = 0,
    ) : this(plugin, size, { _: Player -> MiniMessage.miniMessage().deserialize(richTitle)}, autoUpdateTickDelay)

    companion object {

        /*
        init {

            Reflector.reflect(PluginGUI::class.java.packageName).getSubTypes(PaperListener::class.java)
                .filter { !it.isInterface && !Modifier.isAbstract(it.modifiers) }
                .forEach {
                    val constructor = it.getDeclaredConstructor()
                    constructor.isAccessible = true
                    val instance = constructor.newInstance()
                    instance.register(FlauschigeLibraryPaper.javaPlugin)
                }
        }
        */

        internal val openGUIs = hashMapOf<UUID, PluginGUI>()

        @JvmStatic
        fun HumanEntity.getOpenGUI(): PluginGUI? {
            return openGUIs[this.uniqueId]
        }

        val controllers = HashSet<BukkitTask>()
    }

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

    open fun onClick(event: PluginGUIClick): Boolean = false
    open fun onOpen(player: Player, inventory: Inventory): Boolean = false
    open fun onClose(player: Player, inventory: Inventory): Boolean = false

    protected fun liveInventory(player: Player, inventory: Inventory) {
        if (autoUpdateTickDelay <= 0)
            return

        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, { it ->
            it?.run { controllers.add(this) }

            if (!player.isOnline || player.getOpenGUI() != this) {
                it?.run {
                    controllers.remove(it)
                    it.cancel()
                }

                openGUIs.remove(player.uniqueId, this)
                return@runTaskTimerAsynchronously
            }

            runCatching {
                this.loadLiveGUI(player, inventory)
            }.onFailure {
                it.printStackTrace()
                player.sendRichMessage("<red>${FlauschigeLibraryPaper.displayExceptionMessage.format(this::class.simpleName, it.javaClass.simpleName)}")
                player.closeInventory()
            }
        }, 0, max(autoUpdateTickDelay, 1).toLong())
    }

    open fun reloadForAllViewers(loadBackground: Boolean = false) {
        viewers.forEach { reload(it, loadBackground) }
    }
    open fun reload(player: Player, loadBackground: Boolean = false): Boolean {
        if (player.getOpenGUI() != this)
            return false

        val gui = player.openInventory.topInventory

        if (loadBackground)
            runCatching {
                this.designGUI(player, gui)
            }.onFailure {
                it.printStackTrace()
                player.sendRichMessage("<red>${FlauschigeLibraryPaper.displayExceptionMessage.format(this::class.simpleName, it.javaClass.simpleName)}")
                player.closeInventory()
            }

        runCatching {
            this.loadGUI(player, gui)
        }.onFailure {
            it.printStackTrace()
            player.sendRichMessage("<red>${FlauschigeLibraryPaper.displayExceptionMessage.format(this::class.simpleName, it.javaClass.simpleName)}")
            player.closeInventory()
        }
        return true
    }

    open fun open(player: Player) {
        if (player.getOpenGUI() == this) {
            runCatching {
                this.reload(player, true)
            }.onFailure {
                it.printStackTrace()
                player.sendRichMessage("<red>${FlauschigeLibraryPaper.displayExceptionMessage.format(this::class.simpleName, it.javaClass.simpleName)}")
                player.closeInventory()
            }
            return
        }

        player.getOpenGUI()?.onClose(player, player.openInventory.topInventory)

        val inventory = runCatching {
            this.createGUI(player)
        }.onFailure {
            it.printStackTrace()
            player.sendRichMessage("<red>${FlauschigeLibraryPaper.displayExceptionMessage.format(this::class.simpleName, it.javaClass.simpleName)}")
        }.getOrNull() ?: return
        openGUIs[player.uniqueId] = this
        
        runCatching {
            this.designGUI(player, inventory)
        }.onFailure {
            it.printStackTrace()
            player.sendRichMessage("<red>${FlauschigeLibraryPaper.displayExceptionMessage.format(this::class.simpleName, it.javaClass.simpleName)}")
            return
        }

        runCatching {
            this.loadGUI(player, inventory)
        }.onFailure {
            it.printStackTrace()
            player.sendRichMessage("<red>${FlauschigeLibraryPaper.displayExceptionMessage.format(this::class.simpleName, it.javaClass.simpleName)}")
            return
        }

        player.openInventory(inventory)

        runCatching {
            this.onOpen(player, inventory)
        }.onFailure {
            it.printStackTrace()
            player.sendRichMessage("<red>${FlauschigeLibraryPaper.displayExceptionMessage.format(this::class.simpleName, it.javaClass.simpleName)}")
            return player.closeInventory()
        }
        
        if (autoUpdateTickDelay > 0)
            runCatching {
                this.liveInventory(player, inventory)
            }.onFailure {
                it.printStackTrace()
                player.sendRichMessage("<red>${FlauschigeLibraryPaper.displayExceptionMessage.format(this::class.simpleName, it.javaClass.simpleName)}")
                return player.closeInventory()
            }
    }

    @OptIn(ExperimentalStdlibApi::class)
    val isAnvilGUI
        get() = this is AnvilGUI

    val viewers: List<Player> get() {
        return Bukkit.getOnlinePlayers().filter {
            it.getOpenGUI() == this
        }
    }
}

private class PaperGUIListener private constructor(): PaperListener() {

    @EventHandler
    private fun inventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as Player
        val gui = player.getOpenGUI() ?: return
        
        val clickedInventory = event.clickedInventory ?: return
        if (clickedInventory != event.view.topInventory)
            return

        runCatching {
            gui.onClick(
                PluginGUIClick(
                    player,
                    gui,
                    clickedInventory,
                    event.currentItem,
                    event.slot,
                    event.click,
                    event.cursor.let {
                        if (it.type.isAir)
                            return@let null

                        return@let it
                    }, event)
            )
        }.onFailure {
            it.printStackTrace()
            player.sendRichMessage("<red>${FlauschigeLibraryPaper.displayExceptionMessage.format(this::class.simpleName, it.javaClass.simpleName)}")
            player.closeInventory()
        }
    }

    @Suppress("DEPRECATION")
    @EventHandler
    private fun inventoryClose(event: InventoryCloseEvent) {
        val player = event.player as Player
        val gui = player.getOpenGUI() ?: return

        val inventory = player.openInventory.topInventory
        inventory.clear()

        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            val newInventory = player.openInventory.topInventory

            if (inventory.location == newInventory.location && newInventory.location == null)
                return@Runnable

            PluginGUI.openGUIs.remove(player.uniqueId, gui)
            runCatching {
                gui.onClose(player, inventory)
            }.onFailure {
                it.printStackTrace()
                player.sendRichMessage("<red>${FlauschigeLibraryPaper.displayExceptionMessage.format(this::class.simpleName, it.javaClass.simpleName)}")
            }
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
        PluginGUI.openGUIs.remove(player.uniqueId, gui)
    }
}

data class PluginGUIClick(val player: Player,
                          val gui: PluginGUI,
                          val inventory: Inventory,
                          val clickedItem: ItemStack?,
                          val clickedSlot: Int,
                          val clickType: ClickType,
                          val cursorItem: ItemStack?,
                          val source: InventoryClickEvent,
) {
    var isCancelled: Boolean
        get() = source.isCancelled
        set(value) { source.isCancelled = value }

    override fun toString(): String {
        return listOf(player, gui, inventory, clickedItem, clickedSlot, clickType, cursorItem, source).toString()
    }
}