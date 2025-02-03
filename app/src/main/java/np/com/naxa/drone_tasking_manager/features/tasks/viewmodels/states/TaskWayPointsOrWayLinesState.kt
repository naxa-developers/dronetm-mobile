package np.com.naxa.drone_tasking_manager.features.tasks.viewmodels.states

import com.google.gson.JsonObject

/**
 * Represents the state of loading and processing Task Waypoints or Waylines data.
 *
 * This sealed class encapsulates the different states that the process of retrieving and handling
 * either Task Waypoints or Waylines can be in. It can be in an idle state, successfully loaded
 * with data, in an error state, or in a loading state.
 */
sealed class TaskWayPointsOrWayLinesState {
    data object Idle : TaskWayPointsOrWayLinesState()
    data class Success(val jsonObject: JsonObject, val isWayPoints: Boolean) :
        TaskWayPointsOrWayLinesState()
    data class Error(val message: String) : TaskWayPointsOrWayLinesState()
    data object Loading : TaskWayPointsOrWayLinesState()
}