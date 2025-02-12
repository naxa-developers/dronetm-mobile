package np.com.naxa.drone_tasking_manager.features.download_and_transfer.viewmodels

import android.net.Uri
import androidx.core.net.toFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import np.com.naxa.drone_tasking_manager.features.download_and_transfer.viewmodels.states.DownloadAndTransferState
import np.com.naxa.drone_tasking_manager.features.download_and_transfer.handlers.FileDownloadHandler
import np.com.naxa.drone_tasking_manager.features.file_transfer.handler.FileTransferHandler
import np.com.naxa.drone_tasking_manager.features.file_transfer.handler.TransferResult
import java.io.File

class DownloadAndTransferFileViewModel(
    private val downloadHandler: FileDownloadHandler,
    private val transferHandler: FileTransferHandler
) : ViewModel() {

    private val _downloadAndTransferState =
        MutableStateFlow<DownloadAndTransferState>(DownloadAndTransferState.Idle(false))
    val downloadAndTransferState: StateFlow<DownloadAndTransferState> =
        _downloadAndTransferState.asStateFlow()

    // variable to store file
    private var _file: File? = null
    val file: File? = _file

    fun setFile(file: File?) {
        _file = file
    }


    /**
     * Initiates the download process for a given URL and updates the download and transfer state accordingly.
     *
     * @param url The URL of the file to be downloaded. Defaults to a sample image URL if not provided.
     *            If the URL is empty, the function returns immediately without performing any action.
     */
    fun startDownload(url: String = "https://dev-dronetm.s3.ap-south-1.amazonaws.com/flight_plan_1.kmz") {
        // Update the state to indicate that downloading has started with 0% progress
        // update(DownloadAndTransferState.Downloading(0F))

        // If url is empty
        if (url.isEmpty()) {
            update(DownloadAndTransferState.DownloadError("Invalid URL"))
            return
        }

        // Else initiate download
        viewModelScope.launch {

            // Start the download process and collect the results
            // downloadHandler.download(url).collect { result ->
            //     when (result) {
            //         is DownloadResult.Progress -> {
            //             // Update the state with the current download progress
            //             update(DownloadAndTransferState.Downloading(result.progress))
            //         }

            //         is DownloadResult.Success -> {
            //             // Update the state to indicate that the download is complete
            //             update(DownloadAndTransferState.DownloadCompleted(result.file))
            //         }

            //         is DownloadResult.Error -> {
            //             // Update the state to indicate that an error occurred during download
            //             update(DownloadAndTransferState.DownloadError(result.message, url))
            //         }
            //     }
            // }

            // Attempt to download the file with retries
            downloadHandler.downloadWithDownloadManager(
                url,
                maxRetries = 3,
                onInitiated = {
                    update(DownloadAndTransferState.Downloading(0F))
                },
                onProgress = { progress ->
                    update(DownloadAndTransferState.Downloading(progress))
                },
                onSuccess = { file ->
                    update(DownloadAndTransferState.DownloadCompleted(file.toFile()))
                },
                onError = { error ->
                    update(DownloadAndTransferState.DownloadError(error, url))
                }
            )
        }
    }

    /**
     * Initiates the file transfer process and updates the transfer state accordingly.
     *
     * @param file The file to be transferred. This function launches a coroutine to handle the transfer asynchronously.
     *             It updates the transfer state to reflect the progress, success, or error of the transfer operation.
     */
    fun startTransfer(file: File? = null, destinationUri: Uri) {
        // Update the state to indicate that transferring has started with 0% progress
        update(DownloadAndTransferState.Transferring(0F))

        // If file is null
        if (file == null && _file == null) {
            update(DownloadAndTransferState.TransferError("File is null", null, destinationUri))
            return
        }

        // Else initiate transfer
        viewModelScope.launch {
            // Start the transfer process and collect the results

            transferHandler.transfer((file ?: _file)!!, destinationUri).collect { result ->
                when (result) {
                    is TransferResult.Progress -> {
                        // Update the state with the current transfer progress
                        update(DownloadAndTransferState.Transferring(result.progress))
                    }

                    is TransferResult.Success -> {
                        // Update the state to indicate that the transfer is complete
                        update(DownloadAndTransferState.TransferCompleted)
                    }

                    is TransferResult.Error -> {
                        // Update the state to indicate that an error occurred during transfer
                        update(
                            DownloadAndTransferState.TransferError(
                                result.message,
                                file ?: _file,
                                destinationUri
                            )
                        )
                    }
                }
            }
        }
    }

    /**
     * Updates the current download and transfer state to indicate that a download is in progress.
     *
     * @param state The current state of the download and transfer process, which is used to update
     *              the internal state
     */
    fun update(state: DownloadAndTransferState) {
        viewModelScope.launch {
            if (state is DownloadAndTransferState.Idle) {
                setFile(null)
            }
            
            _downloadAndTransferState.emit(state)
        }
    }

    companion object {
        /**
         * A factory for creating instances of [ViewModel] subclasses.
         * This factory is used to instantiate the [DownloadAndTransferFileViewModel]
         * with the necessary dependencies.
         */
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            /**
             * Creates a new instance of the specified [ViewModel] class.
             *
             * @param modelClass The class of the [ViewModel] to create.
             * @param extras Additional parameters for creating the [ViewModel].
             * @return An instance of the specified [ViewModel] class.
             * @throws IllegalArgumentException if the [ViewModel] cannot be created.
             */
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                // Get the Application object from extras
                val application = checkNotNull(extras[APPLICATION_KEY])

                // Get DownloadFileManager
                val downloadManager =
                    FileDownloadHandler(application.applicationContext)

                // Get TransferFileManager
                val transferManager =
                    FileTransferHandler(application.applicationContext)

                return DownloadAndTransferFileViewModel(downloadManager, transferManager) as T
            }
        }
    }
}