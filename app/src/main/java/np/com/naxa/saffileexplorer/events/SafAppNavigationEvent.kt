package np.com.naxa.saffileexplorer.events

import android.hardware.usb.UsbDevice
import androidx.compose.material3.SnackbarDuration

sealed class SafAppNavigationEvent {
    /**
     * Event triggered when the user tries to navigate to home screen.
     */
    data object OnNavigateToHome : SafAppNavigationEvent()

    /**
     * Event triggered when the user tries to navigate to download and transfer screen.
     */
    data class OnNavigateToDownloadAndTransfer(val device: UsbDevice? = null) :
        SafAppNavigationEvent()

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
    ) : SafAppNavigationEvent()

}