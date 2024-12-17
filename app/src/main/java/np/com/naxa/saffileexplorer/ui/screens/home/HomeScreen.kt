package np.com.naxa.saffileexplorer.ui.screens.home

import android.hardware.usb.UsbDevice
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import np.com.naxa.saffileexplorer.events.SafFileExplorerAppEvent
import np.com.naxa.saffileexplorer.states.UsbDeviceListState
import np.com.naxa.saffileexplorer.ui.local_providers.LocalEventsViewModel
import np.com.naxa.saffileexplorer.ui.local_providers.LocalUsbDeviceListViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navigateToDownloadAndTransfer: (UsbDevice?) -> Unit
) {

    val scope = rememberCoroutineScope()
    val eventsViewModel = LocalEventsViewModel.current
    val usbDeviceListViewModel = LocalUsbDeviceListViewModel.current

    val deviceListState by usbDeviceListViewModel.deviceListState.collectAsState()

    // Requesting storage permission on HomeScreen launch
    LaunchedEffect(eventsViewModel) {
        eventsViewModel.sendEvent(SafFileExplorerAppEvent.OnStoragePermissionRequested)
    }

    when (deviceListState) {
        is UsbDeviceListState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                WaitingToConnectAnimation()
            }
        }

        is UsbDeviceListState.Success -> {
            val device = (deviceListState as UsbDeviceListState.Success).devices.firstOrNull()
            if (device == null) {
                Box(
                    modifier = modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    WaitingToConnectAnimation()
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
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary.copy(
                                alpha = 0.45f
                            )
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "to",
                            fontSize = 36.sp,
                            color = MaterialTheme.colorScheme.primary.copy(
                                alpha = 0.55f
                            )
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = device.deviceName,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary.copy(
                                alpha = 0.65f
                            )
                        )
                    }

                    Button(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(24.dp),
                        onClick = {
                            navigateToDownloadAndTransfer(device)
                        }
                    ) {
                        Text("Download & Transfer")
                    }
                }
            }
        }

        is UsbDeviceListState.Error -> {
            val message = (deviceListState as UsbDeviceListState.Error).message
            // Show error message
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
                                eventsViewModel.sendEvent(SafFileExplorerAppEvent.OnUsbDevicesFetchRequested)
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