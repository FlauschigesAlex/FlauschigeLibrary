@file:Suppress("unused")

package at.flauschigesalex.lib.base.file

import java.io.InputStream
import java.net.URL

@Deprecated("Renamed to ResourceManager", ReplaceWith("ResourceManager"), DeprecationLevel.ERROR)
typealias ResourceHandler = ResourceManager

@Suppress("MemberVisibilityCanBePrivate", "DEPRECATION")
class ResourceManager private constructor(val url: URL) : DataManager(url.toURI()) {

    companion object {
        operator fun invoke(url: URL) : ResourceManager? = runCatching {
            ResourceManager(url)
        }.getOrNull()

        operator fun invoke(urlString: String, classLoader: ClassLoader? = null) : ResourceManager? {
            val loader = classLoader ?: this::class.java.classLoader ?: return null
            val url = loader.getResource(urlString) ?: return null
            return this(url)
        }
    }

    @Deprecated("It's unlikely your action could not be performed with a bytearray.")
    override fun readStream(): InputStream? = runCatching {
        url.openStream()
    }.getOrNull()

    override val isReadable: Boolean
        get() = this.readStream() != null
    override val isWritable: Boolean = false
}