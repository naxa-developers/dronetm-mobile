package np.com.naxa.drone_tasking_manager.features.projects.utils

import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds

object ProjectGeometryUtils {

    /**
     * Calculates the centroid (geometric center) of a polygon represented by a list of coordinates.
     *
     * The function expects a list of lists of lists of doubles, where:
     *   - The outermost list represents a collection of polygons (in this implementation, only the first polygon is used).
     *   - The middle list represents a single polygon, defined by a list of vertices.
     *   - The innermost list represents a single vertex, defined by a list of [x, y] coordinates.
     *
     * This function specifically calculates the centroid of the first polygon in the input list.
     * It computes the centroid by averaging the x-coordinates and y-coordinates of the polygon's vertices.
     *
     * @param coordinates A list of lists of lists of doubles representing the coordinates of polygons.
     *                    The structure should be List<List<List<Double>>>.
     *                    It is expected that the first polygon ([0] index) in this list will be used to compute the centroid.
     *                    Each sublist represents a point with [x, y] coordinates
     * @return A list of doubles containing the [x, y] coordinates of the calculated centroid.
     *         Returns an empty list if the provided list is empty, or the first polygon is empty.
     * @throws IndexOutOfBoundsException If the input `coordinates` list is empty or doesn't have a first element.
     * @throws NoSuchElementException If the first element, representing the points of the polygon, is empty.
     */
    fun calculateCentroid(coordinates: List<List<List<Double>>>): LatLng? {
        return try {
            val points = coordinates[0]
            val xCoords = points.map { it[0] }
            val yCoords = points.map { it[1] }

            val centroidX = xCoords.average()
            val centroidY = yCoords.average()

            LatLng(centroidY, centroidX)
        } catch (e: Exception) {
            null
        }
    }


    /**
     * Calculates the LatLngBounds that encompass a set of geographic coordinates.
     *
     * This function takes a list of coordinates representing a polygon and determines the
     * smallest rectangular bounding box that can contain it.  It assumes the input is a
     * single polygon defined as a list of coordinate pairs (longitude, latitude), wrapped in two additional lists
     * to fit a GeoJSON-like structure.
     *
     * @param coordinates A list of lists of lists of doubles representing the polygon's coordinates.
     *                    The expected structure is:
     *                    `List<List<List<Double>>>` where:
     *                      - The outermost list represents a collection of polygons (in this case, assumed to be a single polygon).
     *                      - The middle list represents a single polygon.
     *                      - The innermost list represents a coordinate pair: `[longitude, latitude]`.
     *                    Example: `[[[10.0, 20.0], [30.0, 40.0], [50.0, 20.0]]]`
     *
     * @return A `LatLngBounds` object representing the bounding box of the polygon, or `null` if:
     *         - The input list is empty.
     *         - The input list is malformed (e.g., not enough elements, elements of the wrong type).
     *         - Any other exception occurs during processing.
     *
     * @throws IllegalArgumentException if the input list is empty or if the inner lists don't have exactly 2 elements.
     *
     */
    fun calculateLatLngBounds(coordinates: List<List<List<Double>>>): LatLngBounds? {
        return try {
            val points = coordinates[0]
            val xCoords = points.map { it[0] }
            val yCoords = points.map { it[1] }

            val southwest = LatLng(
                yCoords.minOrNull() ?: 0.0,
                xCoords.minOrNull() ?: 0.0
            )
            val northeast = LatLng(
                yCoords.maxOrNull() ?: 0.0,
                xCoords.maxOrNull() ?: 0.0
            )

            return LatLngBounds.fromLatLngs(listOf(northeast, southwest))
        } catch (e: Exception) {
            null
        }
    }


    /**
     * Calculates the centroid (center point) of a bounding box.
     *
     * The bounding box is defined by two coordinate pairs: the bottom-left (lng1, lat1)
     * and the top-right (lng2, lat2).
     *
     * @param box A list of four Double values representing the bounding box coordinates:
     *            [lng1, lat1, lng2, lat2].
     *            - lng1: Longitude of the bottom-left corner.
     *            - lat1: Latitude of the bottom-left corner.
     *            - lng2: Longitude of the top-right corner.
     *            - lat2: Latitude of the top-right corner.
     * @return A list containing two Double values representing the centroid coordinates:
     *         [centerX, centerY].
     *         - centerX: Longitude of the centroid.
     *         - centerY: Latitude of the centroid.
     * @throws IllegalArgumentException if the input list `box` does not contain exactly 4 elements.
     */
    fun calculateCentroidOfBBox(box: List<Double>): List<Double> {
        require(box.size == 4) {
            "Box must contain exactly 4 elements: [lng1, lat1, lng2, lat2]"
        }
        val (x1, y1, x2, y2) = box
        val centerX = (x1 + x2) / 2
        val centerY = (y1 + y2) / 2
        return listOf(centerX, centerY)
    }
}