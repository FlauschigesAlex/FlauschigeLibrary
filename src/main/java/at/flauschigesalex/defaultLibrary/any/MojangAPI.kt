package at.flauschigesalex.defaultLibrary.any

import at.flauschigesalex.defaultLibrary.file.JsonManager
import java.util.*

@Suppress("MemberVisibilityCanBePrivate", "DEPRECATION", "unused")
object MojangAPI {

    private val cache = HashSet<MojangProfile>()
    private val invalid = HashSet<Any>()

    /**
     * Called when a non-cached profile is resolved via UUID.
     * @see MojangAPI.addUuidLookup
     */
    @Deprecated("Variable is used as default operation, proceed with caution.")
    var uuidToProfileFunction = { uuid: UUID ->
        find(uuid, "https://api.mojang.com/user/profile/$uuid")
    }
    /**
     * Called when a non-cached profile is resolved via name.
     * @see MojangAPI.addNameLookup
     */
    @Deprecated("Variable is used as default operation, proceed with caution.")
    var nameToProfileFunction = { name: String ->
        find(name, "https://api.mojang.com/users/profiles/minecraft/$name")
    }

    /**
     * Called when a name is resolved via UUID.
     */
    @Deprecated("Variable is used as default operation, proceed with caution.")
    var uuidToNameFunction = { uuid: UUID ->
        profile(uuid)?.name
    }
    /**
     * Called when a UUID is resolved via name.
     */
    @Deprecated("Variable is used as default operation, proceed with caution.")
    var nameToUuidFunction = { name: String ->
        profile(name)?.uuid
    }
    /**
     * Called when a name is corrected.
     */
    @Deprecated("Variable is used as default operation, proceed with caution.")
    var caseCorrectFunction = { name: String ->
        profile(name)?.name
    }

    private val uuidToNameLookup = ArrayList<Pair<LookupCall, (UUID) -> CacheableMojangProfile?>>()
    private val nameToUuidLookup = ArrayList<Pair<LookupCall, (String) -> CacheableMojangProfile?>>()

    /**
     * @param String Name of the profile
     * @param UUID UniqueId of the profile
     * @param Boolean Determines if the profile should be cached.
     */
    fun addNameLookup(lookupCall: LookupCall = LookupCall.BEFORE, function: (UUID) -> CacheableMojangProfile?) {
        this.uuidToNameLookup.add(Pair(lookupCall, function))
    }
    /**
     * @param String Name of the profile
     * @param UUID UniqueId of the profile
     * @param Boolean Determines if the profile should be cached.
     */
    fun addUuidLookup(lookupCall: LookupCall = LookupCall.BEFORE, function: (String) -> CacheableMojangProfile?) {
        this.nameToUuidLookup.add(Pair(lookupCall, function))
    }

    private fun find(any: Any, url: String): MojangProfile? {
        val response = HttpRequestHandler.get(url)
        if (response.statusCode() != 200) {
            invalid.add(any)
            return null
        }

        val body = response.body() ?: ""
        val json = JsonManager(body)

        if (json == null) {
            invalid.add(any)
            return null
        }

        if (!json.contains("id") || !json.contains("name")) {
            invalid.add(any)
            return null
        }

        val uuidS = json.getString("id")
        val item = MojangProfile(json.getString("name")!!, uuidS!!.toUUID())
        cache.add(item)
        return item
    }

    /**
     * @return The profile belonging to the provided UUID
     */
    fun profile(playerUUID: UUID): MojangProfile? {
        if (invalid.contains(playerUUID))
            return null

        val cached = cache.firstOrNull { it.uuid == playerUUID }
        if (cached != null)
            return cached

        for (pair in uuidToNameLookup.filter { it.first == LookupCall.BEFORE }) {
            val value = pair.second.invoke(playerUUID)
            if (value != null)
                return value.profile.apply {
                    if (value.shouldCache)
                        cache.add(this)
                }
        }

        val default = uuidToProfileFunction.invoke(playerUUID)
        if (default != null)
            return default

        for (pair in uuidToNameLookup.filter { it.first == LookupCall.AFTER }) {
            val value = pair.second.invoke(playerUUID)
            if (value != null)
                return value.profile.apply {
                    if (value.shouldCache)
                        cache.add(this)
                }
        }

        return null
    }
    /**
     * @return The profile belonging to the provided name
     */
    fun profile(playerName: String): MojangProfile? {
        if (invalid.contains(playerName))
            return null

        val cached = cache.firstOrNull { it.name.equals(playerName, true) }
        if (cached != null)
            return cached

        for (pair in nameToUuidLookup.filter { it.first == LookupCall.BEFORE }) {
            val value = pair.second.invoke(playerName)
            if (value != null)
                return return value.profile.apply {
                    if (value.shouldCache)
                        cache.add(this)
                }
        }

        val default = nameToProfileFunction.invoke(playerName)
        if (default != null)
            return default

        for (pair in nameToUuidLookup.filter { it.first == LookupCall.AFTER }) {
            val value = pair.second.invoke(playerName)
            if (value != null)
                return return value.profile.apply {
                    if (value.shouldCache)
                        cache.add(this)
                }
        }

        return null
    }

    fun name(playerUUID: UUID): String? {
        return uuidToNameFunction.invoke(playerUUID)
    }
    fun uuid(playerName: String): UUID? {
        return nameToUuidFunction.invoke(playerName)
    }
    fun correctName(playerName: String): String? {
        return caseCorrectFunction.invoke(playerName)
    }

    enum class LookupCall {
        /**
         * Indicates that the custom-lookup is called before the default operation.
         * @see MojangAPI.uuidToProfileFunction
         * @see MojangAPI.nameToProfileFunction
         */
        BEFORE,
        /**
         * Indicates that the custom-lookup is called after the default operation.
         * @see MojangAPI.uuidToProfileFunction
         * @see MojangAPI.nameToProfileFunction
         */
        AFTER;
    }
}

data class MojangProfile(val name: String, val uuid: UUID)
data class CacheableMojangProfile(val profile: MojangProfile, internal val shouldCache: Boolean = true)

internal fun CharSequence.toUUID(): UUID {
    if (this.length != 32)
        throw IllegalArgumentException("String is not 32 chars long.")

    val uuidS = this.substring(0, 8) + "-" +
            this.substring(8, 12) + "-" +
            this.substring(12, 16) + "-" +
            this.substring(16, 20) + "-" +
            this.substring(20)

    if (!uuidS.matches(Regex("[a-z0-9-]+")))
        throw IllegalArgumentException("String does not match UUID-regex.")

    return UUID.fromString(uuidS)
}