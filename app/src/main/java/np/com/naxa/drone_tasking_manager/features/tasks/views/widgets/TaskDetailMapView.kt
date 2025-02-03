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

    fun applyWaypointsCircleLayer() {
        if (libreMap == null) return

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

        if (libreMap?.style?.getSource(waypointsSourceId) != null) {

            if (libreMap?.style?.getLayer(waypointsCircleLayerId) != null) {
                libreMap?.style?.removeLayer(waypointsCircleLayerId)
            }

            if (libreMap?.style?.getImage(takeOffIconName) != null) {
                libreMap?.style?.removeImage(takeOffIconName)
            }

            libreMap?.style?.removeSource(waypointsSourceId)
        }

        libreMap?.style?.addSource(
            GeoJsonSource(
                waypointsSourceId,
                geoJson = geoJson
                // uri = URI.create("https://dev.dronetm.org/api/waypoint/task/${task.id}/?project_id=${task.projectId}&download=false&mode=waylines&rotation_angle=0")
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
                                    Expression.ExpressionLiteral("#484848")
                                )
                            ),
                            PropertyFactory.circleRadius(3.0f),
                            PropertyFactory.circleStrokeWidth(1.5f),
                            PropertyFactory.circleStrokeColor("#D73F3F")
                        )
                    )
                    withProperties(*propertyValues.toTypedArray())
                    withFilter(Expression.not(Expression.eq(Expression.get("index"), 0)))
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
                            PropertyFactory.iconIgnorePlacement(true)
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

    fun applyWaypointsLineLayer() {
        if (libreMap == null) return

        if (libreMap?.style?.getSource(waypointsLineSourceId) != null) {

            if (libreMap?.style?.getLayer(waypointsLineLayerId) != null) {
                libreMap?.style?.removeLayer(waypointsLineLayerId)
            }

            libreMap?.style?.removeSource(waypointsLineSourceId)
        }

        val coordinates =
            FeatureCollection.fromJson(geoJson).features()?.mapNotNull { it.geometry()?.toJson() }
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
            val jsonObject =
                (waypointsOrWayLinesState as TaskWayPointsOrWayLinesState.Success).jsonObject
            val isWayPoints =
                (waypointsOrWayLinesState as TaskWayPointsOrWayLinesState.Success).isWayPoints

            applyWaypointsCircleLayer()
            applyWaypointsLineLayer()
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

                // applyWaypointsCircleLayer()
                // applyWaypointsLineLayer()

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

val geoJson = """
    {
            "type": "FeatureCollection",
            "name": "waypoints",
            "crs": {
                "type": "name",
                "properties": {
                    "name": "urn:ogc:def:crs:OGC:1.3:CRS84"
                }
            },
            "features": [
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305218,
                            24.145746,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 0,
                        "heading": 0,
                        "take_photo": false,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.302791,
                            24.147732,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 1,
                        "heading": 90,
                        "take_photo": false,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.302791,
                            24.147732,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 2,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.30298,
                            24.147732,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 3,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.30317,
                            24.147732,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 4,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.303359,
                            24.147732,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 5,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.303549,
                            24.147732,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 6,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.303738,
                            24.147732,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 7,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.303928,
                            24.147732,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 8,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304117,
                            24.147732,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 9,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304307,
                            24.147732,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 10,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304496,
                            24.147732,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 11,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304686,
                            24.147732,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 12,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304875,
                            24.147732,
                            114.8
                        ]
                    },
                    "properties": {
                        "elevation": 39.0,
                        "index": 13,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 114.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305064,
                            24.147732,
                            116.8
                        ]
                    },
                    "properties": {
                        "elevation": 41.0,
                        "index": 14,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 116.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305254,
                            24.147732,
                            116.8
                        ]
                    },
                    "properties": {
                        "elevation": 41.0,
                        "index": 15,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 116.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305443,
                            24.147732,
                            115.8
                        ]
                    },
                    "properties": {
                        "elevation": 40.0,
                        "index": 16,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 115.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305633,
                            24.147732,
                            112.8
                        ]
                    },
                    "properties": {
                        "elevation": 37.0,
                        "index": 17,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 112.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305822,
                            24.147732,
                            112.8
                        ]
                    },
                    "properties": {
                        "elevation": 37.0,
                        "index": 18,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 112.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.306012,
                            24.147732,
                            114.8
                        ]
                    },
                    "properties": {
                        "elevation": 39.0,
                        "index": 19,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 114.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.306201,
                            24.147732,
                            113.8
                        ]
                    },
                    "properties": {
                        "elevation": 38.0,
                        "index": 20,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 113.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.306391,
                            24.147732,
                            113.8
                        ]
                    },
                    "properties": {
                        "elevation": 38.0,
                        "index": 21,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 113.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.30658,
                            24.147732,
                            113.8
                        ]
                    },
                    "properties": {
                        "elevation": 38.0,
                        "index": 22,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 113.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.30677,
                            24.147732,
                            111.8
                        ]
                    },
                    "properties": {
                        "elevation": 36.0,
                        "index": 23,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 111.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.306959,
                            24.147732,
                            111.8
                        ]
                    },
                    "properties": {
                        "elevation": 36.0,
                        "index": 24,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 111.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.307148,
                            24.147732,
                            111.8
                        ]
                    },
                    "properties": {
                        "elevation": 36.0,
                        "index": 25,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 111.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.307338,
                            24.147732,
                            114.8
                        ]
                    },
                    "properties": {
                        "elevation": 39.0,
                        "index": 26,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 114.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.307527,
                            24.147732,
                            113.8
                        ]
                    },
                    "properties": {
                        "elevation": 38.0,
                        "index": 27,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 113.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.307717,
                            24.147732,
                            113.8
                        ]
                    },
                    "properties": {
                        "elevation": 38.0,
                        "index": 28,
                        "heading": 90,
                        "take_photo": false,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 113.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.307527,
                            24.147364,
                            115.8
                        ]
                    },
                    "properties": {
                        "elevation": 40.0,
                        "index": 29,
                        "heading": -90,
                        "take_photo": false,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 115.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.307527,
                            24.147364,
                            115.8
                        ]
                    },
                    "properties": {
                        "elevation": 40.0,
                        "index": 30,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 115.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.307338,
                            24.147364,
                            112.8
                        ]
                    },
                    "properties": {
                        "elevation": 37.0,
                        "index": 31,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 112.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.307148,
                            24.147364,
                            113.8
                        ]
                    },
                    "properties": {
                        "elevation": 38.0,
                        "index": 32,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 113.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.306959,
                            24.147364,
                            113.8
                        ]
                    },
                    "properties": {
                        "elevation": 38.0,
                        "index": 33,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 113.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.30677,
                            24.147364,
                            113.8
                        ]
                    },
                    "properties": {
                        "elevation": 38.0,
                        "index": 34,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 113.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.30658,
                            24.147364,
                            113.8
                        ]
                    },
                    "properties": {
                        "elevation": 38.0,
                        "index": 35,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 113.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.306391,
                            24.147364,
                            113.8
                        ]
                    },
                    "properties": {
                        "elevation": 38.0,
                        "index": 36,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 113.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.306201,
                            24.147364,
                            114.8
                        ]
                    },
                    "properties": {
                        "elevation": 39.0,
                        "index": 37,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 114.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.306012,
                            24.147364,
                            113.8
                        ]
                    },
                    "properties": {
                        "elevation": 38.0,
                        "index": 38,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 113.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305822,
                            24.147364,
                            111.8
                        ]
                    },
                    "properties": {
                        "elevation": 36.0,
                        "index": 39,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 111.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305633,
                            24.147364,
                            111.8
                        ]
                    },
                    "properties": {
                        "elevation": 36.0,
                        "index": 40,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 111.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305443,
                            24.147364,
                            114.8
                        ]
                    },
                    "properties": {
                        "elevation": 39.0,
                        "index": 41,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 114.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305254,
                            24.147364,
                            114.8
                        ]
                    },
                    "properties": {
                        "elevation": 39.0,
                        "index": 42,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 114.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305064,
                            24.147364,
                            114.8
                        ]
                    },
                    "properties": {
                        "elevation": 39.0,
                        "index": 43,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 114.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304875,
                            24.147364,
                            114.8
                        ]
                    },
                    "properties": {
                        "elevation": 39.0,
                        "index": 44,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 114.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304686,
                            24.147364,
                            116.8
                        ]
                    },
                    "properties": {
                        "elevation": 41.0,
                        "index": 45,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 116.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304496,
                            24.147364,
                            116.8
                        ]
                    },
                    "properties": {
                        "elevation": 41.0,
                        "index": 46,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 116.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304307,
                            24.147364,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 47,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304117,
                            24.147364,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 48,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.303928,
                            24.147364,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 49,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.303738,
                            24.147364,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 50,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.303549,
                            24.147364,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 51,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.303359,
                            24.147364,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 52,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.30317,
                            24.147364,
                            119.8
                        ]
                    },
                    "properties": {
                        "elevation": 44.0,
                        "index": 53,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 119.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.30298,
                            24.147364,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 54,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.302791,
                            24.147364,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 55,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.302602,
                            24.147364,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 56,
                        "heading": -90,
                        "take_photo": false,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.302791,
                            24.146996,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 57,
                        "heading": 90,
                        "take_photo": false,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.302791,
                            24.146996,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 58,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.30298,
                            24.146996,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 59,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.30317,
                            24.146996,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 60,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.303359,
                            24.146996,
                            119.8
                        ]
                    },
                    "properties": {
                        "elevation": 44.0,
                        "index": 61,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 119.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.303549,
                            24.146996,
                            119.8
                        ]
                    },
                    "properties": {
                        "elevation": 44.0,
                        "index": 62,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 119.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.303738,
                            24.146996,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 63,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.303928,
                            24.146996,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 64,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304117,
                            24.146996,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 65,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304307,
                            24.146996,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 66,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304496,
                            24.146996,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 67,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304686,
                            24.146996,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 68,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304875,
                            24.146996,
                            116.8
                        ]
                    },
                    "properties": {
                        "elevation": 41.0,
                        "index": 69,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 116.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305064,
                            24.146996,
                            114.8
                        ]
                    },
                    "properties": {
                        "elevation": 39.0,
                        "index": 70,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 114.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305254,
                            24.146996,
                            114.8
                        ]
                    },
                    "properties": {
                        "elevation": 39.0,
                        "index": 71,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 114.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305443,
                            24.146996,
                            116.8
                        ]
                    },
                    "properties": {
                        "elevation": 41.0,
                        "index": 72,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 116.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305633,
                            24.146996,
                            115.8
                        ]
                    },
                    "properties": {
                        "elevation": 40.0,
                        "index": 73,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 115.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305822,
                            24.146996,
                            115.8
                        ]
                    },
                    "properties": {
                        "elevation": 40.0,
                        "index": 74,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 115.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.306012,
                            24.146996,
                            113.8
                        ]
                    },
                    "properties": {
                        "elevation": 38.0,
                        "index": 75,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 113.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.306201,
                            24.146996,
                            113.8
                        ]
                    },
                    "properties": {
                        "elevation": 38.0,
                        "index": 76,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 113.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.306391,
                            24.146996,
                            115.8
                        ]
                    },
                    "properties": {
                        "elevation": 40.0,
                        "index": 77,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 115.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.30658,
                            24.146996,
                            115.8
                        ]
                    },
                    "properties": {
                        "elevation": 40.0,
                        "index": 78,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 115.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.30677,
                            24.146996,
                            115.8
                        ]
                    },
                    "properties": {
                        "elevation": 40.0,
                        "index": 79,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 115.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.306959,
                            24.146996,
                            114.8
                        ]
                    },
                    "properties": {
                        "elevation": 39.0,
                        "index": 80,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 114.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.307148,
                            24.146996,
                            114.8
                        ]
                    },
                    "properties": {
                        "elevation": 39.0,
                        "index": 81,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 114.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.307338,
                            24.146996,
                            113.8
                        ]
                    },
                    "properties": {
                        "elevation": 38.0,
                        "index": 82,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 113.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.307527,
                            24.146996,
                            112.8
                        ]
                    },
                    "properties": {
                        "elevation": 37.0,
                        "index": 83,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 112.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.307717,
                            24.146996,
                            112.8
                        ]
                    },
                    "properties": {
                        "elevation": 37.0,
                        "index": 84,
                        "heading": 90,
                        "take_photo": false,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 112.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.307527,
                            24.146627,
                            112.8
                        ]
                    },
                    "properties": {
                        "elevation": 37.0,
                        "index": 85,
                        "heading": -90,
                        "take_photo": false,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 112.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.307527,
                            24.146627,
                            112.8
                        ]
                    },
                    "properties": {
                        "elevation": 37.0,
                        "index": 86,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 112.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.307338,
                            24.146627,
                            112.8
                        ]
                    },
                    "properties": {
                        "elevation": 37.0,
                        "index": 87,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 112.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.307148,
                            24.146627,
                            113.8
                        ]
                    },
                    "properties": {
                        "elevation": 38.0,
                        "index": 88,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 113.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.306959,
                            24.146627,
                            113.8
                        ]
                    },
                    "properties": {
                        "elevation": 38.0,
                        "index": 89,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 113.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.30677,
                            24.146627,
                            113.8
                        ]
                    },
                    "properties": {
                        "elevation": 38.0,
                        "index": 90,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 113.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.30658,
                            24.146627,
                            114.8
                        ]
                    },
                    "properties": {
                        "elevation": 39.0,
                        "index": 91,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 114.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.306391,
                            24.146627,
                            114.8
                        ]
                    },
                    "properties": {
                        "elevation": 39.0,
                        "index": 92,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 114.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.306201,
                            24.146627,
                            115.8
                        ]
                    },
                    "properties": {
                        "elevation": 40.0,
                        "index": 93,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 115.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.306012,
                            24.146627,
                            115.8
                        ]
                    },
                    "properties": {
                        "elevation": 40.0,
                        "index": 94,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 115.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305822,
                            24.146627,
                            114.8
                        ]
                    },
                    "properties": {
                        "elevation": 39.0,
                        "index": 95,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 114.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305633,
                            24.146627,
                            114.8
                        ]
                    },
                    "properties": {
                        "elevation": 39.0,
                        "index": 96,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 114.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305443,
                            24.146627,
                            115.8
                        ]
                    },
                    "properties": {
                        "elevation": 40.0,
                        "index": 97,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 115.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305254,
                            24.146627,
                            116.8
                        ]
                    },
                    "properties": {
                        "elevation": 41.0,
                        "index": 98,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 116.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305064,
                            24.146627,
                            116.8
                        ]
                    },
                    "properties": {
                        "elevation": 41.0,
                        "index": 99,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 116.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304875,
                            24.146627,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 100,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304686,
                            24.146627,
                            116.8
                        ]
                    },
                    "properties": {
                        "elevation": 41.0,
                        "index": 101,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 116.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304496,
                            24.146627,
                            116.8
                        ]
                    },
                    "properties": {
                        "elevation": 41.0,
                        "index": 102,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 116.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304307,
                            24.146627,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 103,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304117,
                            24.146627,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 104,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.303928,
                            24.146627,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 105,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.303738,
                            24.146627,
                            119.8
                        ]
                    },
                    "properties": {
                        "elevation": 44.0,
                        "index": 106,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 119.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.303549,
                            24.146627,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 107,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.303359,
                            24.146627,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 108,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.30317,
                            24.146627,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 109,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.30298,
                            24.146627,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 110,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.302791,
                            24.146627,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 111,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.302602,
                            24.146627,
                            119.8
                        ]
                    },
                    "properties": {
                        "elevation": 44.0,
                        "index": 112,
                        "heading": -90,
                        "take_photo": false,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 119.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.302791,
                            24.146259,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 113,
                        "heading": 90,
                        "take_photo": false,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.302791,
                            24.146259,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 114,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.30298,
                            24.146259,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 115,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.30317,
                            24.146259,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 116,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.303359,
                            24.146259,
                            119.8
                        ]
                    },
                    "properties": {
                        "elevation": 44.0,
                        "index": 117,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 119.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.303549,
                            24.146259,
                            119.8
                        ]
                    },
                    "properties": {
                        "elevation": 44.0,
                        "index": 118,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 119.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.303738,
                            24.146259,
                            119.8
                        ]
                    },
                    "properties": {
                        "elevation": 44.0,
                        "index": 119,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 119.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.303928,
                            24.146259,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 120,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304117,
                            24.146259,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 121,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304307,
                            24.146259,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 122,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304496,
                            24.146259,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 123,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304686,
                            24.146259,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 124,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304875,
                            24.146259,
                            116.8
                        ]
                    },
                    "properties": {
                        "elevation": 41.0,
                        "index": 125,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 116.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305064,
                            24.146259,
                            114.8
                        ]
                    },
                    "properties": {
                        "elevation": 39.0,
                        "index": 126,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 114.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305254,
                            24.146259,
                            114.8
                        ]
                    },
                    "properties": {
                        "elevation": 39.0,
                        "index": 127,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 114.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305443,
                            24.146259,
                            116.8
                        ]
                    },
                    "properties": {
                        "elevation": 41.0,
                        "index": 128,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 116.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305633,
                            24.146259,
                            115.8
                        ]
                    },
                    "properties": {
                        "elevation": 40.0,
                        "index": 129,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 115.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305822,
                            24.146259,
                            115.8
                        ]
                    },
                    "properties": {
                        "elevation": 40.0,
                        "index": 130,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 115.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.306012,
                            24.146259,
                            114.8
                        ]
                    },
                    "properties": {
                        "elevation": 39.0,
                        "index": 131,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 114.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.306201,
                            24.146259,
                            115.8
                        ]
                    },
                    "properties": {
                        "elevation": 40.0,
                        "index": 132,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 115.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.306391,
                            24.146259,
                            115.8
                        ]
                    },
                    "properties": {
                        "elevation": 40.0,
                        "index": 133,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 115.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.30658,
                            24.146259,
                            115.8
                        ]
                    },
                    "properties": {
                        "elevation": 40.0,
                        "index": 134,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 115.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.30677,
                            24.146259,
                            113.8
                        ]
                    },
                    "properties": {
                        "elevation": 38.0,
                        "index": 135,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 113.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.306959,
                            24.146259,
                            113.8
                        ]
                    },
                    "properties": {
                        "elevation": 38.0,
                        "index": 136,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 113.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.307148,
                            24.146259,
                            113.8
                        ]
                    },
                    "properties": {
                        "elevation": 38.0,
                        "index": 137,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 113.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.307338,
                            24.146259,
                            113.8
                        ]
                    },
                    "properties": {
                        "elevation": 38.0,
                        "index": 138,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 113.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.307527,
                            24.146259,
                            113.8
                        ]
                    },
                    "properties": {
                        "elevation": 38.0,
                        "index": 139,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 113.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.307717,
                            24.146259,
                            113.8
                        ]
                    },
                    "properties": {
                        "elevation": 38.0,
                        "index": 140,
                        "heading": 90,
                        "take_photo": false,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 113.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.307527,
                            24.145891,
                            113.8
                        ]
                    },
                    "properties": {
                        "elevation": 38.0,
                        "index": 141,
                        "heading": -90,
                        "take_photo": false,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 113.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.307527,
                            24.145891,
                            113.8
                        ]
                    },
                    "properties": {
                        "elevation": 38.0,
                        "index": 142,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 113.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.307338,
                            24.145891,
                            114.8
                        ]
                    },
                    "properties": {
                        "elevation": 39.0,
                        "index": 143,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 114.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.307148,
                            24.145891,
                            114.8
                        ]
                    },
                    "properties": {
                        "elevation": 39.0,
                        "index": 144,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 114.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.306959,
                            24.145891,
                            114.8
                        ]
                    },
                    "properties": {
                        "elevation": 39.0,
                        "index": 145,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 114.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.30677,
                            24.145891,
                            114.8
                        ]
                    },
                    "properties": {
                        "elevation": 39.0,
                        "index": 146,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 114.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.30658,
                            24.145891,
                            115.8
                        ]
                    },
                    "properties": {
                        "elevation": 40.0,
                        "index": 147,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 115.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.306391,
                            24.145891,
                            115.8
                        ]
                    },
                    "properties": {
                        "elevation": 40.0,
                        "index": 148,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 115.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.306201,
                            24.145891,
                            115.8
                        ]
                    },
                    "properties": {
                        "elevation": 40.0,
                        "index": 149,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 115.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.306012,
                            24.145891,
                            115.8
                        ]
                    },
                    "properties": {
                        "elevation": 40.0,
                        "index": 150,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 115.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305822,
                            24.145891,
                            115.8
                        ]
                    },
                    "properties": {
                        "elevation": 40.0,
                        "index": 151,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 115.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305633,
                            24.145891,
                            115.8
                        ]
                    },
                    "properties": {
                        "elevation": 40.0,
                        "index": 152,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 115.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305443,
                            24.145891,
                            116.8
                        ]
                    },
                    "properties": {
                        "elevation": 41.0,
                        "index": 153,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 116.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305254,
                            24.145891,
                            115.8
                        ]
                    },
                    "properties": {
                        "elevation": 40.0,
                        "index": 154,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 115.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305064,
                            24.145891,
                            115.8
                        ]
                    },
                    "properties": {
                        "elevation": 40.0,
                        "index": 155,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 115.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304875,
                            24.145891,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 156,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304686,
                            24.145891,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 157,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304496,
                            24.145891,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 158,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304307,
                            24.145891,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 159,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304117,
                            24.145891,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 160,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.303928,
                            24.145891,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 161,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.303738,
                            24.145891,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 162,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.303549,
                            24.145891,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 163,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.303359,
                            24.145891,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 164,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.30317,
                            24.145891,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 165,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.30298,
                            24.145891,
                            120.8
                        ]
                    },
                    "properties": {
                        "elevation": 45.0,
                        "index": 166,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 120.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.302791,
                            24.145891,
                            120.8
                        ]
                    },
                    "properties": {
                        "elevation": 45.0,
                        "index": 167,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 120.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.302602,
                            24.145891,
                            119.8
                        ]
                    },
                    "properties": {
                        "elevation": 44.0,
                        "index": 168,
                        "heading": -90,
                        "take_photo": false,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 119.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.302791,
                            24.145523,
                            119.8
                        ]
                    },
                    "properties": {
                        "elevation": 44.0,
                        "index": 169,
                        "heading": 90,
                        "take_photo": false,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 119.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.302791,
                            24.145523,
                            119.8
                        ]
                    },
                    "properties": {
                        "elevation": 44.0,
                        "index": 170,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 119.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.30298,
                            24.145523,
                            119.8
                        ]
                    },
                    "properties": {
                        "elevation": 44.0,
                        "index": 171,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 119.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.30317,
                            24.145523,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 172,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.303359,
                            24.145523,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 173,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.303549,
                            24.145523,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 174,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.303738,
                            24.145523,
                            116.8
                        ]
                    },
                    "properties": {
                        "elevation": 41.0,
                        "index": 175,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 116.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.303928,
                            24.145523,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 176,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304117,
                            24.145523,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 177,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304307,
                            24.145523,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 178,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304496,
                            24.145523,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 179,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304686,
                            24.145523,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 180,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304875,
                            24.145523,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 181,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305064,
                            24.145523,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 182,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305254,
                            24.145523,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 183,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305443,
                            24.145523,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 184,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305633,
                            24.145523,
                            116.8
                        ]
                    },
                    "properties": {
                        "elevation": 41.0,
                        "index": 185,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 116.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305822,
                            24.145523,
                            116.8
                        ]
                    },
                    "properties": {
                        "elevation": 41.0,
                        "index": 186,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 116.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.306012,
                            24.145523,
                            115.8
                        ]
                    },
                    "properties": {
                        "elevation": 40.0,
                        "index": 187,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 115.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.306201,
                            24.145523,
                            115.8
                        ]
                    },
                    "properties": {
                        "elevation": 40.0,
                        "index": 188,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 115.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.306391,
                            24.145523,
                            114.8
                        ]
                    },
                    "properties": {
                        "elevation": 39.0,
                        "index": 189,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 114.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.30658,
                            24.145523,
                            114.8
                        ]
                    },
                    "properties": {
                        "elevation": 39.0,
                        "index": 190,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 114.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.30677,
                            24.145523,
                            116.8
                        ]
                    },
                    "properties": {
                        "elevation": 41.0,
                        "index": 191,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 116.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.306959,
                            24.145523,
                            114.8
                        ]
                    },
                    "properties": {
                        "elevation": 39.0,
                        "index": 192,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 114.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.307148,
                            24.145523,
                            114.8
                        ]
                    },
                    "properties": {
                        "elevation": 39.0,
                        "index": 193,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 114.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.307338,
                            24.145523,
                            114.8
                        ]
                    },
                    "properties": {
                        "elevation": 39.0,
                        "index": 194,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 114.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.307527,
                            24.145523,
                            111.8
                        ]
                    },
                    "properties": {
                        "elevation": 36.0,
                        "index": 195,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 111.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.307717,
                            24.145523,
                            111.8
                        ]
                    },
                    "properties": {
                        "elevation": 36.0,
                        "index": 196,
                        "heading": 90,
                        "take_photo": false,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 111.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.307527,
                            24.145155,
                            112.8
                        ]
                    },
                    "properties": {
                        "elevation": 37.0,
                        "index": 197,
                        "heading": -90,
                        "take_photo": false,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 112.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.307527,
                            24.145155,
                            112.8
                        ]
                    },
                    "properties": {
                        "elevation": 37.0,
                        "index": 198,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 112.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.307338,
                            24.145155,
                            114.8
                        ]
                    },
                    "properties": {
                        "elevation": 39.0,
                        "index": 199,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 114.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.307148,
                            24.145155,
                            115.8
                        ]
                    },
                    "properties": {
                        "elevation": 40.0,
                        "index": 200,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 115.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.306959,
                            24.145155,
                            115.8
                        ]
                    },
                    "properties": {
                        "elevation": 40.0,
                        "index": 201,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 115.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.30677,
                            24.145155,
                            114.8
                        ]
                    },
                    "properties": {
                        "elevation": 39.0,
                        "index": 202,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 114.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.30658,
                            24.145155,
                            115.8
                        ]
                    },
                    "properties": {
                        "elevation": 40.0,
                        "index": 203,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 115.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.306391,
                            24.145155,
                            115.8
                        ]
                    },
                    "properties": {
                        "elevation": 40.0,
                        "index": 204,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 115.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.306201,
                            24.145155,
                            114.8
                        ]
                    },
                    "properties": {
                        "elevation": 39.0,
                        "index": 205,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 114.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.306012,
                            24.145155,
                            115.8
                        ]
                    },
                    "properties": {
                        "elevation": 40.0,
                        "index": 206,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 115.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305822,
                            24.145155,
                            115.8
                        ]
                    },
                    "properties": {
                        "elevation": 40.0,
                        "index": 207,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 115.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305633,
                            24.145155,
                            115.8
                        ]
                    },
                    "properties": {
                        "elevation": 40.0,
                        "index": 208,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 115.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305443,
                            24.145155,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 209,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305254,
                            24.145155,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 210,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305064,
                            24.145155,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 211,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304875,
                            24.145155,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 212,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304686,
                            24.145155,
                            116.8
                        ]
                    },
                    "properties": {
                        "elevation": 41.0,
                        "index": 213,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 116.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304496,
                            24.145155,
                            116.8
                        ]
                    },
                    "properties": {
                        "elevation": 41.0,
                        "index": 214,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 116.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304307,
                            24.145155,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 215,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304117,
                            24.145155,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 216,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.303928,
                            24.145155,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 217,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.303738,
                            24.145155,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 218,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.303549,
                            24.145155,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 219,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.303359,
                            24.145155,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 220,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.30317,
                            24.145155,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 221,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.30298,
                            24.145155,
                            120.8
                        ]
                    },
                    "properties": {
                        "elevation": 45.0,
                        "index": 222,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 120.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.302791,
                            24.145155,
                            120.8
                        ]
                    },
                    "properties": {
                        "elevation": 45.0,
                        "index": 223,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 120.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.302602,
                            24.145155,
                            122.8
                        ]
                    },
                    "properties": {
                        "elevation": 47.0,
                        "index": 224,
                        "heading": -90,
                        "take_photo": false,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 122.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.302791,
                            24.144787,
                            119.8
                        ]
                    },
                    "properties": {
                        "elevation": 44.0,
                        "index": 225,
                        "heading": 90,
                        "take_photo": false,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 119.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.302791,
                            24.144787,
                            119.8
                        ]
                    },
                    "properties": {
                        "elevation": 44.0,
                        "index": 226,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 119.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.30298,
                            24.144787,
                            119.8
                        ]
                    },
                    "properties": {
                        "elevation": 44.0,
                        "index": 227,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 119.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.30317,
                            24.144787,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 228,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.303359,
                            24.144787,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 229,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.303549,
                            24.144787,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 230,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.303738,
                            24.144787,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 231,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.303928,
                            24.144787,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 232,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304117,
                            24.144787,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 233,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304307,
                            24.144787,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 234,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304496,
                            24.144787,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 235,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304686,
                            24.144787,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 236,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304875,
                            24.144787,
                            116.8
                        ]
                    },
                    "properties": {
                        "elevation": 41.0,
                        "index": 237,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 116.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305064,
                            24.144787,
                            116.8
                        ]
                    },
                    "properties": {
                        "elevation": 41.0,
                        "index": 238,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 116.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305254,
                            24.144787,
                            116.8
                        ]
                    },
                    "properties": {
                        "elevation": 41.0,
                        "index": 239,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 116.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305443,
                            24.144787,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 240,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305633,
                            24.144787,
                            116.8
                        ]
                    },
                    "properties": {
                        "elevation": 41.0,
                        "index": 241,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 116.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305822,
                            24.144787,
                            116.8
                        ]
                    },
                    "properties": {
                        "elevation": 41.0,
                        "index": 242,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 116.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.306012,
                            24.144787,
                            114.8
                        ]
                    },
                    "properties": {
                        "elevation": 39.0,
                        "index": 243,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 114.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.306201,
                            24.144787,
                            116.8
                        ]
                    },
                    "properties": {
                        "elevation": 41.0,
                        "index": 244,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 116.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.306391,
                            24.144787,
                            116.8
                        ]
                    },
                    "properties": {
                        "elevation": 41.0,
                        "index": 245,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 116.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.30658,
                            24.144787,
                            116.8
                        ]
                    },
                    "properties": {
                        "elevation": 41.0,
                        "index": 246,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 116.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.30677,
                            24.144787,
                            113.8
                        ]
                    },
                    "properties": {
                        "elevation": 38.0,
                        "index": 247,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 113.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.306959,
                            24.144787,
                            113.8
                        ]
                    },
                    "properties": {
                        "elevation": 38.0,
                        "index": 248,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 113.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.307148,
                            24.144787,
                            113.8
                        ]
                    },
                    "properties": {
                        "elevation": 38.0,
                        "index": 249,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 113.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.307338,
                            24.144787,
                            115.8
                        ]
                    },
                    "properties": {
                        "elevation": 40.0,
                        "index": 250,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 115.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.307527,
                            24.144787,
                            114.8
                        ]
                    },
                    "properties": {
                        "elevation": 39.0,
                        "index": 251,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 114.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.307717,
                            24.144787,
                            114.8
                        ]
                    },
                    "properties": {
                        "elevation": 39.0,
                        "index": 252,
                        "heading": 90,
                        "take_photo": false,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 114.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.307527,
                            24.144419,
                            112.8
                        ]
                    },
                    "properties": {
                        "elevation": 37.0,
                        "index": 253,
                        "heading": -90,
                        "take_photo": false,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 112.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.307527,
                            24.144419,
                            112.8
                        ]
                    },
                    "properties": {
                        "elevation": 37.0,
                        "index": 254,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 112.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.307338,
                            24.144419,
                            111.8
                        ]
                    },
                    "properties": {
                        "elevation": 36.0,
                        "index": 255,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 111.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.307148,
                            24.144419,
                            113.8
                        ]
                    },
                    "properties": {
                        "elevation": 38.0,
                        "index": 256,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 113.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.306959,
                            24.144419,
                            113.8
                        ]
                    },
                    "properties": {
                        "elevation": 38.0,
                        "index": 257,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 113.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.30677,
                            24.144419,
                            112.8
                        ]
                    },
                    "properties": {
                        "elevation": 37.0,
                        "index": 258,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 112.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.30658,
                            24.144419,
                            114.8
                        ]
                    },
                    "properties": {
                        "elevation": 39.0,
                        "index": 259,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 114.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.306391,
                            24.144419,
                            114.8
                        ]
                    },
                    "properties": {
                        "elevation": 39.0,
                        "index": 260,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 114.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.306201,
                            24.144419,
                            114.8
                        ]
                    },
                    "properties": {
                        "elevation": 39.0,
                        "index": 261,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 114.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.306012,
                            24.144419,
                            115.8
                        ]
                    },
                    "properties": {
                        "elevation": 40.0,
                        "index": 262,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 115.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305822,
                            24.144419,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 263,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305633,
                            24.144419,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 264,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305443,
                            24.144419,
                            116.8
                        ]
                    },
                    "properties": {
                        "elevation": 41.0,
                        "index": 265,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 116.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305254,
                            24.144419,
                            114.8
                        ]
                    },
                    "properties": {
                        "elevation": 39.0,
                        "index": 266,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 114.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305064,
                            24.144419,
                            114.8
                        ]
                    },
                    "properties": {
                        "elevation": 39.0,
                        "index": 267,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 114.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304875,
                            24.144419,
                            115.8
                        ]
                    },
                    "properties": {
                        "elevation": 40.0,
                        "index": 268,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 115.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304686,
                            24.144419,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 269,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304496,
                            24.144419,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 270,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304307,
                            24.144419,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 271,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304117,
                            24.144419,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 272,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.303928,
                            24.144419,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 273,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.303738,
                            24.144419,
                            119.8
                        ]
                    },
                    "properties": {
                        "elevation": 44.0,
                        "index": 274,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 119.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.303549,
                            24.144419,
                            120.8
                        ]
                    },
                    "properties": {
                        "elevation": 45.0,
                        "index": 275,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 120.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.303359,
                            24.144419,
                            120.8
                        ]
                    },
                    "properties": {
                        "elevation": 45.0,
                        "index": 276,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 120.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.30317,
                            24.144419,
                            119.8
                        ]
                    },
                    "properties": {
                        "elevation": 44.0,
                        "index": 277,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 119.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.30298,
                            24.144419,
                            120.8
                        ]
                    },
                    "properties": {
                        "elevation": 45.0,
                        "index": 278,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 120.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.302791,
                            24.144419,
                            120.8
                        ]
                    },
                    "properties": {
                        "elevation": 45.0,
                        "index": 279,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 120.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.302602,
                            24.144419,
                            122.8
                        ]
                    },
                    "properties": {
                        "elevation": 47.0,
                        "index": 280,
                        "heading": -90,
                        "take_photo": false,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 122.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.302791,
                            24.14405,
                            120.8
                        ]
                    },
                    "properties": {
                        "elevation": 45.0,
                        "index": 281,
                        "heading": 90,
                        "take_photo": false,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 120.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.302791,
                            24.14405,
                            120.8
                        ]
                    },
                    "properties": {
                        "elevation": 45.0,
                        "index": 282,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 120.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.30298,
                            24.14405,
                            120.8
                        ]
                    },
                    "properties": {
                        "elevation": 45.0,
                        "index": 283,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 120.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.30317,
                            24.14405,
                            120.8
                        ]
                    },
                    "properties": {
                        "elevation": 45.0,
                        "index": 284,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 120.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.303359,
                            24.14405,
                            119.8
                        ]
                    },
                    "properties": {
                        "elevation": 44.0,
                        "index": 285,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 119.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.303549,
                            24.14405,
                            119.8
                        ]
                    },
                    "properties": {
                        "elevation": 44.0,
                        "index": 286,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 119.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.303738,
                            24.14405,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 287,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.303928,
                            24.14405,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 288,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304117,
                            24.14405,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 289,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304307,
                            24.14405,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 290,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304496,
                            24.14405,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 291,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304686,
                            24.14405,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 292,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304875,
                            24.14405,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 293,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305064,
                            24.14405,
                            115.8
                        ]
                    },
                    "properties": {
                        "elevation": 40.0,
                        "index": 294,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 115.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305254,
                            24.14405,
                            115.8
                        ]
                    },
                    "properties": {
                        "elevation": 40.0,
                        "index": 295,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 115.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305443,
                            24.14405,
                            114.8
                        ]
                    },
                    "properties": {
                        "elevation": 39.0,
                        "index": 296,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 114.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305633,
                            24.14405,
                            114.8
                        ]
                    },
                    "properties": {
                        "elevation": 39.0,
                        "index": 297,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 114.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305822,
                            24.14405,
                            114.8
                        ]
                    },
                    "properties": {
                        "elevation": 39.0,
                        "index": 298,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 114.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.306012,
                            24.14405,
                            114.8
                        ]
                    },
                    "properties": {
                        "elevation": 39.0,
                        "index": 299,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 114.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.306201,
                            24.14405,
                            114.8
                        ]
                    },
                    "properties": {
                        "elevation": 39.0,
                        "index": 300,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 114.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.306391,
                            24.14405,
                            114.8
                        ]
                    },
                    "properties": {
                        "elevation": 39.0,
                        "index": 301,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 114.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.30658,
                            24.14405,
                            114.8
                        ]
                    },
                    "properties": {
                        "elevation": 39.0,
                        "index": 302,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 114.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.30677,
                            24.14405,
                            113.8
                        ]
                    },
                    "properties": {
                        "elevation": 38.0,
                        "index": 303,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 113.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.306959,
                            24.14405,
                            112.8
                        ]
                    },
                    "properties": {
                        "elevation": 37.0,
                        "index": 304,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 112.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.307148,
                            24.14405,
                            112.8
                        ]
                    },
                    "properties": {
                        "elevation": 37.0,
                        "index": 305,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 112.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.307338,
                            24.14405,
                            110.8
                        ]
                    },
                    "properties": {
                        "elevation": 35.0,
                        "index": 306,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 110.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.307527,
                            24.14405,
                            110.8
                        ]
                    },
                    "properties": {
                        "elevation": 35.0,
                        "index": 307,
                        "heading": 90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 110.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.307717,
                            24.14405,
                            110.8
                        ]
                    },
                    "properties": {
                        "elevation": 35.0,
                        "index": 308,
                        "heading": 90,
                        "take_photo": false,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 110.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.307527,
                            24.143682,
                            113.8
                        ]
                    },
                    "properties": {
                        "elevation": 38.0,
                        "index": 309,
                        "heading": -90,
                        "take_photo": false,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 113.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.307527,
                            24.143682,
                            113.8
                        ]
                    },
                    "properties": {
                        "elevation": 38.0,
                        "index": 310,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 113.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.307338,
                            24.143682,
                            111.8
                        ]
                    },
                    "properties": {
                        "elevation": 36.0,
                        "index": 311,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 111.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.307148,
                            24.143682,
                            111.8
                        ]
                    },
                    "properties": {
                        "elevation": 36.0,
                        "index": 312,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 111.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.306959,
                            24.143682,
                            111.8
                        ]
                    },
                    "properties": {
                        "elevation": 36.0,
                        "index": 313,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 111.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.30677,
                            24.143682,
                            114.8
                        ]
                    },
                    "properties": {
                        "elevation": 39.0,
                        "index": 314,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 114.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.30658,
                            24.143682,
                            115.8
                        ]
                    },
                    "properties": {
                        "elevation": 40.0,
                        "index": 315,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 115.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.306391,
                            24.143682,
                            115.8
                        ]
                    },
                    "properties": {
                        "elevation": 40.0,
                        "index": 316,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 115.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.306201,
                            24.143682,
                            116.8
                        ]
                    },
                    "properties": {
                        "elevation": 41.0,
                        "index": 317,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 116.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.306012,
                            24.143682,
                            115.8
                        ]
                    },
                    "properties": {
                        "elevation": 40.0,
                        "index": 318,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 115.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305822,
                            24.143682,
                            115.8
                        ]
                    },
                    "properties": {
                        "elevation": 40.0,
                        "index": 319,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 115.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305633,
                            24.143682,
                            115.8
                        ]
                    },
                    "properties": {
                        "elevation": 40.0,
                        "index": 320,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 115.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305443,
                            24.143682,
                            114.8
                        ]
                    },
                    "properties": {
                        "elevation": 39.0,
                        "index": 321,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 114.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305254,
                            24.143682,
                            116.8
                        ]
                    },
                    "properties": {
                        "elevation": 41.0,
                        "index": 322,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 116.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.305064,
                            24.143682,
                            116.8
                        ]
                    },
                    "properties": {
                        "elevation": 41.0,
                        "index": 323,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 116.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304875,
                            24.143682,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 324,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304686,
                            24.143682,
                            116.8
                        ]
                    },
                    "properties": {
                        "elevation": 41.0,
                        "index": 325,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 116.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304496,
                            24.143682,
                            116.8
                        ]
                    },
                    "properties": {
                        "elevation": 41.0,
                        "index": 326,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 116.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304307,
                            24.143682,
                            117.8
                        ]
                    },
                    "properties": {
                        "elevation": 42.0,
                        "index": 327,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 117.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.304117,
                            24.143682,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 328,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.303928,
                            24.143682,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 329,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.303738,
                            24.143682,
                            119.8
                        ]
                    },
                    "properties": {
                        "elevation": 44.0,
                        "index": 330,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 119.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.303549,
                            24.143682,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 331,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.303359,
                            24.143682,
                            118.8
                        ]
                    },
                    "properties": {
                        "elevation": 43.0,
                        "index": 332,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 118.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.30317,
                            24.143682,
                            119.8
                        ]
                    },
                    "properties": {
                        "elevation": 44.0,
                        "index": 333,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 119.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.30298,
                            24.143682,
                            119.8
                        ]
                    },
                    "properties": {
                        "elevation": 44.0,
                        "index": 334,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 119.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.302791,
                            24.143682,
                            119.8
                        ]
                    },
                    "properties": {
                        "elevation": 44.0,
                        "index": 335,
                        "heading": -90,
                        "take_photo": true,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 119.8
                    }
                },
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -110.302602,
                            24.143682,
                            119.8
                        ]
                    },
                    "properties": {
                        "elevation": 44.0,
                        "index": 336,
                        "heading": -90,
                        "take_photo": false,
                        "gimbal_angle": "-90",
                        "speed": 10.54,
                        "altitude": 119.8
                    }
                }
            ]
        }
""".trimIndent()