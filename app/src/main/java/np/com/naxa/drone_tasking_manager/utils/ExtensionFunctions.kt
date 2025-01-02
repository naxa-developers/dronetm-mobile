package np.com.naxa.drone_tasking_manager.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import np.com.naxa.drone_tasking_manager.ui.navigation.Routes
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

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
 * Extension function to convert a given uri instance into an ImageBitmap if of image file.
 *
 * This function attempts to decode the file using BitmapFactory and then converts the resulting
 * Bitmap into an ImageBitmap. If any exceptions occur during the decoding process, null is returned.
 *
 * @return The resulting ImageBitmap, or null if an exception occurred during decoding.
 */
fun Uri.asImageBitmap(): ImageBitmap? {
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

/**
 * Extension function that checks if a [Uri] represents a valid document file.
 *
 * @param context The context needed to check the URI
 * @return `true` if this URI represents a document file, `false` otherwise or if an error occurs
 */
fun Uri?.isDocumentUri(context: Context): Boolean {
    return try {
        if (this == null) {
            false
        } else {
            DocumentFile.isDocumentUri(context, this)
        }
    } catch (e: Exception) {
        false
    }
}


/**
 * Suspends the coroutine and asynchronously creates a PNG bitmap representing the folder tree
 * of a KMZ file.
 *
 * This function operates on a background thread using `Dispatchers.IO`.
 *
 * @param bitmapWidth The desired width of the generated bitmap in pixels.
 *         Default is 1024
 * @param bitmapHeight The desired height of the generated bitmap in pixels.
 *        Defaults to 500.
 * @param textColor The desired text color of the generated bitmap.
 *        Defaults to Black.
 * @param backgroundColor The desired background color of the generated bitmap.
 *        Defaults to White.
 * @param textSize The desired text size of the generated bitmap.
 *        Defaults to 24f.
 * @return A File object representing the generated PNG image file containing the folder tree
 *         if the file is a KMZ and the processing is successful. Returns null otherwise.
 * @throws Exception If there are any errors during processing, such as file I/O errors or invalid KMZ format.
 */
suspend fun File.folderTreeBitmapIfKmzFile(
    bitmapWidth: Int = 1024,
    bitmapHeight: Int = 500,
    textColor: Color = Color.Black,
    backgroundColor: Color = Color.White,
    textSize: Float = 24f
): ImageBitmap? =
    withContext(Dispatchers.IO) {
        try {
            if (extension.lowercase() != "kmz") return@withContext null

            val zis = ZipInputStream(FileInputStream(this@folderTreeBitmapIfKmzFile))
            val fileTree = mutableMapOf<String, MutableList<String>>()

            var entry: ZipEntry? = zis.nextEntry
            while (entry != null) {
                val fileName = entry.name
                val folders = fileName.split("/")
                val file = folders.last()

                val folderPath = folders.dropLast(1).joinToString("/")
                fileTree.getOrPut(folderPath) { mutableListOf() }.add(file)

                entry = zis.nextEntry
            }
            zis.closeEntry()
            zis.close()

            val bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint()

            // Set the background color
            paint.color = backgroundColor.toColor()
            canvas.drawRect(0f, 0f, bitmapWidth.toFloat(), bitmapHeight.toFloat(), paint)

            // Set the font and text color
            paint.color = textColor.toColor()

            // Draw the folder and file hierarchy
            var y = if (fileTree.keys.firstOrNull()?.isBlank() == true) 0f else 40f
            fun drawFolderTree(folder: String, indent: String) {
                paint.textSize = textSize
                paint.typeface = Typeface.DEFAULT_BOLD
                canvas.drawText("$indent$folder", 100f, y, paint)
                y += 40f

                fileTree[folder]?.forEach { file ->
                    paint.textSize = textSize * 0.9f
                    paint.typeface = Typeface.DEFAULT
                    canvas.drawText("$indent- $file", 100f, y, paint)
                    y += 30f
                }

                fileTree.keys.filter { it.startsWith("$folder/") }.forEach { subfolder ->
                    val subfolderName = subfolder.substringAfter("$folder/").substringBefore("/")
                    drawFolderTree("$folder/$subfolderName", "$indent  ")
                }

                y += 20f
            }

            drawFolderTree(fileTree.keys.firstOrNull() ?: "", "")

            bitmap.asImageBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


/**
 * Converts a Compose UI `Color` to an `android.graphics.Color`.
 */
fun Color.toColor(): Int {
    return android.graphics.Color.argb(
        (this.alpha * 255).toInt(),
        (this.red * 255).toInt(),
        (this.green * 255).toInt(),
        (this.blue * 255).toInt()
    )
}