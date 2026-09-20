package at.flauschigesalex.lib.database.mongo

import at.flauschigesalex.lib.base.file.DataManager
import at.flauschigesalex.lib.base.file.json.JsonManager
import at.flauschigesalex.lib.base.file.json.deserializeOrThrow
import at.flauschigesalex.lib.base.file.json.readJson
import at.flauschigesalex.lib.database.base.DatabaseLogin
import at.flauschigesalex.lib.database.mongo.serializer.MongoLoginSerializer
import at.flauschigesalex.lib.database.mongo.serializer.ServerAddress
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Suppress("unused")
@Serializable(MongoLoginSerializer::class)
class MongoLogin internal constructor(
    val hosts: Set<ServerAddress>,
    override val username: String,
    override val password: String,
    val database: String
) : DatabaseLogin<ServerAddress>() {

    companion object {
        fun parse(handler: JsonManager) = runCatching { invoke(handler) }
        fun parseOrNull(handler: JsonManager) = this.parse(handler).getOrNull()
        fun parseOrThrow(handler: JsonManager) = this.parse(handler).getOrThrow()
        
        fun parse(handler: DataManager) = runCatching { invoke(handler) }
        fun parseOrNull(handler: DataManager) = this.parse(handler).getOrNull()
        fun parseOrThrow(handler: DataManager) = this.parse(handler).getOrThrow()
        
        private fun invoke(data: DataManager) = invoke(data.readJson()
            ?: throw IllegalArgumentException("Missing json in ${data.uri}"))

        private fun invoke(json: JsonManager): MongoLogin = json.deserializeOrThrow<MongoLogin>()

        operator fun invoke(
            host: ServerAddress,
            username: String,
            password: String,
            database: String,
            vararg moreHosts: ServerAddress
        ) : MongoLogin {
            val hosts = setOf(host) + moreHosts.toSet()
            return MongoLogin(hosts, username, password, database)
        }
    }
    
    init {
        require(hosts.isNotEmpty()) { "At least one host is required" }
    }

    @Transient
    @Deprecated("Deprecated by hosts", ReplaceWith("hosts"))
    override val host: ServerAddress = hosts.first()
}