package at.flauschigesalex.lib.database.mongo

import at.flauschigesalex.lib.base.file.DataManager
import at.flauschigesalex.lib.base.file.JsonManager
import at.flauschigesalex.lib.database._internal.DatabaseLogin
import com.mongodb.ServerAddress

@Suppress("MemberVisibilityCanBePrivate")
class MongoLogin(
    override val host: ServerAddress,
    override val username: String,
    override val password: String,
    val database: String,
    val moreHosts: Collection<ServerAddress>,
) : DatabaseLogin<ServerAddress>() {

    companion object {
        operator fun invoke(
            host: ServerAddress,
            username: String,
            password: String,
            database: String,
            vararg moreHosts: ServerAddress
        ) : MongoLogin = MongoLogin(host, username, password, database, moreHosts.toList())

        operator fun invoke(jsonResource: DataManager) : MongoLogin? {
            val string = jsonResource.readString() ?: return null
            val json = JsonManager(string) ?: return null
            return MongoLogin(json)
        }
        operator fun invoke(json: JsonManager) : MongoLogin {
            var hostnameList: List<String> = json.getStringList("hostname")
            if (hostnameList.isEmpty())
                hostnameList = listOf(json.getString("hostname") ?: throw IllegalArgumentException("Login-Phrase \"hostname\" cannot be null!"))
            
            if (hostnameList.isEmpty())
                throw IllegalArgumentException("Login-Phrase \"hostname\" cannot be empty!")

            val portList: List<Int> = json.get("port")?.let {
                if (it is List<*>) it.map { it.toString().toInt() }
                else listOf(it.toString().toInt())
            } ?: listOf(27017)

            if (portList.isEmpty())
                throw IllegalArgumentException("Login-Phrase \"port\" cannot be empty!")

            if (hostnameList.size != portList.size && hostnameList.size > 1)
                throw IllegalArgumentException("Login-Phrase \"hostname\" and \"port\" must have the same size!")

            val hostnames = hostnameList.mapIndexed { index, hostname -> ServerAddress(hostname, portList[index]) }
            if (hostnames.isEmpty()) throw IllegalArgumentException()

            val username = json.getString("username")
                ?: throw IllegalArgumentException("Login-Phrase \"username\" cannot be null!")

            val password = json.getString("password") ?: throw IllegalArgumentException("Login-Phrase \"password\" cannot be null!")
            val database = json.getString("database") ?: throw IllegalArgumentException("Login-Phrase \"database\" cannot be null!")

            val hostname = hostnames.first()
            val additionalHostnames = hostnames.drop(1)

            return MongoLogin(hostname, username, password, database, additionalHostnames)
        }
        operator fun invoke(map: Map<String, Any?>) : MongoLogin {
            return this(JsonManager(map))
        }
    }

    val allHosts: Collection<ServerAddress> get() = moreHosts.toMutableList().also { it.addFirst(host) }
}
