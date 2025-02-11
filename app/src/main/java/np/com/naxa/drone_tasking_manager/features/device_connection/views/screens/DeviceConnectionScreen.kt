package np.com.naxa.drone_tasking_manager.features.device_connection.views.screens

import android.os.Environment
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import np.com.naxa.drone_tasking_manager.events.DroneTMAppEvent
import np.com.naxa.drone_tasking_manager.features.device_connection.viewmodels.states.UsbDeviceState
import np.com.naxa.drone_tasking_manager.features.device_connection.views.widgets.WaitingToConnectAnimation
import np.com.naxa.drone_tasking_manager.local_providers.LocalEventsViewModel
import np.com.naxa.drone_tasking_manager.local_providers.LocalNavigationEventsViewModel
import np.com.naxa.drone_tasking_manager.local_providers.LocalUsbDeviceViewModel
import np.com.naxa.drone_tasking_manager.navigation.routes.Routes
import np.com.naxa.drone_tasking_manager.navigation.viewmodels.events.DroneTMAppNavigationEvent

/**
 * Displays the Device Connection screen, showing the current connection state of a USB device.
 *
 * This screen handles different states of the USB device, including:
 * - `WaitingToConnect`: Shows an animation while waiting for a device to connect.
 * - `Connected`: Displays information about the connected device and provides a button to navigate to the Download & Transfer screen.
 * - `Error`: Shows an error message and a button to retry the connection.
 *
 * The screen also requests storage permission on launch.
 *
 * @param modifier The modifier to apply to this layout.
 * @param route The route to navigate when the user clicks the "Initiate Transfer" or "Download & Transfer" button.
 *
 */
@Composable
fun DeviceConnectionScreen(
    modifier: Modifier = Modifier,
    route: Routes
) {

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val eventsViewModel = LocalEventsViewModel.current
    val navigationEventsViewModel = LocalNavigationEventsViewModel.current
    val usbDeviceViewModel = LocalUsbDeviceViewModel.current

    val deviceState by usbDeviceViewModel.deviceState.collectAsState()

    // Requesting storage permission on HomeScreen launch
    LaunchedEffect(eventsViewModel) {
        eventsViewModel.sendEvent(DroneTMAppEvent.OnStoragePermissionRequested)
    }


    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (deviceState) {
            UsbDeviceState.WaitingToConnect -> {
                WaitingToConnectAnimation()
            }

            is UsbDeviceState.Connected -> {
                val state = deviceState as UsbDeviceState.Connected
                val stable = state.stable
                val device = state.device

                if (!stable) {
                    WaitingToConnectAnimation(
                        label = "Waiting for stable connection..."
                    )
                } else {

                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Connected",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary.copy(
                                alpha = 0.45f
                            )
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "to",
                            fontSize = 32.sp,
                            color = MaterialTheme.colorScheme.primary.copy(
                                alpha = 0.55f
                            )
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "${(device.manufacturerName ?: device.deviceName)} - ${device.productName ?: ""}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary.copy(
                                alpha = 0.65f
                            ),
                            textAlign = TextAlign.Center,
                        )
                    }

                    Button(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(24.dp),
                        onClick = {
                            scope.launch {

                                if (route == Routes.DownloadAndTransfer) {
                                    navigationEventsViewModel.sendEvent(
                                        DroneTMAppNavigationEvent.OnNavigateToDownloadAndTransfer(
                                            device
                                        )
                                    )
                                    return@launch
                                }

                                navigationEventsViewModel.sendEvent(
                                    DroneTMAppNavigationEvent.OnNavigateToFileTransfer(
                                        deviceId = device.deviceId.toString()
                                    )
                                )

                            }
                        }
                    ) {
                        Text(
                            if (route == Routes.DownloadAndTransfer) "Download & Transfer" else "Initiate Transfer",
                            color = Color.White
                        )
                    }

                }

            }

            is UsbDeviceState.Error -> {
                val message = (deviceState as UsbDeviceState.Error).message
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Error: $message")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                eventsViewModel.sendEvent(DroneTMAppEvent.OnUsbDeviceSearchRequested)
                            }
                        }
                    ) {
                        Text("Refresh")
                    }
                }

            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .wrapContentWidth()
                .wrapContentHeight()
                .padding(top = 32.dp, start = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = {
                    scope.launch {
                        if (route == Routes.FileTransfer) {
                            try {
                                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                                    ?.listFiles()?.forEach { f ->
                                        try {
                                            f.deleteRecursively()
                                        } catch (e: Exception) {
                                            // Do Nothing
                                        }
                                    }
                            } catch (e: Exception) {
                                // Do Nothing
                            }

                        }

                        navigationEventsViewModel.sendEvent(DroneTMAppNavigationEvent.OnPopBackStack)
                    }
                }
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null
                )
            }
        }
    }

}