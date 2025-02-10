package np.com.naxa.drone_tasking_manager.features.tasks.views.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import np.com.naxa.drone_tasking_manager.features.tasks.viewmodels.events.TasksEvent
import np.com.naxa.drone_tasking_manager.features.tasks.viewmodels.states.TaskDetailState
import np.com.naxa.drone_tasking_manager.features.tasks.views.widgets.DownloadTaskFlightPlanIconButton
import np.com.naxa.drone_tasking_manager.features.tasks.views.widgets.TaskDetailMapView
import np.com.naxa.drone_tasking_manager.features.tasks.views.widgets.TaskDetailSectionView
import np.com.naxa.drone_tasking_manager.local_providers.LocalNavigationEventsViewModel
import np.com.naxa.drone_tasking_manager.local_providers.LocalTasksViewModel
import np.com.naxa.drone_tasking_manager.navigation.viewmodels.events.DroneTMAppNavigationEvent
import kotlin.math.roundToInt

@Composable
fun TaskDetailsScreen(
    modifier: Modifier = Modifier,
    taskId: String? = null,
    projectId: String? = null
) {

    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val navigationEventsViewModel = LocalNavigationEventsViewModel.current

    val tasksViewModel = LocalTasksViewModel.current
    val state by tasksViewModel.taskDetailState.collectAsState()

    val mapViewMaxHeightPx =
        with(LocalDensity.current) { (configuration.screenHeightDp * 0.5).dp.toPx() }
    val mapViewMinHeightPx =
        with(LocalDensity.current) { (configuration.screenHeightDp * 0.25).dp.toPx() }
    var mapViewOffset by remember { mutableFloatStateOf(0f) }

    var waypointsCount: Int? by remember { mutableStateOf(null) }

    val listState = rememberLazyListState()

    var isScrollable by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (taskId != null) {
            tasksViewModel.triggerEvent(TasksEvent.FetchTaskById(taskId, true))
            tasksViewModel.triggerEvent(TasksEvent.ResetState(taskFlightPlanDownloadState = true))
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { listState.layoutInfo }
            .collect { layoutInfo ->
                val totalHeightPx = layoutInfo.visibleItemsInfo.sumOf { it.size }
                val contentHeight = with(density) { totalHeightPx.toDp() }

                isScrollable =
                    contentHeight > (configuration.screenHeightDp.dp - with(density) { mapViewMaxHeightPx.toDp() })
                if (!isScrollable) mapViewOffset = 0f
            }
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val newOffset =
                    (mapViewOffset + delta).coerceIn(-(mapViewMaxHeightPx - mapViewMinHeightPx), 0f)
                mapViewOffset = newOffset
                return Offset.Zero
            }
        }
    }

    when (state) {
        TaskDetailState.Idle, TaskDetailState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is TaskDetailState.Success -> {
            val task = (state as TaskDetailState.Success).projectTask.copy(
                projectId = projectId
            )
            Box(modifier = modifier.fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(top = with(density) { mapViewMaxHeightPx.toDp() }),
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(nestedScrollConnection),
                    userScrollEnabled = isScrollable
                ) {
                    item {
                        TaskDetailSectionView(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(16.dp),
                            task = task,
                            waypointsCount = waypointsCount
                        )
                    }
                }

                TaskDetailMapView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(with(density) { mapViewMaxHeightPx.toDp() })
                        .offset { IntOffset(x = 0, y = mapViewOffset.roundToInt()) },
                    task = task,
                    onWaypointsLoaded = {
                        waypointsCount = it
                    }
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .wrapContentWidth()
                        .wrapContentHeight()
                        .padding(top = 32.dp, start = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
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

                    Text(
                        "#${task.projectTaskIndex ?: ""}",
                        style = MaterialTheme.typography.titleMedium
                    )

                    if (waypointsCount != null)
                        Spacer(modifier = Modifier.weight(1f))

                    if (waypointsCount != null)
                        DownloadTaskFlightPlanIconButton(
                            task = task.copy(
                                projectId = projectId
                            )
                        )
                }
            }
        }

        is TaskDetailState.Error -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val error = (state as TaskDetailState.Error).message
                Text(error)
            }
        }
    }

}