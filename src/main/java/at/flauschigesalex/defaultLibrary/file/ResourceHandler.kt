package at.flauschigesalex.defaultLibrary.file

import at.flauschigesalex.defaultLibrary.FlauschigeLibrary
import java.io.InputStream
import java.net.URL

class ResourceHandler(val url: URL) : DataHandler() {

    constructor(urlString: String) : this(
        FlauschigeLibrary::class.java.classLoader.getResource(urlString)!!
    )

    override fun readStream(): InputStream? {
        return tryCatch({
            url.openStream()
        })
    }

    override fun isReadable(): Boolean {
        return this.readStream() != null
    }

    override fun isWritable(): Boolean {
        return false
    }
}