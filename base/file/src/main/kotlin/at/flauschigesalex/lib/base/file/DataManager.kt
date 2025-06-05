package at.flauschigesalex.lib.base.file

import java.io.InputStream
import java.io.InputStreamReader
import java.net.URI
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

abstract class DataManager protected constructor(val uri: URI) {

    abstract fun readStream(): InputStream?
    open fun readString(charset: Charset = StandardCharsets.UTF_8): String? {
        val stream = this.readStream() ?: return null
        return InputStreamReader(stream, charset).readText()
    }

    abstract val isReadable: Boolean
    abstract val isWritable: Boolean

    override fun toString(): String {
        return this.readString() ?: super.toString()
    }
}