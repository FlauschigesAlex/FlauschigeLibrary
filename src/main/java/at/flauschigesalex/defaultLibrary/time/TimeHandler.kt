@file:Suppress("unused")

package at.flauschigesalex.defaultLibrary.time

import at.flauschigesalex.defaultLibrary.time.countdown.Countdown
import at.flauschigesalex.defaultLibrary.time.countdown.CountdownFormat
import at.flauschigesalex.defaultLibrary.time.countdown.CountdownFormat.Companion.default
import java.time.*
import java.util.concurrent.TimeUnit

class TimeHandler private constructor(private val originalMilliSecond: Long) {

    companion object {
        /**
         * Default [ZoneId] used by all TimeHandlers unless explicitly instructed otherwise.
         * @see [TimeHandler.toLocalDateTime]
         * @see [TimeHandler.toZonedDateTime]
         */
        var defaultZoneId: ZoneId = ZoneId.systemDefault()

        /**
         * Creates a TimeHandler with the current time as its value.
         */
        fun now(): TimeHandler = this.ofEpochMilli(System.currentTimeMillis())

        /**
         * Creates a TimeHandler with the provided epoch-millisecond as its value.
         */
        fun ofEpochMilli(millisecond: Long): TimeHandler = TimeHandler(millisecond)
        /**
         * Creates a TimeHandler with the provided epoch-second as its value.
         */
        fun ofEpochSecond(second: Long): TimeHandler = TimeHandler(second * 1000)

        /**
         * Creates a TimeHandler with the provided [instant][Instant] as its value.
         */
        fun ofInstant(instant: Instant): TimeHandler = this.ofEpochMilli(instant.toEpochMilli())
        /**
         * Creates a TimeHandler with the provided [date-time][ZonedDateTime] as its value.
         */
        fun ofZonedDateTime(zdt: ZonedDateTime): TimeHandler = this.ofInstant(zdt.toInstant())

    }

    var epochMillisecond = originalMilliSecond
        private set
    val epochSecond: Long get() = epochMillisecond / 1000

    /**
     * Adds the provided time to its value.
     */
    fun plus(unit: TimeUnit, amount: Long): TimeHandler {
        return change(unit, amount)
    }
    /**
     * Subtracts the provided time from its value.
     */
    fun subtract(unit: TimeUnit, amount: Long): TimeHandler {
        return change(unit, -amount)
    }
    private fun change(unit: TimeUnit, amount: Long): TimeHandler {
        epochMillisecond += unit.toMillis(amount)
        return this
    }

    fun copy(): TimeHandler {
        return TimeHandler(epochMillisecond)
    }
    fun copyOriginal(): TimeHandler {
        return TimeHandler(originalMilliSecond)
    }

    fun toInstant(): Instant {
        return Instant.ofEpochMilli(epochMillisecond)
    }
    fun toDuration(other: TimeHandler): Duration {
        return Duration.ofMillis(this.epochMillisecond - other.epochMillisecond)
    }
    fun toCountdown(other: TimeHandler = now(), formatter: CountdownFormat = default): String {
        return Countdown.create(this.toDuration(other), formatter)
    }

    fun toZonedDateTime(zoneId: ZoneId = defaultZoneId): ZonedDateTime {
        return ZonedDateTime.of(this.toLocalDateTime(zoneId), zoneId)
    }
    fun toLocalDateTime(zoneId: ZoneId = defaultZoneId): LocalDateTime {
        return LocalDateTime.ofInstant(this.toInstant(), zoneId)
    }

    override fun toString(): String {
        return epochMillisecond.toString()
    }
}