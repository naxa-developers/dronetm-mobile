package np.com.naxa.drone_tasking_manager.features.file_transfer.views.screens

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.launch
import np.com.naxa.drone_tasking_manager.core.widgets.SafDirectoryAccessRequestDialog
import np.com.naxa.drone_tasking_manager.features.file_transfer.handler.FileTransferHandler
import np.com.naxa.drone_tasking_manager.features.file_transfer.viewmodels.events.FileTransferEvents
import np.com.naxa.drone_tasking_manager.features.file_transfer.viewmodels.states.FileTransferState
import np.com.naxa.drone_tasking_manager.features.file_transfer.views.widgets.FileTransferCompleteStateView
import np.com.naxa.drone_tasking_manager.features.file_transfer.views.widgets.FileTransferErrorStateView
import np.com.naxa.drone_tasking_manager.features.file_transfer.views.widgets.FileTransferIdleStateView
import np.com.naxa.drone_tasking_manager.features.file_transfer.views.widgets.FileTransferringStateView
import np.com.naxa.drone_tasking_manager.features.file_transfer.views.widgets.SafDirectorySelectedStateView
import np.com.naxa.drone_tasking_manager.local_providers.LocalFileTransferViewModel
import np.com.naxa.drone_tasking_manager.local_providers.LocalUsbDeviceViewModel
import java.io.File

@Composable
fun FileTransferScreen(
    modifier: Modifier = Modifier,
    filePath: String? = null,
    deviceId: Int? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val transferViewModel = LocalFileTransferViewModel.current
    var showSafAccessRequestDialog by remember { mutableStateOf(false) }

    val usbDeviceViewModel = LocalUsbDeviceViewModel.current
    var device: UsbDevice? by remember {
        mutableStateOf(null)
    }

    val file: File? by remember(filePath) {
        mutableStateOf(
            if (filePath != null) {
                try {
                    File(filePath)
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }
        )
    }

    val transferState by transferViewModel.transferState.collectAsState()

    LaunchedEffect(deviceId) {
        scope.launch {
            device = deviceId?.let { usbDeviceViewModel.deviceWithId(it) }
        }
    }

    val directoryAccessLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                try {
                    // Persist the permissions
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )

                    val destinationUri = FileTransferHandler.djiWaypointUri(
                        context,
                        uri
                    ) ?: uri

                    DocumentFile.fromTreeUri(context, destinationUri)?.let {
                        transferViewModel.updateTransferState(
                            FileTransferState.SafDirectorySelected(
                                it
                            )
                        )
                    }

                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        "Failed to access selected directory",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } else {
            Toast.makeText(context, "Storage access denied", Toast.LENGTH_LONG).show()
        }
    }


    @SuppressLint("QueryPermissionsNeeded")
    val requestDirectoryAccess = remember(file) {
        {
            scope.launch {
                try {
                    val intent = FileTransferHandler.createDirectoryAccessIntent(context)

                    // Check if there's a file manager that can handle our request
                    if (intent.resolveActivity(context.packageManager) != null) {
                        directoryAccessLauncher.launch(intent)
                    } else {
                        directoryAccessLauncher.launch(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE))
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        "Failed to access selected directory",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    when (transferState) {
        FileTransferState.Idle -> {
            FileTransferIdleStateView(
                modifier = modifier.fillMaxSize(),
                file = file,
                device = device,
                onSwipeToTransfer = {
                    showSafAccessRequestDialog = true
                }
            )
        }

        is FileTransferState.SafDirectorySelected -> {
            val directory = (transferState as FileTransferState.SafDirectorySelected).directory
            SafDirectorySelectedStateView(
                modifier = modifier.fillMaxSize(),
                directory = directory,
            ) {
                if (file == null) return@SafDirectorySelectedStateView
                transferViewModel.triggerEvent(
                    FileTransferEvents.TransferInitiated(
                        file!!,
                        destinationUri = FileTransferHandler.djiWaypointUri(
                            context,
                            it.uri
                        ) ?: it.uri,
                    )
                )
            }
        }

        is FileTransferState.Transferring -> {
            val progress =
                (transferState as FileTransferState.Transferring).progress
            FileTransferringStateView(
                modifier = modifier.fillMaxSize(),
                progress = progress,
            )
        }

        FileTransferState.TransferCompleted -> {
            FileTransferCompleteStateView(
                modifier = modifier.fillMaxSize()
            )
        }

        is FileTransferState.TransferError -> {
            val error = (transferState as FileTransferState.TransferError).error
            FileTransferErrorStateView(
                modifier = modifier.fillMaxSize(),
                error = error,
                onRetry = {
                    showSafAccessRequestDialog = true
                }
            )
        }
    }

    // Show SAF request dialog
    if (showSafAccessRequestDialog) {
        SafDirectoryAccessRequestDialog(
            onDismissRequest = { showSafAccessRequestDialog = false },
            onRequest = {
                showSafAccessRequestDialog = false
                requestDirectoryAccess()
            },
            onCancel = {
                showSafAccessRequestDialog = false
            },
            file = file
        )
    }
}