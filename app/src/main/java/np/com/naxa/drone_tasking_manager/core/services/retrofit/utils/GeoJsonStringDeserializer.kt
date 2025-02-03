package np.com.naxa.drone_tasking_manager.core.services.retrofit.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import org.maplibre.geojson.FeatureCollection
import java.lang.reflect.Type

class GeoJsonStringDeserializer : JsonDeserializer<FeatureCollection> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): FeatureCollection {
        val jsonObject = json.asJsonObject

        val featureCollection =
            if (jsonObject.has("results")) jsonObject.getAsJsonObject("results") else null

        if (featureCollection == null || !featureCollection.has("features")) throw Exception("Invalid GeoJSON format")

        return FeatureCollection.fromJson(featureCollection.toString())

    }
}