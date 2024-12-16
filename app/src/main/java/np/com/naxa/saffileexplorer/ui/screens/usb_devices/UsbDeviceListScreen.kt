package np.com.naxa.saffileexplorer.ui.screens.usb_devices

import android.hardware.usb.UsbDevice
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import np.com.naxa.saffileexplorer.events.SafFileExplorerAppEvent
import np.com.naxa.saffileexplorer.states.UsbDeviceListState
import np.com.naxa.saffileexplorer.ui.local_providers.LocalEventsViewModel
import np.com.naxa.saffileexplorer.ui.local_providers.LocalUsbDeviceListViewModel

@Composable
fun UsbDeviceListScreen(
    modifier: Modifier,
    navigateToDeviceContents: (UsbDevice) -> Unit
) {
    val scope = rememberCoroutineScope()
    val eventsViewModel = LocalEventsViewModel.current
    val usbDeviceListViewModel = LocalUsbDeviceListViewModel.current

    LaunchedEffect(eventsViewModel) {
        scope.launch {
            eventsViewModel.sendEvent(SafFileExplorerAppEvent.OnStoragePermissionRequested)
        }
    }

    val deviceListState by usbDeviceListViewModel.deviceListState.collectAsState()

    when (deviceListState) {
        is UsbDeviceListState.Loading -> {
            // Show loading indicator
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.wrapContentSize())
            }
        }

        is UsbDeviceListState.Success -> {
            val devices = (deviceListState as UsbDeviceListState.Success).devices
            if (devices.isEmpty()) {
                // Display "No devices connected" message and refresh button
                Box(
                    modifier = modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No devices connected")
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
            } else {
                // Display list of devices in LazyColumn
                LazyColumn(
                    modifier = modifier.fillMaxSize()
                ) {
                    items(devices) { device ->
                        DeviceItem(
                            device,
                            onClick = {
                                navigateToDeviceContents.invoke(device)
                            }
                        )
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