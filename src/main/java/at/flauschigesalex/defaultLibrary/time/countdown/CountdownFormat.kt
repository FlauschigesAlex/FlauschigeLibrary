package at.flauschigesalex.defaultLibrary.time.countdown

import at.flauschigesalex.defaultLibrary.time.countdown.CountdownFieldDisplay.FIRST_NOT_NULL
import java.time.Duration
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.*
import kotlin.math.abs

@Suppress("unused")
/**
 * @param display Determines the visible [timeunits][TimeUnit] by mapping them with a suffix.
 * @param countdownFieldDisplay Determines the visibility of fields depending on its value.
 * @param spacer Determines the characters put between a value and its suffix.
 * @param absoluteValue Determines if the duration should be handled as absolute.
 * @param allowEmptyReturn Determines if the returned value can be empty.
 *
 * @see [CountdownFieldDisplay]
 */
open class CountdownFormat(internal val display: Map<TimeUnit, String>,
                           private val countdownFieldDisplay: CountdownFieldDisplay,
                           internal val spacer: String = "",
                           internal val absoluteValue: Boolean = false,
                           internal val allowEmptyReturn: Boolean = false
) {

    companion object {
        val default = CountdownFormat(
            mapOf(
                DAYS to "d",
                HOURS to "h",
                MINUTES to "m",
                SECONDS to "s",
            ), FIRST_NOT_NULL, absoluteValue = true)

        val defaultMS = CountdownFormat(
            mapOf(
                DAYS to "d",
                HOURS to "h",
                MINUTES to "m",
                SECONDS to "s",
                MILLISECONDS to "ms",
            ), FIRST_NOT_NULL, absoluteValue = true)
    }

    init {
        if (display.containsKey(MICROSECONDS))
            throw IllegalArgumentException("${this::class.java.simpleName} contains illegal argument '$MICROSECONDS'")

        if (display.isEmpty())
            throw IllegalArgumentException("${this::class.java.simpleName} requires at least one field to display.")
    }

    internal fun format(duration: Duration): String {
        var millis = if (absoluteValue) abs(duration.toMillis()) else duration.toMillis()
        val builder = StringBuilder()

        if (display.containsKey(DAYS)) {
            val days = MILLISECONDS.toDays(millis)
            if (days > 0)
                millis -= DAYS.toMillis(days)

            countdownFieldDisplay.appendIfRequired(days, builder, display[DAYS], this)
        }
        if (display.containsKey(HOURS)) {
            val hours = MILLISECONDS.toHours(millis)
            if (hours > 0)
                millis -= HOURS.toMillis(hours)

            countdownFieldDisplay.appendIfRequired(hours, builder, display[HOURS], this)
        }
        if (display.containsKey(MINUTES)) {
            val minutes = MILLISECONDS.toMinutes(millis)
            if (minutes > 0)
                millis -= MINUTES.toMillis(minutes)

            countdownFieldDisplay.appendIfRequired(minutes, builder, display[MINUTES], this)
        }
        if (display.containsKey(SECONDS)) {
            val seconds = MILLISECONDS.toSeconds(millis)
            if (seconds > 0)
                millis -= SECONDS.toMillis(seconds)

            countdownFieldDisplay.appendIfRequired(seconds, builder, display[SECONDS], this)
        }
        if (display.containsKey(MILLISECONDS))
            countdownFieldDisplay.appendIfRequired(millis, builder, display[MILLISECONDS], this)

        if (builder.isEmpty() && !allowEmptyReturn)
            builder.append("0${spacer}${display.values.last()}")

        return builder.toString().trim()
    }
}

enum class CountdownFieldDisplay {
    /**
     * Displays any field that has a value greater than zero.
     */
    NOT_NULL,

    /**
     * Displays any field that has a value greater than zero or isn't the first visible field.
     */
    FIRST_NOT_NULL,

    /**
     * Displays any field regardless of its value.
     */
    ANY;

    internal fun appendIfRequired(value: Long, builder: StringBuilder, display: String?, parent: CountdownFormat) {
        val valueNull = value <= 0
        val builderEmpty = builder.isEmpty()
        val string = "$value${parent.spacer}${display ?: ""} "

        when (this) {
            ANY -> builder.append(string)
            NOT_NULL -> {
                if (!valueNull) builder.append(string)
            }

            FIRST_NOT_NULL -> {
                if (valueNull && builderEmpty)
                    return
                builder.append(string)
            }
        }
    }
}