package at.flauschigesalex.lib.database.base

abstract class DatabaseHandler<T: DatabaseLogin<*>, D : DatabaseHandler<T, D>> {

    abstract val loginData: T
}