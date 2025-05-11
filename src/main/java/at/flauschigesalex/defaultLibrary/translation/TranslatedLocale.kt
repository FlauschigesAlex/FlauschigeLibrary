@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package at.flauschigesalex.defaultLibrary.translation

import at.flauschigesalex.defaultLibrary.FlauschigeLibrary
import at.flauschigesalex.defaultLibrary.any.Validator
import at.flauschigesalex.defaultLibrary.file.JsonManager
import org.json.simple.JSONObject
import java.util.*

class TranslatedLocale private constructor(val locale: Locale) {

    @Suppress("unused")
    companion object {
        /**
         * To ensure that the performance of the program is not affecting the main thread, an exception will be thrown.
         * Set this variable's value to false to allow synchronous translation lookups.
         * @see TranslatedLocale.findList
         */
        @JvmStatic var requireAsyncThread: Boolean = true

        /**
         * @since [Version 1.14.0](https://github.com/FlauschigesAlex/FlauschigeLibrary/releases/tag/v1.14.0)
         * @see TranslationSource
         */
        @JvmStatic var translationSource: TranslationSource = TranslationSource.RESOURCE

        private var _fallbackLocale : TranslatedLocale? = null
        /**
         * This locale is used when a locale is not translated or a translation-value is missing.
         */
        @JvmStatic var fallbackLocale: TranslatedLocale
            get() = _fallbackLocale!!
            set(value) {
                _fallbackLocale = value
                println("Translation-Fallback-Locale is now set to '${value}'")
            }

        private val locales = HashMap<Locale, TranslatedLocale?>()

        @JvmStatic
        fun of(locale: Locale): TranslatedLocale {
            return locales.getOrDefault(locale, fallbackLocale)!!
        }

        @JvmStatic
        @Deprecated("Requires a collection of locales.", level = DeprecationLevel.ERROR)
        fun register() = Unit
        
        @JvmStatic
        fun register(vararg locales: Locale) {
            this.register(locales.toList())
        }
        @JvmStatic
        fun register(locales: Collection<Locale>) {
            locales.forEach { TranslatedLocale(it) }
        }

        fun validateKey(input: String): Validator<String> {
            var translationKey = input

            if (translationKey.isEmpty() || translationKey.isBlank())
                return Validator(translationKey, false, "TranslationKey cannot be empty.")

            translationKey = translationKey.trim()
            if (!translationKey.contains("."))
                return Validator(
                    translationKey,
                    false,
                    "TranslationKey must have sub-key: Required : main.sub -> Provided : $translationKey"
                )

            if (translationKey.startsWith(".") || translationKey.endsWith("."))
                return Validator(
                    translationKey,
                    false,
                    "TranslationKey is malformed: Cannot start or end with \".\""
                )

            return Validator(translationKey, true)
        }
    }

    init {
        if (locales.containsKey(locale))
            throw TranslationException("Duplicate locale \"" + locale.toLanguageTag() + "\" is not allowed.")
        
        if (_fallbackLocale == null)
            _fallbackLocale = this

        locales[locale] = this
    }


    private val fileCache = HashMap<String, JsonManager>()
    private val cache = HashMap<String, List<String>>()

    fun contains(translationKey: String): Boolean {
        try {
            val found = find(translationKey)
            return found.isValid
        } catch (fail: Exception) {
            fail.printStackTrace()
        }

        return false
    }

    fun find(translationKey: String, replacements: Map<String, Any?> = mapOf()): Validator<String> {
        val builder = StringBuilder()
        val validator = findList(translationKey, replacements);
        for (value in validator.value) {
            builder.append(value).append("\n")
        }

        return Validator(builder.toString().trim(), validator.isValid)
    }

    fun findList(translationKey: String, replacements: Map<String, Any?> = mapOf()): Validator<List<String>> {

        if (FlauschigeLibrary.library.mainThread == Thread.currentThread() && requireAsyncThread)
            throw TranslationException("Method \"findList\" may only be used asynchronously.")

        val response = validateKey(translationKey)
        if (!response.isValid)
            return Validator(listOf(response.value), false)
        else response.reason.let { if (it != null) System.err.println(it) }

        if (cache.containsKey(translationKey))
            return Validator(cache[translationKey]!!.map { modify(it, replacements) }, true)

        val fileName = fileName(translationKey)
        if (fileCache.containsKey(fileName)) {
            val json = fileCache[fileName]
            if (json == null) {
                val validator = this.fallback(translationKey, replacements)
                cache[translationKey] = validator.value
                return validator
            }

            val value = json.getObject(translationKey)

            if (value == null) {
                val validator = this.fallback(translationKey, replacements)
                cache[translationKey] = validator.value
                return validator
            }

            if (value is List<*>) {
                if (value.isEmpty()) {
                    val validator = this.fallback(translationKey, replacements)
                    cache[translationKey] = validator.value
                    return validator
                }

                cache[translationKey] = value.map { it.toString() }
                val list: List<String> = ArrayList(value.stream()
                    .map { modify(it.toString(), replacements) }.toList()
                )
                return Validator(list, true)
            }

            if (value is JSONObject) return findList("$translationKey._", replacements)

            cache[translationKey] = listOf(value.toString())
            return Validator(listOf(modify(value.toString(), replacements)), true)
        }

        val json = translationSource.invoke(locale, fileName)
        if (json == null) {
            val validator = this.fallback(translationKey, replacements)
            cache[translationKey] = validator.value
            return validator
        }
        
        try {
            fileCache[fileName] = json
            return this.findList(translationKey, replacements)

        } catch (jFail: Exception) {
            val validator = this.fallback(translationKey, replacements)
            cache[translationKey] = validator.value
            return validator
        }
    }

    private fun fileName(translationKey: String): String {
        return translationKey.split(".").dropLastWhile { it.isEmpty() }.toTypedArray().first()
    }

    private fun fallback(translationKey: String, replacements: Map<String, Any?>): Validator<List<String>> {
        if (fallbackLocale == this) {
            return Validator(listOf(translationKey), false)
        }

        return fallbackLocale.findList(translationKey, replacements)
    }

    private fun throwException(notEnough: Boolean, key: String, correct: String, found: String, source: String) {
        if (notEnough) throw TranslationException("Key \"$key\" requires more arguments: $correct but found: <$found>\nsource: $source")
        throw TranslationException("Key \"$key\" requires fewer arguments: $correct but found: <$found>\nsource: $source")
    }

    fun modify(value: String, replacements: Map<String, Any?> = emptyMap()): String {
        val modifierNames = Modifiers.entries.joinToString("|") { it.name.lowercase() }
        val tagPattern = Regex("<($modifierNames)(:([^<>]*))?>")

        var result = value
        while (true) {
            val match = tagPattern.find(result) ?: break

            val fullTag = match.value
            val modName = match.groupValues[1]
            val args = match.groupValues[3].split(":").filter { it.isNotEmpty() }

            val modifier = Modifiers.valueOf(modName.uppercase())
            val replacement = modifier.func(args, TranslationData(this, replacements))

            result = result.replaceFirst(fullTag, replacement)
        }
        return result
    }

    enum class Modifiers(internal val func: (List<String>, TranslationData) -> String) {
        TRANSLATE ({ list, data ->
            var result: String? = null

            for (item: String in list) {
                val validator = data.locale.find(item, data.replacements)
                if (!validator.isValid)
                    continue

                val value = validator.value
                result = data.locale.modify(value, data.replacements)
                break
            }
            result ?: list.first()
        }),
        VAR ({ list, data ->
            var result: String? = null

            for (item: String in list) {
                val replacement = data.replacements[item] ?: continue

                val value = replacement.toString();
                result = data.locale.modify(value, data.replacements)
                break
            }
            result ?: list.first()
        })
        ;
    }
    data class TranslationData(val locale: TranslatedLocale, val replacements: Map<String, Any?>)
}