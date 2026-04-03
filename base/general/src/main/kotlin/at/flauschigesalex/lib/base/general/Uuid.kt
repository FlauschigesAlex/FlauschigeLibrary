@file:Suppress("unused")
@file:OptIn(ExperimentalUuidApi::class)

package at.flauschigesalex.lib.base.general

import java.time.Instant
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toKotlinUuid


val UUID.v7EpochMillis: Long
    get() = this.toKotlinUuid().v7EpochMillis
val Uuid.v7EpochMillis: Long
    get() {
        val b = this.toByteArray() // 16 bytes, MSB -> LSB
        // first 48 bits = unix time in ms (big-endian)
        var ts = 0L
        for (i in 0 until 6) {
            ts = (ts shl 8) or (b[i].toLong() and 0xFF)
        }
        return ts
    }

val UUID.v7Instant: Instant
    get() = this.toKotlinUuid().v7Instant
val Uuid.v7Instant: Instant
    get() = Instant.ofEpochMilli(v7EpochMillis)