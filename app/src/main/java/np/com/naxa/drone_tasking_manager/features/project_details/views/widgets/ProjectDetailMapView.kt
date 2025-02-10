package np.com.naxa.drone_tasking_manager.features.project_details.views.widgets

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.gson.JsonObject
import np.com.naxa.drone_tasking_manager.R
import np.com.naxa.drone_tasking_manager.core.widgets.MaplibreCompose
import np.com.naxa.drone_tasking_manager.core.widgets.rememberCameraPosition
import np.com.naxa.drone_tasking_manager.features.projects.models.Project
import np.com.naxa.drone_tasking_manager.features.projects.models.toFeatureJsonStr
import np.com.naxa.drone_tasking_manager.features.tasks.models.ProjectTaskState
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

@Composable
fun ProjectDetailMapView(
    modifier: Modifier = Modifier,
    project: Project,
    onFeatureClick: (JsonObject?) -> Unit
) {

    val context = LocalContext.current

    var libreMap: MapLibreMap? by remember { mutableStateOf(null) }

    val cameraPositionState = rememberCameraPosition(
        initialTarget = LatLng(27.82, 85.32),
        initialZoom = 12.0,
    )

    // Map Click Listener
    DisposableEffect(libreMap) {
        val clickListener = MapLibreMap.OnMapClickListener { latLng ->
            val point = libreMap?.projection?.toScreenLocation(latLng)
            val queried = point?.let {
                libreMap?.queryRenderedFeatures(
                    it, *listOf("project-tasks-fill-layer---").toTypedArray()
                )
            }

            if (!queried.isNullOrEmpty()) {
                val feature = queried.first()
                if (feature.properties()?.has("point_count") == false) {
                    onFeatureClick.invoke(feature.properties())
                }
            }

            true
        }

        libreMap?.addOnMapClickListener(clickListener)

        onDispose {
            libreMap?.removeOnMapClickListener(clickListener)
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

                applyLayers(libreMap, project)
                applyLockedIconLayer(context, libreMap, project)
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
    }
}


/**
 * Applies layers to the provided MapLibre map to visualize project and task geometries.
 *
 * This function takes a MapLibreMap instance and a Project object as input. It then visualizes the project and its associated tasks
 * on the map by adding GeoJSON data as a source and creating fill and line layers for rendering.
 *
 * The function performs the following actions:
 * 1. **Handles Empty or Null Cases:**
 *    - Returns early if the project has no tasks or if the `libreMap` is null, preventing unnecessary operations.
 * 2. **Defines Layer and Source IDs:**
 *    - Sets up unique IDs for the GeoJSON source, the task fill layer, the task line layer, and the project boundary line layer.
 *      These IDs are used to identify and manipulate the layers and source later.
 * 3. **Zooms to Project Bounds (if available):**
 *    - Checks if the project geometry has bounding box (bbox) information.
 *    - If bounds are present, it calculates the northeast and southwest corners and animates the map's camera to fit the project's bounds.
 *    - The camera animation ensures that the entire project area is visible within the map's viewport.
 * 4. **Clears Existing Layers and Source:**
 *    - Checks if a source with the defined `sourceId` already exists in the map's style.
 *    - If the source exists, it also checks for the existence of the fill layer, line layer and boundary layers.
 *    - If any of the layers or the source exist, they are removed from the map's style before adding new ones. This ensures that layers are not duplicated.
 * 5. **Prepares GeoJSON Data:**
 *    - Creates a list of `Feature` objects to be added to the GeoJSON source.
 *    - Adds the project's geometry (if available) as a Feature.
 *    - Converts each task in the project to a `Feature` and adds them to the list.
 * 6. **Adds GeoJSON Source:**
 *    - Adds a `GeoJsonSource` to the map's style using the generated GeoJSON data.
 * 7. **Adds Fill Layer for Tasks:**
 *    - Adds a `FillLayer` to the map for visualizing the filled areas of the task geometries.
 *
 * @param libreMap The MapLibreMap instance to which the locked icon layer will be added.
 * Can be null, in which case the function will return.
 * @param project The Project containing the tasks. The function will iterate through the tasks
 * and add an icon for the locked ones.
 *
 */
private fun applyLayers(libreMap: MapLibreMap?, project: Project) {
    val tasks = project.tasks
    if (tasks.isEmpty()) return
    if (libreMap == null) return

    val sourceId = "project-geometry---"
    val tasksFillLayerId = "project-tasks-fill-layer---"
    val tasksLineLayerId = "project-tasks-line-layer---"
    val boundaryLineLayerId = "project-boundary-line-layer---"

    val bounds = project.geometry?.properties?.bbox

    if (!bounds.isNullOrEmpty() && bounds.size == 4) {
        val northeast = LatLng(bounds[1], bounds[0])
        val southwest = LatLng(bounds[3], bounds[2])

        libreMap.animateCamera(
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

    if (libreMap.style?.getSource(sourceId) != null) {

        if (libreMap.style?.getLayer(tasksFillLayerId) != null) {
            libreMap.style?.removeLayer(tasksFillLayerId)
        }

        if (libreMap.style?.getLayer(tasksLineLayerId) != null) {
            libreMap.style?.removeLayer(tasksLineLayerId)
        }

        if (libreMap.style?.getLayer(boundaryLineLayerId) != null) {
            libreMap.style?.removeLayer(boundaryLineLayerId)
        }

        libreMap.style?.removeSource(sourceId)
    }

    val features = mutableListOf<Feature>()
    if (project.geometry?.geometry != null) {
        features.add(Feature.fromJson(project.geometry.toFeatureJsonStr()))
    }
    features.addAll(tasks.mapNotNull { it.toFeatureJsonStr() }.map { Feature.fromJson(it) })

    libreMap.style?.addSource(
        GeoJsonSource(
            sourceId,
            features = FeatureCollection.fromFeatures(features),
        )
    ).also {
        libreMap.style?.addLayer(
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

        libreMap.style?.addLayer(
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

        libreMap.style?.addLayer(
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

/**
 * Applies a locked icon layer to the map to indicate tasks that are locked for mapping.
 *
 * This function adds a symbol layer to the provided [libreMap] to visually represent
 * tasks within the given [project] that are in the `LockedForMapping` state.
 * It first checks if any tasks are locked and if a MapLibreMap is provided. If not it will return.
 * It then clears any existing locked icon layer, source, and image to prevent duplicates.
 * After clearing it creates a GeoJsonSource from the locked tasks and a symbol layer that
 * uses a lock icon to represent these tasks on the map.
 *
 * @param context The Android context, used for accessing resources like the lock icon drawable.
 * @param libreMap The MapLibreMap instance to which the locked icon layer will be added. Can be null, in which case the function will return.
 * @param project The Project containing the tasks. The function will iterate through the tasks and add an icon for the locked ones.
 */
private fun applyLockedIconLayer(context: Context, libreMap: MapLibreMap?, project: Project) {
    val tasks = project.tasks
    if (tasks.none { it.state == ProjectTaskState.LockedForMapping }) return
    if (libreMap == null) return

    val lockedSourceId = "locked-tasks-source---"
    val lockedSymbolLayerId = "project-tasks-symbol-layer---"
    val lockedIconName = "locked-icon---"

    if (libreMap.style?.getSource(lockedSourceId) != null) {

        if (libreMap.style?.getLayer(lockedSymbolLayerId) != null) {
            libreMap.style?.removeLayer(lockedSymbolLayerId)
        }

        if (libreMap.style?.getImage(lockedIconName) != null) {
            libreMap.style?.removeImage(lockedIconName)
        }

        libreMap.style?.removeSource(lockedSourceId)
    }

    val features = mutableListOf<Feature>()

    features.addAll(tasks.filter { it.state == ProjectTaskState.LockedForMapping }
        .mapNotNull { it.toLockedFeatureJsonStr() }.map { Feature.fromJson(it) })

    libreMap.style?.addSource(
        GeoJsonSource(
            lockedSourceId,
            features = FeatureCollection.fromFeatures(features),
        )
    ).also {

        ContextCompat.getDrawable(context, R.drawable.outline_lock_24)?.let { drawable ->
            libreMap.style?.addImage(lockedIconName, drawable)
        }

        libreMap.style?.addLayer(
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