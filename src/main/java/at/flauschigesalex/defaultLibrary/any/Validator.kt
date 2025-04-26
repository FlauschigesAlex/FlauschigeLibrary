@file:Suppress("unused")

package at.flauschigesalex.defaultLibrary.any

class Validator<T>(val value: T, val isValid: Boolean, val reason: String? = null) {

    constructor(input: T, supplier: (T) -> Boolean, reason: String? = null): this(input, supplier.invoke(input), reason)

    fun validOrNull(): T? {
        return if (isValid) value else null
    }
    fun validOrDefault(default: T): T {
        return validOrNull() ?: default
    }

    fun ifValid(validConsumer: (T) -> Unit) {
        if (isValid) validConsumer.invoke(value)
    }

    fun unlessValid(invalidConsumer: (T) -> Unit) {
        if (!isValid) invalidConsumer.invoke(value)
    }

    @Deprecated("legacy", ReplaceWith("unlessValid(invalidConsumer)"))
    fun ifInvalid(invalidConsumer: (T) -> Unit) = unlessValid(invalidConsumer)

    fun ifValidOrElse(validConsumer: (T) -> Unit, invalidConsumer: (T) -> Unit) {
        if (isValid) validConsumer.invoke(value)
        else invalidConsumer.invoke(value)
    }
}