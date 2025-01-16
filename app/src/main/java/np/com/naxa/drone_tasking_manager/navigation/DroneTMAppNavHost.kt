package np.com.naxa.drone_tasking_manager.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import np.com.naxa.drone_tasking_manager.Routes
import np.com.naxa.drone_tasking_manager.features.login.views.screens.LoginScreen
import np.com.naxa.drone_tasking_manager.ui.screens.download_and_transfer.DownloadAndTransferFileScreen
import np.com.naxa.drone_tasking_manager.ui.screens.home.HomeScreen
import np.com.naxa.drone_tasking_manager.ui.screens.splash.SplashScreen

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
fun DroneTMAppNavHost(
    modifier: Modifier = Modifier,
    navHostController: NavHostController = rememberNavController()
) {
    // Set up the NavHost with different destinations
    NavHost(
        navController = navHostController,
        startDestination = Routes.Splash.path
    ) {

        // Route for the Splash Screen
        composable(Routes.Splash.path) {
            SplashScreen(
                modifier = modifier,
            )
        }

        // Route for the Home screen
        composable(Routes.Home.path) {
            HomeScreen(
                modifier = modifier,
            )
        }

        composable(Routes.DownloadAndTransfer.path) { backStackEntry ->
            val deviceId = backStackEntry.arguments?.getString("deviceId")
            DownloadAndTransferFileScreen(
                modifier = modifier,
                deviceId = deviceId?.toIntOrNull(),
            )
        }

        // Route for the Home screen
        composable(Routes.Login.path) {
            LoginScreen(
                modifier
            )
        }
    }
}