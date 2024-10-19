@file:Suppress("MemberVisibilityCanBePrivate")

package at.flauschigesalex.defaultLibrary.file

import lombok.Getter
import org.bson.BsonDocument
import org.bson.Document
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.File
import java.net.http.HttpResponse
import java.util.concurrent.atomic.AtomicBoolean

@Suppress("unused") @Getter
class JsonManager internal constructor(content: String) {

    var originalContent: String
        private set
    var content: String
        private set

    constructor() : this("{}")

    constructor(content: Any) : this(content.toString())

    constructor(map: Map<String?, Any?>) : this() {
        this.writeMany(map)
    }

    constructor(document: Document) : this(document.toJson())

    constructor(response: HttpResponse<String>) : this(response.body())

    constructor(file: File) : this(FileHandler(file).readString()!!)

    constructor(resource: ResourceHandler) : this(resource.readString()!!)

    init {
        try {
            JSONParser().parse(content)
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to read json from string:\n$content")
        }

        this.originalContent = content
        this.content = content
    }

    fun asObject(): Any {
        return JSONParser().parse(content)
    }

    fun asJsonObject(): JSONObject {
        return asObject() as JSONObject
    }

    private fun get(jsonObject: JSONObject, path: String): Any? {
        if (!jsonObject.containsKey(path))
            return null

        return jsonObject[path]
    }

    fun getObject(path: String): Any? {
        if (path.isEmpty()) return asObject()

        val jsonObject = asJsonObject()

        if (!path.contains("."))
            return get(jsonObject, path)

        val completed = arrayListOf<String>()
        var currentJO = jsonObject

        val splitSourcePath = path.split(".").toList()
        for (splitSource in splitSourcePath) {
            val obj = get(currentJO, splitSource) ?: return null
            val currentPath = completed.joinToString(separator = ".")+"."+splitSource

            if (currentPath == path)
                return obj

            try {
                completed.add(splitSource)
                currentJO = obj as JSONObject
            } catch (fail: Exception) {
                fail.printStackTrace()
                break
            }
        }
        return null
    }

    fun getJsonObject(path: String): JSONObject? {
        if (path.isEmpty()) return asJsonObject()

        try {
            return getObject(path) as JSONObject?
        } catch (ignore: Exception) {}
        return null
    }

    fun getJsonManager(path: String): JsonManager? {
        try {
            return JsonManager(getJsonObject(path)!!)
        } catch (ignore: Exception) {}
        return null
    }

    @Suppress("UNCHECKED_CAST", "UNUSED_PARAMETER")
    fun <W> getList(path: String, cast: Class<W>, nullable: Boolean = true): List<W>? {
        try {
            return getObjectList(path)?.map { it as W }
        } catch (ignore: Exception) {}

        if (nullable)
            return null

        return listOf()
    }

    fun getObjectList(path: String, nullable: Boolean = true): List<Any>? {
        try {
            return getObject(path).let { it as List<*> }.map { it as Any }
        } catch (ignore: Exception) {}

        if (nullable)
            return null

        return listOf()
    }

    fun getJsonList(path: String, nullable: Boolean = true): List<JsonManager>? {
        try {
            return getStringList(path)?.map { JsonManager(it) }
        } catch (ignore: Exception) {}

        if (nullable)
            return null

        return listOf()
    }

    fun getStringList(path: String, nullable: Boolean = true): List<String>? {
        try {
            return getObjectList(path)?.map { it.toString() }
        } catch (ignore: Exception) {}

        if (nullable)
            return null

        return listOf()
    }

    fun getIntegerList(path: String, nullable: Boolean = true): List<Int>? {
        try {
            return getStringList(path)?.map { it.toInt() }
        } catch (ignore: Exception) {}

        if (nullable)
            return null

        return listOf()
    }

    fun getLongList(path: String, nullable: Boolean = true): List<Long>? {
        try {
            return getStringList(path)?.map { it.toLong() }
        } catch (ignore: Exception) {}

        if (nullable)
            return null

        return listOf()
    }

    fun getShortList(path: String, nullable: Boolean = true): List<Short>? {
        try {
            return getStringList(path)?.map { it.toShort() }
        } catch (ignore: Exception) {}

        if (nullable)
            return null

        return listOf()
    }

    fun getDoubleList(path: String, nullable: Boolean = true): List<Double>? {
        try {
            return getStringList(path)?.map { it.toDouble() }
        } catch (ignore: Exception) {}

        if (nullable)
            return null

        return ArrayList()
    }

    fun getFloatList(path: String, nullable: Boolean = true): List<Float>? {
        try {
            return getStringList(path)?.map { it.toFloat() }
        } catch (ignore: Exception) {}

        if (nullable)
            return null

        return ArrayList()
    }

    fun getBooleanList(path: String, nullable: Boolean = true): List<Boolean>? {
        try {
            return getObjectList(path)?.map { it as Boolean }
        } catch (ignore: Exception) {}

        if (nullable)
            return null

        return ArrayList()
    }

    fun getString(path: String): String? {
        val obj = getObject(path) ?: return null
        return obj.toString()
    }

    fun getInteger(path: String): Int? {
        try {
            return getString(path)!!.toInt()
        } catch (ignore: Exception) {}
        return null
    }

    fun getLong(path: String): Long? {
        try {
            return getString(path)!!.toLong()
        } catch (ignore: Exception) {}
        return null
    }

    fun getShort(path: String): Short? {
        try {
            return getString(path)!!.toShort()
        } catch (ignore: Exception) {}
        return null
    }

    fun getDouble(path: String): Double? {
        try {
            return getString(path)!!.toDouble()
        } catch (ignore: Exception) {}
        return null
    }

    fun getFloat(path: String): Float? {
        try {
            return getString(path)!!.toFloat()
        } catch (ignore: Exception) {}
        return null
    }

    fun getBoolean(path: String): Boolean? {
        try {
            return getString(path).toBoolean()
        } catch (ignore: Exception) {}
        return null
    }

    @JvmOverloads fun copyTo(path: String, newPath: String, override: Boolean = true): Boolean {
        if (!this.has(path) && !override)
            return false

        if (this.has(newPath) && !override)
            return false

        return this.write(newPath, this.getObject(path))
    }

    @JvmOverloads fun move(path: String, newPath: String, override: Boolean = true): Boolean {
        if (!this.has(path) && !override)
            return false

        if (this.has(newPath) && !override)
            return false

        if (!this.write(newPath, this.getObject(path)))
            return false

        this.remove(path)
        return true
    }

    fun removeMany(vararg paths: String) {
        return this.removeMany(paths.toList())
    }

    fun removeMany(paths: Collection<String>) {
        for (path in paths)
            this.remove(path)
    }

    fun remove(path: String) {
        if (!this.has(path))
            return

        if (!path.contains(".")) {
            val jsonObject = this.asJsonObject()
            jsonObject.remove(path)
            this.content = jsonObject.toString()

            this.checkRemoveEmpty()
            return
        }

        val child: String = path.split(".").last()
        val parent = path.substring(0, path.length - (child.length + 1))

        val jsonObject = this.getJsonObject(parent)
        jsonObject!!.remove(child)
        this.write(parent, jsonObject)

        this.checkRemoveEmpty()

        return
    }

    fun writeIfAbsent(path: String, obj: Any?): Boolean {
        if (this.has(path)) return true
        return write(path, obj)
    }

    fun writeMany(map: Map<String?, Any?>): Boolean {
        val success = AtomicBoolean(true)
        map.forEach { (string: String?, `object`: Any?) ->
            if (!this.write(string!!, `object`)) success.set(false)
        }

        return success.get()
    }

    fun write(path: String, obj: Any?): Boolean {
        return this.write(path, asJsonObject(), obj) != null
    }

    private fun write(path: String, jsonObject: JSONObject, obj: Any?): JSONObject? {
        var o = obj
        if (o == null) {
            this.remove(path)
            return jsonObject
        }

        val parts = path.split("\\.").toMutableList()
        val original = asJsonObject()
        var current = original

        for (i in parts.indices) {
            val part = parts[i]
            val currentObject = current[part]

            if (i == parts.size - 1) {
                if (o is Enum<*>) o = o.toString()
                if (o is JsonManager) o = o.asJsonObject()

                current[part] = o
                this.content = original.toJSONString()
                return current
            }

            if (currentObject is JSONObject) {
                current = currentObject
                continue
            }

            if (currentObject == null) {
                val newObject = JSONObject()
                current[part] = newObject
                current = newObject
                continue
            }
            return null
        }
        return null
    }

    private fun checkRemoveEmpty(
        jsonObject: JSONObject = asJsonObject(),
        path: StringBuilder = StringBuilder()
    ): Boolean {
        for (obj in jsonObject.keys) {
            if (obj !is JSONObject)
                continue

            if (!checkRemoveEmpty(obj, path.append(if (path.isEmpty()) "" else ".").append(obj)))
                continue

            if (!obj.isEmpty())
                continue

            jsonObject.remove(obj)
        }
        return false
    }

    fun has(path: String): Boolean {
        return getObject(path) != null
    }
    fun contains(path: String): Boolean {
        return this.has(path)
    }

    fun isOriginalContent(): Boolean {
        return content == originalContent
    }
    fun isModifiedContent(): Boolean {
        return !isOriginalContent()
    }

    fun toDocument(): Document {
        return Document.parse(content)
    }
    fun toBsonDocument(): BsonDocument {
        return toDocument().toBsonDocument()
    }

    fun clone(): JsonManager {
        val json = JsonManager(content)
        json.originalContent = originalContent

        return json
    }
    fun cloneOriginal(): JsonManager {
        return JsonManager(originalContent)
    }

    override fun toString(): String {
        return content
    }
}
