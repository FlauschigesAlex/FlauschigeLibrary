@file:Suppress("unused")

package at.flauschigesalex.lib.minecraft.paper.base.utils

import io.papermc.paper.persistence.PersistentDataContainerView
import io.papermc.paper.persistence.PersistentDataViewHolder
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataHolder
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin

@Deprecated("Use consumer instead", ReplaceWith("persistentData(plugin, block)")) fun PersistentDataContainer.handler(plugin: Plugin): PersistentData = (this as PersistentDataContainerView).handler(plugin)
fun PersistentDataContainer.persistentData(plugin: Plugin, invocation: PersistentData.() -> Unit) = PersistentData(this, plugin).invocation()

@Deprecated("Use consumer instead", ReplaceWith("persistentData(plugin, block)")) fun PersistentDataViewHolder.handler(plugin: Plugin): PersistentData = this.persistentDataContainer.handler(plugin)
fun PersistentDataViewHolder.persistentData(plugin: Plugin, invocation: PersistentData.() -> Unit) = this.persistentDataContainer.persistentData(plugin, invocation)

@Deprecated("Use consumer instead", ReplaceWith("persistentData(plugin, block)")) fun PersistentDataHolder.handler(plugin: Plugin): PersistentData = this.persistentDataContainer.handler(plugin)
fun PersistentDataHolder.persistentData(plugin: Plugin, invocation: PersistentData.() -> Unit) = this.persistentDataContainer.persistentData(plugin, invocation)

@Deprecated("Use consumer instead", ReplaceWith("persistentData(plugin, block)")) fun PersistentDataContainerView.handler(plugin: Plugin): PersistentData = PersistentData(this, plugin)
fun PersistentDataContainerView.persistentData(plugin: Plugin, invocation: PersistentData.() -> Unit) = PersistentData(this, plugin).invocation()

class PersistentData(private val container: PersistentDataContainerView, private val plugin: Plugin) {
    constructor(holder: PersistentDataHolder, plugin: Plugin) : this(holder.persistentDataContainer, plugin)

    private fun namespace(path: String) = NamespacedKey(plugin, path)

    fun contains(path: String): Boolean = container.has(namespace(path))
    fun <T: Any> contains(path: String, type: PersistentDataType<*, T>): Boolean = container.has(namespace(path), type)

    fun <T: Any> get(path: String, type: PersistentDataType<*, T>): T? = container.get(namespace(path), type)
    fun <T: Any> getOrDefault(path: String, type: PersistentDataType<*, T>, default: T): T = this.get(path, type) ?: default

    fun get(path: String): String? = this.get(path, PersistentDataType.STRING)
    fun getOrDefault(path: String, default: String): String = this.getOrDefault(path, PersistentDataType.STRING, default)

    fun <T: Any> put(path: String, type: PersistentDataType<*, T>, value: T): T? {
        val container = this.container as? PersistentDataContainer ?: return null

        return get(path, type).also {
            container.set(namespace(path), type, value)
        }
    }
    fun <T: Any> putIfAbsent(path: String, type: PersistentDataType<*, T>, value: T): T? {
        val currentValue = get(path, type)
        if (currentValue != null)
            return currentValue

        return this.put(path, type, value)
    }

    operator fun set(path: String, value: String) = this.put(path, value)
    fun put(path: String, value: String): String? = this.put(path, PersistentDataType.STRING, value)
    fun putIfAbsent(path: String, value: String): String? = this.putIfAbsent(path, PersistentDataType.STRING, value)

    fun remove(path: String): Boolean {
        val container = this.container as? PersistentDataContainer ?: return false

        container.remove(namespace(path))
        return true
    }
    fun <T: Any> remove(path: String, type: PersistentDataType<*, T>): Boolean {
        if (!this.contains(path, type))
            return false

        return this.remove(path)
    }
    fun <T: Any> remove(path: String, type: PersistentDataType<*, T>, value: T): Boolean {
        val current = this.get(path, type) ?: return false
        if (current != value)
            return false

        return this.remove(path)
    }
}