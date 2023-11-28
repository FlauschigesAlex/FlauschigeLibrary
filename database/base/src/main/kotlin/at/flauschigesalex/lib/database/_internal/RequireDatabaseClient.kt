package at.flauschigesalex.lib.database._internal

interface RequireDatabaseClient<H: DatabaseHandler<*, *>> {

    fun connect(): H
    fun disconnect()
}