package at.flauschigesalex.lib.database.mongo.serializer

import com.mongodb.ServerAddress as ServerAddressMongo
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal object ServerAddressSerializer : KSerializer<ServerAddressMongo> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ServerAddress", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: ServerAddressMongo) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): ServerAddressMongo = decoder.decodeString().let(::ServerAddressMongo)
}

typealias ServerAddress = @Serializable(ServerAddressSerializer::class) ServerAddressMongo