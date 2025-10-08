package np.com.naxa.drone_tasking_manager.features.download_and_transfer.handlers

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.os.StatFs
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.database.getIntOrNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import np.com.naxa.drone_tasking_manager.utils.randomWord
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit


/**
 * Sealed class representing the result of a download operation.
 * It can be one of three states: Progress, Success, or Error.
 */
sealed class DownloadResult {

    /**
     * Represents the progress of the download.
     *
     * @property progress A float value indicating the percentage of the download completed (0.0 to 1.0).
     */
    data class Progress(val progress: Float) : DownloadResult()

    /**
     * Represents a successful download.
     *
     * @property file The downloaded file.
     */
    data class Success(val file: File) : DownloadResult()

    /**
     * Represents an error that occurred during the download.
     *
     * @property message A string containing the error message.
     */
    data class Error(val message: String) : DownloadResult()
}

/**
 * Represents different types of errors that can occur during a download process.
 */
sealed class DownloadError : Exception() {

    /**
     * Error indicating that storage access was denied.
     *
     * @property message A detailed message about the error.
     */
    data class StorageAccessDenied(override val message: String) : DownloadError()

    /**
     * Error indicating that there is insufficient space to complete the download.
     *
     * @property message A detailed message about the error.
     */
    data class InsufficientSpace(override val message: String) : DownloadError()

    /**
     * Error indicating a network-related issue occurred during the download.
     *
     * @property message A detailed message about the error.
     */
    data class NetworkError(override val message: String) : DownloadError()

    /**
     * Error indicating an unknown issue occurred during the transfer.
     *
     * @property message A detailed message about the error.
     */
    data class UnknownError(override val message: String) : DownloadError()
}

class FileDownloadHandler(private val context: Context) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private var tempFiles = mutableListOf<File>()

    /**
     * Downloads a file from the specified URL and emits the download progress and result.
     *
     * @param url The URL of the file to be downloaded.
     * @return A [Flow] emitting [DownloadResult] which can be either progress updates or the final result.
     */
    fun download(url: String): Flow<DownloadResult> = flow {
        try {
            // Attempt to download the file with retries
            val response = downloadWithRetry(url)

            // Check if the response is successful
            if (!response.isSuccessful) {
                throw DownloadError.NetworkError("Server returned ${response.code}")
            }

            // Get the content length of the response
            val contentLength = response.body?.contentLength() ?: -1
            // Check if there is enough space to download the file
            if (contentLength > 0 && !checkAvailableSpace(contentLength)) {
                throw DownloadError.InsufficientSpace("Not enough space to download file")
            }

            // Create a temporary file to store the downloaded content
            val tempFile = createTempFile(context, url)

            // Get the response body
            val body = response.body
            if (body.contentLength() == 0L) {
                emit(DownloadResult.Error("Empty response"))
                return@flow
            }

            var bytesWritten = 0L

            // Write the downloaded content to the temporary file
            FileOutputStream(tempFile).use { output ->
                body.byteStream().use { input ->
                    val buffer = ByteArray(BUFFER_SIZE)
                    var bytes = input.read(buffer)

                    // Read and write the content in chunks
                    while (bytes >= 0) {
                        output.write(buffer, 0, bytes)
                        bytesWritten += bytes
                        bytes = input.read(buffer)

                        // Emit progress of the download
                        val progress = if (contentLength > 0) {
                            bytesWritten.toFloat() / contentLength.toFloat()
                        } else {
                            -1f
                        }
                        emit(DownloadResult.Progress(progress))
                    }
                }
            }

            emit(DownloadResult.Success(tempFile))

        } catch (e: DownloadError) {
            emit(DownloadResult.Error(e.message ?: "Download error"))
        } catch (e: IOException) {
            emit(DownloadResult.Error("Network error: ${e.localizedMessage}"))
        } catch (e: Exception) {
            emit(DownloadResult.Error("Unexpected error: ${e.localizedMessage}"))
        } finally {
            // Clean up resources
            cleanup()
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Creates a temporary file in the application's cache directory with the original file extension.
     *
     * This function generates a temporary file with a prefix of "download_",
     * preserves the original file extension, and adds it to the list of temporary files for management.
     *
     * @param context The context used to access the application's cache directory.
     * @param url The URL of the file being downloaded to extract the file extension.
     * @return The created temporary File object.
     */
    private fun createTempFile(context: Context, url: String): File {
        // Extract the file extension from the URL
        val extension = Uri.parse(url).lastPathSegment?.substringAfterLast('.', "")

        // Create a temp file with the original extension
        val file = if (!extension.isNullOrBlank()) {
            File.createTempFile("download_", ".$extension", context.cacheDir)
        } else {
            // Fallback to default temp file creation if no extension is found
            File.createTempFile("download_", null, context.cacheDir)
        }

        tempFiles.add(file)
        return file
    }

    /**
     * Cleans up temporary files created during the operation.
     *
     * This function iterates over a list of temporary files and attempts to delete each one.
     * If a file exists, it is deleted. Any exceptions that occur during the deletion process
     * are caught, and a failure to clean up is logged (though the logging implementation is not shown here).
     * After attempting to delete all files, the list of temporary files is cleared.
     */
    private fun cleanup() {
        tempFiles.forEach { file ->
            try {
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                // Log cleanup failure
            }
        }
        tempFiles.clear()
    }

    /**
     * Downloads a resource from the specified URL with a retry mechanism.
     * This function attempts to download the resource up to a specified number of retries
     * in case of failure, implementing an exponential backoff strategy between attempts.
     *
     * @param url The URL of the resource to download.
     * @param maxRetries The maximum number of retry attempts (default is 3).
     * @return A Response object containing the result of the download.
     * @throws IOException if the download fails after the maximum number of attempts.
     */
    private suspend fun downloadWithRetry(
        url: String,
        maxRetries: Int = 3
    ): Response = withContext(Dispatchers.IO) {
        var lastException: Exception? = null
        repeat(maxRetries) { attempt ->
            try {
                val request = Request.Builder().url(url).build()
                return@withContext client.newCall(request).execute()
            } catch (e: Exception) {
                lastException = e
                if (attempt < maxRetries - 1) {
                    delay(1000L * (attempt + 1)) // Exponential backoff
                }
            }
        }
        throw lastException ?: IOException("Download failed after $maxRetries attempts")
    }

    /**
     * Initiates a file download using the Android DownloadManager and provides callbacks for various stages of the download process.
     *
     * @param url The URL of the file to be downloaded.
     * @param maxRetries The maximum number of retry attempts if the download fails. Default is 3.
     * @param onInitiated A callback invoked when the download is initiated.
     * @param onProgress A callback invoked to report download progress, providing a progress value between 0 and 1.
     * @param onSuccess A callback invoked when the download is successful, providing the URI of the downloaded file.
     * @param onError A callback invoked when an error occurs during the download, providing an error message.
     */
    suspend fun downloadWithDownloadManager(
        url: String,
        maxRetries: Int = 3,
        onInitiated: () -> Unit = {},
        onProgress: (progress: Float) -> Unit = {},
        onSuccess: (uri: Uri) -> Unit = {},
        onError: (error: String) -> Unit = {}
    ) {
        onInitiated.invoke()
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
        val uri = Uri.parse(url)
        val filename = filenameIfUrlNotContainsIt(url)
        val request = DownloadManager.Request(uri)
        request.setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS,
            "DroneTM/${filename.split(".").first()}/$filename"
        )

        CoroutineScope(Dispatchers.IO).launch {
            var retries = 0
            var downloadId: Long?
            var downloadFileUri: Uri? = null

            while (retries < maxRetries) {
                downloadId = downloadManager?.enqueue(request)
                var success = false
                var error = "Unable to download"

                while (true) {
                    val query = DownloadManager.Query().setFilterById(downloadId!!)
                    val cursor: Cursor? = downloadManager?.query(query)
                    if (cursor != null && cursor.moveToFirst()) {
                        val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                        val status =
                            if (statusIndex >= 0) cursor.getInt(statusIndex) else DownloadManager.STATUS_FAILED

                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            success = true
                            val uriStringIndex =
                                cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                            val uriString =
                                if (uriStringIndex >= 0) cursor.getString(uriStringIndex) else null
                            uriString?.let {
                                downloadFileUri = Uri.parse(it)
                            }
                            break
                        } else if (status == DownloadManager.STATUS_FAILED) {
                            val reasonIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
                            val errorCode =
                                if (statusIndex >= 0) cursor.getIntOrNull(reasonIndex) else null

                            error = when (errorCode) {
                                DownloadManager.ERROR_FILE_ALREADY_EXISTS -> "File already exists"
                                DownloadManager.ERROR_CANNOT_RESUME -> "Cannot resume download"
                                DownloadManager.ERROR_DEVICE_NOT_FOUND -> "Device not found"
                                DownloadManager.ERROR_FILE_ERROR -> "File error"
                                DownloadManager.ERROR_INSUFFICIENT_SPACE -> "Insufficient space"
                                DownloadManager.ERROR_HTTP_DATA_ERROR -> "HTTP data error"
                                DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> "Unhandled HTTP code"
                                DownloadManager.ERROR_TOO_MANY_REDIRECTS -> "Too many redirects"
                                DownloadManager.PAUSED_QUEUED_FOR_WIFI -> "Paused for Wi-Fi"
                                DownloadManager.PAUSED_UNKNOWN -> "Paused for unknown reason"
                                else -> "Download failed"
                            }

                            break
                        }

                        val bytesDownloadedSoFarColumnIndex =
                            cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                        val bytesDownloaded =
                            if (bytesDownloadedSoFarColumnIndex >= 0) cursor.getInt(
                                bytesDownloadedSoFarColumnIndex
                            ) else null

                        val totalSizeColumnIndex =
                            cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                        val totalBytes =
                            if (totalSizeColumnIndex >= 0) cursor.getInt(totalSizeColumnIndex) else null

                        if (totalBytes != null && totalBytes > 0) {
                            val progress = (bytesDownloaded?.toFloat() ?: 0F) / totalBytes.toFloat()
                            onProgress.invoke(progress)
                        }
                    }
                    cursor?.close()
                    delay(100L)
                }

                if (success) {
                    // Download successful
                    if (downloadFileUri != null) {
                        onSuccess.invoke(downloadFileUri!!)
                    } else {
                        onError.invoke(error)
                    }
                    break
                } else {
                    // Retry if download failed
                    retries++
                    if (retries == 3) {
                        // Handle max retries reached
                        // You can show a notification or a UI message to the user
                        // that the download has failed after max retries
                        onError.invoke(error)
                    }
                }
            }
        }
    }


    /**
     * Checks if there is enough available space in the external storage.
     *
     * This function retrieves the available bytes in the external storage directory
     * and compares it with the specified file size to determine if there is sufficient
     * space for the file.
     *
     * @param fileSize The size of the file to be checked in bytes.
     * @return True if there is enough available space; false otherwise.
     */
    private fun checkAvailableSpace(fileSize: Long): Boolean {
        val stats = StatFs(context.getExternalFilesDir(null)?.path)
        val availableBytes = stats.availableBytes
        return availableBytes > fileSize
    }

    /**
     * Generates a filename for a URL if it doesn't already contain one.
     *
     * This function attempts to derive a filename in the following order:
     * 1. From the URL's file extension
     * 2. From the content type of an HTTP connection
     * 3. Fallback to a random filename with .tmp extension
     *
     * @param url The URL to generate a filename for
     * @return A String containing the generated filename with appropriate extension
     * @throws Exception if there are network or IO issues, caught internally
     */
    private suspend fun filenameIfUrlNotContainsIt(url: String): String =
        withContext(Dispatchers.IO) {
            try {
                var extension = MimeTypeMap.getFileExtensionFromUrl(url)
                if (!extension.isNullOrBlank()) {
                    return@withContext Uri.parse(url).lastPathSegment
                        ?: (15.randomWord() + ".$extension")
                }

                val connection = URL(url).openConnection() as HttpURLConnection
                connection.connect()
                val mimeType = connection.contentType

                Log.d(TAG, "filenameIfUrlNotContainsIt: $mimeType")

                extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "kmz"
                "${15.randomWord()}.$extension"
            } catch (e: Exception) {
                Log.e(TAG, "filenameIfUrlNotContains: ${e.message}", e)
                15.randomWord() + ".tmp"
            }
        }


    companion object {
        private const val BUFFER_SIZE = 8192
        private const val TAG = "DownloadFileManager"
    }
} 