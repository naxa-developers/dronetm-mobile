package np.com.naxa.drone_tasking_manager.features.projects.dto

import com.google.gson.annotations.SerializedName


data class ProjectsResponseDto(
    @SerializedName("results") var results: ArrayList<Result> = arrayListOf(),
    @SerializedName("pagination") var pagination: Pagination? = Pagination()
)