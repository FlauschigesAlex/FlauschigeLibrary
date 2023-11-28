package at.flauschigesalex.lib.base.time.countdown

import at.flauschigesalex.lib.base.time.TimeHandler
import at.flauschigesalex.lib.base.time.countdown.CountdownFormat.Companion.default
import java.time.Duration

@Suppress("unused")
@ExperimentalStdlibApi
fun Duration.toCountdown(formatter: CountdownFormat = default): String {
    return formatter.format(this)
}

@Suppress("unused")
object Countdown {

    /**
     * Creates a countdown with the current time as start.
     * @see [CountdownFormat]
     */
    fun nowUntil(end: TimeHandler, formatter: CountdownFormat = default): String {
        return TimeHandler.now().toCountdown(end, formatter)
    }

    /**
     * @see [CountdownFormat]
     */
    @Deprecated("legacy", ReplaceWith("toCountdown(end, formatter)", imports = ["TimeHandler"]))
    fun create(start: TimeHandler, end: TimeHandler, formatter: CountdownFormat = default): String {
        return start.toCountdown(end, formatter)
    }

    /**
     * @see [Duration.toCountdown]
     * @see [CountdownFormat]
     */
    fun create(duration: Duration, formatter: CountdownFormat = default): String {
        return formatter.format(duration)
    }
}