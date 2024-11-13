@file:Suppress("unused")

package at.flauschigesalex.defaultLibrary.any

class InputValidator<T>(val input: T, val isValid: Boolean, val reason: String? = null) {

    constructor(input: T, supplier: (T) -> Boolean, reason: String? = null): this(input, supplier.invoke(input), reason)

    fun validOrNull(): T? {
        return if (isValid) input else null
    }
    fun validOrDefault(default: T): T {
        return validOrNull() ?: default
    }

    fun ifValid(validConsumer: (T) -> Unit) {
        if (isValid) validConsumer.invoke(input)
    }

    fun unlessValid(invalidConsumer: (T) -> Unit) {
        if (!isValid) invalidConsumer.invoke(input)
    }

    @Deprecated("legacy")
    fun ifInvalid(invalidConsumer: (T) -> Unit) = unlessValid(invalidConsumer)

    fun ifValidOrElse(validConsumer: (T) -> Unit, invalidConsumer: (T) -> Unit) {
        if (isValid) validConsumer.invoke(input)
        else invalidConsumer.invoke(input)
    }
}