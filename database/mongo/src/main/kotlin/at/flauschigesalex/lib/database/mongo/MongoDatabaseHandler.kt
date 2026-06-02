package at.flauschigesalex.lib.database.mongo

import at.flauschigesalex.lib.database.base.DatabaseHandler
import at.flauschigesalex.lib.database.base.RequireDatabaseClient
import com.mongodb.MongoClientSettings
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.serializer
import kotlinx.serialization.serializerOrNull
import org.bson.BsonDocument
import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.Document
import org.bson.UuidRepresentation
import org.bson.codecs.BsonValueCodec
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecProvider
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import org.bson.codecs.pojo.PojoCodecProvider

@Suppress("unused", "MemberVisibilityCanBePrivate")
class MongoDatabaseHandler(override val loginData: MongoLogin) :
    DatabaseHandler<MongoLogin, MongoDatabaseHandler>(),
    RequireDatabaseClient<MongoDatabaseHandler>
{

    var client: MongoClient? = null
        private set
    var database: MongoDatabase? = null
        private set
    
    @JvmName("getCollectionTyped")
    inline fun <reified T: Any> getCollection(name: String): Result<MongoCollection<T>> = runCatching {
        val database = this.database ?: throw IllegalStateException("MongoDB client not connected.")
        val registries = CodecRegistries.fromRegistries(
            CodecRegistries.fromCodecs(T::class.java.getMongoCodec()),
            database.codecRegistry
        )
        return@runCatching database.getCollection(name, T::class.java).withCodecRegistry(registries)
    }
    @JvmName("getCollectionOrNullTyped")
    inline fun <reified T: Any> getCollectionOrNull(name: String) = this.getCollection<T>(name).getOrNull()
    @JvmName("getCollectionOrThrowTyped")
    inline fun <reified T: Any> getCollectionOrThrow(name: String) = this.getCollection<T>(name).getOrThrow()
    
    fun getCollection(name: String): Result<MongoCollection<Document>> = runCatching {
        val database = this.database ?: throw IllegalStateException("MongoDB client not connected.")
        return@runCatching database.getCollection(name)
    }
    fun getCollectionOrNull(name: String) = this.getCollection(name).getOrNull()
    fun getCollectionOrThrow(name: String) = this.getCollection(name).getOrThrow()
    
    init {
        this.connect().onFailure { it.printStackTrace() }
    }

    override fun connect() = runCatching {
        val addresses = loginData.hosts as List<ServerAddress?>
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
            CodecRegistries.fromProviders(pojoBuilder.build()),
            CodecRegistries.fromProviders(FallbackCodec)
        )

        val client = MongoClients.create(settings) ?: throw IllegalStateException("Could not connect to database!")
        database = client.getDatabase(loginData.database).withCodecRegistry(pojoRegistry)
        this.client = client

        return@runCatching this
    }

    override fun disconnect() {
        client?.close()
    }
}

inline fun <reified C : Any> Class<C>.getMongoCodec(): Codec<C> = this.getMongoCodec(serializer<C>())

fun <C : Any> Class<C>.getMongoCodec(serializer: KSerializer<C>): Codec<C> = object : Codec<C> {
    private val defaultCodec = BsonValueCodec()
    override fun getEncoderClass(): Class<C> = this@getMongoCodec
    
    override fun encode(writer: BsonWriter, value: C, encoderContext: EncoderContext) {
        val document = BsonDocument.parse("""{"value": ${Json.encodeToString(serializer, value)}}""")
        defaultCodec.encode(writer, document.getValue("value"), encoderContext)
    }

    override fun decode(reader: BsonReader, decoderContext: DecoderContext): C? = runCatching {
        val document = BsonDocument("value", defaultCodec.decode(reader, decoderContext))
        val element = Json.parseToJsonElement(document.toJson()).jsonObject.getValue("value")
        return@runCatching Json.decodeFromJsonElement(serializer, element)
    }.getOrNull()
}

internal object FallbackCodec: CodecProvider {
    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> get(clazz: Class<T>, registry: CodecRegistry): Codec<T>? {
        val serializer = serializerOrNull(clazz) as? KSerializer<T> ?: return null
        return clazz.getMongoCodec(serializer)
    }
}
