package np.com.naxa.drone_tasking_manager.features.projects.dto

import com.google.gson.annotations.SerializedName


data class Pagination(
    @SerializedName("has_next") var hasNext: Boolean? = null,
    @SerializedName("has_prev") var hasPrev: Boolean? = null,
    @SerializedName("next_num") var nextNum: Int? = null,
    @SerializedName("prev_num") var prevNum: Int? = null,
    @SerializedName("page") var page: Int? = null,
    @SerializedName("per_page") var perPage: Int? = null,
    @SerializedName("total") var total: Int? = null
)