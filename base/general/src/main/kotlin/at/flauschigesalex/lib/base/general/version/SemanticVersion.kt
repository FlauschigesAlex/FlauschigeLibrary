@file:Suppress("unused")

package at.flauschigesalex.lib.base.general.version

data class SemanticVersion(val major: Int, val minor: Int, val patch: Int, val suffix: String = ""): Comparable<SemanticVersion> {
    companion object {
        
        fun parse(string: String): Result<SemanticVersion> = runCatching {
            val split = string.split(".")
            
            val check = split.take(2).map { 
                it.toInt()
            }
            
            if (check.size >= 2) {
                val patchRaw = split.getOrNull(2)
                val patch = patchRaw?.takeWhile { it.isDigit() }?.toInt() ?: 0
                
                val versionRaw = "${check[0]}.${check[1]}.${patch}"
                val suffix = string.removePrefix(versionRaw).removePrefix("-")
                
                return@runCatching SemanticVersion(check[0], check[1], patch, suffix)
            }
            
            val major = string.takeWhile { it.isDigit() }.toInt()
            val suffix = string.removePrefix(major.toString()).removePrefix("-")
            
            return@runCatching SemanticVersion(major, 0, 0, suffix)
        }
        fun parseOrNull(string: String): SemanticVersion? = this.parse(string).getOrNull()
        fun parseOrThrow(string: String): SemanticVersion = this.parse(string).getOrThrow()
    }
    
    constructor(major: Int, minor: Int, patch: Int, type: VersionType, build: Int): this(
        major, minor, patch, type.names.firstOrNull()?.let { "$it.$build" } ?: ""
    )
    
    val version: String = "$major.$minor.$patch${suffix.takeIf { it.isNotBlank() }?.let { "-$it" } ?: ""}"

    /**
     * The version type of this version. Defaults to [VersionType.RELEASE] if no matching suffix is found.
     */
    val type: VersionType = VersionType.entries.find { s ->
        s.names.any { suffix.startsWith(it, ignoreCase = true) }
    } ?: VersionType.RELEASE
    
    private fun Char?.weight(): Int {
        if (this == null) return Int.MAX_VALUE
        
        if (this.toString().matches(Regex("[0-9a-zA-Z]")).not())
            return Int.MAX_VALUE
        
        return this.code
    }
    
    override fun compareTo(other: SemanticVersion): Int {
        if (this.major != other.major) return this.major.compareTo(other.major)
        if (this.minor != other.minor) return this.minor.compareTo(other.minor)
        if (this.patch != other.patch) return this.patch.compareTo(other.patch)
        
        if (this.type != other.type) return this.type.compareTo(other.type)
        
        val size = maxOf(this.version.length, other.version.length)
        
        for (i in 0 until size) {
            val thisWeight: Int = this.version.getOrNull(i).weight()
            val otherWeight: Int = other.version.getOrNull(i).weight()
            
            if (thisWeight != otherWeight) return thisWeight - otherWeight
        }
        
        return 0   
    }

    override fun toString(): String = version
}

enum class VersionType(internal vararg val names: String) {
    SNAPSHOT("snapshot", "snap", "s"),
    ALPHA("alpha", "a"),
    BETA("beta", "b"),
    RELEASE_CANDIDATE("release-candidate", "cr", "rc"),
    RELEASE,
    ;
}