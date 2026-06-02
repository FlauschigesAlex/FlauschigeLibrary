package at.flauschigesalex.lib.database.minio.bucket

import io.minio.messages.Item

@Suppress("unused")
data class BucketEntry(
    val name: String,
    val item: Item,
    val data: ByteArray?,
) {
    val isDirectory: Boolean = item.isDir
    val isFile: Boolean = this.isDirectory.not()
    
    val hasData: Boolean = data != null
    
    override fun equals(other: Any?): Boolean = other is BucketEntry && item == other.item
    override fun hashCode(): Int = item.hashCode()
}
