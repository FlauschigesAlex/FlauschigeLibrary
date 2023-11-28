package at.flauschigesalex.lib.database.mongo

import at.flauschigesalex.lib.database._internal.DatabaseHandler
import at.flauschigesalex.lib.database._internal.RequireDatabaseClient
import com.mongodb.MongoClientSettings
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.bson.Document
import org.bson.UuidRepresentation
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.pojo.PojoCodecProvider

@Suppress("unused", "MemberVisibilityCanBePrivate")
class MongoDatabaseHandler(override val loginData: MongoLogin) : DatabaseHandler<MongoLogin, MongoDatabaseHandler>(),
    RequireDatabaseClient<MongoDatabaseHandler> {

    var client: MongoClient? = null
        private set
    var database: MongoDatabase? = null
        private set

    fun getCollection(name: String): MongoCollection<Document> {
        return database!!.getCollection(name)
    }
    fun <DocumentLike: Class<*>> getCollection(name: String, clazz: Class<DocumentLike>): MongoCollection<DocumentLike> {
        return database!!.getCollection(name, clazz)
    }

    override fun connect(): MongoDatabaseHandler {
        try {
            val addresses = loginData.allHosts as List<ServerAddress?>
            val credential = MongoCredential.createCredential(
                loginData.username,
                loginData.database,
                loginData.password.toCharArray()
            )

            val settings = MongoClientSettings.builder()
                .credential(credential)
                .applyToClusterSettings { it.hosts(addresses) }
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .build()

            val pojoBuilder = PojoCodecProvider.builder()
            val pojoRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(pojoBuilder.build())
            )

            client = MongoClients.create(settings)
            database = client!!.getDatabase(loginData.database).withCodecRegistry(pojoRegistry)

            return this
        } catch (fail: Exception) {
            throw RuntimeException(fail)
        }
    }

    override fun disconnect() {
        client?.close()
    }
}