package np.com.naxa.drone_tasking_manager.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.storage.StorageManager
import android.provider.DocumentsContract
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.io.FileInputStream
import java.io.IOException


/**
 * Sealed class representing the result of a transfer operation.
 * It can be one of three states: Progress, Success, or Error.
 */
sealed class TransferResult {

    /**
     * Represents the progress of the download.
     *
     * @property progress A float value indicating the percentage of the transfer completed (0.0 to 1.0).
     */
    data class Progress(val progress: Float) : TransferResult()

    /**
     * Represents a successful transfer.
     *
     * @property file The transferred file.
     */
    data class Success(val file: File) : TransferResult()

    /**
     * Represents an error that occurred during the transfer.
     *
     * @property message A string containing the error message.
     */
    data class Error(val message: String) : TransferResult()
}

/**
 * Sealed class representing various errors that can occur during a transfer operation.
 * This class extends Exception to provide specific error types related to transfer failures.
 */
sealed class TransferError : Exception() {

    /**
     * Represents an error when access to storage is denied.
     *
     * @property message A string containing the error message.
     */
    data class StorageAccessDenied(override val message: String) : TransferError()

    /**
     * Represents an error when there is insufficient space for the transfer.
     *
     * @property message A string containing the error message.
     */
    data class InsufficientSpace(override val message: String) : TransferError()

    /**
     * Represents an error when the file being transferred is corrupted.
     *
     * @property message A string containing the error message.
     */
    data class FileCorrupted(override val message: String) : TransferError()

    /**
     * Represents an unknown error that occurred during the transfer.
     *
     * @property message A string containing the error message.
     */
    data class UnknownError(override val message: String) : TransferError()
}

private const val TAG = "TransferFileManager"

class FileTransferHandler(private val context: Context) {

    private val djiAppPath = "Android/data/com.itheamc.djiapp/files"


    fun transfer(file: File, destinationUri: Uri): Flow<TransferResult> = flow {
        try {

            // Checking if has all required permission related to storage
            // if (!hasRequiredAccess()) {
            //     throw TransferError.StorageAccessDenied("Storage access denied")
            // }

            // Checking if file is valid
            if (!validateDownloadedFile(file)) {
                throw TransferError.FileCorrupted("Invalid file")
            }

            // Emitting state
            emit(TransferResult.Progress(0F))

            // Attempt to download the file with retries
            transferUsingSAF(
                file,
                destinationUri,
                onInitiated = {
                    emit(TransferResult.Progress(0F))
                },
                onProgress = {
                    emit(TransferResult.Progress(it))
                },
                onCompleted = {
                    emit(TransferResult.Success(file))
                },
                onError = {
                    emit(TransferResult.Error(it))
                },
            )

        } catch (e: TransferError) {
            emit(TransferResult.Error(e.message ?: "Download error"))
        } catch (e: IOException) {
            emit(TransferResult.Error("Transfer error: ${e.localizedMessage}"))
        } catch (e: Exception) {
            emit(TransferResult.Error("Unexpected error: ${e.localizedMessage}"))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Validates the downloaded file to ensure it meets certain criteria.
     *
     * This function checks if the file exists, has a non-zero length,
     * is readable, and is not corrupted.
     *
     * @param file The file to be validated.
     * @return True if the file is valid; false otherwise.
     */
    private fun validateDownloadedFile(file: File): Boolean {
        return file.exists() &&
                file.length() > 0 &&
                file.canRead() &&
                !isFileCorrupted(file)
    }

    /**
     * Checks if the specified file is corrupted.
     *
     * This function attempts to read the file to determine if it is corrupted.
     * If the file can be read without throwing an exception, it is considered valid.
     * If an exception occurs during the read attempt, the file is deemed corrupted.
     *
     * @param file The file to be checked for corruption.
     * @return True if the file is corrupted; false otherwise.
     */
    private fun isFileCorrupted(file: File): Boolean {
        return try {
            // Basic corruption check by trying to read the file
            file.inputStream().use { it.read() }
            false
        } catch (e: Exception) {
            true
        }
    }


    private fun transferUsingDirectAccess(sourceFile: File): Boolean {
        return try {
            val djiDir = File(context.getExternalFilesDir(null)?.parentFile, djiAppPath)
            if (!djiDir.exists() && !djiDir.mkdirs()) return false

            val destFile = File(djiDir, sourceFile.name)
            sourceFile.copyTo(destFile, overwrite = true)
            true
        } catch (e: Exception) {
            false
        }
    }


    /**
     * Transfers a file using the Storage Access Framework (SAF) with progress tracking and error handling.
     *
     * This function performs the following operations:
     * 1. Creates a new file at the destination location
     * 2. Copies the source file content to the destination using buffered streaming
     * 3. Provides progress updates during the transfer
     * 4. Verifies the transfer completion
     *
     * @param sourceFile The source file to be transferred
     * @param destinationUri The URI of the destination directory where the file will be copied
     * @param onInitiated Callback function invoked when transfer is initiated (optional)
     * @param onProgress Callback function providing transfer progress as float between 0-1 (optional)
     * @param onCompleted Callback function invoked when transfer successfully completes (optional)
     * @param onError Callback function invoked with error message if transfer fails (optional)
     *
     * The function uses a buffer size of up to 1MB for efficient transfer of large files while
     * maintaining reasonable memory usage. Transfer progress is calculated and reported based on
     * bytes transferred and total file size.
     *
     * @throws Exception if file creation or transfer fails
     */
    private suspend fun transferUsingSAF(
        sourceFile: File,
        destinationUri: Uri,
        onInitiated: suspend () -> Unit = {},
        onProgress: suspend (Float) -> Unit = {},
        onCompleted: suspend () -> Unit = {},
        onError: suspend (String) -> Unit = {}
    ) {
        try {

            // On initiated the transfer task
            onInitiated()

            // Get or create the document file
            val documentFile = DocumentFile.fromTreeUri(context, destinationUri)

            // Creating the file name
            val fileName = deleteFileIfAlreadyExistAndGetName(sourceFile, documentFile)

            // Create the destination file
            val destFile = documentFile?.createFile("*/*", fileName)

            // Copying content to the destination file
            destFile?.uri?.let { uri ->
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    FileInputStream(sourceFile).use { inputStream ->
                        try {
                            val totalBytes = sourceFile.length()
                            val bufferSize =
                                totalBytes.coerceAtMost(1024 * 1024).toInt() // Max 1 MB
                            val buffer = ByteArray(bufferSize)
                            var bytesTransferred = 0L
                            var length: Int
                            while (inputStream.read(buffer).also { length = it } > 0) {
                                outputStream.write(
                                    buffer,
                                    0,
                                    length
                                )
                                bytesTransferred += length

                                val progress = bytesTransferred.toFloat() / totalBytes.toFloat()

                                // Update progress
                                onProgress(progress)
                            }
                        } catch (e: Exception) {
                            // On Error the transfer task
                            onError(e.message ?: "Unknown error")
                        }
                    }
                }
            }

            if (destFile == null) {
                onError("Unable to create file")
            }

            if (destFile!!.exists() && destFile.isFile) {
                try {
                    sourceFile.delete()
                } catch (e: Exception) {
                    Log.e(TAG, "transferUsingSAF: ${e.message}", e)
                }
                onCompleted.invoke()
            } else {
                onError("Unable to transfer file")
            }

        } catch (e: Exception) {
            onError(e.message ?: "Error copying file")
        }
    }

    /**
     * Gets a filename by checking and deleting any existing file with the same name.
     *
     * This function takes a source file and a document file directory, checks if a file
     * with the same name already exists in the target directory, and if found, deletes it.
     * This ensures there won't be naming conflicts when creating new files.
     *
     * @param sourceFile The source file whose name will be used
     * @param documentFile The target DocumentFile directory where to check for existing files
     * @return The filename string. Returns the original filename if:
     *         - The original filename is blank
     *         - No existing file with the same name is found
     *         - An exception occurs during the process
     */
    private fun deleteFileIfAlreadyExistAndGetName(
        sourceFile: File,
        documentFile: DocumentFile?
    ): String {
        try {

            // If destination directory is null, return original file name
            if (documentFile == null) return sourceFile.name

            // Getting extension of the source file
            val extension = sourceFile.name.substringAfterLast(".", missingDelimiterValue = "kmz")

            // Getting destination directory name
            val dirName = documentFile.name

            // Creating new file name with same name as destination directory with the source file extension
            val fileName = "${dirName}.$extension"

            // Checking if file with same name already exist
            val existingFile = documentFile.findFile(fileName) ?: return fileName

            // If file is already exist, delete it
            if (existingFile.exists()) existingFile.delete()

            return fileName
        } catch (e: Exception) {
            return sourceFile.name
        }
    }

    /**
     * Generates a unique filename for a file in a given document directory.
     *
     * This function attempts to create a unique filename by appending incremental numbers
     * to the base filename if a file with the original name already exists. It will try
     * up to 20 iterations before settling on a final name.
     *
     * @param sourceFile The original file for which to generate a unique name
     * @param documentFile The DocumentFile representing the directory to check for existing files
     * @return A unique filename string. If the original filename is blank, returns it unchanged.
     *         If no unique name is found after 20 attempts, returns the last attempted name.
     */
    private fun getUniqueFileName(sourceFile: File, documentFile: DocumentFile?): String {
        try {
            val fileName = sourceFile.name
            if (fileName.isNullOrBlank()) return fileName

            val segments = fileName.split(".")
            val baseName = segments.first()
            val extension = segments.last()

            // Check original name first
            if (documentFile?.findFile(fileName)?.exists() != true) {
                return fileName
            }

            // Try up to 25 iterations
            for (i in 1..25) {
                val newFileName = "${baseName}_$i.$extension"
                if (documentFile.findFile(newFileName)?.exists() != true) {
                    return newFileName
                }
            }

            // If no unique name found after 25 attempts, return last attempted name
            return "${baseName}_25.$extension"
        } catch (e: Exception) {
            Log.e(TAG, "getUniqueFileName: ${e.message}", e)
            return sourceFile.name
        }
    }

    /**
     * Creates a new file in the specified folder using the Storage Access Framework.
     *
     * @param destinationUri The URI of the parent folder where the file will be created
     * @param fileName The name of the file to be created
     * @param mimeType The MIME type of the file to be created
     * @return The URI of the created file, or null if creation fails
     */
    private fun createFileInDestination(
        destinationUri: Uri,
        fileName: String,
        mimeType: String = "*/*"
    ): Uri? {
        return try {
            DocumentsContract.createDocument(
                context.contentResolver,
                destinationUri,
                mimeType,
                fileName
            )
        } catch (e: Exception) {
            Log.e("FileCreation", "Error creating file", e)
            null
        }
    }

    companion object {
        /**
         * Creates an intent to open a document tree for accessing a specific directory on a connected device.
         *
         * This function constructs an intent that allows the user to select a directory
         * for the application to access. The initial URI dynamically points to the target directory
         * on the connected device based on the Android version and device storage paths.
         *
         * @param context activity context.
         * @return An Intent configured to open the document tree for the specified directory.
         */
        fun createDirectoryAccessIntent(context: Context): Intent {

            // Getting the device path
            val devicePath =
                getSpecificDevicePath(context)

            // Create the initial URI pointing to the target directory
            val initialUri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // For Android 8.0 and above
                DocumentsContract.buildTreeDocumentUri(
                    "com.android.externalstorage.documents",
                    devicePath
                )
            } else {
                // For older versions (fallback to default tree URI as specific paths may not work)
                Uri.parse("content://com.android.externalstorage.documents/tree/$devicePath")
            }

            // Create and configure the intent
            return Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                if (initialUri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri)
                }

                // Add flags to allow extended access permissions
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
            }
        }


        /**
         * Retrieves a list of connected device paths available on the system.
         *
         * This function checks both internal and external storage devices,
         * including USB drives and other connected media, and returns their paths.
         *
         * @param context The application context.
         * @return A list of paths for the connected devices.
         */
        private fun getConnectedDevicePaths(context: Context): List<String> {
            val storageManager =
                context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
            val storageVolumes = storageManager.storageVolumes

            val paths = mutableListOf<String>()
            for (volume in storageVolumes) {
                val path = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    volume.directory?.absolutePath
                } else {
                    // Handle for Android versions below R (API 30)
                    runCatching {
                        val javaFile =
                            File(volume.javaClass.getMethod("getPath").invoke(volume) as String)
                        javaFile.absolutePath
                    }.onFailure { e ->
                        // Log the exception for debugging purposes
                        Log.e("FilePathError", "Failed to get file path", e)
                    }.getOrNull()
                }

                if (path != null) {
                    paths.add(path)
                    Log.d(
                        TAG,
                        "getConnectedDevicePaths(isPrimary: ${volume.isPrimary}):  $path"
                    )
                }
            }
            return paths
        }

        /**
         * Retrieves the specific device path for the application data directory.
         *
         * This function iterates over the connected device paths and checks for the existence
         * of a specific directory structure. If the expected directory is found, its path is returned.
         *
         * @param context The context used to access system services.
         * @return The path to the specific device directory if it exists, or null if not found.
         */
        private fun getSpecificDevicePath(context: Context): String? {
            val connectedPaths = getConnectedDevicePaths(context)
            for (path in connectedPaths) {
                // Check for expected directory structure
                val specificPath = "$path/Android/data/dji.go.v5/files/waypoint"
                if (File(specificPath).exists()) {
                    return specificPath
                }
            }
            return null
        }

        /**
         * Retrieves the URI of the DJI waypoint directory from the given context and URI.
         * Searches through possible directory structures to find the waypoint folder.
         *
         * @param context the Android context to use for accessing the file system
         * @param uri the URI of the root directory to start the search from
         * @return the URI of the DJI waypoint directory, or null if it could not be found
         */
        fun djiWaypointUri(context: Context, uri: Uri): Uri? {
            return try {
                val rootDirectory = DocumentFile.fromTreeUri(context, uri) ?: return null

                // Define possible directory paths to search
                val possiblePaths = listOf(
                    listOf("Android", "data", "dji.go.v5", "files", "waypoint"),
                    listOf("data", "dji.go.v5", "files", "waypoint"),
                    listOf("dji.go.v5", "files", "waypoint"),
                    listOf("files", "waypoint")
                )

                // Search through each possible path
                for (path in possiblePaths) {
                    var currentDir: DocumentFile? = rootDirectory
                    var pathValid = true

                    // Navigate through each directory in the path
                    for (dirName in path) {
                        currentDir = currentDir?.findFile(dirName)
                        if (currentDir == null) {
                            pathValid = false
                            break
                        }
                    }

                    // If we successfully traversed the entire path
                    if (pathValid && currentDir != null) {
                        // Get folders excluding "capability" and "map_preview"
                        // val validFolders = currentDir.listFiles().filter {
                        //     it.isDirectory &&
                        //             !setOf("capability", "map_preview").contains(it.name)
                        // }

                        // Return the first valid folder's URI if it exists
                        // validFolders.firstOrNull()?.uri?.let { return it }

                        return currentDir.uri
                    }
                }

                null
            } catch (e: Exception) {
                null
            }
        }

        // /**
        //  * Retrieves the URI of the DJI waypoint directory from the given context and URI.
        //  * Searches through possible directory structures to find the waypoint folder.
        //  *
        //  * @param context the Android context to use for accessing the file system
        //  * @param uri the URI of the root directory to start the search from
        //  * @return the URI of the DJI waypoint directory, or null if it could not be found
        //  */
        // fun djiWaypointUri(context: Context, uri: Uri): Uri? {
        //     return try {
        //         val rootDirectory = DocumentFile.fromTreeUri(context, uri) ?: return null

        //         // Define possible directory paths to search
        //         val possiblePaths = listOf(
        //             listOf("Android", "data", "dji.go.v5", "files", "waypoint"),
        //             listOf("data", "dji.go.v5", "files", "waypoint"),
        //             listOf("dji.go.v5", "files", "waypoint"),
        //             listOf("files", "waypoint")
        //         )

        //         // Search through each possible path
        //         for (path in possiblePaths) {
        //             var currentDir: DocumentFile? = rootDirectory
        //             var pathValid = true

        //             // Navigate through each directory in the path
        //             for (dirName in path) {
        //                 currentDir = currentDir?.findFile(dirName)
        //                 if (currentDir == null) {
        //                     pathValid = false
        //                     break
        //                 }
        //             }

        //             // If we successfully traversed the entire path
        //             if (pathValid && currentDir != null) {
        //                 // Get folders excluding "capability" and "map_preview"
        //                 val validFolders = currentDir.listFiles().filter {
        //                     it.isDirectory &&
        //                             !setOf("capability", "map_preview").contains(it.name)
        //                 }

        //                 // Return the first valid folder's URI if it exists
        //                 validFolders.firstOrNull()?.uri?.let { return it }
        //             }
        //         }

        //         null
        //     } catch (e: Exception) {
        //         null
        //     }
        // }
    }

}