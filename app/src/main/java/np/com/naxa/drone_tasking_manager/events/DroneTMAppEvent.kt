package np.com.naxa.drone_tasking_manager.events

import android.hardware.usb.UsbDevice
import java.io.File

sealed class DroneTMAppEvent {
    /**
     * Event triggered when the user requests storage permission.
     */
    data object OnStoragePermissionRequested : DroneTMAppEvent()

    /**
     * Event triggered when the app is searching for USB devices..
     */
    data object OnUsbDeviceSearchRequested : DroneTMAppEvent()

    /**
     * Event triggered when the user initiates a download.
     *
     * @param device The USB device associated with the download (optional).
     * @param url The URL of the file to download.
     */
    data class OnDownloadInitiated(val device: UsbDevice?, val url: String) :
        DroneTMAppEvent()

    /**
     * Event triggered when the user initiates a file transfer.
     *
     * @param device The USB device associated with the transfer (optional).
     * @param file The file to be transferred.
     */
    data class OnTransferInitiated(val device: UsbDevice?, val file: File?) :
        DroneTMAppEvent()

    /**
     * Event triggered when the user requests access to a directory on the SAF (Storage Access Framework).
     *
     * @param device The USB device associated with the SAF directory (optional).
     * @param file The file representing the SAF directory.
     */
    data class OnDroneTMDirectoryAccessRequested(val device: UsbDevice?, val file: File?) :
        DroneTMAppEvent()

    /**
     * Event triggered when the user requests to pick a file.
     */
    data object OnFilePickerRequested : DroneTMAppEvent()

}