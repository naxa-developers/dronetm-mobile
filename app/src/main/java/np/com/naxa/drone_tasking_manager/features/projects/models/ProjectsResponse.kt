package np.com.naxa.drone_tasking_manager.features.projects.models

data class ProjectsResponse(
    val hasNext: Boolean? = null,
    val hasPrev: Boolean? = null,
    val page: Int? = null,
    val total: Int? = null,
    val projects: List<Project> = emptyList()
)