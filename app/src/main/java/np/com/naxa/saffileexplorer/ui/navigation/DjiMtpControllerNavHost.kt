package np.com.naxa.saffileexplorer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import np.com.naxa.saffileexplorer.ui.screens.download_and_transfer.DownloadAndTransferFileScreen
import np.com.naxa.saffileexplorer.ui.screens.splash.SplashScreen
import np.com.naxa.saffileexplorer.ui.screens.usb_devices.UsbDeviceListScreen


/**
 * Represents the available navigation routes in the application.
 *
 * @property label Human-readable name for the route
 * @property path URL path pattern for the route
 */
enum class Routes(
    val label: String,
    val path: String,
) {
    /**
     * Route for displaying the splash screen.
     * Path: /splash
     */
    Splash(
        label = "Splash",
        path = "splash"
    ),

    /**
     * Route for displaying the list of connected devices.
     * Path: /connected-devices
     */
    DevicesList(
        label = "Connected Devices",
        path = "connected-devices"
    ),

    /**
     * Route for displaying the contents of a specific device.
     * Path: /device-contents/{deviceId}
     * @param {deviceId} The unique identifier of the device
     */
    DeviceContents(
        label = "Device Contents",
        path = "device-contents/{deviceId}"
    ),

    /**
     * Route for displaying the download and transfer contents of a specific device.
     * Path: /download-and-transfer/{deviceId}
     * @param {deviceId} The unique identifier of the device
     */
    DownloadAndTransfer(
        label = "Download And Transfer",
        path = "download-and-transfer/{deviceId}"
    ),
}


/**
 * Composable function that sets up the navigation host for the DJI MTP Controller.
 *
 * This function defines the navigation structure of the application, including the
 * routes for displaying a list of USB devices and the details of a specific device.
 *
 * @param modifier Optional [Modifier] to customize the layout of the NavHost.
 * @param navHostController The [NavHostController] used for managing navigation within the app.
 */
@Composable
fun SafFileExplorerNavHost(
    modifier: Modifier = Modifier,
    navHostController: NavHostController = rememberNavController()
) {
    // Set up the NavHost with different destinations
    NavHost(
        navController = navHostController,
        startDestination = Routes.Splash.path
    ) {
        // Route for the USB Devices list screen
        composable(Routes.Splash.path) {
            SplashScreen(
                modifier = modifier,
                onNavigate = {
                    navHostController.navigate(Routes.DevicesList.path) {
                        // Prevent back navigation to Splash
                        popUpTo(Routes.Splash.path) {
                            inclusive = true
                        }
                        // Avoid re-creating the DevicesList screen
                        launchSingleTop = true
                    }
                }
            )
        }

        // Route for the USB Devices list screen
        composable(Routes.DevicesList.path) {
            UsbDeviceListScreen(
                modifier = modifier,
                navigateToDeviceContents = { device ->
                    navHostController.navigate(
                        Routes.DownloadAndTransfer.path.replace(
                            "{deviceId}",
                            device.deviceId.toString()
                        )
                    )
                }
            )
        }

        composable(Routes.DownloadAndTransfer.path) { backStackEntry ->
            val deviceId = backStackEntry.arguments?.getString("deviceId")
            DownloadAndTransferFileScreen(
                modifier = modifier,
                deviceId = deviceId?.toIntOrNull(),
            )
        }
    }
}