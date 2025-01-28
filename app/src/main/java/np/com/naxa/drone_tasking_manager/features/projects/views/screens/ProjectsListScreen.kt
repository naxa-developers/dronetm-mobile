package np.com.naxa.drone_tasking_manager.features.projects.views.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
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
import np.com.naxa.drone_tasking_manager.features.projects.viewmodels.events.ProjectsEvent
import np.com.naxa.drone_tasking_manager.features.projects.viewmodels.states.ProjectsListState
import np.com.naxa.drone_tasking_manager.features.projects.views.widgets.ProjectItem
import np.com.naxa.drone_tasking_manager.local_providers.LocalNavigationEventsViewModel
import np.com.naxa.drone_tasking_manager.local_providers.LocalProjectsViewModel
import np.com.naxa.drone_tasking_manager.navigation.viewmodels.events.DroneTMAppNavigationEvent

@Composable
fun ProjectsListScreen(modifier: Modifier = Modifier) {

    val navigationEventsViewModel = LocalNavigationEventsViewModel.current
    val viewModel = LocalProjectsViewModel.current
    val state by viewModel.projectsState.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (state !is ProjectsListState.Success) {
            viewModel.triggerEvent(ProjectsEvent.FetchProjects())
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            ProjectsListState.Idle -> {
                ElevatedButton(
                    onClick = {
                        viewModel.triggerEvent(ProjectsEvent.FetchProjects())
                    }
                ) {
                    Text(text = "Fetch Projects")
                }
            }

            ProjectsListState.Loading -> {
                CircularProgressIndicator()
            }

            is ProjectsListState.Success -> {
                val projects = (state as ProjectsListState.Success).projects
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(projects) { project ->
                        ProjectItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    bottom = 12.dp, start = 16.dp, end = 16.dp
                                ),
                            project = project,
                            onItemClick = {
                                scope.launch {
                                    it.id?.let { id ->
                                        navigationEventsViewModel.sendEvent(
                                            DroneTMAppNavigationEvent.OnNavigateToProjectDetail(
                                                id
                                            )
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }

            is ProjectsListState.Error -> {
                val message = (state as ProjectsListState.Error).message
                Text(text = message)
            }
        }
    }

}