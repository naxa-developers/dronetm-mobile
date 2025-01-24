package np.com.naxa.drone_tasking_manager.features.projects.models

data class ProjectGeometry(
    val type: String? = null,
    val coordinates: ArrayList<ArrayList<ArrayList<Double>>> = arrayListOf()
)