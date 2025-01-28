package np.com.naxa.drone_tasking_manager.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import np.com.naxa.drone_tasking_manager.Routes
import np.com.naxa.drone_tasking_manager.features.login.views.screens.LoginScreen
import np.com.naxa.drone_tasking_manager.features.project_details.views.screens.ProjectDetailsScreen
import np.com.naxa.drone_tasking_manager.features.projects.views.screens.ProjectsListScreen
import np.com.naxa.drone_tasking_manager.features.projects.views.screens.ProjectsMapScreen
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

        // Route for the Download and Transfer screen
        composable(Routes.DownloadAndTransfer.path) { backStackEntry ->
            val deviceId = backStackEntry.arguments?.getString("deviceId")
            DownloadAndTransferFileScreen(
                modifier = modifier,
                deviceId = deviceId?.toIntOrNull(),
            )
        }

        // Route for the Home Screen
        composable(Routes.Login.path) {
            LoginScreen()
        }

        navigation(
            startDestination = Routes.ProjectsList.path,
            route = Routes.Projects.path,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
        ) {
            // Route for the Projects List Screen
            composable(
                Routes.ProjectsList.path,
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
            ) {
                ProjectsListScreen(
                    modifier = modifier,
                )
            }

            // Route for the Projects Map Screen
            composable(
                Routes.ProjectsMap.path,
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
            ) {
                ProjectsMapScreen(
                    modifier = modifier,
                )
            }
        }

        // Route for the Project Details screen
        composable(
            Routes.ProjectDetails.path,
            enterTransition = {
                slideIn(
                    animationSpec = tween(),
                    initialOffset = { IntOffset(it.width, 0) }
                )
            },
            exitTransition = {
                slideOut(
                    animationSpec = tween(),
                    targetOffset = { IntOffset(0, 0) },
                )
            },
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")
            ProjectDetailsScreen(
                modifier = modifier,
                projectId = id,
            )
        }

    }
}