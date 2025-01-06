package np.com.naxa.drone_tasking_manager.ui.screens.download_and_transfer

import android.hardware.usb.UsbDevice
import android.util.Patterns
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import np.com.naxa.drone_tasking_manager.events.DroneTMAppEvent
import np.com.naxa.drone_tasking_manager.events.DroneTMAppNavigationEvent
import np.com.naxa.drone_tasking_manager.states.DownloadAndTransferState
import np.com.naxa.drone_tasking_manager.ui.local_providers.LocalDownloadAndTransferFileViewModel
import np.com.naxa.drone_tasking_manager.ui.local_providers.LocalEventsViewModel
import np.com.naxa.drone_tasking_manager.ui.local_providers.LocalNavigationEventsViewModel
import np.com.naxa.drone_tasking_manager.ui.local_providers.LocalUsbDeviceViewModel
import java.io.File

@Composable
fun DownloadAndTransferFileScreen(modifier: Modifier = Modifier, deviceId: Int?) {

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
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

    var downloadUrl by rememberSaveable {
        mutableStateOf("")
    }

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
        SafDirectoryAccessDialog(
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
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray.copy(
                            alpha = 0.8f
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "to",
                        fontSize = 14.sp,
                        color = Color.LightGray.copy(
                            alpha = 0.9f
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (device != null) "${(device?.manufacturerName ?: device?.deviceName)} - ${device?.productName ?: ""}" else "Unknown Device",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(75.dp))

                    when (showUrlInputBox) {
                        true -> {
                            TextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = downloadUrl,
                                onValueChange = { downloadUrl = it },
                                label = { Text("Download URL") },
                                colors = TextFieldDefaults.colors(
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    errorIndicatorColor = Color.Transparent
                                ),
                                shape = OutlinedTextFieldDefaults.shape,
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            if (clipboardManager.hasText()) {
                                                val pastedText =
                                                    clipboardManager.getText().toString()
                                                if (Patterns.WEB_URL.matcher(pastedText)
                                                        .matches()
                                                ) {
                                                    downloadUrl = pastedText
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        "Clipboard content is not a valid URL",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "Nothing to paste",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.ContentPaste,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(24.dp)
                                                .padding(start = 8.dp)
                                        )
                                    }

                                }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp),
                                shape = RoundedCornerShape(50),
                                onClick = {
                                    scope.launch {
                                        eventsViewModel.sendEvent(
                                            DroneTMAppEvent.OnDownloadInitiated(
                                                device,
                                                downloadUrl
                                            )
                                        )
                                    }
                                },
                                enabled = downloadUrl.trim()
                                    .isNotBlank() && Patterns.WEB_URL.matcher(downloadUrl).matches()
                            ) {
                                Text("Download", color = Color.White)
                                Icon(
                                    Icons.Default.Download,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(start = 8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp),
                                shape = RoundedCornerShape(50),
                                onClick = {
                                    scope.launch {
                                        eventsViewModel.sendEvent(
                                            DroneTMAppEvent.OnFilePickerRequested
                                        )
                                    }
                                }
                            ) {
                                Text("Pick File From Storage", color = Color.White)
                                Icon(
                                    Icons.Default.UploadFile,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(start = 8.dp)
                                )
                            }
                        }

                        false -> {
                            Button(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp),
                                shape = RoundedCornerShape(50),
                                onClick = {
                                    scope.launch {
                                        eventsViewModel.sendEvent(
                                            DroneTMAppEvent.OnFilePickerRequested
                                        )
                                    }
                                }
                            ) {
                                Text("Pick File From Storage", color = Color.White)
                                Icon(
                                    Icons.Default.UploadFile,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(start = 8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp),
                                shape = RoundedCornerShape(50),
                                onClick = {
                                    scope.launch {
                                        downloadAndTransferViewModel.update(
                                            DownloadAndTransferState.Idle(
                                                true
                                            )
                                        )
                                    }
                                }
                            ) {
                                Text("Download with Url", color = Color.White)
                                Icon(
                                    Icons.Default.AddLink,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(start = 8.dp)
                                )
                            }
                        }
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
                        "File: ${file.name}",
                        color = MaterialTheme.colorScheme.primary
                    )
                    FilePreview(
                        file = file,
                        onDelete = {
                            scope.launch {
                                try {
                                    file.delete()
                                } catch (e: Exception) {
                                    // Do Nothing
                                } finally {
                                    downloadAndTransferViewModel.update(
                                        DownloadAndTransferState.Idle(
                                            false
                                        )
                                    )
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
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
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("DownloadError: $error")
                    Spacer(modifier = Modifier.height(12.dp))
                    if (url != null) {
                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp),
                            shape = RoundedCornerShape(4.dp),
                            onClick = {
                                scope.launch {
                                    eventsViewModel.sendEvent(
                                        DroneTMAppEvent.OnDownloadInitiated(device, url)
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

        is DownloadAndTransferState.SafDirectorySelected -> {
            val directory =
                (downloadAndTransferState as DownloadAndTransferState.SafDirectorySelected).directory
            // Show list of directories
            Column(
                modifier = modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    modifier = Modifier.padding(
                        top = 16.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                    text = ".../.../${directory.name}",
                    style = MaterialTheme.typography.labelLarge
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp),
                ) {
                    items(directory.listFiles().filter { it.isDirectory }) { directory ->
                        DirectoryItem(directory) {
                            downloadAndTransferViewModel.startTransfer(
                                destinationUri = directory.uri,
                            )
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
                        imageVector = Icons.Default.ThumbUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
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
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("TransferError: $error")
                    Spacer(modifier = Modifier.height(8.dp))

                    if (file != null) {
                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp),
                            shape = RoundedCornerShape(4.dp),
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
