package np.com.naxa.saffileexplorer.states

import android.hardware.usb.UsbDevice

/**
 * Represents the state of the USB device list.
 */
sealed class UsbDeviceListState {

    /**
     * Represents the loading state when the USB devices are being fetched.
     */
    data object Loading : UsbDeviceListState()

    /**
     * Represents the success state containing a list of detected USB devices.
     *
     * @param devices A list of [UsbDevice] objects that were successfully retrieved.
     */
    data class Success(val devices: List<UsbDevice>) : UsbDeviceListState()

    /**
     * Represents the error state when there is an issue retrieving the USB devices.
     *
     * @param message A string containing the error message describing the issue.
     */
    data class Error(val message: String) : UsbDeviceListState()
}