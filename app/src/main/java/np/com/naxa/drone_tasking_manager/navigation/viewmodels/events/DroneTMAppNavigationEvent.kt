package np.com.naxa.drone_tasking_manager.navigation.viewmodels.events

import android.hardware.usb.UsbDevice
import androidx.compose.material3.SnackbarDuration

sealed class DroneTMAppNavigationEvent {
    /**
     * Event triggered when the user tries to navigate to home screen.
     */
    data object OnNavigateToHome : DroneTMAppNavigationEvent()

    /**
     * Event triggered when the user tries to navigate to download and transfer screen.
     */
    data class OnNavigateToDownloadAndTransfer(val device: UsbDevice? = null) :
        DroneTMAppNavigationEvent()

    /**
     * Event triggered when the user tries to navigate back to previous screen.
     */
    data object OnPopBackStack : DroneTMAppNavigationEvent()

    /**
     * Event triggered when the user tries to show snack bar
     */
    data class OnSnackBarShow(
        val message: String,
        val actionLabel: String? = null,
        val withDismissAction: Boolean = false,
        val duration: SnackbarDuration = SnackbarDuration.Short,
        val onDismissed: () -> Unit = {},
        val onActionPerformed: () -> Unit = {},
    ) : DroneTMAppNavigationEvent()

    /**
     * `OnNavigateToLogin` is a navigation event that signals a request to navigate to the login screen.
     *
     * This event is part of the `DroneTMAppNavigationEvent` hierarchy, used within the DroneTM application
     * to manage navigation state and transitions.  When this event is dispatched, it should trigger
     * the application's navigation mechanism to transition to the designated login view or screen.
     *
     * Typically, this event is dispatched when:
     *  - A user attempts to access a restricted area without being logged in.
     *  - The application determines that the user's authentication has expired.
     *  - The user explicitly clicks a "Login" button or link.
     *
     * The `OnNavigateToLogin` object itself does not contain any data; it solely represents the
     * intent to navigate.
     *
     * @see DroneTMAppNavigationEvent
     */
    data object OnNavigateToLogin : DroneTMAppNavigationEvent()


    /**
     * Represents a navigation event that triggers navigation to the Projects screen.
     *
     * This object is a concrete implementation of the [DroneTMAppNavigationEvent] sealed class.
     * It signifies an intention to navigate the user to the screen where they can view and manage their projects.
     *
     */
    data object OnNavigateToProjects : DroneTMAppNavigationEvent()


    /**
     * Represents a navigation event to the project detail screen.
     *
     * This event is dispatched when the user intends to view the details of a specific project.
     *
     * @property id The unique identifier of the project to navigate to.
     * @constructor Creates an instance of OnNavigateToProjectDetail with the given project ID.
     */
    data class OnNavigateToProjectDetail(val id: String) : DroneTMAppNavigationEvent()

    /**
     * Represents a navigation event to the task detail screen.
     *
     * This event is dispatched when the user intends to view the details of a specific task.
     *
     * @property id The unique identifier of the task to navigate to.
     *   This ID is used to fetch and display the relevant task data on the detail screen.
     * @constructor Creates an instance of OnNavigateToTaskDetail with the specified task ID.
     */
    data class OnNavigateToTaskDetail(val id: String) : DroneTMAppNavigationEvent()
}