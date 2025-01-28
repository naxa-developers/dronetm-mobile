package np.com.naxa.drone_tasking_manager.features.project_details.views.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.gson.JsonObject
import np.com.naxa.drone_tasking_manager.R
import np.com.naxa.drone_tasking_manager.core.widgets.MaplibreCompose
import np.com.naxa.drone_tasking_manager.core.widgets.rememberCameraPosition
import np.com.naxa.drone_tasking_manager.features.projects.models.Project
import np.com.naxa.drone_tasking_manager.features.projects.models.ProjectTaskState
import np.com.naxa.drone_tasking_manager.features.projects.models.toFeatureJsonStr
import np.com.naxa.drone_tasking_manager.features.projects.models.toLockedFeatureJsonStr
import np.com.naxa.drone_tasking_manager.features.projects.views.widgets.ProjectInfoBottomSheet
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.LayoutPropertyValue
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PaintPropertyValue
import org.maplibre.android.style.layers.PropertyValue
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailMapView(
    modifier: Modifier = Modifier,
    project: Project,
) {

    val context = LocalContext.current
    val sourceId = "project-geometry-${project.id}"
    val tasksFillLayerId = "project-tasks-fill-layer-${project.id}"
    val tasksLineLayerId = "project-tasks-line-layer-${project.id}"
    val boundaryLineLayerId = "project-boundary-line-layer-${project.id}"

    val lockedSourceId = "locked-tasks-source-${project.id}"
    val lockedSymbolLayerId = "project-tasks-symbol-layer-${project.id}"
    val lockedIconName = "locked-icon--"

    var libreMap: MapLibreMap? by remember { mutableStateOf(null) }

    var infoJsonObject: JsonObject? by remember { mutableStateOf(null) }
    var showInfoBottomSheet by remember { mutableStateOf(false) }
    val infoSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)


    val cameraPositionState = rememberCameraPosition(
        initialTarget = LatLng(27.82, 85.32),
        initialZoom = 12.0,
    )

    fun applyLayers() {
        val tasks = project.tasks
        if (tasks.isEmpty()) return
        if (libreMap == null) return

        val bounds = project.geometry?.properties?.bbox

        if (!bounds.isNullOrEmpty() && bounds.size == 4) {
            val northeast = LatLng(bounds[1], bounds[0])
            val southwest = LatLng(bounds[3], bounds[2])

            libreMap?.animateCamera(
                CameraUpdateFactory.newLatLngBounds(
                    LatLngBounds.fromLatLngs(
                        listOf(northeast, southwest)
                    ),
                    20,
                    220,
                    20,
                    20
                ),
                500
            )
        }

        if (libreMap?.style?.getSource(sourceId) != null) {

            if (libreMap?.style?.getLayer(tasksFillLayerId) != null) {
                libreMap?.style?.removeLayer(tasksFillLayerId)
            }

            if (libreMap?.style?.getLayer(tasksLineLayerId) != null) {
                libreMap?.style?.removeLayer(tasksLineLayerId)
            }

            if (libreMap?.style?.getLayer(boundaryLineLayerId) != null) {
                libreMap?.style?.removeLayer(boundaryLineLayerId)
            }

            libreMap?.style?.removeSource(sourceId)
        }

        val features = mutableListOf<Feature>()
        if (project.geometry?.geometry != null) {
            features.add(Feature.fromJson(project.geometry.toFeatureJsonStr()))
        }
        features.addAll(tasks.mapNotNull { it.toFeatureJsonStr() }.map { Feature.fromJson(it) })

        libreMap?.style?.addSource(
            GeoJsonSource(
                sourceId,
                features = FeatureCollection.fromFeatures(features),
            )
        ).also {
            libreMap?.style?.addLayer(
                FillLayer(
                    tasksFillLayerId,
                    sourceId,
                ).apply {

                    val propertyValues = mutableListOf<PropertyValue<*>>()

                    propertyValues.add(PaintPropertyValue("fill-color", Expression.get("color")))
                    // propertyValues.add(PaintPropertyValue("fill-opacity", 0.025))
                    propertyValues.add(
                        PaintPropertyValue(
                            "fill-outline-color",
                            "#D73F3F"
                        )
                    )

                    withProperties(*propertyValues.toTypedArray())
                    withFilter(Expression.not(Expression.has("project_boundary")))
                }
            )

            libreMap?.style?.addLayer(
                LineLayer(
                    tasksLineLayerId,
                    sourceId,
                ).apply {

                    val propertyValues = mutableListOf<PropertyValue<*>>()

                    propertyValues.add(PaintPropertyValue("line-color", "#D73F3F"))
                    propertyValues.add(PaintPropertyValue("line-opacity", 0.1))

                    withProperties(*propertyValues.toTypedArray())
                    withFilter(Expression.not(Expression.has("project_boundary")))
                }
            )

            libreMap?.style?.addLayer(
                LineLayer(
                    boundaryLineLayerId,
                    sourceId,
                ).apply {

                    val propertyValues = mutableListOf<PropertyValue<*>>()

                    propertyValues.add(PaintPropertyValue("line-color", "#D73F3F"))

                    withProperties(*propertyValues.toTypedArray())
                    withFilter(Expression.has("project_boundary"))
                }
            )
        }

    }

    fun applyLockedIconLayer() {
        val tasks = project.tasks
        if (tasks.none { it.state == ProjectTaskState.LockedForMapping }) return
        if (libreMap == null) return

        if (libreMap?.style?.getSource(lockedSourceId) != null) {

            if (libreMap?.style?.getLayer(lockedSymbolLayerId) != null) {
                libreMap?.style?.removeLayer(lockedSymbolLayerId)
            }

            if (libreMap?.style?.getImage(lockedIconName) != null) {
                libreMap?.style?.removeImage(lockedIconName)
            }

            libreMap?.style?.removeSource(lockedSourceId)
        }

        val features = mutableListOf<Feature>()

        features.addAll(tasks.filter { it.state == ProjectTaskState.LockedForMapping }
            .mapNotNull { it.toLockedFeatureJsonStr() }.map { Feature.fromJson(it) })

        libreMap?.style?.addSource(
            GeoJsonSource(
                lockedSourceId,
                features = FeatureCollection.fromFeatures(features),
            )
        ).also {

            ContextCompat.getDrawable(context, R.drawable.outline_lock_24)?.let { drawable ->
                libreMap?.style?.addImage(lockedIconName, drawable)
            }

            libreMap?.style?.addLayer(
                SymbolLayer(
                    lockedSymbolLayerId,
                    lockedSourceId,
                ).apply {

                    val propertyValues = mutableListOf<PropertyValue<*>>()

                    propertyValues.add(LayoutPropertyValue("icon-image", lockedIconName))
                    propertyValues.add(LayoutPropertyValue("icon-size", 0.75))

                    withProperties(*propertyValues.toTypedArray())
                }
            )
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

                applyLayers()
                applyLockedIconLayer()

                libre.addOnMapClickListener { latLng ->
                    val point = libre.projection.toScreenLocation(latLng)
                    val queried = libre.queryRenderedFeatures(
                        point, *listOf(tasksFillLayerId).toTypedArray()
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

        ExpandableLegendsView(
            modifier = Modifier
                .align(
                    Alignment.BottomStart
                )
                .fillMaxWidth(0.45f)
                .wrapContentHeight()
                .padding(12.dp)
                .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp))

        )

        ProjectTaskInfoBottomSheet(
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