package np.com.naxa.drone_tasking_manager.features.download_and_transfer.views.screens

import android.hardware.usb.UsbDevice
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import np.com.naxa.drone_tasking_manager.core.widgets.SafDirectoryAccessRequestDialog
import np.com.naxa.drone_tasking_manager.events.DroneTMAppEvent
import np.com.naxa.drone_tasking_manager.features.download_and_transfer.viewmodels.states.DownloadAndTransferState
import np.com.naxa.drone_tasking_manager.features.download_and_transfer.views.widgets.DownloadAndTransferDownloadErrorStateView
import np.com.naxa.drone_tasking_manager.features.download_and_transfer.views.widgets.DownloadAndTransferDownloadedStateView
import np.com.naxa.drone_tasking_manager.features.download_and_transfer.views.widgets.DownloadAndTransferDownloadingStateView
import np.com.naxa.drone_tasking_manager.features.download_and_transfer.views.widgets.DownloadAndTransferIdleStateView
import np.com.naxa.drone_tasking_manager.features.download_and_transfer.views.widgets.DownloadAndTransferSafDirectorySelectedStateView
import np.com.naxa.drone_tasking_manager.features.download_and_transfer.views.widgets.DownloadAndTransferTransferErrorStateView
import np.com.naxa.drone_tasking_manager.features.download_and_transfer.views.widgets.DownloadAndTransferTransferredStateView
import np.com.naxa.drone_tasking_manager.features.download_and_transfer.views.widgets.DownloadAndTransferTransferringStateView
import np.com.naxa.drone_tasking_manager.features.file_transfer.handler.FileTransferHandler
import np.com.naxa.drone_tasking_manager.local_providers.LocalDownloadAndTransferFileViewModel
import np.com.naxa.drone_tasking_manager.local_providers.LocalEventsViewModel
import np.com.naxa.drone_tasking_manager.local_providers.LocalNavigationEventsViewModel
import np.com.naxa.drone_tasking_manager.local_providers.LocalUsbDeviceViewModel
import np.com.naxa.drone_tasking_manager.navigation.viewmodels.events.DroneTMAppNavigationEvent
import java.io.File

@Composable
fun DownloadAndTransferFileScreen(modifier: Modifier = Modifier, deviceId: Int?) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val usbDeviceViewModel = LocalUsbDeviceViewModel.current
    val downloadAndTransferViewModel = LocalDownloadAndTransferFileViewModel.current
    val eventsViewModel = LocalEventsViewModel.current
    val navigationEventsViewModel = LocalNavigationEventsViewModel.current
    var downloadedFile: File? by remember { mutableStateOf(null) }
    var showDialog by rememberSaveable { mutableStateOf(false) }

    var device: UsbDevice? by remember {
        mutableStateOf(null)
    }

    val downloadAndTransferState by downloadAndTransferViewModel.downloadAndTransferState.collectAsState()

    BackHandler {
        if (downloadAndTransferViewModel.downloadAndTransferState.value !is DownloadAndTransferState.Idle) {
            if (downloadAndTransferViewModel.downloadAndTransferState.value is DownloadAndTransferState.SafDirectorySelected) {
                if (downloadedFile != null) {
                    downloadAndTransferViewModel.update(
                        DownloadAndTransferState.DownloadCompleted(
                            downloadedFile!!
                        )
                    )
                    return@BackHandler
                }
            }

            downloadAndTransferViewModel.update(
                DownloadAndTransferState.Idle(false)
            )
            return@BackHandler
        }

        val isShowingUrlInputBox =
            (downloadAndTransferViewModel.downloadAndTransferState.value as DownloadAndTransferState.Idle).showUrlInputBox

        if (isShowingUrlInputBox) {
            downloadAndTransferViewModel.update(
                DownloadAndTransferState.Idle(false)
            )
            return@BackHandler
        }

        navigationEventsViewModel.sendEvent(DroneTMAppNavigationEvent.OnPopBackStack)
    }

    LaunchedEffect(deviceId) {
        scope.launch {
            downloadAndTransferViewModel.update(DownloadAndTransferState.Idle(false))
            device = deviceId?.let { usbDeviceViewModel.deviceWithId(it) }
        }
    }

    // Show SAF request dialog
    if (showDialog) {
        SafDirectoryAccessRequestDialog(
            onDismissRequest = { showDialog = false },
            onRequest = {
                showDialog = false
                scope.launch {
                    eventsViewModel.sendEvent(
                        DroneTMAppEvent.OnDroneTMDirectoryAccessRequested(device, it)
                    )
                }
            },
            onCancel = {
                downloadAndTransferViewModel.update(
                    DownloadAndTransferState.TransferError("Access Denied", it)
                )
                showDialog = false
            },
            file = downloadedFile
        )
    }

    if (device == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Unable to load device")
        }
        return
    }

    when (downloadAndTransferState) {
        is DownloadAndTransferState.Idle -> {
            val showUrlInputBox =
                (downloadAndTransferState as DownloadAndTransferState.Idle).showUrlInputBox

            DownloadAndTransferIdleStateView(
                modifier = Modifier.fillMaxSize(),
                device = device,
                showUrlInputBox = showUrlInputBox,
                onDownloadClicked = {
                    scope.launch {
                        eventsViewModel.sendEvent(
                            DroneTMAppEvent.OnDownloadInitiated(
                                device,
                                it
                            )
                        )
                    }
                },
                onPickFileClicked = {
                    scope.launch {
                        eventsViewModel.sendEvent(
                            DroneTMAppEvent.OnFilePickerRequested
                        )
                    }
                },
                onShowUrlInputBox = {
                    scope.launch {
                        downloadAndTransferViewModel.update(
                            DownloadAndTransferState.Idle(
                                true
                            )
                        )
                    }
                }
            )
        }

        is DownloadAndTransferState.Downloading -> {
            val progress =
                (downloadAndTransferState as DownloadAndTransferState.Downloading).progress

            DownloadAndTransferDownloadingStateView(
                modifier = Modifier.fillMaxSize(),
                progress = progress
            )
        }

        is DownloadAndTransferState.DownloadCompleted -> {
            val file = (downloadAndTransferState as DownloadAndTransferState.DownloadCompleted).file
            DownloadAndTransferDownloadedStateView(
                modifier = Modifier.fillMaxSize(),
                file = file,
                onSwipe = {
                    downloadedFile = file
                    showDialog = true
                }
            )
        }

        is DownloadAndTransferState.DownloadError -> {
            val error = (downloadAndTransferState as DownloadAndTransferState.DownloadError).error
            val url = (downloadAndTransferState as DownloadAndTransferState.DownloadError).url

            DownloadAndTransferDownloadErrorStateView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                url = url,
                error = error,
                onRetry = {
                    url?.let {
                        scope.launch {
                            eventsViewModel.sendEvent(
                                DroneTMAppEvent.OnDownloadInitiated(device, it)
                            )
                        }
                    }
                }
            )
        }

        is DownloadAndTransferState.SafDirectorySelected -> {
            val directory =
                (downloadAndTransferState as DownloadAndTransferState.SafDirectorySelected).directory
            // Show list of directories
            DownloadAndTransferSafDirectorySelectedStateView(
                modifier = Modifier.fillMaxSize().padding(top = 56.dp),
                directory = directory,
                onDirectorySelected = {
                    downloadAndTransferViewModel.startTransfer(
                        destinationUri = FileTransferHandler.djiWaypointUri(
                            context,
                            it.uri
                        ) ?: it.uri,
                    )
                }
            )
        }

        is DownloadAndTransferState.Transferring -> {
            val progress =
                (downloadAndTransferState as DownloadAndTransferState.Transferring).progress
            DownloadAndTransferTransferringStateView(
                modifier = Modifier.fillMaxSize(),
                progress = progress
            )
        }

        is DownloadAndTransferState.TransferCompleted -> {
            DownloadAndTransferTransferredStateView(
                modifier = Modifier.fillMaxSize(),
            )
        }

        is DownloadAndTransferState.TransferError -> {
            val error = (downloadAndTransferState as DownloadAndTransferState.TransferError).error
            val file = (downloadAndTransferState as DownloadAndTransferState.TransferError).file
            // Show error message
            DownloadAndTransferTransferErrorStateView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                error = error,
                file = file,
                onRetry = {
                    downloadedFile = file
                    showDialog = true
                }
            )
        }
    }
}
