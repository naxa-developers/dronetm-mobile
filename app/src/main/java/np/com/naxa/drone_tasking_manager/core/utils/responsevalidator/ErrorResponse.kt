package np.com.naxa.drone_tasking_manager.core.utils.responsevalidator

import com.google.gson.annotations.SerializedName

data class ErrorResponse(
    @SerializedName("detail") val detail: String?,
)