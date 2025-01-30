package np.com.naxa.drone_tasking_manager.features.projects.dto.projects

import com.google.gson.annotations.SerializedName
import np.com.naxa.drone_tasking_manager.features.projects.dto.project.ProjectDto

data class ProjectsResponseDto(
    @SerializedName("results") var results: ArrayList<ProjectDto> = arrayListOf(),
    @SerializedName("pagination") var pagination: Pagination? = Pagination()
)