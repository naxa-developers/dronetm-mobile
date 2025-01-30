package np.com.naxa.drone_tasking_manager.features.projects.viewmodels.states

import org.maplibre.geojson.Feature


/**
 * Represents the state of fetching and processing project centroids.
 *
 * This sealed class encapsulates the different states that the process of
 * retrieving and working with project centroid data can be in. It allows for
 * a type-safe way to handle the different outcomes and stages of this operation.
 */
sealed class ProjectsCentroidState {
    data object Idle : ProjectsCentroidState()
    data class Success(val features: List<Feature>) : ProjectsCentroidState()
    data class Error(val message: String) : ProjectsCentroidState()
    data object Loading : ProjectsCentroidState()
}