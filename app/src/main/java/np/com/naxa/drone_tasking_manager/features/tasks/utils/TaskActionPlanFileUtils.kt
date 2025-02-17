package np.com.naxa.drone_tasking_manager.features.tasks.utils

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import np.com.naxa.drone_tasking_manager.utils.getFileFromMediaStorage
import java.io.File

/**
 * `TaskActionPlanFileUtils` provides utility functions for managing files associated with task action plans,
 * primarily focusing on handling downloaded files. It includes functionality to retrieve downloaded files
 * and check if a specific file has already been downloaded. This object handles the differences in
 * file access mechanisms between Android versions, utilizing the MediaStore API for Android 10 (API 29)
 * and above, and the traditional file system for older versions.
 */
object TaskActionPlanFileUtils {
    /**
     * Retrieves a downloaded file associated with a given task ID.
     *
     * This function handles different storage mechanisms based on the Android version.
     * - For Android 10 (API level 29) and above, it utilizes the MediaStore API to query the Downloads collection.
     * - For older Android versions, it checks for the file directly in the external Downloads directory.
     *
     * The file is expected to be located in the "DroneTM" subfolder within the user's Downloads directory
     * (or, for newer versions, within the MediaStore's "Download/DroneTM/" relative path).
     *
     * @param context The application context.
     * @param taskId The ID of the task associated with the downloaded file (should match the file name).
     *               For Android Q and above the file name should match the `taskId` exactly.
     * @return A [File] object representing the downloaded file if found, or null if the file is not found.
     * If the file was found in MediaStore the returned file will be a temporary file in the cache dir.
     *
     */
    fun downloadedFileOf(context: Context, taskId: String): File? {
        val fileName = "${taskId}_flight_plan.kmz"

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For Android 10 and above (Using MediaStore API)
            val uri = MediaStore.Downloads.EXTERNAL_CONTENT_URI

            val projection = arrayOf(MediaStore.Downloads._ID, MediaStore.Downloads.DISPLAY_NAME)
            val selection =
                "${MediaStore.Downloads.RELATIVE_PATH} = ? AND ${MediaStore.Downloads.DISPLAY_NAME} = ?"
            val relativePath = "Download/DroneTM/$taskId/"

            val selectionArgs = arrayOf(relativePath, fileName)

            context.contentResolver.query(uri, projection, selection, selectionArgs, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val id =
                            cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID))
                        val fileUri = ContentUris.withAppendedId(uri, id)

                        fileUri.getFileFromMediaStorage(context, fileName)
                    } else {
                        null
                    }
                }
        } else {
            // For Android 9 and below (Using File API)
            val downloadsDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, "DroneTM/$taskId/$fileName")
            if (file.exists()) {
                file
            } else {
                null
            }
        }
    }


    /**
     * Checks if a file associated with a given task ID has already been downloaded.
     *
     * This function checks if a file related to a specific task has been previously downloaded to the device's
     * Downloads directory. It handles different Android versions using either the MediaStore API (for Android 10 and above)
     * or the File API (for Android 9 and below).
     *
     * @param context The application context.
     * @param taskId The unique identifier of the task. This is used as the filename (without extension) for the downloaded file,
     *               and it will be in the "Download/DroneTM/" for Android 10+ or "Download/DroneTM/$taskId" for Android 9 and below.
     * @return `true` if a file with the given task ID exists in the Downloads directory, `false` otherwise. It also return false if an exception occurs during the check.
     *
     * @throws SecurityException if the application lacks the necessary permissions to access the Downloads directory or query the MediaStore.
     *
     * @see MediaStore
     * @see Environment
     * @see File
     * @see ContentResolver
     */
    fun alreadyDownloaded(context: Context, taskId: String): Boolean {
        val fileName = "${taskId}_flight_plan.kmz"

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val uri = MediaStore.Downloads.EXTERNAL_CONTENT_URI

                val projection =
                    arrayOf(MediaStore.Downloads._ID, MediaStore.Downloads.DISPLAY_NAME)
                val selection =
                    "${MediaStore.Downloads.RELATIVE_PATH} = ? AND ${MediaStore.Downloads.DISPLAY_NAME} = ?"
                val relativePath = "Download/DroneTM/$taskId/"

                val selectionArgs = arrayOf(relativePath, fileName)

                context.contentResolver.query(uri, projection, selection, selectionArgs, null)
                    .use { cursor ->
                        return (cursor?.count ?: 0) > 0
                    }
            } else {
                // For Android 9 and below (Using File API)
                val downloadsDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDir, "DroneTM/$taskId/$fileName")
                return file.exists()
            }
        } catch (e: Exception) {
            return false
        }
    }
}