package np.com.naxa.saffileexplorer.ui.screens.download_and_transfer

import android.hardware.usb.UsbDevice
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import np.com.naxa.saffileexplorer.ui.local_providers.LocalDownloadAndTransferFileViewModel
import np.com.naxa.saffileexplorer.ui.local_providers.LocalEventsViewModel
import np.com.naxa.saffileexplorer.ui.local_providers.LocalUsbDeviceListViewModel
import kotlinx.coroutines.launch
import np.com.naxa.saffileexplorer.events.SafFileExplorerAppEvent
import np.com.naxa.saffileexplorer.states.DownloadAndTransferState
import np.com.naxa.saffileexplorer.utils.asImageBitmap
import java.io.File

@Composable
fun DownloadAndTransferFileScreen(modifier: Modifier = Modifier, deviceId: Int?) {

    val scope = rememberCoroutineScope()
    val usbDeviceListViewModel = LocalUsbDeviceListViewModel.current
    val downloadAndTransferViewModel = LocalDownloadAndTransferFileViewModel.current
    val eventsViewModel = LocalEventsViewModel.current
    var downloadedFile: File? by remember { mutableStateOf(null) }
    var showDialog by remember { mutableStateOf(false) }

    var device: UsbDevice? by remember {
        mutableStateOf(null)
    }

    val downloadAndTransferState by downloadAndTransferViewModel.downloadAndTransferState.collectAsState()

    LaunchedEffect(deviceId) {
        scope.launch {
            downloadAndTransferViewModel.update(DownloadAndTransferState.Idle)
            device = deviceId?.let { usbDeviceListViewModel.deviceOf(it) }
        }
    }


    // Show SAF request dialog
    if (showDialog) {
        SafDirectoryAccessDialog(
            onDismissRequest = { showDialog = false },
            onRequest = {
                showDialog = false
                scope.launch {
                    eventsViewModel.sendEvent(
                        SafFileExplorerAppEvent.OnSafDirectoryAccessRequested(device, it)
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
            Text("Unable to load device - $deviceId")
        }
        return
    }

    when (downloadAndTransferState) {
        is DownloadAndTransferState.Idle -> {
            // Show loading indicator
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Connected",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary.copy(
                            alpha = 0.45f
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "to",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary.copy(
                            alpha = 0.55f
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = device?.deviceName ?: "Unknown Device",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary.copy(
                            alpha = 0.65f
                        )
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                eventsViewModel.sendEvent(
                                    SafFileExplorerAppEvent.OnDownloadInitiated(
                                        device,
                                        // "https://www.youtube.com/watch?v=upFCmUxFNaY"
                                        // "https://sample-videos.com/img/Sample-png-image-30mb.png"
                                        "https://sample-videos.com/img/Sample-jpg-image-30mb.jpg"
                                        // "https://www-cdn.djiits.com/dps/6c185ab72a935e02e7f943913fd91e45.jpg"
                                        //"https://shorturl.at/ip5pG"
                                        // "https://shorturl.at/jKM6c"
                                    )
                                )
                            }
                        }
                    ) {
                        Text("Download")
                    }
                }
            }
        }

        is DownloadAndTransferState.Downloading -> {
            val progress =
                (downloadAndTransferState as DownloadAndTransferState.Downloading).progress

            // Show progress indicator
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(220.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .fillMaxSize(),
                            progress = { progress },
                            strokeCap = StrokeCap.Round,
                            strokeWidth = 20.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = buildAnnotatedString {
                                append(
                                    (progress * 100).toString().subSequence(
                                        0,
                                        if (progress.toString().length >= 4) 4 else 1
                                    )
                                )
                                withStyle(
                                    style = SpanStyle(
                                        fontSize = MaterialTheme.typography.headlineLarge.fontSize * 0.60f,
                                        color = MaterialTheme.colorScheme.primary.copy(
                                            alpha = 0.65f
                                        )
                                    )
                                ) {
                                    append("%")
                                }
                            },
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.primary
                        )

                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Downloading...",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        is DownloadAndTransferState.DownloadCompleted -> {
            val file = (downloadAndTransferState as DownloadAndTransferState.DownloadCompleted).file

            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "DownloadCompleted: ${file.name}",
                        color = MaterialTheme.colorScheme.primary
                    )
                    // Show file to the image view if image
                    if (file.name.endsWith(".jpg") || file.name.endsWith(".png")) {
                        file.asImageBitmap()?.let {
                            Image(
                                it,
                                contentDescription = null,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    SwipeToDoButton {
                        downloadedFile = file
                        showDialog = true
                    }
                }
            }
        }

        is DownloadAndTransferState.DownloadError -> {
            val error = (downloadAndTransferState as DownloadAndTransferState.DownloadError).error
            val url = (downloadAndTransferState as DownloadAndTransferState.DownloadError).url

            // Show error message
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("DownloadError: $error")
                    Spacer(modifier = Modifier.height(8.dp))

                    if (url != null) {
                        Button(
                            onClick = {
                                scope.launch {
                                    eventsViewModel.sendEvent(
                                        SafFileExplorerAppEvent.OnDownloadInitiated(device, url)
                                    )
                                }
                            }
                        ) {
                            Text("Retry Download")
                        }
                    }
                }
            }
        }

        is DownloadAndTransferState.Transferring -> {
            val progress =
                (downloadAndTransferState as DownloadAndTransferState.Transferring).progress

            // Show progress indicator
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(220.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .fillMaxSize(),
                            progress = { progress },
                            strokeCap = StrokeCap.Round,
                            strokeWidth = 20.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = buildAnnotatedString {
                                append(
                                    (progress * 100).toString().subSequence(
                                        0,
                                        if (progress.toString().length >= 4) 4 else 1
                                    )
                                )
                                withStyle(
                                    style = SpanStyle(
                                        fontSize = MaterialTheme.typography.headlineLarge.fontSize * 0.60f,
                                        color = MaterialTheme.colorScheme.primary.copy(
                                            alpha = 0.65f
                                        )
                                    )
                                ) {
                                    append("%")
                                }
                            },
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.primary
                        )

                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Transferring...",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        is DownloadAndTransferState.TransferCompleted -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "Transfer Completed",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.ExtraBold
                        ),
                    )
                }
            }
        }

        is DownloadAndTransferState.TransferError -> {
            val error = (downloadAndTransferState as DownloadAndTransferState.TransferError).error
            val file = (downloadAndTransferState as DownloadAndTransferState.TransferError).file
            // Show error message
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("TransferError: $error")
                    Spacer(modifier = Modifier.height(8.dp))

                    if (file != null) {
                        Button(
                            onClick = {
                                downloadedFile = file
                                showDialog = true
                            }
                        ) {
                            Text("Retry Transfer")
                        }
                    }
                }
            }
        }
    }
}
