package at.flauschigesalex.lib.base.time.countdown

import at.flauschigesalex.lib.base.time.countdown.CountdownFieldDisplay.FIRST_NOT_NULL
import java.time.Duration
import at.flauschigesalex.lib.base.time.countdown.CountdownUnit.*

@Suppress("unused")
/**
 * @param display Determines the visible [timeunits][CountdownUnit] by mapping them with a suffix.
 * @param fieldDisplay Determines the visibility of fields depending on its value.
 * @param spacer Determines the characters put between a value and its suffix.
 * @param absoluteValue Determines if the duration should be handled as absolute.
 * @param allowEmptyReturn Determines if the returned value can be empty.
 *
 * @see [CountdownFieldDisplay]
 */
open class CountdownFormat(display: Map<CountdownUnit, String>,
                           private val fieldDisplay: CountdownFieldDisplay,
                           internal val spacer: String = "",
                           internal val absoluteValue: Boolean = false,
                           private val allowEmptyReturn: Boolean = false
) {

    private val display = display.toList().sortedByDescending { it.first.duration }.toMap()

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
        if (display.isEmpty())
            throw IllegalArgumentException("${this::class.java.simpleName} requires at least one field to display.")
    }

    internal fun format(duration: Duration): String {
        val dur = if (absoluteValue) duration.abs() else duration
        var millis = dur.toMillis()

        val builder = StringBuilder()

        display.forEach { (key, display) ->
            val unitMS = key.duration.toMillis()
            val value = millis / unitMS

            if (value > 0) {
                fieldDisplay.appendIfRequired(value, builder, display, this)
                millis -= value * unitMS
            }
        }

        if (builder.isBlank() && !allowEmptyReturn)
            builder.append("0$spacer${display.values.last()}")

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

    internal fun appendIfRequired(value: Long, builder: StringBuilder, display: String, parent: CountdownFormat) {
        val valueNull = value <= 0
        val builderEmpty = builder.isEmpty()
        val string = "$value${parent.spacer}${display} "

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