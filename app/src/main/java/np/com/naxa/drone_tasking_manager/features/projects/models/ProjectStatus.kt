package np.com.naxa.drone_tasking_manager.features.projects.models

import androidx.compose.ui.graphics.Color

enum class ProjectStatus(
    val color: Color,
    val label: String
) {
    Ongoing(Color(65, 126, 201), "Ongoing"),
    NotStarted(Color(128, 128, 128), "Not Started"),
    Completed(Color(2, 138, 15), "Completed"), ;

    val key: String = name
        .replace(Regex("(?<!^)([A-Z])"), "-$1")
        .lowercase()

    companion object {
        private val keyLookup by lazy { entries.associateBy { it.key } }
        fun fromString(state: String?) = state?.trim()?.lowercase()?.let { keyLookup[it] }
    }
}