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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.style.TextAlign
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
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val navigationEventsViewModel = LocalNavigationEventsViewModel.current

    val tasksViewModel = LocalTasksViewModel.current
    val state by tasksViewModel.taskDetailState.collectAsState()

    val mapViewMaxHeightPx = windowInfo.containerSize.height * 0.6
    val mapViewMinHeightPx = windowInfo.containerSize.height * 0.25
    var mapViewOffset by remember { mutableFloatStateOf(0f) }

    var waypointsCount: Int? by remember { mutableStateOf(null) }

    val listState = rememberLazyListState()

    var fullyScrolled by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (taskId != null) {
            tasksViewModel.triggerEvent(TasksEvent.FetchTaskById(taskId, true))
            tasksViewModel.triggerEvent(TasksEvent.ResetState(taskFlightPlanDownloadState = true))
        }
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (!fullyScrolled) {
                    val delta = available.y

                    val newOffset =
                        (mapViewOffset + delta).coerceIn(
                            (-(mapViewMaxHeightPx - mapViewMinHeightPx)).toFloat(),
                            0f
                        )
                    mapViewOffset = newOffset
                }
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
                TaskDetailSectionView(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(nestedScrollConnection)
                        .padding(16.dp),
                    task = task,
                    waypointsCount = waypointsCount,
                    listState = listState,
                    contentPadding = PaddingValues(top = with(density) {
                        mapViewMaxHeightPx.roundToInt().toDp()
                    }),
                    onLayoutInfoChanged = { layoutInfo ->

                        val height4ScrollableContainer =
                            windowInfo.containerSize.height - mapViewMaxHeightPx

                        val scrollable =
                            layoutInfo.viewportSize.height > height4ScrollableContainer
                        if (!scrollable) mapViewOffset = 0f

                        fullyScrolled =
                            layoutInfo.visibleItemsInfo.lastOrNull()?.index == (layoutInfo.totalItemsCount - 1)

                        return@TaskDetailSectionView scrollable
                    }
                )

                TaskDetailMapView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(with(density) { mapViewMaxHeightPx.roundToInt().toDp() })
                        .offset { IntOffset(x = 0, y = mapViewOffset.roundToInt()) },
                    task = task,
                    onWaypointsLoaded = {
                        waypointsCount = it
                    },
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
                            ),
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
                Text(error, modifier = Modifier.padding(20.dp), textAlign = TextAlign.Center)
            }
        }
    }

}