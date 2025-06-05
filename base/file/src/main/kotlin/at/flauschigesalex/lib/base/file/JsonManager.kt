package at.flauschigesalex.lib.base.file

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import org.bson.BsonDocument
import org.bson.Document
import java.io.File
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodySubscribers
import java.nio.ByteBuffer
import java.util.UUID
import java.util.concurrent.Flow
import kotlin.collections.toByteArray

@Suppress("unused")
@Serializable(with = JsonManager.Companion.JsonSerializer::class)
class JsonManager(private var _content: JsonObject) : Cloneable {

    companion object {
        operator fun invoke(): JsonManager {
            return JsonManager(JsonObject(emptyMap()))
        }

        operator fun invoke(json: String): JsonManager? {
            try {
                return JsonManager(Json.parseToJsonElement(json).jsonObject)
            } catch (_: Exception) {}
            return null
        }
        operator fun invoke(json: Any?): JsonManager? = json?.let { invoke(it.toString()) }
        operator fun invoke(json: JsonElement): JsonManager? {
            try {
                JsonManager(json.jsonObject)
            } catch (_: Exception) {}
            return null
        }
        operator fun invoke(map: Map<String, Any?>): JsonManager = JsonManager().apply {
            map.forEach { (key, value) -> this.put(key, value) }
        }
        operator fun invoke(pairs: Collection<Pair<String, Any?>>): JsonManager = invoke(pairs.toMap())
        operator fun invoke(vararg pairs: Pair<String, Any?>): JsonManager = invoke(pairs.toList())
        operator fun invoke(handler: DataManager): JsonManager? = handler.readString()?.let { invoke(it) }
        operator fun invoke(file: File): JsonManager? = invoke(FileManager(file))
        operator fun invoke(document: Document): JsonManager = invoke(document.toJson())!!

        val BodyHandler = HttpResponse.BodyHandler<JsonManager> {
            BodySubscribers.mapping(BodySubscribers.ofString(Charsets.UTF_8)) {
                JsonManager(it)
            }
        }
        @Suppress("FunctionName")
        fun BodyPublisher(json: JsonManager): JsonBodyPublisher = JsonBodyPublisher(json)
        class JsonSerializer : KSerializer<JsonManager> {

            override val descriptor: SerialDescriptor
                get() = PrimitiveSerialDescriptor("JsonManager", PrimitiveKind.STRING)

            override fun deserialize(decoder: Decoder): JsonManager = JsonManager(decoder.decodeString())!!
            override fun serialize(encoder: Encoder, value: JsonManager) = encoder.encodeString(value.toString())
        }
    }

    val content: JsonObject
        get() = _content

    var originalContent = _content
        private set

    fun overrideOriginalContent() {
        originalContent = _content
    }

    operator fun set(path: String, value: Any?) = this.put(path, value)

    @Deprecated("", ReplaceWith("put(path, value)"))
    fun write(path: String, value: Any?) = this.put(path, value)
    fun put(path: String, value: Any?): Any? {
        if (value == null)
            return remove(path)

        val prevoius = this[path]

        val mappedValue = mapValue(value)
        this.putInternal(path, mappedValue)

        return prevoius
    }

    @Deprecated("", ReplaceWith("putIfAbsent(path, value)"))
    fun writeIfAbsent(path: String, value: Any?) = this.put(path, value)
    fun putIfAbsent(path: String, value: Any?): Any? {
        val current = this[path]
        if (current != null)
            return current

        return this.put(path, value)
    }

    @Deprecated("", ReplaceWith("putIfAbsent(path, value)"), DeprecationLevel.ERROR)
    fun writeIfAbsent(map: Map<String, Any?>) {
        throw IllegalAccessException()
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun mapValue(value: Any): JsonElement {
        return when (value) {
            is JsonElement -> value
            is Number -> JsonPrimitive(value)
            is Boolean -> JsonPrimitive(value)
            is JsonManager -> value._content
            is ByteArray -> mapValue(value.map { it.toInt() })
            is Map<*, *> -> value.mapNotNull {
                val entryKey = it.key?.toString() ?: return@mapNotNull null
                val entryValue = it.value ?: return@mapNotNull null

                return@mapNotNull entryKey to mapValue(entryValue)
            }.let { JsonObject(it.toMap()) }
            is Collection<*> -> value.map {
                it?.let { mapValue(it) } ?: JsonPrimitive(null)
            }.let { JsonArray(it) }

            else -> {
                val json = JsonManager(value.toString()) ?: return JsonPrimitive(value.toString())
                mapValue(json)
            }
        }
    }
    private fun putInternal(path: String, value: JsonElement): JsonObject? {
        val keys = path.split(".")

        fun putRec(obj: JsonObject, remainingKeys: List<String>): JsonObject? {
            val key = remainingKeys.first()
            if (remainingKeys.size == 1) {
                val map = obj.toMutableMap()
                map[key] = value
                return JsonObject(map)
            }

            try {
                val nextObj = (obj[key]?.jsonObject) ?: JsonObject(emptyMap())
                val updatedChild = putRec(nextObj, remainingKeys.drop(1)) ?: JsonObject(emptyMap())

                val map = obj.toMutableMap()
                map[key] = updatedChild

                return JsonObject(map)
            } catch (_: Exception) {
                return null
            }
        }

        val new = putRec(_content, keys) ?: return null
        _content = new
        return new
    }

    fun remove(path: String): Any? {
        val prevoius = this[path]
        removeInternal(path) ?: return null

        return prevoius
    }
    private fun removeInternal(path: String): JsonObject? {
        val keys = path.split(".")
        require(keys.isNotEmpty()) { "Pfad darf nicht leer sein." }

        fun removeRec(obj: JsonObject, remainingKeys: List<String>): JsonObject? {
            val key = remainingKeys.first()
            val map = obj.toMutableMap()

            if (remainingKeys.size == 1) {
                map.remove(key)
                return JsonObject(map)
            }

            return try {
                val child = obj[key]?.jsonObject ?: JsonObject(emptyMap())
                val updatedChild = removeRec(child, remainingKeys.drop(1))

                if (updatedChild != null) {
                    if (updatedChild.isEmpty()) {
                        map.remove(key)
                    } else {
                        map[key] = updatedChild
                    }
                }
                JsonObject(map)
            } catch (_: Exception) {
                null
            }
        }

        val new = removeRec(_content, keys) ?: return null
        _content = new
        return new
    }


    operator fun get(path: String): Any? {
        return getFrom(path, _content)
    }
    @Suppress("UNCHECKED_CAST")
    fun getList(path: String): List<Any> {
        val list = this[path] as? List<*> ?: return emptyList()
        return list as List<Any>
    }

    @Deprecated("", ReplaceWith("getJson(path)"))
    fun getJsonManager(path: String): JsonManager? = this.getJson(path)
    fun getJson(path: String): JsonManager? = this.getString(path)?.let { invoke(it) }
    fun getJsonList(path: String): List<JsonManager> = this.getList(path).mapNotNull { invoke(it) }

    fun getString(path: String): String? = this[path]?.toString()
    fun getStringList(path: String): List<String> = this.getList(path).map { it.toString().trim('"') }
    
    fun getUUID(path: String): UUID? = this.getString(path)?.let { runCatching { UUID.fromString(it) }.getOrDefault(null) }
    fun getUUIDList(path: String): List<UUID> = this.getStringList(path).mapNotNull { runCatching { UUID.fromString(it) }.getOrDefault(null) }

    fun getByte(path: String): Byte? = this.getInt(path)?.toByte()
    fun getByteArray(path: String): ByteArray = this.getString(path)?.toByteArray() ?: ByteArray(0)
    @Suppress("UNCHECKED_CAST")
    fun getByteArrayList(path: String): List<ByteArray?> {
        return getList(path).map { (it as? JsonArray)?.mapNotNull { c -> c.jsonPrimitive.int.toByte() }?.toByteArray() }
    }
    
    @Deprecated("", ReplaceWith("getInt(key)"))
    fun getInteger(path: String) = this.getInt(path)
    fun getInt(path: String): Int? = this.getString(path)?.toIntOrNull()
    fun getIntList(path: String): List<Int> = this.getStringList(path).mapNotNull { it.toIntOrNull() }

    fun getLong(path: String): Long? = this.getString(path)?.toLongOrNull()
    fun getLongList(path: String): List<Long> = this.getStringList(path).mapNotNull { it.toLongOrNull() }

    fun getFloat(path: String): Float? = this.getString(path)?.toFloatOrNull()
    fun getFloatList(path: String): List<Float> = this.getStringList(path).mapNotNull { it.toFloatOrNull() }

    fun getDouble(path: String): Double? = this.getString(path)?.toDoubleOrNull()
    fun getDoubleList(path: String): List<Double> = this.getStringList(path).mapNotNull { it.toDoubleOrNull() }

    fun getBoolean(path: String): Boolean? = this.getString(path)?.toBoolean()
    fun getBooleanList(path: String): List<Boolean> = this.getStringList(path).map { it.toBoolean() }

    private fun getFrom(path: String, json: JsonObject): Any? {

        val keys = path.split(".")
        val currentKey = keys.firstOrNull() ?: return null
        val remainingKey = keys.drop(1).joinToString(".")

        val currentValue = json[currentKey] ?: return null

        if (remainingKey.isBlank()) {
            try {
                return currentValue.jsonPrimitive.toKotlinAny()
            } catch (_: Exception) {}
            try {
                return currentValue.jsonObject
            } catch (_: Exception) {}
            try {
                return currentValue.jsonArray
            } catch (_: Exception) {}

            return null
        }

        try {
            return this.getFrom(remainingKey, currentValue.jsonObject)
        } catch (_: Exception) {
        }

        return null
    }

    private fun JsonPrimitive.toKotlinAny(): Any? {
        if (this is JsonNull) return null

        // falls es als String serialisiert war -> String
        if (this.isString) return this.content

        // booleans
        this.booleanOrNull?.let { return it }

        // integers / longs / doubles (versuche int zuerst)
        this.intOrNull?.let { return it }
        this.longOrNull?.let { return it }
        this.floatOrNull?.let { return it }
        this.doubleOrNull?.let { return it }

        // Fallback: content (String)
        return this.content
    }

    /**
     * Merges two [JsonManagers][JsonManager] into a new instance.
     * @param other The JsonManager to merge with
     * @param override Already existing fields should be overridden by the other JsonManager.
     */
    fun merge(other: JsonManager, override: Boolean): JsonManager {
        val new = this.clone()

        other.lowestEntries.forEach { (key, value) ->
            if (new.contains(key) && !override)
                return@forEach

            new[key] = value
        }

        return new
    }
    fun List<JsonManager?>.merge(override: Boolean): JsonManager {
        assert(this.isNotEmpty())

        val mutable = this.filterNotNull().toMutableList()

        while (mutable.size != 1) {
            val first = mutable[0]
            val second = mutable[1]

            mutable[0] = first.merge(second, override)
            mutable.removeAt(1)
        }

        return mutable.first()
    }

    /**
     * @return A deep set of all keys
     * @see keys
     */
    val keySet: Set<String>
        get() = keySet()

    val lowestKeys: Set<String>
        get() {
            val deepKeys = keySet.toMutableList()
            deepKeys.removeIf {
                if (it.equals("_id", true))
                    return@removeIf false

                deepKeys.any { other -> other.startsWith("$it.") }
            }
            return deepKeys.toSet()
        }
    val lowestEntries: Map<String, Any?>
        get() = lowestKeys.associateWith { this[it] }

    private fun keySet(parent: String = ""): Set<String> {
        val keys = this.keys.toMutableSet()
        val sub = keys.mapNotNull {
            val json = this.getJson(it) ?: return@mapNotNull null
            Pair(json, it)
        }.flatMap { it.first.keySet(it.second) }

        keys.addAll(sub)
        return keys.map { "${if (parent.isNotBlank()) "$parent." else ""}$it" }.toSet()
    }

    val entries: Map<String, Any?>
        get() = _content.keys.associateWith { this[it] }
    val keys: Set<String>
        get() = _content.keys
    val values: Collection<Any>
        get() = entries.mapNotNull { it.value }

    fun contains(path: String) = this[path] != null
    fun isOriginalContent() = this._content == this.originalContent
    fun isModifiedContent() = !isOriginalContent()

    fun toObject(): Any = this._content
    fun toJsonObject(): JsonObject = this._content
    fun toDocument(): Document = Document.parse(this.toString())
    fun toBsonDocument(): BsonDocument = this.toDocument().toBsonDocument()

    public override fun clone(): JsonManager = JsonManager(this.toString())!!
    override fun toString(): String = this.toString(true)
    fun toString(pretty: Boolean = true): String {
        val json = Json { prettyPrint = pretty }
        val content = json.parseToJsonElement(_content.toString())
        return json.encodeToString(JsonElement.serializer(), content)
    }
}

class JsonBodyPublisher(json: JsonManager) : HttpRequest.BodyPublisher {
    private val bytes = json.toString().toByteArray(Charsets.UTF_8)

    override fun subscribe(subscriber: Flow.Subscriber<in ByteBuffer>) {
        subscriber.onSubscribe(object : Flow.Subscription {
            private var done = false
            override fun request(n: Long) {
                if (done || n <= 0)
                    return

                done = true
                try {

                    subscriber.onNext(ByteBuffer.wrap(bytes))
                    subscriber.onComplete()

                } catch (e: Exception) {
                    subscriber.onError(e)
                }
            }

            override fun cancel() {
                done = true
            }

        })
    }

    override fun contentLength(): Long = bytes.size.toLong()
}

fun DataManager.readJson(): JsonManager? = this.readString()?.let { JsonManager(it) }