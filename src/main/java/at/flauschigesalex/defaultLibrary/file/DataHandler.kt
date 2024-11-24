package at.flauschigesalex.defaultLibrary.file

import java.io.InputStream

abstract class DataHandler protected constructor() {

    abstract fun readStream(): InputStream?
    open fun readString(): String? {
        val stream = this.readStream() ?: return null
        return StringBuilder().apply {
            stream.readAllBytes().forEach { this.append(it.toInt().toChar()) }
        }.toString()
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