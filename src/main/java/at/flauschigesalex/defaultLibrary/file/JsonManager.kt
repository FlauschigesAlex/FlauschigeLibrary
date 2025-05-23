@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package at.flauschigesalex.defaultLibrary.file

import org.bson.BsonDocument
import org.bson.Document
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.File
import java.net.http.HttpResponse

class JsonManager private constructor(content: String) {

    companion object {

        operator fun invoke() : JsonManager {
            return JsonManager("{}")
        }

        operator fun invoke(jsonString: String?) : JsonManager? {
            try {
                JSONParser().parse(jsonString)
                return JsonManager(jsonString!!)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }
        operator fun invoke(jsonObject: Any?) : JsonManager? {
            return invoke(jsonObject.toString())
        }
        operator fun invoke(map: Map<String, Any?>) : JsonManager {
            return this().apply { this.writeMany(map.filter { it.value != null }) }
        }
        operator fun invoke(vararg pairs: Pair<String, Any?>) : JsonManager {
            return this(mapOf(*pairs))
        }
        operator fun invoke(pairs: Collection<Pair<String, Any?>>) : JsonManager {
            return this(pairs.toMap())
        }
        operator fun invoke(data: DataHandler?) : JsonManager? {
            if (data == null)
                return null
            
            return invoke(data.readString())
        }
        operator fun invoke(file: File) : JsonManager? {
            return this(FileHandler(file))
        }
        operator fun invoke(document: Document) : JsonManager? {
            return this(document.toJson())
        }
    }

    var originalContent: String
        private set
    var content: String
        private set

    init {
        try {
            JSONParser().parse(content)
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to read json from string:\n$content")
        }

        this.originalContent = content
        this.content = content
    }

    private fun get(jsonObject: JSONObject, path: String): Any? {
        if (!jsonObject.containsKey(path))
            return null

        return jsonObject[path]
    }

    fun getObject(path: String): Any? {
        if (path.isEmpty()) return toObject()

        val jsonObject = toJsonObject()

        if (!path.contains("."))
            return get(jsonObject, path)

        val completed = arrayListOf<String>()
        var currentJO = jsonObject

        val splitSourcePath = path.split(".").toList()
        for (splitSource in splitSourcePath) {
            val obj = get(currentJO, splitSource)
            val currentPath = completed.joinToString(separator = ".")+"."+splitSource

            if (currentPath == path)
                return obj ?: if (path.endsWith("._")) null else this.getObject("$path._")

            try {
                completed.add(splitSource)
                currentJO = obj as? JSONObject ?: return null
            } catch (fail: Exception) {
                fail.printStackTrace()
                break
            }
        }
        return null
    }

    fun getJsonObject(path: String): JSONObject? {
        if (path.isEmpty())
            return toJsonObject()

        try {
            return getObject(path) as JSONObject?
        } catch (ignore: Exception) {}
        return null
    }

    fun values(path: String = ""): Collection<Any> {
        val jsonObject = getJsonObject(path) ?: return listOf()
        return jsonObject.values.filterNotNull()
    }

    fun random(path: String = ""): Any {
        return this.randomOrNull(path)!!
    }
    fun randomOrNull(path: String = ""): Any? {
        return values(path).randomOrNull()
    }

    fun getJsonManager(path: String): JsonManager? {
        try {
            return JsonManager(getJsonObject(path)!!)
        } catch (ignore: Exception) {}
        return null
    }

    fun getObjectList(path: String): List<Any> {
        try {
            return getObject(path).let { it as List<*> }.map { it as Any }
        } catch (ignore: Exception) {}

        return listOf()
    }

    fun getJsonList(path: String): List<JsonManager> {
        try {
            return getStringList(path).map { JsonManager(it) }
        } catch (ignore: Exception) {}

        return listOf()
    }

    fun getStringList(path: String): List<String> {
        try {
            return getObjectList(path).map { it.toString() }
        } catch (ignore: Exception) {}

        return listOf()
    }

    fun getIntegerList(path: String): List<Int> {
        try {
            return getStringList(path).map { it.toInt() }
        } catch (ignore: Exception) {}

        return listOf()
    }

    fun getLongList(path: String): List<Long> {
        try {
            return getStringList(path).map { it.toLong() }
        } catch (ignore: Exception) {}

        return listOf()
    }

    fun getShortList(path: String): List<Short> {
        try {
            return getStringList(path).map { it.toShort() }
        } catch (ignore: Exception) {}

        return listOf()
    }

    fun getDoubleList(path: String): List<Double> {
        try {
            return getStringList(path).map { it.toDouble() }
        } catch (ignore: Exception) {}

        return listOf()
    }

    fun getFloatList(path: String): List<Float> {
        try {
            return getStringList(path).map { it.toFloat() }
        } catch (ignore: Exception) {}

        return listOf()
    }

    fun getBooleanList(path: String): List<Boolean> {
        try {
            return getObjectList(path).map { it as Boolean }
        } catch (ignore: Exception) {}

        return listOf()
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

    @Deprecated("", level = DeprecationLevel.ERROR)
    fun copyTo(path: String, newPath: String, override: Boolean = true): Boolean {
        return false
    }

    @Deprecated("", level = DeprecationLevel.ERROR)
    fun move(path: String, newPath: String, override: Boolean = true): Boolean {
        return false
    }

    fun removeMany(vararg paths: String) {
        return this.removeMany(paths.toList())
    }

    fun removeMany(paths: Collection<String>) {
        for (path in paths)
            this.remove(path)
    }

    fun remove(path: String) {
        if (!this.contains(path))
            return

        if (!path.contains(".")) {
            val jsonObject = this.toJsonObject()
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
        if (this.contains(path)) return true
        return write(path, obj)
    }

    fun writeMany(map: Map<String, Any?>): Boolean {
        return map.map { (path, obj) -> this.write(path, obj) }.all { it }
    }

    fun write(path: String, obj: Any?): Boolean {
        return this.write(path, toJsonObject(), obj) != null
    }

    private fun write(path: String, jsonObject: JSONObject, obj: Any?): JSONObject? {
        var temp = obj
        if (temp == null) {
            this.remove(path)
            return jsonObject
        }

        val parts = path.split(".").toList()
        val original = toJsonObject()
        var current = original

        for (i in parts.indices) {
            val part = parts[i]
            val currentObject = current[part]

            if (i == parts.size - 1) {
                if (temp is Enum<*>) temp = temp.toString()
                if (temp is JsonManager) temp = temp.toJsonObject()

                current[part] = temp
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
        jsonObject: JSONObject = toJsonObject(),
        path: StringBuilder = StringBuilder()
    ): Boolean {
        for (obj in jsonObject.keys) {
            if (obj !is JSONObject)
                continue

            if (!checkRemoveEmpty(obj, path.append(if (path.isEmpty()) "" else ".").append(obj)))
                continue

            if (obj.isNotEmpty())
                continue

            jsonObject.remove(obj)
        }
        return false
    }

    fun contains(path: String): Boolean {
        return getObject(path) != null
    }

    fun isOriginalContent(): Boolean {
        return content == originalContent
    }
    fun isModifiedContent(): Boolean {
        return !isOriginalContent()
    }
    
    fun toObject(): Any {
        return JSONParser().parse(content)
    }
    fun toJsonObject(): JSONObject {
        return toObject() as JSONObject
    }
    fun toDocument(): Document {
        return Document.parse(content)
    }
    fun toBsonDocument(): BsonDocument {
        return toDocument().toBsonDocument()
    }
    override fun toString(): String {
        return content
    }

    fun copy(): JsonManager {
        val json = JsonManager(content)
        json.originalContent = originalContent

        return json
    }
    fun copyOriginal(): JsonManager {
        return JsonManager(originalContent)
    }
}
