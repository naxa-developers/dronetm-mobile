package np.com.naxa.drone_tasking_manager.states

import android.net.Uri
import java.io.File

/**
 * Sealed class representing the various states of a file download and transfer process.
 */
sealed class DownloadAndTransferState {

    /**
     * Represents the idle state where no download or transfer is in progress.
     */
    data class Idle(val showUrlInputBox: Boolean = false) : DownloadAndTransferState()

    /**
     * Represents the downloading state with the current progress of the download.
     *
     * @param progress A float value representing the percentage of the download completed (0.0 to 1.0).
     */
    data class Downloading(val progress: Float) : DownloadAndTransferState()

    /**
     * Represents an error that occurred during the download process.
     *
     * @param error A string describing the error that occurred.
     */
    data class DownloadError(val error: String, val url: String? = null) : DownloadAndTransferState()

    /**
     * Represents the download completed state indicating that the download process has finished successfully.
     */
    data class DownloadCompleted(val file: File) : DownloadAndTransferState()

    /**
     * Represents the transferring state with the current progress of the transfer.
     *
     * @param progress A float value representing the percentage of the transfer completed (0.0 to 1.0).
     */
    data class Transferring(val progress: Float) : DownloadAndTransferState()

    /**
     * Represents an error that occurred during the transfer process.
     *
     * @param error A string describing the error that occurred.
     */
    data class TransferError(val error: String, val file: File?, val destinationUri: Uri? = null) : DownloadAndTransferState()

    /**
     * Represents the transfer completed state indicating that the transfer process has finished successfully.
     */
    data object TransferCompleted : DownloadAndTransferState()
}