package np.com.naxa.drone_tasking_manager.features.user.profile.views.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import np.com.naxa.drone_tasking_manager.features.user.profile.views.widgets.ProfileNavData

@Composable
fun UserProfileScreen() {
    val navController = rememberNavController()
    val tabs = listOf(
        ProfileNavData.BasicScreen,
        ProfileNavData.OtherScreen,
        ProfileNavData.PassScreen
    )
    var selectedTabIndex by remember { mutableStateOf(1) }

    Scaffold(
        modifier = Modifier.padding(top = 100.dp),
        topBar = {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, screen ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = {
                            selectedTabIndex = index
                            navController.navigate(screen.route) {
                                launchSingleTop = true
                            }
                        },
                        text = { Text(screen.title) }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = ProfileNavData.BasicScreen.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(ProfileNavData.BasicScreen.route) { BasicDetailsScreen() }
            composable(ProfileNavData.OtherScreen.route) { OtherDetailsScreen() }
            composable(ProfileNavData.PassScreen.route) { PasswordScreen() }
        }
    }
}