package np.com.naxa.drone_tasking_manager.utils

import org.maplibre.android.geometry.LatLng
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

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
}