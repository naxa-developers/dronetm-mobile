package np.com.naxa.saffileexplorer.utils

import android.content.Context
import android.graphics.BitmapFactory
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import np.com.naxa.saffileexplorer.ui.navigation.Routes
import java.io.File
import java.io.FileOutputStream

inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getParcelable(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelable(key) as? T
}

/**
 * Extension function on UsbDevice to check if device is MTP Device
 */
fun UsbDevice.isMtpDevice(): Boolean {
    // Check if the device has an interface with class code 6 (Still image capture device)
    // Check if the device has an interface with class code 8 (USB Mass Storage)
    for (i in 0 until interfaceCount) {
        val usbInterface = getInterface(i)
        if (usbInterface.interfaceClass == 6) { // 6 is the class code for Still image capture devices (MTP/PTP)
            return true
        }
    }
    return false
}

/**
 * Extension function on UsbDevice to check if already has permission for starting communication with it
 */
fun UsbDevice.hasCommunicationPermission(usbManager: UsbManager): Boolean = try {
    usbManager.hasPermission(this)
} catch (e: Exception) {
    false
}


/**
 * Extension function to convert a given path string into a corresponding Routes enum value.
 *
 * This function checks the provided path against known route paths and returns the matching
 * Routes enum instance if found. If the path does not match any known routes, it returns null.
 *
 * @param path The path string to be converted to a Routes instance.
 * @return The corresponding Routes instance if the path matches, or null if no match is found.
 */
fun String.route(): Routes? {
    val routes = mutableMapOf<String, Routes>()
    Routes.entries.forEach { route ->
        routes[route.path] = route
    }

    return routes[this]
}

/**
 * Extension function to convert a given File instance into an ImageBitmap.
 *
 * This function attempts to decode the file using BitmapFactory and then converts the resulting
 * Bitmap into an ImageBitmap. If any exceptions occur during the decoding process, null is returned.
 *
 * @return The resulting ImageBitmap, or null if an exception occurred during decoding.
 */
fun File.asImageBitmap(): ImageBitmap? {
    return try {
        val options = BitmapFactory.Options()
        options.inSampleSize = 2 // This will reduce the image size by half
        val bitmap = BitmapFactory.decodeFile(path, options)
        bitmap?.asImageBitmap()
    } catch (e: Exception) {
        null
    }
}

/**
 * Extension function to generate a random word of specified length.
 *
 * This function creates a random string of lowercase letters with a length equal to
 * the integer value it's called on. Each character is randomly selected from the
 * range 'a' to 'z'.
 *
 * @receiver The integer that determines the length of the generated word
 * @return A string consisting of [Int] number of random lowercase letters
 */
fun Int.randomWord(): String {
    val chars = ('a'..'z')
    return (1..this)
        .map { chars.random() }
        .joinToString("")
}

/**
 * Converts a [DocumentFile] to a [File] object.
 *
 * @param context The context to use for accessing the file system.
 * @return The [File] object representing the document file, or `null` if the conversion failed.
 */
suspend fun DocumentFile.toFile(context: Context): File? = withContext(Dispatchers.IO) {
    if (!isFile || name == null) return@withContext null

    try {
        val file = File(context.cacheDir, name!!)
        val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null

        inputStream.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        file
    } catch (e: Exception) {
        null
    }
}
