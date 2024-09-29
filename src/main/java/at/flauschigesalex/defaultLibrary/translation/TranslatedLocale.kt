@file:Suppress("unused")

package at.flauschigesalex.defaultLibrary.translation

import at.flauschigesalex.defaultLibrary.FlauschigeLibrary
import at.flauschigesalex.defaultLibrary.file.JsonManager
import at.flauschigesalex.defaultLibrary.file.ResourceHandler
import lombok.Getter
import org.json.simple.JSONObject
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer
import java.util.stream.Stream

@Suppress("MemberVisibilityCanBePrivate")
@Getter
class TranslatedLocale private constructor(val locale: Locale) {

    @Suppress("unused")
    companion object {
        private val locales = HashMap<Locale, TranslatedLocale?>()

        private var fallbackLocale: TranslatedLocale? = null

        /**
         * @see TranslatedLocale.register
         * @throws TranslationException provided locale is unknown.
         */
        fun setFallbackLocale(locale: Locale) {
            val translatedLocale = locales[locale]
                ?: throw TranslationException("The provided locale could not be identified, maybe you forgot registering it?")

            this.setFallbackLocale(translatedLocale)
        }
        fun setFallbackLocale(locale: TranslatedLocale) {
            fallbackLocale = locale
        }

        @JvmStatic
        fun fallbackLocale(): TranslatedLocale {
            return fallbackLocale
                ?: throw TranslationException("Could not access the fallback locale, maybe you forgot registering it?")
        }

        @JvmStatic
        fun of(locale: Locale): TranslatedLocale {
            return locales.getOrDefault(locale, fallbackLocale())!!
        }

        @JvmStatic
        fun register(locale: Locale): TranslatedLocale {
            return TranslatedLocale(locale).also {
                if (fallbackLocale == null)
                    fallbackLocale = it
            }
        }
    }

    init {
        if (locales.containsKey(locale))
            throw TranslationException("Duplicate locale \"" + locale.toLanguageTag() + "\" is not allowed.")

        locales[locale] = this
    }


    private val fileCache = HashMap<String, JsonManager>()
    private val cache = HashMap<String, List<String>>()

    fun has(translationKey: String): Boolean {
        try {
            val found = find(translationKey)
            return found != translationKey
        } catch (ignore: Exception) {
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

        if (FlauschigeLibrary.library.mainThread == Thread.currentThread())
            throw TranslationException("Method \"findList\" may only be used asynchronously.")

        val response = TranslationValidator.validateKey(translationKey)
        if (response.failure)
            return listOf(response.translationKey)

        if (cache.containsKey(translationKey))
            return cache[translationKey]!!

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

                val l: List<String> = ArrayList(value.stream()
                    .map { modify(it.toString(), replacements) }.toList()
                )
                cache[translationKey] = l
                return l
            }

            if (value is JSONObject) return findList("$translationKey._", replacements)

            return listOf(modify(value.toString(), replacements))
        }

        try {

            val resource = ResourceHandler("translations/" + locale.toLanguageTag() + "/" + fileName + ".json")
            try {
                val json = JsonManager(resource)

                fileCache[fileName] = json
                return this.findList(translationKey, replacements)

            } catch (jFail: Exception) {
                val list = this.fallback(translationKey, replacements)
                cache[translationKey] = list
                return list
            }

        } catch (fail: Exception) {
            val list = this.fallback(translationKey, replacements)
            cache[translationKey] = list
            return list
        }
    }

    private fun fileName(translationKey: String): String {
        return translationKey.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
    }

    private fun fallback(translationKey: String, replacements: Map<String, Any>): List<String> {
        if (fallbackLocale == this) {
            return listOf(translationKey)
        }

        return fallbackLocale().findList(translationKey, replacements)
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

    val map: Map<String, (Modifier) -> String> = mapOf(
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

    data class Modifier(
        val key: String, val input: String, val provided: String,
        val strings: Stream<String>, val replacements: Map<String, Any>
    )
}