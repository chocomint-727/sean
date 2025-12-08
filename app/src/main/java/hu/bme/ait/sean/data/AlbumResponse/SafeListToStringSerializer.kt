package hu.bme.ait.sean.data.AlbumResponse

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.*

object SafeListToStringSerializer : JsonTransformingSerializer<Tags>(Tags.serializer()) {

    override fun transformDeserialize(element: JsonElement): JsonElement {
        return when (element) {
            is JsonObject -> element                            // OK
            is JsonNull -> JsonObject(emptyMap())               // null → empty object
            is JsonPrimitive -> JsonObject(emptyMap())          // "" → empty object
            else -> JsonObject(emptyMap())
        }
    }
}