package np.com.naxa.drone_tasking_manager.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import np.com.naxa.drone_tasking_manager.events.DroneTMAppNavigationEvent
import np.com.naxa.drone_tasking_manager.events.DroneTMAppEvent
import np.com.naxa.drone_tasking_manager.states.UsbDeviceState
import np.com.naxa.drone_tasking_manager.ui.local_providers.LocalEventsViewModel
import np.com.naxa.drone_tasking_manager.ui.local_providers.LocalNavigationEventsViewModel
import np.com.naxa.drone_tasking_manager.ui.local_providers.LocalUsbDeviceViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
) {

    val scope = rememberCoroutineScope()
    val eventsViewModel = LocalEventsViewModel.current
    val navigationEventsViewModel = LocalNavigationEventsViewModel.current
    val usbDeviceViewModel = LocalUsbDeviceViewModel.current

    val deviceState by usbDeviceViewModel.deviceState.collectAsState()

    // Requesting storage permission on HomeScreen launch
    LaunchedEffect(eventsViewModel) {
        eventsViewModel.sendEvent(DroneTMAppEvent.OnStoragePermissionRequested)
    }

    when (deviceState) {
        UsbDeviceState.WaitingToConnect -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                WaitingToConnectAnimation()
            }
        }

        is UsbDeviceState.Connected -> {
            val state = deviceState as UsbDeviceState.Connected
            val stable = state.stable
            val device = state.device

            if (!stable) {
                Box(
                    modifier = modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    WaitingToConnectAnimation(
                        label = "Waiting for stable connection..."
                    )
                }
            } else {
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
                                navigationEventsViewModel.sendEvent(
                                    DroneTMAppNavigationEvent.OnNavigateToDownloadAndTransfer(
                                        device
                                    )
                                )
                            }
                        }
                    ) {
                        Text("Download & Transfer")
                    }
                }
            }

        }

        is UsbDeviceState.Error -> {
            val message = (deviceState as UsbDeviceState.Error).message
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
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
    }

}