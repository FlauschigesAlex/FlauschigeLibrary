package at.flauschigesalex.defaultLibrary.file

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

@Suppress("unused", "MemberVisibilityCanBePrivate")
class FileHandler(val file: File) : DataHandler() {

    constructor(filePath: String) : this(File(filePath))

    fun createJsonFile(): File? {
        return this.createFile()?.apply {
            write("{}")
        }
    }
    fun createFile(): File? {
        if (file.exists())
            return file

        return tryCatch({
            if (file.createNewFile()) file else null
        })
    }
    fun createDirectory(): File? {
        if (file.exists() && file.isDirectory)
            return file

        return tryCatch({
            if (file.mkdir()) file else null
        })
    }

    override fun readStream(): InputStream? {
        return tryCatch({
            file.inputStream()
        })
    }

    fun write(obj: Any): Boolean {
        return write(obj.toString().toByteArray())
    }
    fun write(stream: InputStream): Boolean {
        return this.write(stream.readAllBytes())
    }
    fun write(bytes: ByteArray): Boolean {
        if (!this.isWritable())
            return false

        return tryCatch({
            FileOutputStream(file).apply {
                this.write(bytes)
                this.close()
            }
        }) != null
    }

    private fun delete(file: File, deep: Boolean = true): Boolean {
        if (!file.exists())
            return true

        if (file.isDirectory && deep) {
            val files = file.listFiles()
            if (files != null)
                for (dirFile in files)
                    if (!delete(dirFile))
                        return false
        }

        return file.delete()
    }

    fun delete(deep: Boolean = true): Boolean {
        return this.delete(file, deep)
    }

    override fun isReadable(): Boolean {
        return file.exists() && file.isFile && file.canRead()
    }

    override fun isWritable(): Boolean {
        return this.isReadable() && file.canWrite()
    }

    override fun toString(): String {
        return file.toString()
    }
}