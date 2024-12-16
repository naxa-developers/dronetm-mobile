package np.com.naxa.saffileexplorer.ui.app

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import np.com.naxa.saffileexplorer.ui.local_providers.LocalDownloadAndTransferFileViewModel
import np.com.naxa.saffileexplorer.ui.local_providers.LocalEventsViewModel
import np.com.naxa.saffileexplorer.ui.local_providers.LocalUsbDeviceListViewModel
import np.com.naxa.saffileexplorer.ui.navigation.SafFileExplorerNavHost
import np.com.naxa.saffileexplorer.ui.navigation.Routes
import np.com.naxa.saffileexplorer.ui.theme.SAFFileExplorerTheme
import np.com.naxa.saffileexplorer.utils.route
import np.com.naxa.saffileexplorer.viewmodel.DownloadAndTransferFileViewModel
import np.com.naxa.saffileexplorer.viewmodel.EventsViewModel
import np.com.naxa.saffileexplorer.viewmodel.UsbDeviceListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafFileExplorerApp(
    usbDeviceListViewModel: UsbDeviceListViewModel,
    eventsViewModel: EventsViewModel,
    downloadAndTransferViewModel: DownloadAndTransferFileViewModel
) {

    val navController = rememberNavController()

    val backStackEntry by navController.currentBackStackEntryFlow.collectAsState(initial = null)

    val currentRoute by remember {
        derivedStateOf {
            backStackEntry?.destination?.route?.route()
        }
    }

    val topBarTitle by remember {
        derivedStateOf {
            currentRoute?.label ?: "Home"
        }
    }

    CompositionLocalProvider(
        LocalUsbDeviceListViewModel provides usbDeviceListViewModel,
        LocalEventsViewModel provides eventsViewModel,
        LocalDownloadAndTransferFileViewModel provides downloadAndTransferViewModel
    ) {
        SAFFileExplorerTheme {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    if (currentRoute != Routes.Splash) {
                        TopAppBar(
                            title = {
                                Text(topBarTitle)
                            },
                            navigationIcon = {
                                if (currentRoute != null && currentRoute != Routes.DevicesList) {
                                    IconButton(
                                        onClick = {
                                            navController.popBackStack()
                                        }) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = null
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            ) { innerPadding ->
                SafFileExplorerNavHost(
                    modifier = Modifier.padding(
                        if (currentRoute != Routes.Splash) innerPadding else PaddingValues(
                            0.dp
                        )
                    ),
                    navHostController = navController,
                )
            }
        }

    }
}