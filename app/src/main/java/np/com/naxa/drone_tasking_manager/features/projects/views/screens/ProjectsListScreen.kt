package np.com.naxa.drone_tasking_manager.features.projects.views.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import np.com.naxa.drone_tasking_manager.features.projects.viewmodels.events.ProjectsEvent
import np.com.naxa.drone_tasking_manager.features.projects.views.widgets.*
import np.com.naxa.drone_tasking_manager.local_providers.LocalNavigationEventsViewModel
import np.com.naxa.drone_tasking_manager.local_providers.LocalProjectsViewModel
import np.com.naxa.drone_tasking_manager.navigation.viewmodels.events.DroneTMAppNavigationEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsListScreen(modifier: Modifier = Modifier) {

    val scope = rememberCoroutineScope()
    val navigationEventsViewModel = LocalNavigationEventsViewModel.current
    val viewModel = LocalProjectsViewModel.current
    val state by viewModel.projectsState.collectAsState()
    val listState = rememberLazyListState()

    var selectedFilter by remember { mutableStateOf(if (state.onlyMine) ProjectFilterItem.OnlyMine else ProjectFilterItem.All) }

    LaunchedEffect(Unit) {
        if (state.error != null || state.projects.isEmpty()) {
            viewModel.triggerEvent(
                ProjectsEvent.FetchProjects(
                    onlyMine = selectedFilter == ProjectFilterItem.OnlyMine
                )
            )
        }
    }

    // Load more items when the user scrolls to the end
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .collect { visibleItems ->
                if (visibleItems.isNotEmpty() && visibleItems.last().index >= state.projects.size - 1 && !state.fetching) {
                    viewModel.triggerEvent(
                        ProjectsEvent.FetchProjects(
                            onlyMine = selectedFilter == ProjectFilterItem.OnlyMine
                        )
                    )
                }
            }
    }


    PullToRefreshBox(
        modifier = modifier.fillMaxSize(),
        isRefreshing = state.refresh,
        onRefresh = {
            viewModel.triggerEvent(
                ProjectsEvent.FetchProjects(
                    onlyMine = selectedFilter == ProjectFilterItem.OnlyMine,
                    refresh = true
                )
            )
        },
        contentAlignment = Alignment.Center,
    ) {

        if (state.projects.isEmpty() && state.fetching) {
            CircularProgressIndicator()
        } else if (state.error != null) {
            Text(text = state.error!!)
        } else {
            if (state.projects.isEmpty()) {
                Text(text = "No projects found")
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {
                    items(state.projects) { project ->
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

                    if (state.fetching) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .padding(top = 16.dp)
                                        .size(20.dp),
                                    strokeWidth = 2.5.dp
                                )
                            }
                        }
                    }
                }
            }
        }

        FilterFloatingActionButtonAndSheet(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            visible = state.projects.isNotEmpty(),
            selected = selectedFilter,
            onSelected = {
                selectedFilter = it
                viewModel.triggerEvent(
                    ProjectsEvent.FetchProjects(
                        onlyMine = it == ProjectFilterItem.OnlyMine,
                        refresh = true
                    )
                )
            }
        )
    }
}