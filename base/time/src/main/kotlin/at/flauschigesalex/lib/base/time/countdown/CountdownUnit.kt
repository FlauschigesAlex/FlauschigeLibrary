package at.flauschigesalex.lib.base.time.countdown

import java.time.Duration
import java.time.temporal.Temporal
import java.time.temporal.TemporalUnit

private const val YEAR_IN_SECONDS = 31556952L
@Suppress("unused")
enum class CountdownUnit(private val duration: Duration) : TemporalUnit, Comparable<CountdownUnit> {

    MILLISECONDS(Duration.ofMillis(1)),
    SECONDS(Duration.ofSeconds(1)),
    MINUTES(Duration.ofMinutes(1)),
    HOURS(Duration.ofHours(1)),
    DAYS(Duration.ofDays(1)),
    MONTHS(Duration.ofSeconds(YEAR_IN_SECONDS / 12)),
    YEARS(Duration.ofSeconds(YEAR_IN_SECONDS)),

    ;

    override fun getDuration(): Duration = duration
    override fun isDurationEstimated(): Boolean = ordinal > DAYS.ordinal
    override fun isDateBased(): Boolean = false
    override fun isTimeBased(): Boolean = true

    @Suppress("UNCHECKED_CAST")
    override fun <R : Temporal?> addTo(temporal: R, amount: Long): R? = temporal?.plus(amount, this) as? R

    override fun between(temporal1Inclusive: Temporal?, temporal2Exclusive: Temporal?): Long = temporal1Inclusive?.until(temporal2Exclusive, this) ?: 0
}