@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package at.flauschigesalex.defaultLibrary.utils

data class InputValidator<T>(val input: T, val valid: Boolean) {

    constructor(input: T, supplier: (T) -> Boolean): this(input, supplier.invoke(input))

    fun validOrNull(): T? {
        return if (valid) input else null
    }
    fun validOrDefault(default: T): T {
        return validOrNull() ?: default
    }

    fun ifValid(validConsumer: (T) -> Unit) {
        if (valid) validConsumer.invoke(input)
    }

    fun ifInvalid(invalidConsumer: (T) -> Unit) {
        if (!valid) invalidConsumer.invoke(input)
    }

    fun ifValidOrElse(validConsumer: (T) -> Unit, invalidConsumer: (T) -> Unit) {
        if (valid) validConsumer.invoke(input)
        else invalidConsumer.invoke(input)
    }
}