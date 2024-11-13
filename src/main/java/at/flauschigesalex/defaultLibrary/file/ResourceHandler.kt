package at.flauschigesalex.defaultLibrary.file

import at.flauschigesalex.defaultLibrary.FlauschigeLibrary
import java.io.InputStream
import java.net.URL

class ResourceHandler private constructor(val url: URL) : DataHandler() {

    companion object {
        operator fun invoke(url: URL) : ResourceHandler? {
            try {
                return ResourceHandler(url)
            } catch (fail: Exception) { fail.printStackTrace() }
            return null
        }

        operator fun invoke(urlString: String) : ResourceHandler? {
            try {
                val url = FlauschigeLibrary::class.java.classLoader.getResource(urlString)!!
                return this(url)
            } catch (fail: Exception) { fail.printStackTrace() }
            return null
        }
    }

    override fun readStream(): InputStream? {
        return tryCatch { url.openStream() }
    }

    override fun isReadable(): Boolean {
        return this.readStream() != null
    }

    override fun isWritable(): Boolean {
        return false
    }
}