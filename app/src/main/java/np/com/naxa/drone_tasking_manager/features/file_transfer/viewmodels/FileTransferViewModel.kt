package np.com.naxa.drone_tasking_manager.features.file_transfer.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import np.com.naxa.drone_tasking_manager.features.file_transfer.handler.FileTransferHandler
import np.com.naxa.drone_tasking_manager.features.file_transfer.handler.TransferResult
import np.com.naxa.drone_tasking_manager.features.file_transfer.viewmodels.events.FileTransferEvents
import np.com.naxa.drone_tasking_manager.features.file_transfer.viewmodels.states.FileTransferState
import java.io.File
import javax.inject.Inject

@HiltViewModel
class FileTransferViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {

    /**
     * Handles file transfers, such as sending and receiving files.
     */
    private val _transferHandler by lazy { FileTransferHandler(context) }

    /**
     * Represents the different states of task details.
     */
    private val _transferState =
        MutableStateFlow<FileTransferState>(FileTransferState.Idle)
    val transferState = _transferState.asStateFlow()

    /**
     * Handles events related to file transfer.
     *
     * This function receives a [FileTransferEvents] and performs the corresponding action.
     *
     * @param event The [FileTransferEvents] to handle.
     *
     * @see FileTransferEvents
     */
    fun triggerEvent(event: FileTransferEvents) {
        when (event) {
            is FileTransferEvents.TransferInitiated -> {
                startTransfer(event.file, event.destinationUri)
            }
        }
    }

    /**
     * Updates the current state of a file transfer.
     *
     * This function takes a [FileTransferState] object and emits it to the
     * [_transferState] MutableStateFlow, effectively updating the state
     * observed by any collectors of this flow.  The update is performed
     * within the ViewModel's scope using `viewModelScope.launch`, ensuring
     * that the coroutine is tied to the lifecycle of the ViewModel.
     *
     * @param state The new [FileTransferState] to be emitted. This represents
     *              the current status of the file transfer.
     *
     */
    fun updateTransferState(state: FileTransferState) {
        viewModelScope.launch {
            _transferState.emit(state)
        }
    }


    /**
     * Starts the file transfer process from a given file to a specified destination URI.
     *
     * This function initiates the transfer of a file to a destination URI. It uses the
     * `_transferHandler` to perform the actual transfer and updates the `_transferState`
     * accordingly based on the transfer's progress, success, or failure.
     *
     * The function operates within the `viewModelScope`, allowing it to be automatically
     * cancelled when the associated ViewModel is cleared.
     *
     * @param file The file to be transferred.
     * @param destinationUri The URI representing the destination where the file should be transferred.
     *
     * The function updates the `_transferState` which is a Flow that can be observed to get live
     * update on the transfer process and its current status.
     *
     */
    private fun startTransfer(file: File, destinationUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _transferState.emit(FileTransferState.Transferring(0f))
            _transferHandler.transfer(file, destinationUri).collect { result ->
                when (result) {
                    is TransferResult.Progress -> {
                        _transferState.emit(FileTransferState.Transferring(result.progress))
                    }

                    is TransferResult.Success -> {
                        _transferState.emit(FileTransferState.TransferCompleted)
                    }

                    is TransferResult.Error -> {
                        _transferState.emit(
                            FileTransferState.TransferError(
                                result.message,
                            )
                        )
                    }
                }

            }
        }
    }


}