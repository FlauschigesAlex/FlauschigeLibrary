package at.flauschigesalex.lib.database.base

interface RequireDatabaseClient<H: DatabaseHandler<*, *>> {

    fun connect(): Result<H>
    fun disconnect()
}