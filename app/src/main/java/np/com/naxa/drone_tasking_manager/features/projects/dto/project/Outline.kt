package np.com.naxa.drone_tasking_manager.features.projects.dto.project

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type
import com.google.gson.annotations.SerializedName

data class Outline(
    @SerializedName("type") var type: String? = null,
    @SerializedName("geometry") var geometry: Geometry? = null,
    @SerializedName("properties") var properties: Properties? = null,
    @SerializedName("id") var id: String? = null
)

class OutlineDeserializer : JsonDeserializer<Outline> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Outline {
        val jsonObject = json.asJsonObject
        return when (val type = jsonObject.get("type").asString) {
            "Feature" -> Outline(
                type = type,
                geometry = context.deserialize(jsonObject.get("geometry"), Geometry::class.java),
                properties = context.deserialize(jsonObject.get("properties"), Properties::class.java),
                id = jsonObject.get("id").asString
            )
            "Polygon" -> Outline(
                type = "Feature",
                geometry = context.deserialize(json, Geometry::class.java)
            )

            else -> throw JsonParseException("Unsupported outline type: $type")
        }
    }
}