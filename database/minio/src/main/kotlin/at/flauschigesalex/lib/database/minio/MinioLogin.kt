package at.flauschigesalex.lib.database.minio

import at.flauschigesalex.lib.base.file.DataManager
import at.flauschigesalex.lib.base.file.json.JsonManager
import at.flauschigesalex.lib.base.file.json.readJson
import at.flauschigesalex.lib.database.base.DatabaseLogin
import java.net.URI

@Suppress("unused")
class MinioLogin(
    host: URI,
    override val username: String,
    override val password: String
) : DatabaseLogin<URI>() {
    
    constructor(host: URI, username: String, password: String, port: Int) :
            this(host, username, password) { this.port = port }

    companion object {
        fun parse(handler: JsonManager) = runCatching { invoke(handler) }
        fun parseOrNull(handler: JsonManager) = this.parse(handler).getOrNull()
        fun parseOrThrow(handler: JsonManager) = this.parse(handler).getOrThrow()
        
        fun parse(handler: DataManager) = runCatching { invoke(handler) }
        fun parseOrNull(handler: DataManager) = this.parse(handler).getOrNull()
        fun parseOrThrow(handler: DataManager) = this.parse(handler).getOrThrow()
        
        private fun invoke(data: DataManager) = invoke(data.readJson()
            ?: throw IllegalArgumentException("Missing json in ${data.uri}"))
        private fun invoke(json: JsonManager): MinioLogin {
            
            val hostname = json.getString("hostname") ?: throw IllegalArgumentException("Missing hostname in json")
            val username = json.getString("username") ?: throw IllegalArgumentException("Missing username in json")
            val password = json.getString("password") ?: throw IllegalArgumentException("Missing password in json")
            val port = json.getInt("port") ?: -1

            return MinioLogin(URI(hostname), username, password, port)
        }
    }
    
    var port: Int = if (host.port == -1) 9000 else host.port
        private set

    override val host: URI = URI.create("http://${host.path}:${port}")
}