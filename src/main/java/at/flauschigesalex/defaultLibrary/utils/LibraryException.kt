package at.flauschigesalex.defaultLibrary.utils

import java.lang.RuntimeException

@Suppress("unused")
open class LibraryException : RuntimeException {

    constructor() : super()
    constructor(message: String) : super(message)
    constructor(throwable: Throwable) : super(throwable)
    constructor(message: String, throwable: Throwable) : super(message, throwable)

}