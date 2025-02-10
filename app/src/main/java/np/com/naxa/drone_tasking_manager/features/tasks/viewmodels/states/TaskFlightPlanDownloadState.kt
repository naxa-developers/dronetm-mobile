package np.com.naxa.drone_tasking_manager.features.tasks.viewmodels.states

import java.io.File

sealed class TaskFlightPlanDownloadState {
    data object Idle: TaskFlightPlanDownloadState()
    data class Downloading(val progress: Float) : TaskFlightPlanDownloadState()
    data class DownloadError(val error: String, val url: String? = null) : TaskFlightPlanDownloadState()
    data class DownloadCompleted(val file: File) : TaskFlightPlanDownloadState()
}