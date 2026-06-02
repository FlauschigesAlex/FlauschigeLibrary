@file:Suppress("unused")

package at.flauschigesalex.lib.database.minio

import at.flauschigesalex.lib.database.base.DatabaseHandler
import at.flauschigesalex.lib.database.base.RequireDatabaseClient
import at.flauschigesalex.lib.database.minio.bucket.MinioBucket
import io.minio.MinioClient
import io.minio.credentials.StaticProvider

class MinioDatabaseHandler(override val loginData: MinioLogin) :
    DatabaseHandler<MinioLogin, MinioDatabaseHandler>(),
    RequireDatabaseClient<MinioDatabaseHandler> 
{

    private val provider = StaticProvider(loginData.username, loginData.password, null)
    var client : MinioClient? = null
        private set
    
    init {
        this.connect().onFailure { it.printStackTrace() }
    }

    override fun connect() = runCatching {
        if (client != null) return@runCatching this
        
        client = MinioClient.builder()
            .endpoint(loginData.host.toString())
            .credentialsProvider(provider)
            .build()
        
        return@runCatching this
    }
    override fun disconnect() {
        client?.close()
    }

    fun getBucket(name: String): Result<MinioBucket> = runCatching {
        client?.let { MinioBucket(it, name) } ?: throw IllegalStateException("Minio client not connected.")
    }
    fun getBucketOrNull(name: String): MinioBucket? = this.getBucket(name).getOrNull()
    fun getBucketOrThrow(name: String): MinioBucket = this.getBucket(name).getOrThrow()
}
