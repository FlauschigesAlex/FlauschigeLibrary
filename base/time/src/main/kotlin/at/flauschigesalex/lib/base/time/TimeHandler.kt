package at.flauschigesalex.lib.base.time

import at.flauschigesalex.lib.base.time.countdown.Countdown
import at.flauschigesalex.lib.base.time.countdown.CountdownFormat
import java.time.*
import java.time.temporal.*

@Suppress("unused", "MemberVisibilityCanBePrivate")
class TimeHandler private constructor(val temporal: Temporal) : Temporal, Comparable<TimeHandler> {

    companion object {

        /**
         * Creates a TimeHandler with the current time as its value.
         */
        fun now(): TimeHandler = ofEpochMilli(System.currentTimeMillis())

        /**
         * Creates a TimeHandler with the provided epoch-millisecond as its value.
         */
        fun ofEpochMilli(millisecond: Long): TimeHandler = TimeHandler(Instant.ofEpochMilli(millisecond))
        /**
         * Creates a TimeHandler with the provided epoch-second as its value.
         */
        fun ofEpochSecond(second: Long): TimeHandler = TimeHandler(Instant.ofEpochSecond(second))

        /**
         * Creates a TimeHandler with the provided [temporal][Temporal] as its value.
         */
        fun ofTemporal(temporal: Temporal): TimeHandler = TimeHandler(temporal)
    }

    override fun isSupported(unit: TemporalUnit?): Boolean = temporal.isSupported(unit)
    override fun isSupported(field: TemporalField?): Boolean = temporal.isSupported(field)
    override fun getLong(field: TemporalField?): Long = temporal.getLong(field)

    override fun with(field: TemporalField?, newValue: Long): TimeHandler = TimeHandler(temporal.with(field, newValue))
    override fun with(adjuster: TemporalAdjuster?): TimeHandler = TimeHandler(temporal.with(adjuster))
    override fun plus(amountToAdd: Long, unit: TemporalUnit?): TimeHandler = TimeHandler(temporal.plus(amountToAdd, unit))
    override fun plus(amount: TemporalAmount?): TimeHandler = TimeHandler(temporal.plus(amount))
    override fun until(endExclusive: Temporal?, unit: TemporalUnit?): Long = temporal.until(endExclusive, unit)
    override fun minus(amount: TemporalAmount?): TimeHandler = TimeHandler(temporal.minus(amount))
    override fun minus(amountToSubtract: Long, unit: TemporalUnit?): TimeHandler = TimeHandler(temporal.minus(amountToSubtract, unit))

    override fun compareTo(other: TimeHandler): Int {
        return Instant.from(temporal).compareTo(Instant.from(other.temporal))
    }

    fun toCountdown(other: Temporal, format: CountdownFormat): String {
        val duration = Duration.between(this, other)
        return Countdown.create(duration, format)
    }
}