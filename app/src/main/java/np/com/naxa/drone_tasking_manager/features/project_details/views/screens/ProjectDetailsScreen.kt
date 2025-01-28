package np.com.naxa.drone_tasking_manager.features.project_details.views.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import np.com.naxa.drone_tasking_manager.features.project_details.viewmodels.events.ProjectDetailEvent
import np.com.naxa.drone_tasking_manager.features.project_details.viewmodels.states.ProjectDetailState
import np.com.naxa.drone_tasking_manager.features.project_details.views.widgets.ExpandableLegendsView
import np.com.naxa.drone_tasking_manager.features.project_details.views.widgets.ProjectDetailMapView
import np.com.naxa.drone_tasking_manager.features.projects.models.Project
import np.com.naxa.drone_tasking_manager.features.projects.viewmodels.events.ProjectsEvent
import np.com.naxa.drone_tasking_manager.features.projects.viewmodels.states.ProjectsListState
import np.com.naxa.drone_tasking_manager.local_providers.LocalNavigationEventsViewModel
import np.com.naxa.drone_tasking_manager.local_providers.LocalProjectDetailViewModel
import np.com.naxa.drone_tasking_manager.local_providers.LocalProjectsViewModel
import np.com.naxa.drone_tasking_manager.navigation.viewmodels.events.DroneTMAppNavigationEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailsScreen(
    modifier: Modifier = Modifier,
    projectId: String?,
) {

    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val navigationEventsViewModel = LocalNavigationEventsViewModel.current

    val viewModel = LocalProjectDetailViewModel.current
    val state by viewModel.projectState.collectAsState()

    LaunchedEffect(Unit) {
        if (projectId != null) {
            viewModel.triggerEvent(ProjectDetailEvent.FetchProjectById(projectId))
        }
    }

    when (state) {
        ProjectDetailState.Idle, ProjectDetailState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is ProjectDetailState.Success -> {
            val project = (state as ProjectDetailState.Success).project
            Box(modifier = modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        ProjectDetailMapView(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height((configuration.screenHeightDp * 0.6f).dp),
                            project = project
                        )
                    }
                }

                TopAppBar(
                    title = {
                        Text(project.name ?: "")
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    navigationEventsViewModel.sendEvent(DroneTMAppNavigationEvent.OnPopBackStack)
                                }
                            }
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent
                    )
                )
            }
        }

        is ProjectDetailState.Error -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val error = (state as ProjectDetailState.Error).message
                Text(error)
            }
        }
    }


}