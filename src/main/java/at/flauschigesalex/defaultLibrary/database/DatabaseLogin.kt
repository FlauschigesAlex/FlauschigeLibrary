package at.flauschigesalex.defaultLibrary.database

import com.mongodb.ServerAddress

abstract class DatabaseLogin protected constructor() {

    abstract val host: ServerAddress

    abstract val username: String
    internal abstract val password: String
    abstract val database: String
}