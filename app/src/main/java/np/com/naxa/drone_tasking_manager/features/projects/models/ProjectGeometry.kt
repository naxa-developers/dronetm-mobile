package np.com.naxa.drone_tasking_manager.features.projects.models

import np.com.naxa.drone_tasking_manager.features.projects.dto.project.Geometry
import np.com.naxa.drone_tasking_manager.features.projects.dto.project.Properties

data class ProjectGeometry(
    val type: String? = null,
    val geometry: Geometry? = null,
    val properties: Properties? = null,
    val id: String? = null
)