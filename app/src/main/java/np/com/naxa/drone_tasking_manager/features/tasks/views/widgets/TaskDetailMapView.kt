package np.com.naxa.drone_tasking_manager.features.tasks.views.widgets


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PointF
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import np.com.naxa.drone_tasking_manager.R
import np.com.naxa.drone_tasking_manager.core.widgets.MaplibreCompose
import np.com.naxa.drone_tasking_manager.core.widgets.rememberCameraPosition
import np.com.naxa.drone_tasking_manager.features.tasks.models.ProjectTask
import np.com.naxa.drone_tasking_manager.features.tasks.viewmodels.events.TasksEvent
import np.com.naxa.drone_tasking_manager.features.tasks.viewmodels.states.TaskWayPointsOrWayLinesState
import np.com.naxa.drone_tasking_manager.local_providers.LocalTasksViewModel
import np.com.naxa.drone_tasking_manager.utils.LatLngUtils
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapLibreMap.OnMapClickListener
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.PropertyValue
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.layers.TransitionOptions
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import org.maplibre.geojson.Point.fromLngLat
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random


@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailMapView(
    modifier: Modifier = Modifier,
    task: ProjectTask,
    onWaypointsLoaded: (Int?) -> Unit = {}
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val tasksViewModel = LocalTasksViewModel.current

    var libreMap: MapLibreMap? by remember { mutableStateOf(null) }

    val waypointsOrWayLinesState by tasksViewModel.taskWayPointsOrWayLinesState.collectAsState()

    var isWaypoints by remember { mutableStateOf(false) }

    var infoJsonObject: JsonObject? by remember { mutableStateOf(null) }
    var showInfoBottomSheet by remember { mutableStateOf(false) }
    val infoSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    val cameraPositionState = rememberCameraPosition(
        initialTarget = LatLng(27.82, 85.32),
        initialZoom = 12.0,
    )

    // Just for dragging feature handling
    var draggable by remember { mutableStateOf(false) }
    var draggedFeatureIndex by remember { mutableStateOf<Int?>(null) }
    var updatedTakeOffPointLatLng: LatLng? by remember { mutableStateOf(null) }
    var showTakeOffPointUpdateAlertDialog by remember { mutableStateOf(false) }

    // For Pick off point change
    val takeOffPointChangeOptionsBottomSheetState =
        rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var showTakeOffPointChangeOptionsBottomSheet by remember { mutableStateOf(false) }
    var takeOffPointChangeOptions by remember { mutableStateOf<TakeOffPointChangeOptions?>(null) }


    // Define the permissions to request
    val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    // Launcher for location permission
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allPermissionsGranted = permissions.all { it.value }
        if (allPermissionsGranted) {
            scope.launch {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        updatedTakeOffPointLatLng = LatLng(it.latitude, it.longitude)
                        showTakeOffPointUpdateAlertDialog = true
                        draggedFeatureIndex = null
                    }
                }
            }
        }
    }


    LaunchedEffect(task) {
        if (task.id == null || task.projectId == null) return@LaunchedEffect

        tasksViewModel.triggerEvent(
            TasksEvent.FetchWayPointsOrWayLines(
                taskId = task.id,
                projectId = task.projectId,
                rotationAngle = 0,
                download = false,
                isWayPoints = true,
                forceRefresh = true
            )
        )
    }

    // Map Click Listener
    DisposableEffect(libreMap) {
        val clickListener = OnMapClickListener { latLng ->

            if (draggable) {
                updatedTakeOffPointLatLng = latLng
                showTakeOffPointUpdateAlertDialog = true

                tasksViewModel.triggerEvent(
                    TasksEvent.DragTakeOffPoint(
                        latLng = latLng,
                        onFeatureCollectionUpdated = { features ->
                            applyWaypointsLineLayer(
                                context,
                                features,
                                libreMap
                            )
                            applyWaypointsCircleLayer(
                                context,
                                features,
                                libreMap
                            )
                        }
                    )
                )

                showTakeOffPointUpdateAlertDialog = true

                return@OnMapClickListener true
            }

            val point = libreMap?.projection?.toScreenLocation(latLng)
            val queried = point?.let {
                libreMap?.queryRenderedFeatures(
                    it,
                    *listOf(
                        "task-waypoints-circle-layer--",
                        "task-waypoints-takeoff-symbol-layer--"
                    ).toTypedArray()
                )
            }

            if (!queried.isNullOrEmpty()) {
                val feature = queried.first()
                if (feature.properties()?.has("point_count") == false) {
                    infoJsonObject = feature.properties()
                    showInfoBottomSheet = true
                }
            }

            true
        }

        libreMap?.addOnMapClickListener(clickListener)

        onDispose {
            libreMap?.removeOnMapClickListener(clickListener)
        }
    }

    // Map Long Click Listener
    // DisposableEffect(libreMap) {
    //     val longClickListener = OnMapLongClickListener { latLng ->
    //         val point = libreMap?.projection?.toScreenLocation(latLng)
    //         val queried = point?.let {
    //             libreMap?.queryRenderedFeatures(
    //                 it,
    //                 *listOf(
    //                     "task-waypoints-circle-layer--",
    //                     "task-waypoints-takeoff-symbol-layer--"
    //                 ).toTypedArray()
    //             )
    //         }

    //         if (!queried.isNullOrEmpty()) {
    //             val feature = queried.first()
    //             if (feature.properties()?.has("point_count") == false) {
    //
    //             }
    //         }

    //         true
    //     }

    //     libreMap?.addOnMapLongClickListener(longClickListener)

    //     onDispose {
    //         libreMap?.removeOnMapLongClickListener(longClickListener)
    //     }
    // }

    // Listen waypoints or way lines data and apply layer to the map
    LaunchedEffect(waypointsOrWayLinesState) {
        when (waypointsOrWayLinesState) {
            TaskWayPointsOrWayLinesState.Idle, TaskWayPointsOrWayLinesState.Loading -> {}
            is TaskWayPointsOrWayLinesState.Success -> {
                val features =
                    (waypointsOrWayLinesState as TaskWayPointsOrWayLinesState.Success).geoJson
                isWaypoints =
                    (waypointsOrWayLinesState as TaskWayPointsOrWayLinesState.Success).isWayPoints
                updatedTakeOffPointLatLng = null

                applyWaypointsLineLayer(context, features, libreMap)
                applyWaypointsCircleLayer(context, features, libreMap)

                onWaypointsLoaded.invoke(features.features()?.size)
            }

            is TaskWayPointsOrWayLinesState.Error -> {
                val error = (waypointsOrWayLinesState as TaskWayPointsOrWayLinesState.Error).message
                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            }
        }
    }

    Box(modifier = modifier) {
        MaplibreCompose(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(draggable) {
                    if (draggable) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                val point = PointF(it.x, it.y)
                                val queried = libreMap?.queryRenderedFeatures(
                                    point,
                                    *listOf(
                                        "task-waypoints-circle-layer--",
                                        "task-waypoints-takeoff-symbol-layer--"
                                    ).toTypedArray()
                                )

                                if (!queried.isNullOrEmpty()) {
                                    val feature = queried.first()
                                    val indexPrimitive = if (feature.properties()
                                            ?.get("index")?.isJsonPrimitive == true
                                    ) feature.properties()?.get("index")?.asJsonPrimitive else null

                                    indexPrimitive?.let { ind ->
                                        val index = if (ind.isNumber) ind.asNumber.toDouble()
                                            .toInt() else -1

                                        if (index == 0) draggedFeatureIndex = 0

                                        Log.d(
                                            "AMIT",
                                            "TaskDetailMapView: $index, $draggedFeatureIndex"
                                        )
                                    }
                                }
                            },
                            onDrag = { change, _ ->
                                if (draggedFeatureIndex == 0) {
                                    libreMap?.projection?.fromScreenLocation(
                                        PointF(
                                            change.position.x,
                                            change.position.y
                                        )
                                    )
                                        ?.let {

                                            updatedTakeOffPointLatLng = it

                                            tasksViewModel.triggerEvent(
                                                TasksEvent.DragTakeOffPoint(
                                                    latLng = it,
                                                    onFeatureCollectionUpdated = { features ->
                                                        applyWaypointsLineLayer(
                                                            context,
                                                            features,
                                                            libreMap
                                                        )
                                                        applyWaypointsCircleLayer(
                                                            context,
                                                            features,
                                                            libreMap
                                                        )
                                                    }
                                                )
                                            )
                                        }
                                }


                            },
                            onDragEnd = {
                                if (draggedFeatureIndex == 0) {
                                    showTakeOffPointUpdateAlertDialog = true
                                }
                                draggedFeatureIndex = null
                            }
                        )
                    }
                },
            cameraPositionState = cameraPositionState,
            enableRotateGestures = false,
            onMapReady = { libre, _ ->
                libreMap = libre

                val bounds = task.geometry?.properties?.bbox

                if (!bounds.isNullOrEmpty() && bounds.size == 4) {
                    val northeast = LatLng(bounds[1], bounds[0])
                    val southwest = LatLng(bounds[3], bounds[2])

                    libreMap?.animateCamera(
                        CameraUpdateFactory.newLatLngBounds(
                            LatLngBounds.fromLatLngs(
                                listOf(northeast, southwest)
                            ),
                            150,
                            220,
                            150,
                            150
                        ),
                        500
                    )
                }
            }
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .wrapContentHeight()
                .padding(end = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            TaskWaypointAngleSlider(
                onAngleChanged = {
                    tasksViewModel.triggerEvent(
                        TasksEvent.RotateWayPointsOrWayLines(
                            angle = it,
                            centroid = task.centroid,
                            onRotatedSuccess = { features ->
                                applyWaypointsLineLayer(context, features, libreMap)
                                applyWaypointsCircleLayer(context, features, libreMap)
                            }
                        )
                    )
                },
                onSaved = { angle ->
                    scope.launch {
                        if (task.id == null || task.projectId == null) return@launch
                        tasksViewModel.updateRotation(angle.roundToInt())

                        tasksViewModel.triggerEvent(
                            TasksEvent.FetchWayPointsOrWayLines(
                                taskId = task.id,
                                projectId = task.projectId,
                                rotationAngle = angle.roundToInt(),
                                download = false,
                                isWayPoints = isWaypoints,
                                forceRefresh = true
                            )
                        )
                    }
                },
                onCanceled = {
                    // resetting state to original one
                    tasksViewModel.triggerEvent(
                        TasksEvent.RestoreFeatureCollection { features ->
                            applyWaypointsLineLayer(context, features, libreMap)
                            applyWaypointsCircleLayer(context, features, libreMap)
                        }
                    )
                }
            )

            TakeOffPointDraggableUnDraggableToggle(
                draggable = draggable,
                onToggled = {
                    if (draggable) {
                        draggable = false
                        return@TakeOffPointDraggableUnDraggableToggle
                    }

                    showTakeOffPointChangeOptionsBottomSheet = true
                }
            )

            WayPointsWayLinesSwitcher(
                onToggle = { wayLines ->
                    if (task.id == null || task.projectId == null) return@WayPointsWayLinesSwitcher

                    tasksViewModel.triggerEvent(
                        TasksEvent.FetchWayPointsOrWayLines(
                            taskId = task.id,
                            projectId = task.projectId,
                            download = false,
                            isWayPoints = !wayLines,
                            forceRefresh = false
                        )
                    )
                },
            )
        }

        TaskWaypointInfoBottomSheet(
            infoJsonObject = infoJsonObject,
            infoSheetState = infoSheetState,
            show = showInfoBottomSheet,
            onDismiss = {
                infoJsonObject = null
                showInfoBottomSheet = false
            }
        )

        if (showTakeOffPointUpdateAlertDialog) {
            UpdateTakeOffPointAlertDialog(
                onDismissRequest = {
                    showTakeOffPointUpdateAlertDialog = false
                    updatedTakeOffPointLatLng = null

                    // resetting state to original one
                    if (draggable) {
                        tasksViewModel.triggerEvent(
                            TasksEvent.RestoreFeatureCollection { features ->
                                applyWaypointsLineLayer(context, features, libreMap)
                                applyWaypointsCircleLayer(context, features, libreMap)
                            }
                        )
                    }
                },
                onConfirm = {
                    showTakeOffPointUpdateAlertDialog = false
                    draggable = false

                    if (task.id == null || task.projectId == null || updatedTakeOffPointLatLng == null) return@UpdateTakeOffPointAlertDialog

                    tasksViewModel.triggerEvent(
                        TasksEvent.UpdateTakeOffPoint(
                            taskId = task.id,
                            projectId = task.projectId,
                            latitude = updatedTakeOffPointLatLng!!.latitude,
                            longitude = updatedTakeOffPointLatLng!!.longitude,
                            download = false,
                            isWayPoints = isWaypoints,
                        )
                    )
                }
            )
        }

        TakeOffPointChangeOptionsBottomSheet(
            sheetState = takeOffPointChangeOptionsBottomSheetState,
            show = showTakeOffPointChangeOptionsBottomSheet,
            onDismiss = {
                showTakeOffPointChangeOptionsBottomSheet = false
                takeOffPointChangeOptions = null
                draggable = false
            },
            onOptionSelected = {
                showTakeOffPointChangeOptionsBottomSheet = false

                when (it) {
                    TakeOffPointChangeOptions.Drag -> {
                        draggable = true
                    }

                    TakeOffPointChangeOptions.CurrentLocation -> {
                        scope.launch {
                            locationPermissionLauncher.launch(locationPermissions)
                        }
                    }
                }
            }
        )
    }
}

private fun applyWaypointsCircleLayer(
    context: Context,
    features: FeatureCollection,
    libreMap: MapLibreMap? = null
) {
    if (libreMap == null) return

    val waypointsSourceId = "task-waypoints-geojson-source--"
    val waypointsCircleLayerId = "task-waypoints-circle-layer--"
    val waypointsTakeOffSymbolLayerId = "task-waypoints-takeoff-symbol-layer--"
    val takeOffIconName = "takeoff-symbol-image--"

    if (libreMap.style?.getSource(waypointsSourceId) != null) {

        if (libreMap.style?.getLayer(waypointsCircleLayerId) != null) {
            libreMap.style?.removeLayer(waypointsCircleLayerId)
        }

        if (libreMap.style?.getLayer(waypointsTakeOffSymbolLayerId) != null) {
            libreMap.style?.removeLayer(waypointsTakeOffSymbolLayerId)
        }

        if (libreMap.style?.getImage(takeOffIconName) != null) {
            libreMap.style?.removeImage(takeOffIconName)
        }

        libreMap.style?.removeSource(waypointsSourceId)
    }

    libreMap.style?.addSource(
        GeoJsonSource(
            waypointsSourceId,
            features
        )
    ).also {

        ContextCompat.getDrawable(context, R.drawable.location_pin_24)?.let { drawable ->
            libreMap.style?.addImage(takeOffIconName, drawable)
        }

        libreMap.style?.addLayer(
            CircleLayer(
                waypointsCircleLayerId,
                waypointsSourceId,
            ).apply {

                val propertyValues = mutableListOf<PropertyValue<*>>()

                propertyValues.addAll(
                    listOf(
                        PropertyFactory.circleColor(
                            Expression.coalesce(
                                Expression.get("color"),
                                Expression.switchCase(
                                    Expression.eq(
                                        Expression.toNumber(Expression.get("index")),
                                        Expression.literal(0.0)
                                    ),
                                    Expression.rgba(0, 0, 0, 0),
                                    Expression.eq(
                                        Expression.toNumber(Expression.get("index")),
                                        Expression.literal(
                                            (features.features()?.size?.toDouble()
                                                ?: 0.0) - 1
                                        )
                                    ),
                                    Expression.rgba(0, 0, 0, 0),
                                    Expression.literal("#D73F3F"),
                                )
                            )
                        ),
                        PropertyFactory.circleRadius(3.0f),
                        PropertyFactory.circleStrokeWidth(1.5f),
                        PropertyFactory.circleStrokeColor("#D73F3F")
                    )
                )
                withProperties(*propertyValues.toTypedArray())
            }
        )

        libreMap.style?.addLayer(
            SymbolLayer(
                waypointsTakeOffSymbolLayerId,
                waypointsSourceId,
            ).apply {

                val propertyValues = mutableListOf<PropertyValue<*>>()

                propertyValues.addAll(
                    listOf(
                        PropertyFactory.iconImage(takeOffIconName),
                        PropertyFactory.iconSize(0.9f),
                        PropertyFactory.iconAllowOverlap(true),
                        PropertyFactory.iconIgnorePlacement(true),
                        PropertyFactory.iconOffset(listOf(0f, -10f).toTypedArray())
                    )
                )

                withProperties(*propertyValues.toTypedArray())
                withFilter(Expression.eq(Expression.get("index"), 0))
            }
        )
    }

}

private fun applyWaypointsLineLayer(
    context: Context,
    features: FeatureCollection,
    libreMap: MapLibreMap? = null
) {
    if (libreMap == null) return


    val waypointsLineSourceId = "task-waypoints-line-geojson-source--"
    val waypointsLineLayerId = "task-waypoints-line-layer--"

    if (libreMap.style?.getSource(waypointsLineSourceId) != null) {

        if (libreMap.style?.getLayer(waypointsLineLayerId) != null) {
            libreMap.style?.removeLayer(waypointsLineLayerId)
        }

        libreMap.style?.removeSource(waypointsLineSourceId)
    }

    val coordinates = features.features()?.mapNotNull { it.geometry()?.toJson() }
        ?.map { Point.fromJson(it) }

    val lineString = coordinates?.let { LineString.fromLngLats(it) }

    if (lineString == null) return


    libreMap.style?.addSource(
        GeoJsonSource(
            waypointsLineSourceId,
            Feature.fromGeometry(lineString)
        )
    ).also {
        libreMap.style?.transition = TransitionOptions(500, 500, true)
        libreMap.style?.addLayer(
            LineLayer(
                waypointsLineLayerId,
                waypointsLineSourceId,
            ).apply {

                withProperties()

                val propertyValues = mutableListOf<PropertyValue<*>>()

                propertyValues.addAll(
                    listOf(
                        PropertyFactory.lineColor(
                            Expression.coalesce(
                                Expression.get("color"),
                                Expression.ExpressionLiteral("#484848")
                            )
                        ),
                        PropertyFactory.lineWidth(2f),
                        PropertyFactory.lineDasharray(
                            listOf(
                                2.0f,
                                3.0f
                            ).toTypedArray()
                        )
                    )
                )

                withProperties(*propertyValues.toTypedArray())
            }
        )


    }


    applyWaypointsArrowLayer(context, coordinates, libreMap)

}

private fun applyWaypointsArrowLayer(
    context: Context,
    coordinates: List<Point> = emptyList(),
    libreMap: MapLibreMap? = null
) {
    if (libreMap == null) return

    if (coordinates.size < 4) return

    val waypointsArrowIndicatorSourceId = "task-waypoints-arrow-indicator-geojson-source--"
    val waypointsArrowIndicatorSymbolLayerId = "task-waypoints-arrow-indicator-symbol-layer--"
    val arrowIndicatorIconName = "indicator-arrow-icon--"

    if (libreMap.style?.getSource(waypointsArrowIndicatorSourceId) != null) {

        if (libreMap.style?.getLayer(waypointsArrowIndicatorSymbolLayerId) != null) {
            libreMap.style?.removeLayer(waypointsArrowIndicatorSymbolLayerId)
        }

        if (libreMap.style?.getImage(arrowIndicatorIconName) != null) {
            libreMap.style?.removeImage(arrowIndicatorIconName)
        }

        libreMap.style?.removeSource(waypointsArrowIndicatorSourceId)
    }

    val features = mutableListOf<Feature>()
    val numArrows = min(coordinates.size, (coordinates.size / 5))

    val randomIndices = (0 until coordinates.size - 1).shuffled().take(numArrows)

    for (i in randomIndices) {
        val start = coordinates[i]
        val end = coordinates[i + 1]

        val bearing = LatLngUtils.getBearing(
            LatLng(start.latitude(), start.longitude()),
            LatLng(end.latitude(), end.longitude())
        )

        // Random value between 0.25 and 0.75
        val fraction = Random.nextFloat() * 0.5f + 0.25f
        val midPoint = fromLngLat(
            start.longitude() + (end.longitude() - start.longitude()) * fraction,
            start.latitude() + (end.latitude() - start.latitude()) * fraction
        )

        val feature = Feature.fromGeometry(midPoint).apply {
            addNumberProperty("bearing", bearing)
        }
        features.add(feature)
    }

    if (features.isEmpty()) return

    libreMap.style?.addSource(
        GeoJsonSource(
            waypointsArrowIndicatorSourceId,
            FeatureCollection.fromFeatures(features)
        )
    ).also {
        ContextCompat.getDrawable(context, R.drawable.arrow_up_24)?.let { drawable ->
            libreMap.style?.addImage(arrowIndicatorIconName, drawable)
        }

        libreMap.style?.addLayer(
            SymbolLayer(
                waypointsArrowIndicatorSymbolLayerId,
                waypointsArrowIndicatorSourceId,
            ).apply {

                val propertyValues = mutableListOf<PropertyValue<*>>()

                propertyValues.addAll(
                    listOf(
                        PropertyFactory.iconImage(arrowIndicatorIconName),
                        PropertyFactory.iconSize(0.8f),
                        PropertyFactory.iconRotate(Expression.get("bearing")),
                        PropertyFactory.iconAllowOverlap(true),
                        PropertyFactory.iconIgnorePlacement(true)
                    )
                )

                withProperties(*propertyValues.toTypedArray())
            }
        )
    }
}