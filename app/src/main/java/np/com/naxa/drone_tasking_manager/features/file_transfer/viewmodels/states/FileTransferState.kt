package np.com.naxa.drone_tasking_manager.features.file_transfer.viewmodels.states

import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.File

/**
 * Sealed class representing the various states of a file download and transfer process.
 */
sealed class FileTransferState {
    /**
     * Represents the idle state where no download or transfer is in progress.
     */
    data object Idle : FileTransferState()

    /**
     * Represents the state of the download and transfer process when the destination directories have been listed.
     *
     * @param directory The selected directory through saf.
     */
    data class SafDirectorySelected(val directory: DocumentFile) : FileTransferState()

    /**
     * Represents the transferring state with the current progress of the transfer.
     *
     * @param progress A float value representing the percentage of the transfer completed (0.0 to 1.0).
     */
    data class Transferring(val progress: Float) : FileTransferState()

    /**
     * Represents an error that occurred during the transfer process.
     *
     * @param error A string describing the error that occurred.
     */
    data class TransferError(val error: String) :
        FileTransferState()

    /**
     * Represents the transfer completed state indicating that the transfer process has finished successfully.
     */
    data object TransferCompleted : FileTransferState()
}