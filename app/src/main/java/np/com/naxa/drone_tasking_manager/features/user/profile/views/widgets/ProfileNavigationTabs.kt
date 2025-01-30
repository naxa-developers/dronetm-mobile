package np.com.naxa.drone_tasking_manager.features.user.profile.views.widgets

import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun ProfileNavigationTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    TabRow(selectedTabIndex = selectedTab) {
        Tab(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            text = { Text("Basic Details") }
        )
        Tab(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            text = { Text("Other Details") }
        )
        Tab(
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            text = { Text("Password") }
        )
    }
}