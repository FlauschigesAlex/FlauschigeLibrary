@file:Suppress("unused")

package at.flauschigesalex.lib.base.file.json

import at.flauschigesalex.lib.base.file.DataManager
import at.flauschigesalex.lib.base.file.FileManager
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import java.io.File
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodySubscribers
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.Flow
import kotlin.enums.EnumEntries

@Serializable(JsonManager.Companion.JsonSerializer::class)
class JsonManager(private var _content: JsonObject) : Cloneable {

    companion object {
        operator fun invoke(): JsonManager {
            return JsonManager(JsonObject(emptyMap()))
        }

        fun parse(json: String): Result<JsonManager> {
            return runCatching { this.parseOrThrow(json) }
        }
        fun parseOrThrow(json: String): JsonManager = JsonManager(Json.parseToJsonElement(json).jsonObject)
        
        operator fun invoke(json: String): JsonManager? = this.parse(json).getOrNull()
        operator fun invoke(json: Any?): JsonManager? = json?.let {
            invoke(it.toString()) ?: it.serializeOrNull()
        }
        operator fun invoke(json: JsonElement): JsonManager? = runCatching { JsonManager(json.jsonObject) }.getOrNull()
        operator fun invoke(map: Map<String, Any?>): JsonManager = JsonManager().apply {
            map.forEach { (key, value) -> this.put(key, value) }
        }
        operator fun invoke(pairs: Collection<Pair<String, Any?>>): JsonManager = invoke(pairs.toMap())
        operator fun invoke(vararg pairs: Pair<String, Any?>): JsonManager = invoke(pairs.toList())
        operator fun invoke(handler: DataManager): JsonManager? = handler.readString()?.let { invoke(it) }
        operator fun invoke(file: File): JsonManager? = invoke(FileManager(file))

        fun listOf(json: String): List<JsonManager> = runCatching {
            val array = Json.parseToJsonElement(json).jsonArray
            return array.map { item -> item.serializeOrThrow() }
        }.getOrElse { JsonManager(json)?.let { listOf(it) } ?: emptyList() }
        
        fun listOf(json: Any?): List<JsonManager> = json?.let { listOf(it.toString()) } ?: emptyList()
        fun listOf(handler: DataManager): List<JsonManager> = handler.readString()?.let { listOf(it) } ?: emptyList()
        fun listOf(file: File): List<JsonManager> = listOf(FileManager(file))
        
        var json: Json = Json {
            prettyPrint = true
        }
         
        val BodyHandler = HttpResponse.BodyHandler<JsonManager> {
            BodySubscribers.mapping(BodySubscribers.ofString(Charsets.UTF_8)) {
                JsonManager(it)
            }
        }
        val ListBodyHandler = HttpResponse.BodyHandler<List<JsonManager>> {
            BodySubscribers.mapping(BodySubscribers.ofString(Charsets.UTF_8)) {
                runCatching {
                    val array = Json.parseToJsonElement(it).jsonArray
                    val jsonList = array.map { item ->
                        item.serializeOrThrow()
                    }
                    return@runCatching jsonList
                }.getOrNull()
            }
        }
        
        @Suppress("FunctionName")
        fun BodyPublisher(json: JsonManager): JsonBodyPublisher = JsonBodyPublisher(json)
        object JsonSerializer : KSerializer<JsonManager> {
            override val descriptor: SerialDescriptor = JsonObject.serializer().descriptor
            override fun deserialize(decoder: Decoder): JsonManager = JsonManager(decoder.decodeSerializableValue(JsonObject.serializer()))
            override fun serialize(encoder: Encoder, value: JsonManager) = encoder.encodeSerializableValue(JsonObject.serializer(), value.content)
        }
    }

    val content: JsonObject
        get() = JsonObject(this._content)

    var originalContent: JsonObject = content
        private set

    fun overrideOriginalContent() {
        originalContent = _content
    }

    operator fun set(path: String, value: Any?) = this.put(path, value)

    fun put(path: String, value: Any?): Any? {
        if (value == null)
            return remove(path)

        val previous = this[path]

        val mappedValue = mapValue(value)
        this.putInternal(path, mappedValue)

        return previous
    }

    fun putIfAbsent(path: String, value: Any?): Any? {
        val current = this[path]
        if (current != null)
            return current

        return this.put(path, value)
    }

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
    private fun putInternal(path: String, value: JsonElement): JsonObject {
        val keys = path.split(".")

        fun putRec(obj: JsonObject, remainingKeys: List<String>): JsonObject {
            val key = remainingKeys.first()
            if (remainingKeys.size == 1) {
                val map = obj.toMutableMap()
                map[key] = value
                return JsonObject(map)
            }

            val nextObj = obj[key] as? JsonObject ?: JsonObject(emptyMap())
            val updatedChild = putRec(nextObj, remainingKeys.drop(1))

            val map = obj.toMutableMap()
            map[key] = updatedChild

            return JsonObject(map)
        }

        val new = putRec(_content, keys)
        _content = new
        return new
    }

    fun remove(path: String): Any? {
        val previous = this[path]
        removeInternal(path) ?: return null

        return previous
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

            return runCatching { val child = obj[key]?.jsonObject ?: JsonObject(emptyMap())
                val updatedChild = removeRec(child, remainingKeys.drop(1))

                if (updatedChild != null) {
                    if (updatedChild.isEmpty()) {
                        map.remove(key)
                    } else {
                        map[key] = updatedChild
                    }
                }
                JsonObject(map)
            }.getOrNull()
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

    fun getJson(path: String): JsonManager? = this.getString(path)?.let { invoke(it) }
    fun getJsonList(path: String): List<JsonManager> = this.getList(path).mapNotNull { invoke(it) }

    fun getString(path: String): String? = this[path]?.toString()
    fun getStringList(path: String): List<String> = this.getList(path).map {
        if (it is JsonPrimitive) it.content else it.toString()
    }
    
    fun getUUID(path: String): UUID? = this.getString(path)?.let { runCatching { UUID.fromString(it) }.getOrDefault(null) }
    fun getUUIDList(path: String): List<UUID> = this.getStringList(path).mapNotNull { runCatching { UUID.fromString(it) }.getOrDefault(null) }

    fun getByte(path: String): Byte? = this.getInt(path)?.toByte()
    fun getByteArray(path: String): ByteArray {
        return when (val value = this[path]) {
            is String -> value.toByteArray(Charsets.UTF_8)
            is JsonArray -> {
                val bytes = ByteArray(value.size)
                value.forEachIndexed { index, element ->
                    val primitive = element as? JsonPrimitive ?: return ByteArray(0)
                    if (primitive.isString) return ByteArray(0)
                    val number = primitive.intOrNull ?: return ByteArray(0)
                    if (number !in Byte.MIN_VALUE..Byte.MAX_VALUE) return ByteArray(0)
                    bytes[index] = number.toByte()
                }
                bytes
            }
            else -> ByteArray(0)
        }
    }
    @Suppress("UNCHECKED_CAST")
    fun getByteArrayList(path: String): List<ByteArray?> {
        return getList(path).map { (it as? JsonArray)?.mapNotNull { c -> c.jsonPrimitive.int.toByte() }?.toByteArray() }
    }
    
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

    @Suppress("UNCHECKED_CAST")
    inline fun <reified E : Enum<E>> getEnum(path: String): E? {
        val entries = runCatching {
            E::class.java.getDeclaredMethod("getEntries").invoke(null) as EnumEntries<E>
        }.getOrNull() ?: return null

        return getEnum(entries, path)
    }
    fun <E: Enum<E>> getEnum(entries: EnumEntries<E>, path: String): E? = this.getString(path)?.let { s -> entries.find { it.name.equals(s, true) } }

    private fun getFrom(path: String, json: JsonObject): Any? {

        val keys = path.split(".")
        val currentKey = keys.firstOrNull() ?: return null
        val remainingKey = keys.drop(1).joinToString(".")

        val currentValue = json[currentKey] ?: return null

        if (remainingKey.isBlank()) {
            runCatching { return currentValue.jsonPrimitive.toKotlinAny() }
            runCatching { return currentValue.jsonObject }
            runCatching { return currentValue.jsonArray }
            return null
        }

        return runCatching { this.getFrom(remainingKey, currentValue.jsonObject) }.getOrNull()
    }

    private fun JsonPrimitive.toKotlinAny(): Any? {
        if (this is JsonNull) return null

        if (this.isString) return this.content

        this.booleanOrNull?.let { return it }

        this.intOrNull?.let { return it }
        this.longOrNull?.let { return it }
        this.doubleOrNull?.let { return it }
        this.floatOrNull?.let { return it }

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
        return filterNotNull().reduceOrNull { merged, next -> merged.merge(next, override) } ?: JsonManager()
    }
    
    val keySet: Set<String>
        get() = keySet()
    
    /**
     * @return A deep set of all keys
     * @see keys
     */
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

    public override fun clone(): JsonManager = JsonManager(this.toString())!!
    override fun toString(): String {
        val content = json.parseToJsonElement(_content.toString())
        return json.encodeToString(JsonElement.serializer(), content)
    }
}

class JsonBodyPublisher(json: JsonManager) : HttpRequest.BodyPublisher {
    private val bytes = json.toString().toByteArray(Charsets.UTF_8)

    override fun subscribe(subscriber: Flow.Subscriber<in ByteBuffer>) {
        subscriber.onSubscribe(object : Flow.Subscription {
            private var isComplete = false
            override fun request(n: Long) {
                if (isComplete || n <= 0) return
                isComplete = true
                
                runCatching {
                    subscriber.onNext(ByteBuffer.wrap(bytes))
                    subscriber.onComplete()
                }.onFailure { subscriber.onError(it) }
            }

            override fun cancel() {
                isComplete = true
            }
        })
    }

    override fun contentLength(): Long = bytes.size.toLong()
}

fun DataManager.readJson(): JsonManager? = this.readString()?.let { JsonManager(it) }
fun DataManager.readJsonList(): List<JsonManager> = this.readString()?.let { JsonManager.listOf(it) }.orEmpty()