package np.com.naxa.drone_tasking_manager.core.utils.responsevalidator

import com.google.gson.Gson
import retrofit2.HttpException

fun validateResponse (e: HttpException)  :String {

    // Extract error body for non-200 responses
    val errorBody = e.response()?.errorBody()?.string()
    val statusCode = e.code()


        return  if (statusCode in 400..499)  {
            // Parse  error response
            val errorResponse = parseErrorBody(errorBody)
            ("Error: ${errorResponse?.detail}")
        }
        else  {
            // Generic HTTP error handling
            ("HTTP Error $statusCode: ${e.message()}")
        }
}

// Helper function to parse error body
private fun parseErrorBody(errorBody: String?): ErrorResponse? {
    return try {
        Gson().fromJson(errorBody, ErrorResponse::class.java)
    } catch (e: Exception) {
        ErrorResponse("Error : Failed to parse response") // Return if parsing fails
    }
}