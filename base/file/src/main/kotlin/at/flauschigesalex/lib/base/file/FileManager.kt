@file:Suppress("unused")

package at.flauschigesalex.lib.base.file

import java.io.File
import java.io.InputStream

@Deprecated("Renamed to FileManager", ReplaceWith("FileManager"), DeprecationLevel.ERROR)
typealias FileHandler = FileManager

@Suppress("MemberVisibilityCanBePrivate")
class FileManager(val file: File) : DataManager(file.toURI()) {

    constructor(path: String) : this(File(path))
    constructor(parent: File?, path: String) : this(File(parent, path))
    constructor(parent: FileManager?, path: String) : this(parent?.file, path)
    
    val originalContent: String? = this.readString()

    @Deprecated("Deprecated", level = DeprecationLevel.ERROR)
    fun createJsonFile(): File? = createFile()
    
    fun createFile(): File? {
        if (file.exists())
            return file

        val parent = file.parentFile
        if (parent != null && !file.parentFile.mkdirs() && !parent.exists())
            return null

        try {
            file.createNewFile()
            return file
        } catch (_: Exception) {
            return null
        }
    }
    fun createDirectory(): File? {
        if (file.exists() && file.isDirectory)
            return file

        try {
            file.mkdirs()
            return file
        } catch (_: Exception) {
            return null
        }
    }

    override fun readStream(): InputStream? {
        return try {
            file.inputStream()
        } catch (_: Exception) {
            null
        }
    }

    fun write(obj: Any): Boolean {
        return write(obj.toString().toByteArray())
    }
    fun write(stream: InputStream): Boolean {
        return this.write(stream.readAllBytes())
    }
    fun write(bytes: ByteArray): Boolean {
        if (!this.isWritable)
            return false

        runCatching { 
            file.writeBytes(bytes)
            return true
        }
        return false
    }

    private fun delete(file: File): Boolean? {
        if (!file.exists())
            return null

        return file.delete() || file.deleteRecursively()
    }

    fun delete(): Boolean? {
        return this.delete(file)
    }
    
    fun move(newFile: File): Boolean {
        return file.renameTo(newFile)
    }
    fun copy(newFile: File): File {
        return file.copyTo(newFile, overwrite = true)
    }

    val exists: Boolean
        get() = file.exists()
    override val isReadable: Boolean
        get() = exists && file.isFile && file.canRead()
    override val isWritable: Boolean
        get() = isReadable && file.canWrite()
    
    val listFiles: List<FileManager> 
        get() = file.listFiles.map { FileManager(it) }

    override fun toString(): String {
        return file.toString()
    }
}

val File.listFiles: List<File>
    get() = this.listFiles()?.toList() ?: emptyList()
val File.list: List<String>
    get() = this.list()?.toList() ?: emptyList()