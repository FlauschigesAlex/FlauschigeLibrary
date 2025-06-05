package at.flauschigesalex.lib.database.minio

import at.flauschigesalex.lib.base.file.DataManager
import at.flauschigesalex.lib.base.file.JsonManager
import at.flauschigesalex.lib.database._internal.DatabaseLogin
import java.net.URI

class MinioLogin(
    host: URI,
    override val username: String,
    override val password: String
) : DatabaseLogin<URI>() {

    companion object {
        operator fun invoke(handler: DataManager): MinioLogin {
            val content = handler.readString()
            val json = content?.let { JsonManager(it) } ?: throw IllegalArgumentException("Failed to read json from ${handler.uri}: $content")
            val hostname = json.getString("hostname") ?: throw IllegalArgumentException("Missing hostname in json")
            val username = json.getString("username") ?: throw IllegalArgumentException("Missing username in json")
            val password = json.getString("password") ?: throw IllegalArgumentException("Missing password in json")
            val port = json.getInt("port") ?: -1

            return MinioLogin(URI(hostname), username, password, port)
        }
    }
    
    var port: Int = if (host.port == -1) 9000 else host.port
        private set
    override val host = URI("http://${host.path}:${port}")
    
    constructor(host: URI, username: String, password: String, port: Int) : this(host, username, password) {
        this.port = port
    }
}