package at.flauschigesalex.lib.base.file

import java.lang.Exception

@Suppress("unused")
object Environment {

    private val fields = HashMap<String, String>()
    private val file = FileManager(".env")

    init {
        if (!file.exists)
            file.createFile()

        read(file)
        read(ResourceManager(".env"))
    }

    private fun read(handler: DataManager?) {
        handler ?: return
        val string = handler.readString() ?: return

        string.split(System.lineSeparator()).mapNotNull {
            if (it.contains('=').not()) return@mapNotNull null
            
            val key = it.substringBefore('=')
            val value = it.substringAfter('=')
            
            return@mapNotNull key to value
        }.toMap().apply {
            fields.putAll(this)
        }
    }

    operator fun get(key: String) = System.getenv(key) ?: fields[key]
    fun getOrDefault(key: String, default: String) = get(key) ?: default
    fun getOrElse(key: String, default: () -> String) = get(key) ?: default()

    @Deprecated("", ReplaceWith("set(key, value)")) 
    fun put(key: String, value: String) = set(key, value)
    
    operator fun set(key: String, value: String) {
        fields[key] = value
        file.write(fields.map { "${it.key}=${it.value}" }.joinToString(System.lineSeparator()))
    }
}