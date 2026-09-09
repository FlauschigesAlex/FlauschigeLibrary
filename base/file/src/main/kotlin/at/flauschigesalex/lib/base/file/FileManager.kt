@file:Suppress("unused")

package at.flauschigesalex.lib.base.file

import java.io.File
import java.io.InputStream

@Suppress("MemberVisibilityCanBePrivate")
class FileManager(val file: File) : DataManager(file.toURI()) {

    constructor(path: String) : this(File(path))
    constructor(parent: File?, path: String) : this(File(parent, path))
    constructor(parent: FileManager?, path: String) : this(parent?.file, path)
    
    fun createFile(): File? = runCatching {
        if (file.exists())
            return@runCatching file.takeIf { it.isFile }

        val parent = file.parentFile
        if (parent != null && !parent.isDirectory && !parent.mkdirs() && !parent.isDirectory)
            return@runCatching null

        if (!file.createNewFile() && !file.isFile)
            return@runCatching null

        file.takeIf { it.isFile }
    }.getOrNull()
    fun createDirectory(): File? = runCatching {
        if (!file.isDirectory && !file.mkdirs() && !file.isDirectory)
            return@runCatching null

        file.takeIf { it.isDirectory }
    }.getOrNull()

    override fun readStream(): InputStream? = runCatching {
        file.inputStream()
    }.getOrNull()

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
    fun copy(newFile: File): File? {
        val success = file.copyRecursively(newFile, overwrite = true)
        return if (success) newFile else null
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