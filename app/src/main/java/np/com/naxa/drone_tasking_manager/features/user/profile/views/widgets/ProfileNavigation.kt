package np.com.naxa.drone_tasking_manager.features.user.profile.views.widgets

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import np.com.naxa.drone_tasking_manager.features.user.profile.views.screens.BasicDetailsScreen
import np.com.naxa.drone_tasking_manager.features.user.profile.views.screens.OtherDetailsScreen
import np.com.naxa.drone_tasking_manager.features.user.profile.views.screens.PasswordScreen

@Composable
fun ProfileNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "basic_details") {
        composable("basic_details") { BasicDetailsScreen(navController) }
        composable("other_details") { OtherDetailsScreen(navController) }
        composable("password") { PasswordScreen(navController) }
    }
}