package np.com.naxa.drone_tasking_manager.features.projects.views.screens

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
import com.google.gson.JsonObject
import np.com.naxa.drone_tasking_manager.core.widgets.MaplibreCompose
import np.com.naxa.drone_tasking_manager.core.widgets.rememberCameraPosition
import np.com.naxa.drone_tasking_manager.features.projects.viewmodels.events.ProjectsEvent
import np.com.naxa.drone_tasking_manager.features.projects.viewmodels.states.ProjectsCentroidState
import np.com.naxa.drone_tasking_manager.features.projects.views.widgets.ProjectInfoBottomSheet
import np.com.naxa.drone_tasking_manager.local_providers.LocalProjectsViewModel
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.LayoutPropertyValue
import org.maplibre.android.style.layers.PaintPropertyValue
import org.maplibre.android.style.layers.PropertyValue
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonOptions
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.FeatureCollection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsMapScreen(
    modifier: Modifier = Modifier
) {

    val sourceId = "project-centroid-geojson-id"
    val circleLayerId = "project-centroid-fill-layer-id"
    val symbolLayerId = "project-centroid-line-layer-id"

    val viewModel = LocalProjectsViewModel.current
    val state by viewModel.projectsCentroidState.collectAsState()
    var libreMap: MapLibreMap? by remember { mutableStateOf(null) }

    var infoJsonObject: JsonObject? by remember { mutableStateOf(null) }
    var showInfoBottomSheet by remember { mutableStateOf(false) }
    val infoSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    LaunchedEffect(Unit) {
        if (state !is ProjectsCentroidState.Success) {
            viewModel.triggerEvent(ProjectsEvent.FetchProjectsCentroid())
        }
    }

    val cameraPositionState = rememberCameraPosition(
        initialTarget = LatLng(27.82, 85.32),
        initialZoom = 12.0,
    )

    when (state) {
        is ProjectsCentroidState.Error -> {}
        ProjectsCentroidState.Idle -> {}
        ProjectsCentroidState.Loading -> {}
        is ProjectsCentroidState.Success -> {
            val features = (state as ProjectsCentroidState.Success).features
            if (libreMap != null) {
                if (libreMap?.style?.getSource(sourceId) != null) {

                    if (libreMap?.style?.getLayer(circleLayerId) != null) {
                        libreMap?.style?.removeLayer(circleLayerId)
                    }

                    if (libreMap?.style?.getLayer(symbolLayerId) != null) {
                        libreMap?.style?.removeLayer(symbolLayerId)
                    }

                    libreMap?.style?.removeSource(sourceId)
                }

                libreMap?.style?.addSource(
                    GeoJsonSource(
                        sourceId,
                        features = FeatureCollection.fromFeatures(features),
                        options = GeoJsonOptions().withCluster(true)
                    )
                ).also {
                    libreMap?.style?.addLayer(
                        CircleLayer(
                            circleLayerId,
                            sourceId,
                        ).apply {

                            val propertyValues = mutableListOf<PropertyValue<*>>()

                            propertyValues.add(
                                PaintPropertyValue(
                                    "circle-color",
                                    Expression.coalesce(
                                        Expression.get("color"),
                                        Expression.ExpressionLiteral("#D73F3F")
                                    )
                                )
                            )
                            propertyValues.add(
                                PaintPropertyValue(
                                    "circle-radius", Expression.switchCase(
                                        Expression.bool(
                                            Expression.has("point_count"),
                                            Expression.ExpressionLiteral(true)
                                        ),
                                        Expression.step(
                                            Expression.get("point_count"),
                                            Expression.ExpressionLiteral(12),
                                            Expression.ExpressionLiteral(20),
                                            Expression.ExpressionLiteral(14),
                                            Expression.ExpressionLiteral(50),
                                            Expression.ExpressionLiteral(18),
                                            Expression.ExpressionLiteral(100),
                                            Expression.ExpressionLiteral(20),
                                            Expression.ExpressionLiteral(150),
                                            Expression.ExpressionLiteral(25),
                                            Expression.ExpressionLiteral(200),
                                            Expression.ExpressionLiteral(30)
                                        ),
                                        Expression.ExpressionLiteral(10),
                                    )
                                )
                            )

                            propertyValues.add(PaintPropertyValue("circle-stroke-color", "white"))
                            propertyValues.add(PaintPropertyValue("circle-stroke-width", 2.0))
                            withProperties(*propertyValues.toTypedArray())
                        }
                    )

                    libreMap?.style?.addLayer(
                        SymbolLayer(
                            symbolLayerId,
                            sourceId,
                        ).apply {

                            val propertyValues = mutableListOf<PropertyValue<*>>()

                            propertyValues.addAll(
                                listOf(
                                    LayoutPropertyValue(
                                        "text-field",
                                        Expression.get("point_count_abbreviated")
                                    ),
                                    LayoutPropertyValue("text-size", 12.0),
                                    PaintPropertyValue("text-color", "white"),
                                )
                            )

                            withProperties(*propertyValues.toTypedArray())
                        }
                    )
                }

            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        MaplibreCompose(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapReady = { libre, _ ->
                libreMap = libre

                libre.addOnMapClickListener { latLng ->
                    val point = libre.projection.toScreenLocation(latLng)
                    val queried = libre.queryRenderedFeatures(
                        point, *listOf(circleLayerId, symbolLayerId).toTypedArray()
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

        ProjectInfoBottomSheet(
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