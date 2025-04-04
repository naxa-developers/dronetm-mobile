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
 * Downloads a file from a given URL and saves it to the app's internal storage under a specified project folder.
 *
 * This function performs the following steps:
 * 1. Opens a connection to the provided URL.
 * 2. Creates necessary directories within the app's internal storage:
 *    - `filesDir/downloads/TiffFiles/{projectName}`
 * 3. Extracts the file extension from the URL.
 * 4. Creates a file within the project folder using the provided filename and extracted extension.
 * 5. Downloads the file from the URL and saves it to the newly created file.
 * 6. Displays a toast message confirming the download and providing the file's absolute path.
 *
 * @param context The application context, used for accessing the app's internal storage and displaying toasts.
 * @param fileUrl The URL of the file to download.
 * @param fileName The desired name for the downloaded file (without the extension).
 * @param projectName The name of the project, used to create a subfolder for the file within the "TiffFiles" directory.
 * @param viewModelScope The coroutine scope used to launch the toast message on the main thread.
 *
 * @throws Exception if there are issues with URL connection, file creation, or file writing.
 *
 * Example Usage:
 * ```kotlin
 *  val fileUrl = "https://example.com/path/to/myimage.tiff"
 *  val fileName = "myImage"
 *  val projectName = "Project1"
 *  downloadFileToAppFolder(context, fileUrl, fileName, projectName, viewModelScope)
 * ```
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


/**
 * Checks if a TIFF file associated with a given project name exists in the application's internal storage.
 *
 * The function checks for the existence of a file named "[projectName].tif" within a specific directory structure:
 * `internal_storage/downloads/TiffFiles/[projectName]/[projectName].tif`.
 *
 * @param context The application context, used to access the internal files directory.
 * @param projectName The name of the project. This name will be used as both the folder name and the base file name (e.g., "myProject" -> "myProject.tif").
 * @return `true` if the file exists at the specified location, `false` otherwise.
 */
fun isFileExists(context: Context, projectName: String): Boolean {
    val projectFolder = File(context.filesDir, "downloads/TiffFiles/$projectName")
    val fileName: String = "$projectName.tif"
    val file = File(projectFolder, fileName)

    return file.exists() // Returns true if the file exists, false otherwise
}

/**
 * Extracts the file extension from a given URL.
 *
 * This function decodes the URL using UTF-8 and then uses the `MimeTypeMap`
 * to extract the file extension. It handles cases where the extension
 * might be absent or the URL is malformed.
 *
 * @param url The URL string from which to extract the file extension.
 * @return The file extension (e.g., "jpg", "pdf", "png") or `null` if no
 *         extension is found or if the URL is invalid/cannot be decoded.
 * @throws IllegalArgumentException if the character set name is invalid
 *
 * Example Usage:
 * ```
 * val url1 = "https://www.example.com/image.jpg"
 * val extension1 = getFileExtensionFromUrl(url1) // Returns "jpg"
 *
 * val url2 = "https://www.example.com/document.pdf?param=value"
 * val extension2 = getFileExtensionFromUrl(url2) // Returns "pdf"
 *
 * val url3 = "https://www.example.com/noextension"
 * val extension3 = getFileExtensionFromUrl(url3) // Returns null
 * ```
 */
fun getFileExtensionFromUrl(url: String): String? {
    val decodedUrl = URLDecoder.decode(url, StandardCharsets.UTF_8.name())
    val extension = MimeTypeMap.getFileExtensionFromUrl(decodedUrl)
    return if (extension.isNullOrEmpty()) null else extension
}

/**
 * Retrieves a TIFF file from the application's internal storage based on the provided project name.
 *
 * This function searches for a specific TIFF file within the application's internal storage directory.
 * The file is expected to be located within the following path structure:
 * `[app's internal files directory]/downloads/TiffFiles/[projectName]/[projectName].tif`.
 *
 * @param context The application context used to access the internal storage.
 * @param projectName The name of the project, which is used to determine the file's name and directory.
 * @return A File object representing the TIFF file if it exists, otherwise null.
 *
 * @throws java.lang.SecurityException if the application does not have permission to access internal storage.
 * @sample
 *   val myProjectFile: File? = getFileFromSavedLocation(applicationContext, "MyProject")
 *   if (myProjectFile != null) {
 *       // Process the file
 *       println("File found: ${myProjectFile.absolutePath}")
 *   } else {
 *       println("File not found.")
 *   }
 */
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


/**
 * Retrieves the absolute file path of a file within the app's internal storage.
 *
 * This function constructs a file path based on the provided project name and file name,
 * relative to the app's internal files directory. It then checks if the file exists
 * at that location.
 *
 * @param context The application context, used to access the internal files directory.
 * @param projectName The name of the project, used as a subdirectory within "downloads/TiffFiles/".
 * @param fileName The name of the file to locate.
 * @return The absolute file path as a String if the file exists; otherwise, returns null.
 *
 * @throws NullPointerException If the context provided is null.
 *
 * Example Usage:
 * ```
 * val filePath = getFilePath(applicationContext, "MyProject", "image.tiff")
 * if (filePath != null) {
 *     println("File path: $filePath")
 * } else {
 *     println("File not found.")
 * }
 * ```
 */
fun getFilePath(context: Context, projectName: String): String? {
    val filePath = File(context.filesDir, "downloads/TiffFiles/$projectName")
    val fileName: String = "$projectName.tif"
    val file = File(filePath, fileName)

    return if (file.exists()) {
        file.absolutePath // Return the file's absolute path if it exists
    } else {
        null // Return null if the file does not exist
    }
}