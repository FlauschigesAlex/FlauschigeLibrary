package at.flauschigesalex.lib.database.base

abstract class DatabaseLogin<T> protected constructor() {

    abstract val host: T

    abstract val username: String
    abstract val password: String
}