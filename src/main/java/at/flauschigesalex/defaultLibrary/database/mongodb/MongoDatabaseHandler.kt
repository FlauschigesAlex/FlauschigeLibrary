@file:Suppress("UNCHECKED_CAST")

package at.flauschigesalex.defaultLibrary.database.mongodb

import at.flauschigesalex.defaultLibrary.any.Reflector
import at.flauschigesalex.defaultLibrary.database.DatabaseHandler
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

@Suppress("unused")
class MongoDatabaseHandler(override val loginData: MongoLogin) : DatabaseHandler<MongoLogin, MongoDatabaseHandler>() {

    val registeredClasses = HashSet<Class<*>>()

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
            this.registeredClasses.addAll(Reflector.reflect().getSubTypes(MongoInformation::class.java))

            val displayClasses = HashSet<Class<*>>(this.registeredClasses.filter {
                    !it.interfaces.contains(MongoLibInfo::class.java) && it != MongoLibInfo::class.java
                }).sortedBy { it.name }

            if (displayClasses.isNotEmpty()) {

                val out = StringBuilder("\n ${displayClasses.size} class")
                    .append(if (displayClasses.size > 1) "es are" else " is")
                    .append(" now available in your Mongo-Database:")

                out.append(displayClasses.joinToString { "\n - ${it.name}" }).append("\n")
                println(out)
            }

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