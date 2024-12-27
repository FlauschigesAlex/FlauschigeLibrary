package at.flauschigesalex.defaultLibrary.file

import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

abstract class DataHandler protected constructor() {

    abstract fun readStream(): InputStream?
    open fun readString(charset: Charset = StandardCharsets.UTF_8): String? {
        val stream = this.readStream() ?: return null
        return InputStreamReader(stream, charset).readText()
    }

    abstract fun isReadable(): Boolean
    abstract fun isWritable(): Boolean

    protected fun <A> tryCatch(displayException: Boolean = true, func: () -> A?): A? {
        try {
            return func.invoke()
        } catch (fail: Exception) {
            if (displayException) fail.printStackTrace()
        }
        return null
    }

    override fun toString(): String {
        return this.readString() ?: super.toString()
    }
}