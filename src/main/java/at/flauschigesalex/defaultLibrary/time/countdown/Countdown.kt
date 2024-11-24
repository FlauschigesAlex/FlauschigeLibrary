package at.flauschigesalex.defaultLibrary.time.countdown

import at.flauschigesalex.defaultLibrary.time.TimeHandler
import at.flauschigesalex.defaultLibrary.time.countdown.CountdownFormat.Companion.default
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
        return TimeHandler.now().toCountdown(end)
    }

    /**
     * @see [CountdownFormat]
     */
    @Deprecated("legacy", ReplaceWith("toCountdown(end, formatter)", imports = ["at.flauschigesalex.defaultLibrary.time.TimeHandler"]))
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