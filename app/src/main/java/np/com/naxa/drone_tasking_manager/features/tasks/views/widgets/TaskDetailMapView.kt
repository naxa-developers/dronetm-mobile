package np.com.naxa.drone_tasking_manager.features.tasks.views.widgets

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.gson.JsonObject
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
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.PropertyValue
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import org.maplibre.geojson.Point.fromLngLat
import kotlin.math.min
import kotlin.random.Random


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailMapView(
    modifier: Modifier = Modifier,
    task: ProjectTask,
) {

    val context = LocalContext.current
    val tasksViewModel = LocalTasksViewModel.current

    var libreMap: MapLibreMap? by remember { mutableStateOf(null) }

    val waypointsOrWayLinesState by tasksViewModel.taskWayPointsOrWayLinesState.collectAsState()

    // For waypoints
    val waypointsSourceId = "task-waypoints-geojson-source-${task.id}"
    val waypointsCircleLayerId = "task-waypoints-circle-layer-${task.id}"

    // For waypoints lines
    val waypointsLineSourceId = "task-waypoints-line-geojson-source-${task.id}"
    val waypointsLineLayerId = "task-waypoints-line-layer-${task.id}"
    val waypointsTakeOffSymbolLayerId = "task-waypoints-takeoff-symbol-layer-${task.id}"
    val takeOffIconName = "takeoff-symbol-image--"

    // For Indicator arrow
    val waypointsArrowIndicatorSourceId = "task-waypoints-arrow-indicator-geojson-source-${task.id}"
    val waypointsArrowIndicatorSymbolLayerId =
        "task-waypoints-arrow-indicator-symbol-layer-${task.id}"
    val arrowIndicatorIconName = "indicator-arrow-icon--"

    var infoJsonObject: JsonObject? by remember { mutableStateOf(null) }
    var showInfoBottomSheet by remember { mutableStateOf(false) }
    val infoSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)


    val cameraPositionState = rememberCameraPosition(
        initialTarget = LatLng(27.82, 85.32),
        initialZoom = 12.0,
    )

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

    fun applyWaypointsCircleLayer(features: FeatureCollection) {
        if (libreMap == null) return

        if (libreMap?.style?.getSource(waypointsSourceId) != null) {

            if (libreMap?.style?.getLayer(waypointsCircleLayerId) != null) {
                libreMap?.style?.removeLayer(waypointsCircleLayerId)
            }

            if (libreMap?.style?.getLayer(waypointsTakeOffSymbolLayerId) != null) {
                libreMap?.style?.removeLayer(waypointsTakeOffSymbolLayerId)
            }

            if (libreMap?.style?.getImage(takeOffIconName) != null) {
                libreMap?.style?.removeImage(takeOffIconName)
            }

            libreMap?.style?.removeSource(waypointsSourceId)
        }

        libreMap?.style?.addSource(
            GeoJsonSource(
                waypointsSourceId,
                features = features
            )
        ).also {

            ContextCompat.getDrawable(context, R.drawable.location_pin_24)?.let { drawable ->
                libreMap?.style?.addImage(takeOffIconName, drawable)
            }

            libreMap?.style?.addLayer(
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
                                    Expression.match(
                                        Expression.toNumber(Expression.get("index")),
                                        Expression.literal(0.0),
                                        Expression.rgba(0, 0, 0, 0),
                                        Expression.literal("#D73F3F")
                                    )
                                ),
                            ),
                            PropertyFactory.circleRadius(3.0f),
                            PropertyFactory.circleStrokeWidth(1.5f),
                            PropertyFactory.circleStrokeColor("#D73F3F")
                        )
                    )
                    withProperties(*propertyValues.toTypedArray())
//                    withFilter(Expression.not(Expression.eq(Expression.get("index"), 0)))
                }
            )

            libreMap?.style?.addLayer(
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

    fun applyWaypointsArrowLayer(coordinates: List<Point> = emptyList()) {
        if (libreMap == null) return
        if (coordinates.size < 4) return

        if (libreMap?.style?.getSource(waypointsArrowIndicatorSourceId) != null) {

            if (libreMap?.style?.getLayer(waypointsArrowIndicatorSymbolLayerId) != null) {
                libreMap?.style?.removeLayer(waypointsArrowIndicatorSymbolLayerId)
            }

            if (libreMap?.style?.getImage(arrowIndicatorIconName) != null) {
                libreMap?.style?.removeImage(arrowIndicatorIconName)
            }

            libreMap?.style?.removeSource(waypointsArrowIndicatorSourceId)
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

        libreMap?.style?.addSource(
            GeoJsonSource(
                waypointsArrowIndicatorSourceId,
                FeatureCollection.fromFeatures(features)
            )
        ).also {
            ContextCompat.getDrawable(context, R.drawable.arrow_up_24)?.let { drawable ->
                libreMap?.style?.addImage(arrowIndicatorIconName, drawable)
            }

            libreMap?.style?.addLayer(
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

    fun applyWaypointsLineLayer(features: FeatureCollection) {
        if (libreMap == null) return

        if (libreMap?.style?.getSource(waypointsLineSourceId) != null) {

            if (libreMap?.style?.getLayer(waypointsLineLayerId) != null) {
                libreMap?.style?.removeLayer(waypointsLineLayerId)
            }

            libreMap?.style?.removeSource(waypointsLineSourceId)
        }

        val coordinates = features.features()?.mapNotNull { it.geometry()?.toJson() }
            ?.map { Point.fromJson(it) }

        val lineString = coordinates?.let { LineString.fromLngLats(it) }

        if (lineString == null) return


        libreMap?.style?.addSource(
            GeoJsonSource(
                waypointsLineSourceId,
                Feature.fromGeometry(lineString)
            )
        ).also {
            libreMap?.style?.addLayer(
                LineLayer(
                    waypointsLineLayerId,
                    waypointsLineSourceId,
                ).apply {

                    withProperties(

                    )
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

        applyWaypointsArrowLayer(coordinates)
    }


    // Listen waypoints or way lines data and apply layer to the map
    when (waypointsOrWayLinesState) {
        TaskWayPointsOrWayLinesState.Idle, TaskWayPointsOrWayLinesState.Loading -> {}
        is TaskWayPointsOrWayLinesState.Success -> {
            val features =
                (waypointsOrWayLinesState as TaskWayPointsOrWayLinesState.Success).geoJson
            val isWayPoints =
                (waypointsOrWayLinesState as TaskWayPointsOrWayLinesState.Success).isWayPoints

            applyWaypointsCircleLayer(features)
            applyWaypointsLineLayer(features)
        }

        is TaskWayPointsOrWayLinesState.Error -> {
            val error = (waypointsOrWayLinesState as TaskWayPointsOrWayLinesState.Error).message
            Log.d("AMIT", "TaskDetailMapView: $error")
        }
    }


    Box(modifier = modifier.fillMaxSize()) {
        MaplibreCompose(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            enableScrollGestures = false,
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
                            100,
                            220,
                            100,
                            100
                        ),
                        500
                    )
                }

                libre.addOnMapClickListener { latLng ->
                    val point = libre.projection.toScreenLocation(latLng)
                    val queried = libre.queryRenderedFeatures(
                        point,
                        *listOf(
                            waypointsCircleLayerId,
                            waypointsTakeOffSymbolLayerId
                        ).toTypedArray()
                    )

                    if (queried.isNotEmpty()) {
                        val feature = queried.first()
                        if (feature.properties()?.has("point_count") == false) {
                            infoJsonObject = feature.properties()
                            showInfoBottomSheet = true
                        }
                    }

                    true
                }
            }
        )


        TaskWaypointInfoBottomSheet(
            infoJsonObject = infoJsonObject,
            infoSheetState = infoSheetState,
            show = showInfoBottomSheet,
            onDismiss = {
                infoJsonObject = null
                showInfoBottomSheet = false
            }
        )
    }
}