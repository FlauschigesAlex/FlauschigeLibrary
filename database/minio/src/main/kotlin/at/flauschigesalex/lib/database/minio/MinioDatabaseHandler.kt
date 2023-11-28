@file:Suppress("unused")

package at.flauschigesalex.lib.database.minio

import at.flauschigesalex.lib.database._internal.DatabaseHandler
import at.flauschigesalex.lib.database._internal.RequireDatabaseClient
import kotlin.Result
import io.minio.*
import io.minio.credentials.StaticProvider
import io.minio.messages.Item

class MinioDatabaseHandler(override val loginData: MinioLogin) : DatabaseHandler<MinioLogin, MinioDatabaseHandler>(),
    RequireDatabaseClient<MinioDatabaseHandler> {

    private val provider = StaticProvider(loginData.username, loginData.password, null)
    var client : MinioClient? = null
        private set

    override fun connect(): MinioDatabaseHandler {
        client = MinioClient.builder().endpoint(loginData.host.toString()).credentialsProvider(provider).build()
        return this
    }
    override fun disconnect() {
        client?.close()
    }

    fun getBucket(name: String): MinioBucket = MinioBucket(client!!, name)
}

typealias BucketEntry = Triple<String, Item, ByteArray?>
fun BucketEntry.isDirectory(): Boolean = second.isDir

data class MinioBucket(val client: MinioClient, val bucket: String): Iterable<BucketEntry> {

    init {
        if (!client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build()))
            client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build())
    }

    override fun iterator(): Iterator<BucketEntry> = listAllEntries().iterator()

    private fun listAllEntries(recursive: Boolean = false): List<BucketEntry> {
        return client.listObjects(ListObjectsArgs.builder()
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
                    .`object`(key)
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
                .`object`(key)
                .stream(bytes.inputStream(), bytes.size.toLong(), /* partSize = */ 5 * 1024 * 1024)
                .contentType("application/octet-stream")
                .build()
        )
    }
    fun remove(key: String) = runCatching {
        client.removeObject(
            RemoveObjectArgs.builder()
                .bucket(bucket)
                .`object`(key)
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
            client.removeObject(RemoveObjectArgs.builder()
                .bucket(bucket)
                .`object`(it.first)
                .build()
            )
        }
    }
}