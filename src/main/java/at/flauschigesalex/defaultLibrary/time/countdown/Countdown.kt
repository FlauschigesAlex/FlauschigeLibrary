package at.flauschigesalex.defaultLibrary.time.countdown

import at.flauschigesalex.defaultLibrary.time.TimeHandler
import at.flauschigesalex.defaultLibrary.time.countdown.CountdownFormat.Companion.default

@Suppress("unused")
object Countdown {

    /**
     * Creates a countdown with the current time as start.
     * @see [CountdownFormat]
     */
    fun nowUntil(end: TimeHandler, formatter: CountdownFormat = default): String {
        return create(TimeHandler.now(), end, formatter)
    }

    /**
     * @see [CountdownFormat]
     */
    fun create(start: TimeHandler, end: TimeHandler, formatter: CountdownFormat = default): String {
        return formatter.format(start, end)
    }
}