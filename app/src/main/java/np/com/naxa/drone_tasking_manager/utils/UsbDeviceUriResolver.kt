package np.com.naxa.drone_tasking_manager.utils

import android.content.Context
import android.hardware.usb.UsbDevice
import android.net.Uri
import android.os.Environment
import android.os.storage.StorageManager
import android.util.Log
import java.io.File

/**
 * Resolves URIs for USB devices on Android systems.
 * Implements multiple fallback strategies to locate USB device mount points.
 *
 * @property context Android application context used for accessing system services
 */
class UsbDeviceUriResolver(private val context: Context) {

    companion object {
        private const val TAG = "UsbDeviceUriResolver"
        /**
         * Common mount points for detecting external storage or USB devices.
         */
        private val COMMON_MOUNT_POINTS = listOf(
            "/mnt/usb",
            "/storage/usb",
            "/mnt/media_rw",
            "/storage/external_storage",
            "/mnt/usb_storage",
            "/storage/usbdrive",
            "/mnt/usbdisk",
            "/mnt/expand",
            "/dev/bus/usb/001"
        )

        /**
         * System mount points typically used by Android.
         */
        private val SYSTEM_MOUNT_POINTS = listOf(
            "/storage",
            "/mnt",
            "/sdcard",
            "/mnt/sdcard",
            "/storage/emulated"
        )
    }

    /**
     * Attempts to get URI for a USB device using multiple fallback strategies.
     *
     * @param device The USB device to locate
     * @return Uri? The URI of the mounted USB device, or null if not found
     */
    fun getUsbDeviceUri(device: UsbDevice): Uri? = runCatching {
        // Strategy 1: Check external storage directories
        getExternalStorageUris(device).firstOrNull()
            ?: // Strategy 2: Scan common mount points
            COMMON_MOUNT_POINTS.asSequence()
                .mapNotNull { findUsbDeviceInMountPoint(device, it) }
                .firstOrNull()
            ?: // Strategy 3: Check system-mounted storage
            findSystemMountedUsb(device)?.let { Uri.fromFile(it) }
    }.onFailure {
        Log.e(TAG, "Error resolving USB device URI for ${device.deviceName}", it)
    }.getOrNull().also {
        if (it == null) {
            Log.w(TAG, "Could not find URI for USB device: ${device.deviceName}")
        }
    }

    /**
     * Gets URIs for external storage locations that match the USB device.
     *
     * @param device The USB device to match
     * @return List<Uri> List of matching URIs
     */
    private fun getExternalStorageUris(device: UsbDevice): List<Uri> =
        getExternalStorageDirectories()
            .filter { isMatchingUsbStorage(it, device) }
            .map { Uri.fromFile(it) }

    /**
     * Retrieves all available external storage directories.
     *
     * @return List<File> List of external storage directories
     */
    private fun getExternalStorageDirectories(): List<File> = buildList {
        add(Environment.getExternalStorageDirectory())
        runCatching {
            val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
            storageManager.javaClass.getMethod("getVolumes").invoke(storageManager)
                ?.let { volumes ->
                    (volumes as? List<*>)?.forEach { volume ->
                        volume?.javaClass?.getMethod("getPath")?.invoke(volume)?.let {
                            (it as? File)?.let { file -> add(file) }
                        }
                    }
                }
        }.onFailure {
            Log.e(TAG, "Error accessing storage volumes", it)
        }
    }

    /**
     * Determines if a storage directory matches the given USB device.
     *
     * @param storageDir Directory to check
     * @param device USB device to match against
     * @return Boolean true if the directory matches the USB device
     */
    private fun isMatchingUsbStorage(storageDir: File, device: UsbDevice): Boolean {
        return storageDir.name.contains("usb", ignoreCase = true) &&
                storageDir.canRead() &&
                storageDir.isDirectory &&
                matchesDeviceCharacteristics(storageDir, device)
    }

    /**
     * Matches device characteristics with storage properties
     */
    private fun matchesDeviceCharacteristics(storageDir: File, device: UsbDevice): Boolean {
        // Enhanced matching logic could include:
        return (storageDir.totalSpace > 0) && // Check if storage has capacity
                storageDir.name.contains(device.manufacturerName ?: "", ignoreCase = true) ||
                storageDir.name.contains(device.productId.toString(), ignoreCase = true)
    }

    /**
     * Finds USB device in a specific mount point.
     *
     * @param device USB device to locate
     * @param mountPoint Mount point to search
     * @return Uri? URI of the found device or null
     */
    private fun findUsbDeviceInMountPoint(device: UsbDevice, mountPoint: String): Uri? =
        File(mountPoint).takeIf { it.exists() && it.isDirectory }?.let { mountDir ->
            mountDir.listFiles()
                ?.firstOrNull { it.isDirectory && canAccessDirectory(it) }
                ?.let { Uri.fromFile(it) }
        }

    /**
     * Finds system-mounted USB storage.
     *
     * @param device USB device to locate
     * @return File? Found USB directory or null
     */
    private fun findSystemMountedUsb(device: UsbDevice): File? =
        SYSTEM_MOUNT_POINTS.asSequence()
            .map { File(it) } // Convert String to File
            .flatMap { it.listFiles()?.asSequence() ?: emptySequence() }
            .find { dir ->
                dir.isDirectory &&
                        dir.name.contains("usb", ignoreCase = true) &&
                        canAccessDirectory(dir) &&
                        matchesDeviceCharacteristics(dir, device)
            }


    /**
     * Checks if a directory can be accessed.
     *
     * @param directory Directory to check
     * @return Boolean true if directory is accessible
     */
    private fun canAccessDirectory(directory: File): Boolean =
        directory.exists() && directory.isDirectory && directory.canRead()
}
