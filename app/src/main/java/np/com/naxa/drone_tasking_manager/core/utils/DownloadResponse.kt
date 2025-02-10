package np.com.naxa.drone_tasking_manager.core.utils

import java.io.File


/**
 * Represents the response of a download operation.
 * This sealed class encapsulates the possible outcomes of a download:
 * - [Completed]: The download finished successfully, providing the downloaded file.
 * - [Error]: The download failed, providing an error message.
 * - [Downloading]: The download is in progress, providing the current progress as a float.
 */
sealed class DownloadResponse {

    /**
     * Represents a successful download completion.
     *
     * This data class indicates that a file download has finished without any errors.
     * It holds a reference to the [File] object representing the downloaded file.
     *
     * @property file The [File] object representing the successfully downloaded file.
     * @constructor Creates a new Completed object with the specified downloaded file.
     */
    data class Completed(val file: File) : DownloadResponse()


    /**
     * Represents an error response from a download operation.
     *
     * This class encapsulates an error message that describes the reason for the download failure.
     * It is a concrete implementation of the [DownloadResponse] sealed class, specifically
     * representing a failure state.
     *
     * @property message A human-readable message explaining the error that occurred during the download.
     *                  This message should provide enough context for the user to understand why the
     *                  download failed and potentially how to resolve it.
     */
    data class Error(val message: String) : DownloadResponse()


    /**
     * Represents the state of a download in progress.
     *
     * This class indicates that a download operation is currently underway.
     * It carries information about the current progress of the download.
     *
     * @property progress A float value between 0.0 and 1.0 (inclusive) representing the download progress.
     *                   0.0 indicates the download has just started or hasn't started yet.
     *                   1.0 indicates the download is complete.
     *                   Values between 0.0 and 1.0 represent the percentage of data downloaded.
     * @constructor Creates a Downloading object with the specified progress.
     * @see DownloadResponse
     */
    class Downloading(val progress: Float) : DownloadResponse()
}