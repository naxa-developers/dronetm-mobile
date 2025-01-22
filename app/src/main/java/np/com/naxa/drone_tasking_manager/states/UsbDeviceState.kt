package np.com.naxa.drone_tasking_manager.states

import android.hardware.usb.UsbDevice

/**
 * Represents the state of the USB device list.
 */
sealed class UsbDeviceState {

    /**
     * Represents the Waiting state for the usb device connection.
     */
    data object WaitingToConnect : UsbDeviceState()

    /**
     * Represents the Success state for the usb device connection.
     */
    data class Connected(val device: UsbDevice, val stable: Boolean = false) : UsbDeviceState()

    /**
     * Represents the error state when there is an issue connecting the USB device.
     *
     * @param message A string containing the error message describing the issue.
     */
    data class Error(val message: String) : UsbDeviceState()
}