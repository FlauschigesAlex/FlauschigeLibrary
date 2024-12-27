@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package at.flauschigesalex.defaultLibrary.translation

import at.flauschigesalex.defaultLibrary.FlauschigeLibrary
import at.flauschigesalex.defaultLibrary.any.InputValidator
import at.flauschigesalex.defaultLibrary.file.JsonManager
import org.json.simple.JSONObject
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer
import java.util.stream.Stream

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

        fun validateKey(input: String): InputValidator<String> {
            var translationKey = input

            if (translationKey.isEmpty() || translationKey.isBlank())
                return InputValidator(translationKey, false, "TranslationKey cannot be empty.")

            translationKey = translationKey.trim()
            if (!translationKey.contains("."))
                return InputValidator(
                    translationKey,
                    false,
                    "TranslationKey must have sub-key: Required : main.sub -> Provided : $translationKey"
                )

            if (translationKey.startsWith(".") || translationKey.endsWith("."))
                return InputValidator(
                    translationKey,
                    false,
                    "TranslationKey is malformed: Cannot start or end with \".\""
                )

            return InputValidator(translationKey, true)
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
            return found != translationKey
        } catch (fail: Exception) {
            fail.printStackTrace()
        }

        return false
    }

    fun find(translationKey: String, replacements: Map<String, Any> = mapOf()): String {
        val builder = StringBuilder()
        for (value in findList(translationKey, replacements)) {
            builder.append(value).append("\n")
        }

        return builder.toString().trim()
    }

    fun findList(translationKey: String, replacements: Map<String, Any> = mapOf()): List<String> {

        if (FlauschigeLibrary.library.mainThread == Thread.currentThread() && requireAsyncThread)
            throw TranslationException("Method \"findList\" may only be used asynchronously.")

        val response = validateKey(translationKey)
        if (!response.isValid)
            return listOf(response.input)
        else response.reason.let { if (it != null) System.err.println(it) }

        if (cache.containsKey(translationKey))
            return cache[translationKey]!!.map { modify(it, replacements) }

        val fileName = fileName(translationKey)
        if (fileCache.containsKey(fileName)) {
            val json = fileCache[fileName]
            if (json == null) {
                val list = this.fallback(translationKey, replacements)
                cache[translationKey] = list
                return list
            }

            val value = json.getObject(translationKey)

            if (value == null) {
                val list = this.fallback(translationKey, replacements)
                cache[translationKey] = list
                return list
            }

            if (value is List<*>) {
                if (value.isEmpty()) {
                    val l = this.fallback(translationKey, replacements)
                    cache[translationKey] = l
                    return l
                }

                cache[translationKey] = value.map { it.toString() }
                val l: List<String> = ArrayList(value.stream()
                    .map { modify(it.toString(), replacements) }.toList()
                )
                return l
            }

            if (value is JSONObject) return findList("$translationKey._", replacements)

            cache[translationKey] = listOf(value.toString())
            return listOf(modify(value.toString(), replacements))
        }

        val json = translationSource.invoke(locale, fileName)
        if (json == null) {
            val list = this.fallback(translationKey, replacements)
            cache[translationKey] = list
            return list
        }
        
        try {
            fileCache[fileName] = json
            return this.findList(translationKey, replacements)

        } catch (jFail: Exception) {
            val list = this.fallback(translationKey, replacements)
            cache[translationKey] = list
            return list
        }
    }

    private fun fileName(translationKey: String): String {
        return translationKey.split(".").dropLastWhile { it.isEmpty() }.toTypedArray().first()
    }

    private fun fallback(translationKey: String, replacements: Map<String, Any>): List<String> {
        if (fallbackLocale == this) {
            return listOf(translationKey)
        }

        return fallbackLocale.findList(translationKey, replacements)
    }

    private fun throwException(notEnough: Boolean, key: String, correct: String, found: String, source: String) {
        if (notEnough) throw TranslationException("Key \"$key\" requires more arguments: $correct but found: <$found>\nsource: $source")
        throw TranslationException("Key \"$key\" requires fewer arguments: $correct but found: <$found>\nsource: $source")
    }

    private fun modify(input: String, replacements: Map<String, Any>): String {
        val atomicString = AtomicReference(input)
        for ((key, value) in map) {
            if (!atomicString.get().contains("<$key:")) continue

            val skipFirst = AtomicBoolean(true)
            for (string in atomicString.get().split(("<$key:").toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()) {
                if (skipFirst.getAndSet(false)) continue

                val provided = string.split(">".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]

                atomicString.set(
                    atomicString.get().replace(
                        "<$key:$provided>", value.invoke(
                            Modifier(
                                key, atomicString.get(), provided,
                                Arrays.stream(provided.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                                    .toTypedArray()),
                                replacements
                            )
                        )
                    )
                )
            }
        }
        return atomicString.get()
    }

    private val map: Map<String, (Modifier) -> String> = mapOf(
        Pair("var") { modifier: Modifier ->
            val key = AtomicReference<String?>(null)
            val fallback = AtomicReference<String?>(null)
            modifier.strings.forEach { string: String? ->
                if (key.get() == null) {
                    key.set(string)
                } else if (fallback.get() == null) {
                    fallback.set(string)
                } else throwException(
                    false,
                    modifier.key,
                    "<" + modifier.key + ":[key]>",
                    modifier.provided,
                    modifier.input
                )
            }

            if (key.get() == null) throwException(
                true,
                modifier.key,
                "<" + modifier.key + ":[key]>",
                modifier.provided,
                modifier.input
            )
            modifier.replacements.getOrDefault(key.get(), key.get()).toString()
        },
        Pair("translate") { modifier: Modifier ->
            val key: AtomicReference<String> = AtomicReference(null)
            modifier.strings.forEach { string: String ->
                if (key.get() == null) {
                    key.set(string)
                } else throwException(
                    false,
                    modifier.key,
                    "<" + modifier.key + ":[key]>",
                    modifier.provided,
                    modifier.input
                )
            }

            if (key.get() == null) throwException(
                true,
                modifier.key,
                "<" + modifier.key + ":[key]>",
                modifier.provided,
                modifier.input
            )

            val builder = StringBuilder()
            findList(key.get()).forEach(Consumer { string: String? -> builder.append(string).append("\n") })
            builder.toString().trim()
        }
    )

    private data class Modifier(
        val key: String, val input: String, val provided: String,
        val strings: Stream<String>, val replacements: Map<String, Any>
    )
}