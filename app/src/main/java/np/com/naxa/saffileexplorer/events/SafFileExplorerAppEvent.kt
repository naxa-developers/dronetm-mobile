package np.com.naxa.saffileexplorer.events

import android.hardware.usb.UsbDevice
import java.io.File

sealed class SafFileExplorerAppEvent {
    /**
     * Event triggered when the user requests storage permission.
     */
    data object OnStoragePermissionRequested : SafFileExplorerAppEvent()

    /**
     * Event triggered when the user clicks on a specific USB device.
     *
     * @param device The USB device that was clicked.
     */
    data class OnUsbDeviceClick(val device: UsbDevice) : SafFileExplorerAppEvent()

    /**
     * Event triggered when the app is requested to fetch USB devices.
     */
    data object OnUsbDevicesFetchRequested : SafFileExplorerAppEvent()

    /**
     * Event triggered when the user initiates a download.
     *
     * @param device The USB device associated with the download (optional).
     * @param url The URL of the file to download.
     */
    data class OnDownloadInitiated(val device: UsbDevice?, val url: String) :
        SafFileExplorerAppEvent()

    /**
     * Event triggered when the user initiates a file transfer.
     *
     * @param device The USB device associated with the transfer (optional).
     * @param file The file to be transferred.
     */
    data class OnTransferInitiated(val device: UsbDevice?, val file: File?) :
        SafFileExplorerAppEvent()

    /**
     * Event triggered when the user requests access to a directory on the SAF (Storage Access Framework).
     *
     * @param device The USB device associated with the SAF directory (optional).
     * @param file The file representing the SAF directory.
     */
    data class OnSafDirectoryAccessRequested(val device: UsbDevice?, val file: File?) :
        SafFileExplorerAppEvent()

    /**
     * Event triggered when the user requests to pick a file.
     */
    data object OnFilePickerRequested : SafFileExplorerAppEvent()

}