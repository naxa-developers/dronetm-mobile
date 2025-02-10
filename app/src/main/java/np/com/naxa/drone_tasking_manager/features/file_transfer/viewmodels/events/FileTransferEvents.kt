package np.com.naxa.drone_tasking_manager.features.file_transfer.viewmodels.events

import android.net.Uri
import java.io.File

/**
 * Sealed class representing various events that can occur during a file transfer process.
 *
 * This class provides a structured way to handle different stages and occurrences
 * within a file transfer operation. Each subclass represents a specific event,
 * allowing for type-safe event handling.
 */
sealed class FileTransferEvents {

    /**
     * Represents an event indicating that a file transfer has been initiated.
     *
     * This event is emitted when the process of transferring a file has started.
     * It includes the [File] object representing the file being transferred.
     *
     * @property file The [File] object representing the file that is being transferred.
     * @property destinationUri The [Uri] representing the destination URI for the file transfer.
     *
     */
    data class TransferInitiated(val file: File, val destinationUri: Uri) : FileTransferEvents()
}