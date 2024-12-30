package np.com.naxa.saffileexplorer.ui.app

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import np.com.naxa.saffileexplorer.events.SafAppNavigationEvent
import np.com.naxa.saffileexplorer.ui.local_providers.LocalDownloadAndTransferFileViewModel
import np.com.naxa.saffileexplorer.ui.local_providers.LocalEventsViewModel
import np.com.naxa.saffileexplorer.ui.local_providers.LocalNavigationEventsViewModel
import np.com.naxa.saffileexplorer.ui.local_providers.LocalUsbDeviceListViewModel
import np.com.naxa.saffileexplorer.ui.navigation.Routes
import np.com.naxa.saffileexplorer.ui.navigation.SafFileExplorerNavHost
import np.com.naxa.saffileexplorer.ui.theme.SAFFileExplorerTheme
import np.com.naxa.saffileexplorer.utils.route
import np.com.naxa.saffileexplorer.viewmodel.DownloadAndTransferFileViewModel
import np.com.naxa.saffileexplorer.viewmodel.EventsViewModel
import np.com.naxa.saffileexplorer.viewmodel.NavigationEventsViewModel
import np.com.naxa.saffileexplorer.viewmodel.UsbDeviceListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafFileExplorerApp(
    usbDeviceListViewModel: UsbDeviceListViewModel,
    eventsViewModel: EventsViewModel,
    downloadAndTransferViewModel: DownloadAndTransferFileViewModel,
    navigationEventsViewModel: NavigationEventsViewModel
) {

    val navController = rememberNavController()

    val snackBarHostState = remember { SnackbarHostState() }

    val scope = rememberCoroutineScope()

    val backStackEntry by navController.currentBackStackEntryFlow.collectAsState(initial = null)

    val currentRoute by remember {
        derivedStateOf {
            backStackEntry?.destination?.route?.route()
        }
    }

    val topBarTitle by remember {
        derivedStateOf {
            currentRoute?.label ?: "Device"
        }
    }

    LaunchedEffect(Unit) {
        navigationEventsViewModel.appEvents.collect { event ->
            when (event) {

                is SafAppNavigationEvent.OnNavigateToDownloadAndTransfer -> {
                    navController.navigate(
                        Routes.DownloadAndTransfer.path.replace(
                            "{deviceId}",
                            event.device?.deviceId.toString()
                        )
                    )
                }

                SafAppNavigationEvent.OnNavigateToHome -> {
                    navController.navigate(Routes.Home.path) {
                        popUpTo(Routes.Splash.path) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }

                is SafAppNavigationEvent.OnSnackBarShow -> {
                    scope.launch {
                        val result = snackBarHostState.showSnackbar(
                            message = event.message,
                            actionLabel = event.actionLabel,
                            withDismissAction = event.withDismissAction,
                            duration = event.duration,
                        )
                        when (result) {
                            SnackbarResult.ActionPerformed -> {
                                event.onActionPerformed()
                            }

                            SnackbarResult.Dismissed -> {
                                event.onDismissed()
                            }
                        }
                    }
                }
            }
        }
    }

    CompositionLocalProvider(
        LocalUsbDeviceListViewModel provides usbDeviceListViewModel,
        LocalEventsViewModel provides eventsViewModel,
        LocalDownloadAndTransferFileViewModel provides downloadAndTransferViewModel,
        LocalNavigationEventsViewModel provides navigationEventsViewModel
    ) {
        SAFFileExplorerTheme {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                snackbarHost = { SnackbarHost(snackBarHostState) },
                topBar = {
                    if (currentRoute != Routes.Splash && currentRoute != Routes.Home) {
                        CenterAlignedTopAppBar(
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