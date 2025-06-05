package at.flauschigesalex.lib.database._internal

abstract class DatabaseHandler<T: DatabaseLogin<*>, D : DatabaseHandler<T, D>> {

    abstract val loginData: T
}