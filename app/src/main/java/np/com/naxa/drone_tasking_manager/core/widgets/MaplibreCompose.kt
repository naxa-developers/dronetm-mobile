package np.com.naxa.drone_tasking_manager.core.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.launch
import np.com.naxa.drone_tasking_manager.core.theme.PrimaryColor
import np.com.naxa.drone_tasking_manager.utils.toColor
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.LocationComponentOptions
import org.maplibre.android.location.engine.LocationEngineRequest
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style


@Composable
fun MaplibreCompose(
    modifier: Modifier = Modifier,
    initialStyle: String = if (isSystemInDarkTheme()) "https://tiles.basemaps.cartocdn.com/gl/dark-matter-gl-style/style.json" else "https://tiles.basemaps.cartocdn.com/gl/positron-gl-style/style.json",
    cameraPositionState: MutableState<CameraPosition> = rememberCameraPosition(),
    onMapReady: (MapLibreMap, MapView) -> Unit = { _, _ -> },
    enableScrollGestures: Boolean = true,
    enableZoomGestures: Boolean = true,
    enableRotateGestures: Boolean = true,
    enableDoubleTapGestures: Boolean = true,
    enableTiltGestures: Boolean = true,
    enableHorizontalScrollGestures: Boolean = true,
    enableCompass: Boolean = false,
    enableAttribution: Boolean = false,
    enableLogo: Boolean = false,
    enableLocationComponent: Boolean = false,
) {
    val scope = rememberCoroutineScope()
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    var mapView by remember { mutableStateOf<MapView?>(null) }
    var mapboxMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var isLoaded by remember { mutableStateOf(false) }

    // Effect to handle lifecycle events
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView?.onStart()
                Lifecycle.Event.ON_RESUME -> mapView?.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView?.onPause()
                Lifecycle.Event.ON_STOP -> mapView?.onStop()
                Lifecycle.Event.ON_DESTROY -> {
                    mapboxMap?.let { map ->
                        cameraPositionState.value = map.cameraPosition
                    }
                    mapView?.onDestroy()
                }

                else -> {}
            }
        }

        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    Box {
        AndroidView(
            modifier = modifier,
            factory = { context ->
                MapLibre.getInstance(context)
                MapView(context).apply {
                    getMapAsync { map ->
                        map.setStyle(initialStyle) { style ->
                            // Restore previous camera position or use initial position
                            map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPositionState.value))

                            mapboxMap = map
                            mapView = this@apply

                            // Configure map settings
                            map.uiSettings.apply {
                                isScrollGesturesEnabled = enableScrollGestures
                                isZoomGesturesEnabled = enableZoomGestures
                                isRotateGesturesEnabled = enableRotateGestures
                                isTiltGesturesEnabled = enableTiltGestures
                                isDoubleTapGesturesEnabled = enableDoubleTapGestures
                                isHorizontalScrollGesturesEnabled = enableHorizontalScrollGestures
                                isLogoEnabled = enableLogo
                                isAttributionEnabled = enableAttribution
                                isCompassEnabled = enableCompass
                            }

                            if (enableLocationComponent) {
                                setupLocationComponent(context, map, style)
                            }

                            onMapReady(map, this@apply)
                            isLoaded = true
                        }

                        // Update camera position state on all camera movements
                        map.addOnCameraIdleListener {
                            scope.launch {
                                cameraPositionState.value = map.cameraPosition
                            }
                        }
                    }
                }

            },
            update = { _ ->
                mapboxMap?.let { map ->
                    // Update map settings if they change
                    map.uiSettings.apply {
                        isScrollGesturesEnabled = enableScrollGestures
                        isZoomGesturesEnabled = enableZoomGestures
                        isRotateGesturesEnabled = enableRotateGestures
                        isLogoEnabled = enableLogo
                        isAttributionEnabled = enableAttribution
                        isCompassEnabled = enableCompass
                    }
                }
            },
            onRelease = {
                scope.launch {
                    mapboxMap?.cameraPosition?.let {
                        cameraPositionState.value = it
                    }
                }
                mapView = null
                mapboxMap = null
            }
        )

        AnimatedVisibility(
            visible = !isLoaded,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            ShimmerEffectPreview(modifier = Modifier.fillMaxSize())
        }
    }
}

/**
 * Sets up the MapLibre location component.
 *
 * This function configures the location component to display the user's current
 * location on the map, including a pulsing effect, custom colors, and a
 * high-accuracy location engine.
 *
 * @param context The application context.
 * @param map The MapLibreMap instance to add the location component to.
 * @param style The MapLibre style to use for the map.
 *
 * @throws SecurityException if the necessary location permissions are not granted.
 *
 * @SuppressLint("MissingPermission")
 * Suppresses the lint warning about missing location permission checks.
 * This is because the function is assumed to be called only after
 * the necessary permissions have been granted elsewhere.
 */
@SuppressLint("MissingPermission")
private fun setupLocationComponent(context: Context, map: MapLibreMap, style: Style) {
    val locationComponent = map.locationComponent
    val locationComponentOptions = LocationComponentOptions.builder(context)
        .pulseEnabled(true)
        .pulseColor(PrimaryColor.toColor())
        .foregroundTintColor(PrimaryColor.toColor())
        .build()

    val locationComponentActivationOptions = LocationComponentActivationOptions
        .builder(context, style)
        .locationComponentOptions(locationComponentOptions)
        .useDefaultLocationEngine(true)
        .locationEngineRequest(
            LocationEngineRequest.Builder(750)
                .setFastestInterval(750)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .build()
        )
        .build()

    locationComponent.activateLocationComponent(locationComponentActivationOptions)
    locationComponent.isLocationComponentEnabled = true
}


/**
 * A [Saver] implementation for [CameraPosition] objects.
 *
 * This saver allows for the saving and restoring of a [CameraPosition] to and from a [Bundle].
 * It is useful for preserving the camera's state (latitude, longitude, zoom, tilt, bearing)
 * across configuration changes or process death.
 *
 * The saved data is stored in the bundle using the following keys:
 *  - "lat": Latitude of the camera's target.
 *  - "lng": Longitude of the camera's target.
 *  - "zoom": Zoom level of the camera.
 *  - "tilt": Tilt angle of the camera.
 *  - "bearing": Bearing (rotation) of the camera.
 *
 * If the `target` property is null, the latitude and longitude will be stored as 0.0.
 *
 * @see CameraPosition
 * @see Saver
 * @see Bundle
 */
private val CameraPositionSaver = Saver<CameraPosition, Bundle>(
    save = { position ->
        Bundle().apply {
            putDouble("lat", position.target?.latitude ?: 0.0)
            putDouble("lng", position.target?.longitude ?: 0.0)
            putDouble("zoom", position.zoom)
            putDouble("tilt", position.tilt)
            putDouble("bearing", position.bearing)
        }
    },
    restore = { bundle ->
        CameraPosition.Builder()
            .target(
                LatLng(
                    bundle.getDouble("lat"),
                    bundle.getDouble("lng")
                )
            )
            .zoom(bundle.getDouble("zoom"))
            .tilt(bundle.getDouble("tilt"))
            .bearing(bundle.getDouble("bearing"))
            .build()
    }
)

/**
 * Remembers and saves the camera position across recompositions and configuration changes.
 *
 * This function provides a stateful holder for a [CameraPosition] that can be used
 * to control and observe the camera's location, zoom, bearing, and tilt. It utilizes
 * `rememberSaveable` to persist the camera position even when the activity or fragment
 * is recreated, such as during configuration changes.
 *
 * @param initialTarget The initial [LatLng] target of the camera. If null, the target
 *                      is not initialized. Defaults to null.
 * @param initialZoom The initial zoom level of the camera. If null, the zoom is not
 *                    initialized. Defaults to null.
 * @param initialBearing The initial bearing (rotation) of the camera in degrees.
 *                       If null, the bearing is not initialized. Defaults to null.
 * @param initialTilt The initial tilt (viewing angle) of the camera in degrees.
 *                    If null, the tilt is not initialized. Defaults to null.
 * @return A [MutableState] containing the current [CameraPosition]. The value can be
 *         updated to change the camera's position and will be preserved across
 *         recompositions and configuration changes.
 *
 * Example Usage:
 * ```kotlin
 * val cameraPositionState = rememberCameraPosition(
 *     initialTarget = LatLng(37.7749, -122.4194), // San Francisco
 *     initialZoom = 12.0,
 *     initialBearing = 45.0,
 *     initialTilt = 30.0
 * )
 *
 * // Access and modify the camera position:
 * val currentCameraPosition = cameraPositionState.value
 * cameraPositionState.value = CameraPosition.Builder()
 *     .target(LatLng(40.7128, -74.0060)) // New York City
 *     .zoom(10.0 */
@Composable
fun rememberCameraPosition(
    initialTarget: LatLng? = null,
    initialZoom: Double? = null,
    initialBearing: Double? = null,
    initialTilt: Double? = null
): MutableState<CameraPosition> {
    return rememberSaveable(stateSaver = CameraPositionSaver) {
        val builder = CameraPosition.Builder()
        initialTarget?.let { builder.target(it) }
        initialZoom?.let { builder.zoom(it) }
        initialBearing?.let { builder.bearing(it) }
        initialTilt?.let { builder.tilt(it) }

        mutableStateOf(builder.build())
    }
}