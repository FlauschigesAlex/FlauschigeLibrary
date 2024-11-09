package at.flauschigesalex.defaultLibrary.database

abstract class DatabaseHandler<T: DatabaseLogin, D : DatabaseHandler<T, D>> {

    abstract val loginData: T

    abstract fun connect(): D
    abstract fun disconnect()
}