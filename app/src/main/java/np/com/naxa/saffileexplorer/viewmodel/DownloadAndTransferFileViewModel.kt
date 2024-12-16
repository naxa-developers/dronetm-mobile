package np.com.naxa.saffileexplorer.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toFile
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import np.com.naxa.saffileexplorer.states.DownloadAndTransferState
import np.com.naxa.saffileexplorer.utils.FileDownloadHandler
import np.com.naxa.saffileexplorer.utils.FileTransferHandler
import np.com.naxa.saffileexplorer.utils.TransferResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class DownloadAndTransferFileViewModel(
    private val downloadHandler: FileDownloadHandler,
    private val transferHandler: FileTransferHandler
) : ViewModel() {

    private val _downloadAndTransferState =
        MutableStateFlow<DownloadAndTransferState>(DownloadAndTransferState.Idle)
    val downloadAndTransferState: StateFlow<DownloadAndTransferState> =
        _downloadAndTransferState.asStateFlow()

    // variable to store file
    private var _file: File? = null

    fun setFile(file: File?) {
        _file = file
    }


    /**
     * Initiates the download process for a given URL and updates the download and transfer state accordingly.
     *
     * @param url The URL of the file to be downloaded. Defaults to a sample image URL if not provided.
     *            If the URL is empty, the function returns immediately without performing any action.
     */
    fun startDownload(url: String = "https://www-cdn.djiits.com/dps/5919beda1853e73f3db4c2d3ea0f695c.jpg") {
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
            // downloadManager.download(url).collect { result ->
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
    fun startTransfer(file: File? = null, destinationUri: Uri, context: Context) {
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

            //log the destination uri's details
            Log.d("DownloadAndTransferFileViewModel", "startTransfer: ${destinationUri.path}")
            Log.d("DownloadAndTransferFileViewModel", "startTransfer: ${destinationUri.userInfo}")
            Log.d("DownloadAndTransferFileViewModel", "startTransfer: ${destinationUri.scheme}")
            Log.d(
                "DownloadAndTransferFileViewModel",
                "startTransfer: ${getDocumentName(context, destinationUri)}"
            )


            val renamedFile = renameFile(
                (file ?: _file)!!,
                "${
                    getDocumentName(
                        context,
                        destinationUri
                    )
                }.${getFileSuffix((file ?: _file)?.name!!)}"
            )

            transferHandler.transfer((renamedFile ?: file ?: _file)!!, destinationUri).collect { result ->
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


    /**
     * Retrieves the name of a document from a tree URI using DocumentFile.
     *
     * @param context The Android context used to create the DocumentFile
     * @param uri The tree URI of the document whose name needs to be retrieved
     * @return The name of the document as a String, or null if:
     *         - The DocumentFile cannot be created from the URI
     *         - The document name is not accessible
     */
    private fun getDocumentName(context: Context, uri: Uri): String {
        val documentFile = DocumentFile.fromTreeUri(context, uri)
        return documentFile!!.name ?: ""
    }


    /**
     * Attempts to rename a file to a new filename while keeping it in the same directory.
     *
     * @param oldFile The existing File object to be renamed
     * @param newFileName The new name to give the file (just the filename, not the full path)
     * @return Boolean indicating success (true) or failure (false) of the rename operation.
     *         Returns false if:
     *         - The destination already exists
     *         - The source file cannot be modified
     *         - The operation is not supported by the file system
     */
    private fun renameFile(oldFile: File, newFileName: String): File? {

        // Check if the old file exists
        if (!oldFile.exists()) {
            Log.d("DownloadAndTransferFileViewModel", "File does not exist")
            return null
        }

        // Create a new file in the same directory with the new name
        val newFile = File(oldFile.parent, newFileName)

        return if (oldFile.renameTo(newFile)) {

            Log.d("DownloadAndTransferFileViewModel", "renameFile Name: ${newFile.name}")
            Log.d(
                "DownloadAndTransferFileViewModel",
                "renameFile Suffix: ${getFileSuffix(newFile.name)}"
            )

            newFile // Return the new file if renaming is successful
        } else {
            null // Return null if renaming fails
        }
    }


    /**
     * Extracts the file suffix (extension) from the given file name.
     *
     * @param fileName The name of the file to extract the suffix from.
     * @return The file suffix as a String, or an empty string if no suffix is found.
     */
    private fun getFileSuffix(fileName: String): String {
        val suffixName =
            fileName.substringAfterLast('.', "") // Returns empty string if no extension
        Log.d("DownloadAndTransferFileViewModel", "getFileSuffix: $suffixName")
        return suffixName;
    }
}