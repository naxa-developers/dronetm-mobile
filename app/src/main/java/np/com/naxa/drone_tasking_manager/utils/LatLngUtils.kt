package np.com.naxa.drone_tasking_manager.utils

import org.maplibre.android.geometry.LatLng
import org.maplibre.geojson.Point
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Utility object providing functions for geographical calculations related to latitude and longitude (LatLng).
 *
 * This object contains methods to perform common operations on geographical points,
 * such as calculating the bearing between two points.
 */
object LatLngUtils {

    /**
     * Calculates the bearing (in degrees) between two geographical points (start and end).
     *
     * The bearing is the angle in degrees between the north direction and the great-circle
     * line that connects the two points. It represents the initial direction one would
     * travel to go from the start point to the end point, following the shortest path
     * on the Earth's surface.
     *
     * Note:
     *  - The bearing is calculated clockwise from North, so:
     *      - 0 degrees represents North.
     *      - 90 degrees represents East.
     *      - 180 degrees represents South.
     *      - 270 degrees represents West.
     *  - The result may be negative. You might need to normalize it (e.g., add 360 if negative)
     *    to get a bearing in the range [0, 360).
     *
     * @param start The starting geographical point as a LatLng object.
     * @param end The ending geographical point as a LatLng object.
     * @return The bearing in degrees between the start and end points.
     */
    fun getBearing(start: LatLng, end: LatLng): Float {
        val lat1 = Math.toRadians(start.latitude)
        val lon1 = Math.toRadians(start.longitude)
        val lat2 = Math.toRadians(end.latitude)
        val lon2 = Math.toRadians(end.longitude)

        val dLon = lon2 - lon1
        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
        return Math.toDegrees(atan2(y, x)).toFloat()
    }


    fun getBearing(point1: Point, point2: Point): Double {
        val lat1 = point1.latitude() * PI / 180.0
        val lon1 = point1.longitude() * PI / 180.0
        val lat2 = point2.latitude() * PI / 180.0
        val lon2 = point2.longitude() * PI / 180.0

        val dLon = lon2 - lon1

        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)

        return atan2(y, x)
    }


    /**
     * Determines whether a given point lies inside a polygon using the ray-casting algorithm.
     *
     * This function checks if a point is within a polygon by counting the number of times a ray
     * originating from the point intersects the polygon's edges. If the number of intersections is odd,
     * the point is inside; otherwise, it's outside. This implementation specifically handles
     * polygons defined by their outer boundary.
     *
     * @param point The point (represented as LatLng) to be checked.
     * @param polygon The polygon (represented as a serialized polygon with no holes) to check against.
     * @return `true` if the point is inside the polygon, `false` otherwise.
     *
     * @throws IllegalArgumentException if the polygon does not have at least 3 coordinates or if the polygon is null
     * @throws IllegalStateException if the polygon's coordinates are not valid LatLng points
     *
     * @sample
     * val polygon = Polygon(listOf(
     *     LatLng(40.0, -70.0),
     *     LatLng(41.0, -70.0),
     *     LatLng(41.0, -71.0),
     *     LatLng(40.0, -71.0),
     *     LatLng(40.0, -70.0)
     * ))
     * val pointInside = LatLng(40.5, -70.5)
     * val pointOutside = LatLng(39.0, -70.5)
     * println("Point inside: ${isPointInsidePolygonManual(pointInside, polygon)}") // Output: Point inside: true
     * println("Point outside: ${isPointInsidePolygonManual(pointOutside, polygon)}") // Output: Point outside: false
     */

    // Constants for Earth calculations
    object GeoConstants {
        const val EARTH_RADIUS_METERS = 6371000.0  // Earth's radius in meters
        const val BUFFER_DISTANCE_METERS = 500.0    // Buffer distance in meters
    }

    /**
     * Checks if a given point is inside a polygon or within a buffer distance of the polygon's edges.
     *
     * This function first checks if the point is directly inside the polygon. If it is, it returns true.
     * If not, it then iterates through each edge of the polygon and checks if the point is within a
     * predefined buffer distance of that edge. If it is within the buffer of any edge, it returns true.
     * Otherwise, it returns false.
     *
     * The buffer distance is implicitly defined within the `isPointWithinBufferDistanceOfLine` function and is dependent on its logic.
     *
     * @param point The point to check, represented as a `Point` object with longitude and latitude.
     * @param taskPolygon The polygon to check against, represented as a list of list of list of doubles.
     *                    The structure is expected to be:
     *                    `ArrayList<ArrayList<ArrayList<Double>>>`, where:
     *                      - The outermost list contains one element representing the polygon.
     *                      - The middle list contains a single element representing the outer boundary of the polygon.
     *                      - The innermost list represents a list of coordinates, each containing a longitude (index 1) and a latitude (index 0).
     *                    Example: `[[[lat1, lng1], [lat2, lng2], [lat3, lng3], ...]]`
     * @return `true` if the point is inside the polygon or within the buffer distance of any of its edges, `false` otherwise.
     *
     * @throws IndexOutOfBoundsException if the `taskPolygon` does not follow the expected structure, i.e. it has zero elements or doesn't contain a single outer boundary.
     * @throws NumberFormatException if the longitude and latitude are not valid numbers
     *
     * @see isPointInsidePolygon
     * @see isPointWithinBufferDistanceOfLine
     */
    fun isPointInsidePolygonWithBuffer(point: Point, taskPolygon: ArrayList<ArrayList<ArrayList<Double>>>): Boolean {
        val coordinates = taskPolygon[0] // Outer boundary

        // First check if point is inside the original polygon
        if (isPointInsidePolygon(point, taskPolygon)) {
            return true
        }

        // If not inside, check if point is within buffer distance of any polygon edge
        var j = coordinates.size - 1

        for (i in coordinates.indices) {
            val point1 = Point.fromLngLat(coordinates[i][1], coordinates[i][0])
            val point2 = Point.fromLngLat(coordinates[j][1], coordinates[j][0])

            if (isPointWithinBufferDistanceOfLine(point, point1, point2)) {
                return true
            }
            j = i
        }

        return false
    }

    /**
     * Checks if a given point is within a buffer distance of a line segment.
     *
     * This function determines if a point lies within a specified buffer distance
     * around a line segment defined by its start and end points. It leverages the
     * `calculateDistanceToLineSegment` function to compute the shortest distance
     * between the point and the line segment. If this distance is less than or
     * equal to the defined buffer distance, the function returns true; otherwise,
     * it returns false.
     *
     * @param point The point to check, represented as a `Point` object.
     * @param lineStart The starting point of the line segment, represented as a `Point` object.
     * @param lineEnd The ending point of the line segment, represented as a `Point` object.
     * @return `true` if the point is within the buffer distance of the line segment, `false` otherwise.
     *
     * @throws IllegalArgumentException if any of the input points have NaN, infinite, or null coordinates.
     * @see calculateDistanceToLineSegment
     * @see GeoConstants.BUFFER_DISTANCE_METERS
     */
    fun isPointWithinBufferDistanceOfLine(point: Point, lineStart: Point, lineEnd: Point): Boolean {
        // Calculate the distance from point to line segment
        val distance = calculateDistanceToLineSegment(point, lineStart, lineEnd)
        return distance <= GeoConstants.BUFFER_DISTANCE_METERS
    }

    /**
     * Calculates the shortest distance from a given point to a line segment defined by two endpoints.
     *
     * This function uses the Haversine formula to calculate distances between points on the Earth's surface
     * and determines the cross-track and along-track distances to determine the distance from a point to the line segment.
     *
     * @param point The point to calculate the distance from. Must be a Point object containing `latitude()` and `longitude()` functions returning Double in degrees.
     * @param lineStart The starting point of the line segment. Must be a Point object containing `latitude()` and `longitude()` functions returning Double in degrees.
     * @param lineEnd The ending point of the line segment. Must be a Point object containing `latitude()` and `longitude()` functions returning Double in degrees.
     * @return The shortest distance in meters from the point to the line segment.
     *
     * @throws IllegalArgumentException if any of the input points' latitudes or longitudes are outside the valid range (-90 to 90 for latitude and -180 to 180 for longitude).
     *
     * Example Usage:
     * ```kotlin
     * data class GeoPoint(val latitude: Double, val longitude: Double) : Point {
     *   override fun latitude(): Double = latitude
     *   override fun longitude(): Double = longitude
     * }
     *
     * val point = GeoPoint(40.7128, -74.0060) // New York City
     * val lineStart = GeoPoint(34.0522, -118.2437) // Los Angeles
     * val lineEnd = GeoPoint(41.8781, -87.6298) // Chicago
     * val distance = calculateDistanceToLineSegment(point, lineStart, lineEnd)
     * println("Distance from point to line segment: $distance meters")
     * ```
     */
    fun calculateDistanceToLineSegment(point: Point, lineStart: Point, lineEnd: Point): Double {
        // Convert to radians for calculations
//        val lat = point.latitude() * PI / 180.0
//        val lon = point.longitude() * PI / 180.0
//        val lat1 = lineStart.latitude() * PI / 180.0
//        val lon1 = lineStart.longitude() * PI / 180.0
//        val lat2 = lineEnd.latitude() * PI / 180.0
//        val lon2 = lineEnd.longitude() * PI / 180.0

        // Calculate distances to both endpoints
        val d1 = calculateHaversineDistance(point, lineStart)
        val d2 = calculateHaversineDistance(point, lineEnd)

        // Calculate bearing angles
        val bearing1 = getBearing(lineStart, lineEnd)
        val bearing2 = getBearing(lineStart, point)

        // Calculate cross-track distance
        val crossTrackDistance = asin(sin(d1 / GeoConstants.EARTH_RADIUS_METERS) *
                sin(bearing2 - bearing1)) * GeoConstants.EARTH_RADIUS_METERS

        // Calculate along-track distance
        val alongTrackDistance = acos(cos(d1 / GeoConstants.EARTH_RADIUS_METERS) /
                cos(crossTrackDistance / GeoConstants.EARTH_RADIUS_METERS)) * GeoConstants.EARTH_RADIUS_METERS

        // Check if the closest point is within the line segment
        val lineLength = calculateHaversineDistance(lineStart, lineEnd)
        return if (alongTrackDistance > lineLength) {
            min(d1, d2)  // Return distance to nearest endpoint
        } else {
            abs(crossTrackDistance)  // Return perpendicular distance to line
        }
    }

    /**
     * Represents a geographical point with latitude and longitude.
     */
    fun calculateHaversineDistance(point1: Point, point2: Point): Double {
        val lat1 = point1.latitude() * PI / 180.0
        val lon1 = point1.longitude() * PI / 180.0
        val lat2 = point2.latitude() * PI / 180.0
        val lon2 = point2.longitude() * PI / 180.0

        val dLat = lat2 - lat1
        val dLon = lon2 - lon1

        val a = sin(dLat/2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon/2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1-a))

        return GeoConstants.EARTH_RADIUS_METERS * c
    }


    /**
     * Determines if a given point lies inside a polygon.
     *
     * This function uses the ray-casting algorithm to determine if a point is inside a polygon.
     * It works by counting the number of times a ray cast from the point intersects the edges of the polygon.
     * If the number of intersections is odd, the point is inside the polygon; otherwise, it's outside.
     *
     * The polygon is represented as a list of coordinates, where each coordinate is a pair of longitude and latitude values.
     * Only the outer boundary of the polygon is considered for the calculation.
     *
     * @param point The point to check (represented by latitude and longitude as `Double` values).
     * @param taskPolygon A list representing the polygon.
     *                    The structure is: `ArrayList<ArrayList<ArrayList<Double>>>`
     *                    - The first `ArrayList` always contains only one element representing the outer boundary of the polygon.
     *                    - The second `ArrayList` represents the outer boundary coordinates.
     *                    - The third `ArrayList` represents a point in the boundary with two elements: [longitude, latitude]
     *                    For Example: [[[1.0, 2.0], [3.0, 4.0], [5.0, 2.0], [1.0, 2.0]]] represents a triangle.
     * @return `true` if the point is inside the polygon, `false` otherwise.
     * @throws IllegalArgumentException if taskPolygon is empty or has less than 3 points for the outer boundary.
     * @throws IndexOutOfBoundsException if any coordinate point has less than 2 values.
     */
    fun isPointInsidePolygon(point: Point, taskPolygon: ArrayList<ArrayList<ArrayList<Double>>>): Boolean {
        val coordinates = taskPolygon[0] // Outer boundary
        var inside = false
        var j = coordinates.size - 1

        for (i in coordinates.indices) {
            val xi = coordinates[i][0]
            val yi = coordinates[i][1]
            val xj = coordinates[j][0]
            val yj = coordinates[j][1]

            if ((yi > point.latitude()) != (yj > point.latitude()) &&
                (point.longitude() < (xj - xi) * (point.latitude() - yi) / (yj - yi) + xi)
            ) {
                inside = !inside
            }
            j = i
        }

        return inside
    }

}