package np.com.naxa.drone_tasking_manager.features.projects.views.widgets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import np.com.naxa.drone_tasking_manager.core.widgets.MaplibreCompose
import np.com.naxa.drone_tasking_manager.features.projects.dto.project.toFeatureJson
import np.com.naxa.drone_tasking_manager.features.projects.models.Project
import np.com.naxa.drone_tasking_manager.features.projects.utils.ProjectGeometryUtils
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PaintPropertyValue
import org.maplibre.android.style.layers.PropertyValue
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import java.util.Locale

@Composable
fun ProjectItem(
    modifier: Modifier = Modifier,
    project: Project,
    onItemClick: (Project) -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = 0.75.dp,
            color = Color.Gray
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        onClick = {
            onItemClick(project)
        }
    ) {
        MaplibreCompose(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            enableScrollGestures = false,
            enableZoomGestures = false,
            enableRotateGestures = false,
            enableDoubleTapGestures = false,
            enableTiltGestures = false,
            enableHorizontalScrollGestures = false,
            onMapReady = { libreMap, _ ->

                libreMap.addOnMapClickListener { _ ->
                    onItemClick(project)
                    true
                }

                if (project.geometry?.geometry != null) {
                    val bounds =
                        ProjectGeometryUtils.calculateLatLngBounds(project.geometry.geometry.coordinates)

                    bounds?.let {
                        libreMap.animateCamera(CameraUpdateFactory.newLatLngBounds(it, 20), 375)
                    }

                    val feature = project.geometry.geometry.toFeatureJson()

                    val sourceId = "project-geometry-${project.id}"
                    val fillLayerId = "project-geometry-fill-layer-${project.id}"
                    val lineLayerId = "project-geometry-line-layer-${project.id}"

                    if (libreMap.style?.getSource(sourceId) != null) {

                        if (libreMap.style?.getLayer(fillLayerId) != null) {
                            libreMap.style?.removeLayer(fillLayerId)
                        }

                        if (libreMap.style?.getLayer(lineLayerId) != null) {
                            libreMap.style?.removeLayer(lineLayerId)
                        }

                        libreMap.style?.removeSource(sourceId)
                    }

                    libreMap.style?.addSource(
                        GeoJsonSource(
                            sourceId,
                            features = FeatureCollection.fromFeature(
                                Feature.fromJson(
                                    feature
                                )
                            ),
                        )
                    ).also {
                        libreMap.style?.addLayer(
                            FillLayer(
                                fillLayerId,
                                sourceId,
                            ).apply {

                                val propertyValues = mutableListOf<PropertyValue<*>>()

                                propertyValues.add(PaintPropertyValue("fill-color", "#D73F3F"))
                                propertyValues.add(PaintPropertyValue("fill-opacity", 0.1))
                                propertyValues.add(
                                    PaintPropertyValue(
                                        "fill-outline-color",
                                        "#D73F3F"
                                    )
                                )

                                withProperties(*propertyValues.toTypedArray())
                            }
                        )

                        libreMap.style?.addLayer(
                            LineLayer(
                                lineLayerId,
                                sourceId,
                            ).apply {

                                val propertyValues = mutableListOf<PropertyValue<*>>()

                                propertyValues.add(PaintPropertyValue("line-color", "#D73F3F"))

                                withProperties(*propertyValues.toTypedArray())
                            }
                        )
                    }

                }
            }
        )
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier
                        .weight(1f),
                    text = "ID: #${project.slug ?: "unknown"}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.W400,
                    ),
                )
                Text(
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(100)
                        )
                        .padding(
                            start = 12.dp,
                            end = 12.dp,
                            top = 4.dp,
                            bottom = 6.dp
                        ),
                    text = project.status?.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(
                            Locale.ENGLISH
                        ) else it.toString()
                    }
                        ?: "",
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Text(
                text = project.name ?: "Unknown",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Normal
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                color = Color.Red,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append("${project.completedTaskCount ?: 0}")
                        }
                        append("/")
                        append("${project.totalTaskCount ?: 0}")
                    },
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = "Task Completed",
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                progress = {
                    val total =
                        if (project.totalTaskCount == null || project.totalTaskCount == 0) 1F else project.totalTaskCount.toFloat()
                    val completed = project.completedTaskCount?.toFloat() ?: 0F

                    completed / total
                },
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color.LightGray,
                gapSize = (-5).dp,
                strokeCap = StrokeCap.Round,
                drawStopIndicator = {}
            )
        }
    }
}