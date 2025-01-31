package np.com.naxa.drone_tasking_manager.core.services.retrofit.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class LongNumDeserializer : JsonDeserializer<String> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): String {
        return try {
            json.asLong.toString() // ✅ Convert Long to String
        } catch (e: NumberFormatException) {
            json.asString // ✅ Return as String if already a string
        }
    }
}