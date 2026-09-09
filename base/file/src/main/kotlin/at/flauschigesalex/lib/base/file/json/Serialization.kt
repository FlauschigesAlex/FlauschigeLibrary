@file:Suppress("unused")

package at.flauschigesalex.lib.base.file.json

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json

inline fun <reified T: Any> JsonManager.deserializeOrThrow(deserializer: DeserializationStrategy<T>? = null) =
    this.deserialize<T>(deserializer).getOrThrow()
inline fun <reified T: Any> JsonManager.deserializeOrNull(deserializer: DeserializationStrategy<T>? = null) =
    this.deserialize<T>(deserializer).getOrNull()
inline fun <reified T: Any> JsonManager.deserialize(deserializer: DeserializationStrategy<T>? = null): Result<T> =
    runCatching {
        deserializer?.let { s -> Json.decodeFromString(s, this.toString()) }?.let { return@runCatching it }
        return@runCatching Json.decodeFromString(this.toString())
    }

@Deprecated("legacy", ReplaceWith("serializeOrThrow(serializer)"))
inline fun <reified T: Any> T.toJsonManagerOrThrow(serializer: SerializationStrategy<T>? = null): JsonManager = serializeOrThrow(serializer)
@Deprecated("legacy", ReplaceWith("serializeOrNull(serializer)"))
inline fun <reified T: Any> T.toJsonManagerOrNull(serializer: SerializationStrategy<T>? = null): JsonManager? = serializeOrNull(serializer)
@Deprecated("legacy", ReplaceWith("serialize(serializer)"))
inline fun <reified T: Any> T.toJsonManager(serializer: SerializationStrategy<T>? = null): Result<JsonManager> = serialize(serializer)

inline fun <reified T: Any> T.serializeOrThrow(serializer: SerializationStrategy<T>? = null): JsonManager =
    this.serialize(serializer).getOrThrow()
inline fun <reified T: Any> T.serializeOrNull(serializer: SerializationStrategy<T>? = null): JsonManager? =
    this.serialize(serializer).getOrNull()
inline fun <reified T: Any> T.serialize(serializer: SerializationStrategy<T>? = null): Result<JsonManager> =
    runCatching {
        serializer?.let { s -> Json.encodeToString(s, this).let { return@runCatching JsonManager.parseOrThrow(it) } }
        return@runCatching Json.encodeToString(this).let { JsonManager.parseOrThrow(it) }
    }