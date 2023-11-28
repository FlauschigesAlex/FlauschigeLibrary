package at.flauschigesalex.lib.database._internal

abstract class DatabaseLogin<T> protected constructor() {

    abstract val host: T

    abstract val username: String
    abstract val password: String
}