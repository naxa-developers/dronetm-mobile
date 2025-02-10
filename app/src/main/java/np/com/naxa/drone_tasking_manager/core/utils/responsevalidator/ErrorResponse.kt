package np.com.naxa.drone_tasking_manager.core.utils.responsevalidator

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

data class ErrorResponse(
    @SerializedName("detail") val detail: String? = null,
    @SerializedName("details") val details: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("messages") val messages: String? = null,
) {
    companion object {
        fun parseErrorBody(errorBody: String?): ErrorResponse {
            return try {
                Gson().fromJson(errorBody, ErrorResponse::class.java)
            } catch (e: Exception) {
                ErrorResponse("Error : Failed to parse response") // Return if parsing fails
            }
        }
    }
}

fun ErrorResponse.getErrorMessage(): String =
    detail ?: details ?: message ?: messages ?: "Unable to handle your request"

