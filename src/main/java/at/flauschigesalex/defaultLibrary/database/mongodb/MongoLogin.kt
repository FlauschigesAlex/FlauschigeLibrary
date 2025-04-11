@file:Suppress("MemberVisibilityCanBePrivate")

package at.flauschigesalex.defaultLibrary.database.mongodb

import at.flauschigesalex.defaultLibrary.database.DatabaseLogin
import at.flauschigesalex.defaultLibrary.file.DataHandler
import at.flauschigesalex.defaultLibrary.file.JsonManager
import com.mongodb.ServerAddress

class MongoLogin(
    override val host: ServerAddress,

    override val username: String,
    override val password: String,
    override val database: String,

    val moreHosts: Collection<ServerAddress>,
) : DatabaseLogin() {

    companion object {
        operator fun invoke(
            host: ServerAddress,

            username: String,
            password: String,
            database: String,
            vararg moreHosts: ServerAddress
        ) : MongoLogin = MongoLogin(host, username, password, database, moreHosts.toList())

        operator fun invoke(jsonResource: DataHandler) : MongoLogin? {
            val string = jsonResource.readString() ?: return null
            val json = JsonManager(string) ?: return null
            return MongoLogin(json)
        }
        operator fun invoke(json: JsonManager) : MongoLogin {
            val hostnameList: List<String> = json.getObject("hostname")?.let {
                if (it is List<*>) it.map { it.toString() }
                else listOf(it.toString())
            } ?: throw IllegalArgumentException("Login-Phrase \"hostname\" cannot be null!")

            if (hostnameList.isEmpty())
                throw IllegalArgumentException("Login-Phrase \"hostname\" cannot be empty!")

            val portList: List<Int> = json.getObject("port")?.let {
                if (it is List<*>) it.map { it as Int }
                else listOf(it as Int)
            } ?: listOf(27017)

            if (portList.isEmpty())
                throw IllegalArgumentException("Login-Phrase \"port\" cannot be empty!")

            if (hostnameList.size != portList.size && hostnameList.size > 1)
                throw IllegalArgumentException("Login-Phrase \"hostname\" and \"port\" must have the same size!")

            val hostnames = hostnameList.mapIndexed { index, hostname -> ServerAddress(hostname, portList[index]) }
            if (hostnames.isEmpty()) throw IllegalArgumentException()

            val username = json.getString("username")
                ?: throw IllegalArgumentException("Login-Phrase \"username\" cannot be null!")

            val password = json.getString("password") ?: json.getString("accessKey")?.also {
                System.err.println("Login-Phrase \"accessKey\" has been replaced with \"password\".\n" +
                        "Please note that the phrase \"accessKey\" could be dropped at any point.")
            } ?: throw IllegalArgumentException("Login-Phrase \"password\" cannot be null!")
            val database = json.getString("database")
                ?: throw IllegalArgumentException("Login-Phrase \"database\" cannot be null!")

            val hostname = hostnames.first()
            val more = hostnames.drop(1)

            return MongoLogin(hostname, username, password, database, more)
        }
        operator fun invoke(map: Map<String, Any?>) : MongoLogin? {
            return this(JsonManager(map))
        }
    }

    val allHosts: Collection<ServerAddress> get() = moreHosts.toMutableList().also { it.addFirst(host) }
}
