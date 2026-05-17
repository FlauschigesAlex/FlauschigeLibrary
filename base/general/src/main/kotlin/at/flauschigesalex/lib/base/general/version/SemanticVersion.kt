@file:Suppress("unused")

package at.flauschigesalex.lib.base.general.version

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(SemanticVersionSerializer::class)
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
    
    val version: String = "$major.$minor.$patch${suffix.takeIf { it.isNotBlank() }?.let { "-$it" } ?: ""}"
    
    fun Char?.weight(): Int {
        if (this == null) return Int.MAX_VALUE
        
        if (this.toString().matches(Regex("[0-9a-zA-Z]")).not())
            return Int.MAX_VALUE
        
        return this.code
    }
    
    override fun compareTo(other: SemanticVersion): Int {
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

internal object SemanticVersionSerializer: KSerializer<SemanticVersion> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("SemanticVersion", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: SemanticVersion) = encoder.encodeString(value.version)
    override fun deserialize(decoder: Decoder): SemanticVersion = SemanticVersion.parseOrThrow(decoder.decodeString())
}