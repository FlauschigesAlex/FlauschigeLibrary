package at.flauschigesalex.lib.database.minio.bucket

import io.minio.BucketExistsArgs
import io.minio.GetObjectArgs
import io.minio.ListObjectsArgs
import io.minio.MakeBucketArgs
import io.minio.MinioClient
import io.minio.ObjectArgs
import io.minio.PutObjectArgs
import io.minio.RemoveBucketArgs
import io.minio.RemoveObjectArgs

@Suppress("unused")
data class MinioBucket(
    val client: MinioClient,
    val bucket: String
): Iterable<BucketEntry> {

    init {
        if (!client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build()))
            client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build())
    }

    override fun iterator(): Iterator<BucketEntry> = listAllEntries().iterator()

    private fun listAllEntries(recursive: Boolean = false): List<BucketEntry> {
        return client.listObjects(
            ListObjectsArgs.builder()
            .bucket(bucket)
            .recursive(recursive)
            .build()
        ).map { it.get() }.mapNotNull {
            val name = it.objectName()
            val data = get(name).getOrNull()
            BucketEntry(name, it, data)
        }
    }

    operator fun get(key: String): Result<ByteArray?> {
        val key = key.trim('/')

        val result = runCatching {
            client.getObject(
                GetObjectArgs.builder()
                    .bucket(bucket)
                    .withObject(key)
                    .build()
            ).use {
                return Result.success(it.readAllBytes())
            }
        }
        
        return Result.failure(result.exceptionOrNull()!!)
    }
    fun put(key: String, bytes: ByteArray) = runCatching {
        client.putObject(
            PutObjectArgs.builder()
                .bucket(bucket)
                .withObject(key)
                .stream(bytes.inputStream(), bytes.size.toLong(), /* partSize = */ 5 * 1024 * 1024)
                .contentType("application/octet-stream")
                .build()
        )
    }
    fun remove(key: String) = runCatching {
        client.removeObject(
            RemoveObjectArgs.builder()
                .bucket(bucket)
                .withObject(key)
                .build()
        )
    }
    fun deleteBucket(purge: Boolean): Result<Unit> {
        if (purge)
            this.purgeBucket().onFailure { 
                return Result.failure(it)
            }

        return runCatching {
            client.removeBucket(
                RemoveBucketArgs.builder()
                    .bucket(bucket)
                    .build()
            )
        }
    }
    
    fun purgeBucket() = runCatching {
        this.listAllEntries(true).forEach {
            client.removeObject(
                RemoveObjectArgs.builder()
                .bucket(bucket)
                .withObject(it.name)
                .build()
            )
        }
    }
}

fun <O: ObjectArgs.Builder<O, *>> O.withObject(name: String): O = apply { `object`(name) }