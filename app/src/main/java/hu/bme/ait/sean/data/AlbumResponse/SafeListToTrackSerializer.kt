package hu.bme.ait.sean.data.AlbumResponse

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.json.*

object SafeListToTrackSerializer : JsonTransformingSerializer<List<Track?>>(ListSerializer(Track.serializer().nullable)) {

    override fun transformDeserialize(element: JsonElement): JsonElement {
        return when (element) {
            is JsonArray -> element
            is JsonObject -> JsonArray(listOf(element))                            // OK
            is JsonNull -> JsonArray(emptyList())               // null → empty object
            is JsonPrimitive -> JsonArray(emptyList())          // "" → empty object
            else -> JsonArray(emptyList())
        }
    }
}