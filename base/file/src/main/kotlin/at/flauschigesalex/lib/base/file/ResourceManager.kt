@file:Suppress("unused")

package at.flauschigesalex.lib.base.file

import java.io.InputStream
import java.net.URL

@Deprecated("Renamed to ResourceManager", ReplaceWith("ResourceManager"), DeprecationLevel.ERROR)
typealias ResourceHandler = ResourceManager

@Suppress("MemberVisibilityCanBePrivate")
class ResourceManager private constructor(val url: URL) : DataManager(url.toURI()) {

    companion object {
        operator fun invoke(url: URL) : ResourceManager? {
            try {
                return ResourceManager(url)
            } catch (_: Exception) {}
            return null
        }

        operator fun invoke(urlString: String) : ResourceManager? {
            val url = this::class.java.classLoader.getResource(urlString) ?: return null
            return this(url)
        }
    }

    override fun readStream(): InputStream? {
        return try {
            url.openStream()
        } catch (_: Exception) {
            null
        }
    }

    override val isReadable: Boolean
        get() = this.readStream() != null
    override val isWritable: Boolean = false
}