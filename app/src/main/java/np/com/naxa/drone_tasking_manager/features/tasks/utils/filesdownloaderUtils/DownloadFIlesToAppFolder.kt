package np.com.naxa.drone_tasking_manager.features.tasks.utils.filesdownloaderUtils

import android.content.Context
import android.webkit.MimeTypeMap
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/**
 * Downloads a file from a given URL and saves it to the app's internal storage.
 *
 * The file is saved within the following directory structure:
 *  - app's files directory
 *    - downloads
 *      - TiffFiles
 *        - projectName (specified by the user)
 *          - fileName (specified by the user)
 *
 * If the necessary directories ("downloads", "TiffFiles", "projectName") do not exist,
 * they will be created automatically.
 *
 * @param context The application context. Used to access the app's file directory and show toasts.
 * @param fileUrl The URL of the file to download.
 * @param fileName The name to give the downloaded file.
 * @param projectName The name of the project the file belongs to. This will be used to create a
 *                    subfolder within the "TiffFiles" directory.
 *
 * @throws java.net.MalformedURLException If the fileUrl is not a valid URL.
 * @throws java.io.IOException If an I/O error occurs during the download or file saving process.
 */
fun downloadFileToAppFolder(
    context: Context,
    fileUrl: String,
    fileName: String,
    projectName: String,
    viewModelScope: CoroutineScope
) {
    val url = URL(fileUrl)
    val connection = url.openConnection()
    connection.connect()

    val inputStream = connection.getInputStream()
    val downloadsFolder = File(context.filesDir, "downloads") // Create a 'downloads' folder in your app's directory.
    val tiffFilesFolder = File(downloadsFolder, "TiffFiles")
    val projectFolder = File(tiffFilesFolder, projectName)

    val fileExtension = getFileExtensionFromUrl(fileUrl)

    // Create folders if they don't exist
    if (!projectFolder.exists()) {
        projectFolder.mkdirs()
    }

    val file = File(projectFolder, "$fileName.$fileExtension")
    val outputStream = FileOutputStream(file)

    inputStream.use { input ->
        outputStream.use { output ->
            input.copyTo(output)
        }
    }

    viewModelScope.launch(Dispatchers.Main) {

        Toast.makeText(context, "File downloaded to app folder: ${file.absolutePath}", Toast.LENGTH_LONG).show()
    }
}


fun isFileExists(context: Context, projectName: String): Boolean {
    val projectFolder = File(context.filesDir, "downloads/TiffFiles/$projectName")
    val fileName: String = "$projectName.tif"
    val file = File(projectFolder, fileName)

    return file.exists() // Returns true if the file exists, false otherwise
}

fun getFileExtensionFromUrl(url: String): String? {
    val decodedUrl = URLDecoder.decode(url, StandardCharsets.UTF_8.name())
    val extension = MimeTypeMap.getFileExtensionFromUrl(decodedUrl)
    return if (extension.isNullOrEmpty()) null else extension
}

fun getFileFromSavedLocation(context: Context, projectName: String): File? {
    val filePath = File(context.filesDir, "downloads/TiffFiles/$projectName")
    val fileName: String = "$projectName.tif"
    val file = File(filePath, fileName)

    return if (file.exists()) {
        file // Return the file object if it exists
    } else {
        null // Return null if the file does not exist
    }
}