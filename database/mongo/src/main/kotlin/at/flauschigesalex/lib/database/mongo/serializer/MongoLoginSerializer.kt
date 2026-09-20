package at.flauschigesalex.lib.database.mongo.serializer

import at.flauschigesalex.lib.base.file.json.JsonManager
import at.flauschigesalex.lib.database.mongo.MongoLogin
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json

internal object MongoLoginSerializer : KSerializer<MongoLogin> {
    private val jsonSerializer = JsonManager.serializer()
    
    override val descriptor: SerialDescriptor = jsonSerializer.descriptor

    override fun serialize(encoder: Encoder, value: MongoLogin) {
        val json = JsonManager(
            "hostname" to value.hosts,
            "username" to value.username,
            "password" to value.password,
            "database" to value.database,
        )
        
        encoder.encodeSerializableValue(jsonSerializer, json)
    }

    override fun deserialize(decoder: Decoder): MongoLogin {
        val json = jsonSerializer.deserialize(decoder)
        
        var hostsRaw = json.getStringList("hostname").toMutableList().takeIf { it.isNotEmpty() }
            ?: json.getString("hostname")?.let { mutableListOf(it) }!!
        
        val ports = json.getInt("port")?.let { mutableListOf(it) }
            ?: json.getIntList("port").toMutableList().takeIf { it.isNotEmpty() }
        
        if (ports != null) {
            require(hostsRaw.size == ports.size) { "Hosts and ports must have the same size" }
            
            System.err.println("Field 'port' is deprecated, use 'hostname:port' formatting instead.")
            hostsRaw = hostsRaw.zip(ports).map { "${it.first}:${it.second}" }.toMutableList()
        }
        
        val username = json.getString("username")!!
        val password = json.getString("password") ?: json.getString("accessToken")!!
        val database = json.getString("database") ?: "admin"
        
        val hosts = hostsRaw.map(::ServerAddress).toSet()

        return MongoLogin(hosts, username, password, database)
    }

}