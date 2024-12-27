package at.flauschigesalex.defaultLibrary.translation

import at.flauschigesalex.defaultLibrary.file.FileHandler
import at.flauschigesalex.defaultLibrary.file.JsonManager
import at.flauschigesalex.defaultLibrary.file.ResourceHandler
import java.util.*

/**
 * @since [Version 1.14.0](https://github.com/FlauschigesAlex/FlauschigeLibrary/releases/tag/v1.14.0)
 */
@Suppress("unused")
open class TranslationSource protected constructor(displayName: String,
                                                   private val func: (locale: Locale, fileName: String) -> JsonManager?
) {
    companion object {
        
        @JvmStatic var directoryName = "translations"
        
        private val _entries = HashSet<TranslationSource>()
        val entries get() = _entries.toSet()
        
        fun valueOf(name: String): TranslationSource? {
            return _entries.firstOrNull { it.name.equals(name, true) }
        }
        
        val RESOURCE = TranslationSource("RESOURCE") { locale, fileName ->
            ResourceHandler("${directoryName}/${locale.toLanguageTag()}/${fileName}.json")?.let {
                JsonManager(it)
            }
        }
        val FILE = TranslationSource("FILE") { locale, fileName ->
            FileHandler("${directoryName}/${locale.toLanguageTag()}/${fileName}.json").readString()?.let {
                JsonManager(it)
            }
        }
    }
    
    val name = displayName.uppercase()
    
    init { 
        _entries.add(this)
    }
    
    fun invoke(locale: Locale, fileName: String): JsonManager? {
        return func.invoke(locale, fileName)
    }

    override fun toString(): String {
        return name
    }

    override fun equals(other: Any?): Boolean {
        if (other is TranslationSource)
            return other.name == this.name
        
        return false
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}