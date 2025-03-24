package np.com.naxa.drone_tasking_manager.features.tasks.utils.flight_plan_creator

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.json.JSONObject
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.LineString
import org.locationtech.jts.geom.MultiLineString
import org.locationtech.jts.geom.MultiPoint
import org.locationtech.jts.geom.MultiPolygon
import org.locationtech.jts.geom.Point
import org.locationtech.jts.geom.Polygon
import org.locationtech.jts.geom.util.AffineTransformation
import org.locationtech.jts.operation.buffer.BufferOp
import org.locationtech.proj4j.CRSFactory
import org.locationtech.proj4j.CoordinateTransform
import org.locationtech.proj4j.CoordinateTransformFactory
import org.locationtech.proj4j.ProjCoordinate
import java.io.File
import kotlin.math.pow
import kotlin.math.sqrt


/**
 * Enum for the two modes of flight planning: waypoints and waylines.
 */
enum class Mode(val value: String) {
    /**
     * Waypoints mode generates a list of individual points that the drone should visit.
     */
    WayPoints("waypoints"),

    /**
     * WayLines mode generates a list of connected lines that the drone should follow.
     */
    WayLines("waylines")
}


/**
 * Object responsible for creating flight plans in the form of either waypoints or waylines.
 *
 * Provides methods that take into account various parameters such as the area of interest,
 * spacing, altitude, rotation, and no-fly zones. The mode of operation can be either
 * waypoints or waylines, and 3D points can also be generated if required.
 */
object WaypointsOrLines {

    /**
     * Method to generate either waypoints or waylines based on the provided parameters.
     *
     * @param projectArea The area of interest, defined as a GeoJSON Polygon.
     * @param parameters The parameters that influence flight planning, such as spacing and altitude.
     * @param rotationAngle The angle by which the flight plan should be rotated, in degrees.
     * @param generate3d If true, generates 3D points for the flight plan.
     * @param noFlyZones Areas where flight is restricted; waypoints are excluded from these zones.
     * @param takeOffPoint The coordinates of the takeoff point; used to adjust the flight plan to include this point.
     *           It contains the listOf(longitude, latitude)
     * @param rasterDemFilePath The path to the raster DEM file.
     * @param elevatedWaypointsFilePath The path to the elevated waypoints file.
     * @param mode Determines the type of output: "waypoints" for individual points, "waylines" for connecting lines.
     * @return A JSON string representing the generated waypoints or waylines.
     */
    fun create(
        projectArea: String,
        parameters: Parameters.CalculatedParameters,
        rotationAngle: Double = 0.0,
        generate3d: Boolean = false,
        noFlyZones: String? = null,
        takeOffPoint: List<Double>? = null,
        rasterDemFilePath: String? = null,
        elevatedWaypointsFilePath: String? = null,
        mode: Mode = Mode.WayPoints
    ): String {

        if (rasterDemFilePath != null) {
            assert(
                elevatedWaypointsFilePath != null
            ) { "elevatedWaypointsFilePath must not be null if rasterDemFilePath is not null" }
        }

        val geometryFactory = GeometryFactory()
        val polygon = createPolygonFromJson(projectArea)

        val crsFactory = CRSFactory()
        val transformFactory = CoordinateTransformFactory()
        val wgs84 = crsFactory.createFromName("EPSG:4326")
        val webMercator = crsFactory.createFromName("EPSG:3857")
        val transformerTo3857 = transformFactory.createTransform(wgs84, webMercator)
        val transformerTo4326 = transformFactory.createTransform(webMercator, wgs84)

        // Transform the polygon to Web Mercator (EPSG:3857)
        val polygon3857 = transformGeometry(polygon, transformerTo3857)

        // Generate grid and create path
        val grid = generateGridInAoi(
            polygon3857,
            parameters.forwardSpacing,
            parameters.sideSpacing,
            rotationAngle
        )
        var initialPath =
            createPath(
                grid,
                parameters.forwardSpacing,
                rotationAngle,
                generate3d,
                takeOffPoint,
                polygon3857
            )


        val path = mutableListOf<Map<String, Any>>()

        val finalTakeOffPoint = takeOffPoint ?: listOf(
            polygon.centroid.x,
            polygon.centroid.y
        )

        if (finalTakeOffPoint.size >= 2) {
            val firstPathGeometry = initialPath[0]
            val lastPathGeometry = initialPath[initialPath.size - 1]

            val firstPathPoint = firstPathGeometry["coordinates"] as Point
            val lastPathPoint = lastPathGeometry["coordinates"] as Point

            val takeOffPointGeometry = transformGeometry(
                geometryFactory.createPoint(Coordinate(finalTakeOffPoint[0], finalTakeOffPoint[1])),
                transformerTo3857
            ) as Point

            val distanceToFirstPoint = calculateDistance(
                takeOffPointGeometry,
                firstPathPoint,
            )

            val distanceToLastPoint = calculateDistance(
                takeOffPointGeometry,
                lastPathPoint,
            )

            if (distanceToLastPoint < distanceToFirstPoint) {
                initialPath = initialPath.reversed()
            }

            path.add(
                mapOf(
                    "coordinates" to takeOffPointGeometry,
                    "take_photo" to false,
                    "angle" to 0,
                    "gimbal_angle" to "-90"
                )
            )
        }

        path.addAll(initialPath)


        val waypoints = if (mode == Mode.WayLines) {
            removeMiddlePoints(path)
        } else {
            path
        }

        // Transform no-fly zones to Web Mercator (EPSG:3857)
        val mapper = jacksonObjectMapper()
        val noFlyZonesMap = noFlyZones?.let { mapper.readValue<Map<String, Any>>(it) }

        val noFlyPolygons = noFlyZonesMap?.let {
            (it["features"] as List<*>).map { feature ->
                val geometry = (feature as Map<*, *>)["geometry"] as Map<*, *>
                val zonePolygon =
                    geometryFactory.createPolygon(geometry["coordinates"] as Array<Coordinate>)
                transformGeometry(zonePolygon, transformerTo3857)
            }
        } ?: emptyList()

        // Exclude waypoints in no-fly zones
        val filteredWaypoints = excludeNoFlyZones(waypoints, noFlyPolygons)

        // Transform waypoints back to WGS84 (EPSG:4326)
        val features = filteredWaypoints.mapIndexed { index, wp ->
            val coordinates = (wp["coordinates"] as Point).coordinate
            val srcCoord = ProjCoordinate(coordinates.x, coordinates.y)
            val dstCoord = ProjCoordinate()
            transformerTo4326.transform(srcCoord, dstCoord)
            mapOf(
                "type" to "Feature",
                "geometry" to mapOf(
                    "type" to "Point",
                    "coordinates" to listOf(dstCoord.x, dstCoord.y)
                ),
                "properties" to mapOf(
                    "index" to index,
                    "heading" to wp["angle"],
                    "take_photo" to wp["take_photo"],
                    "gimbal_angle" to wp["gimbal_angle"]
                )
            )
        }

        val waypointsOrLinesGeoJson = mapOf("type" to "FeatureCollection", "features" to features)

        // Add elevation data to the waypointsOrLinesGeoJson
        val waypointsWithElevation = if (rasterDemFilePath != null) {
            val elevatedWaypointsFile = Elevation.addFromRasterDem(
                rasterDemFilePath,
                mapper.writeValueAsString(waypointsOrLinesGeoJson),
                elevatedWaypointsFilePath!!
            )

            elevatedWaypointsFile?.let {
                mapper.readValue<Map<String, Any>>(File(it))
            } ?: waypointsOrLinesGeoJson
        } else {
            waypointsOrLinesGeoJson
        }

        // Update the waypoints geojson with the place marks
        val updatedWaypointsOrLinesGeoJson =
            updateWaypointsGeoJsonWithSpeedAndAltitude(waypointsWithElevation, parameters)

        return mapper.writeValueAsString(updatedWaypointsOrLinesGeoJson)
    }


    /**
     * Updates the waypoints GeoJSON with speed and altitude information.
     *
     * @param waypointsGeoJson The GeoJSON map representing the waypoints.
     * @param params The calculated parameters containing flight details like altitude above ground level.
     * @return An updated GeoJSON map with speed and altitude applied to each waypoint.
     */
    private fun updateWaypointsGeoJsonWithSpeedAndAltitude(
        waypointsGeoJson: Map<String, Any>,
        params: Parameters.CalculatedParameters,
    ): Map<String, Any> {

        val features = waypointsGeoJson["features"] as List<*>
        val baseElevation = try {
            val feature = features.first() as? Map<*, *>
            (feature?.get("geometry") as? Map<*, *>)?.let { g ->
                g["coordinates"]?.let {
                    (it as List<*>)[2] as Double
                }
            } ?: 0.0
        } catch (e: Exception) {
            0.0
        }

        val updatedFeatures = features.map {
            val feature = it as Map<*, *>
            val geometry = feature["geometry"] as Map<*, *>
            val coordinates = (geometry["coordinates"] as List<*>).toMutableList()
            val altitude = try {
                val elevation = coordinates[2] as Double
                val differenceInElevation = baseElevation - elevation
                params.altitudeAboveGroundLevel - differenceInElevation
            } catch (e: IndexOutOfBoundsException) {
                params.altitudeAboveGroundLevel
            }.also { alt ->
                if (coordinates.size < 3) coordinates.add(alt) else coordinates[2] = it
            }

            val properties =
                (feature["properties"] as? MutableMap<*, *>)?.toMutableMap() ?: mutableMapOf()
            properties["speed"] = params.groundSpeed
            properties["altitude"] = altitude

            feature.toMutableMap().apply {
                this["geometry"] = geometry.toMutableMap().apply {
                    this["coordinates"] = coordinates
                }
                this["properties"] = properties
            }
        }

        return waypointsGeoJson.toMutableMap().apply {
            this["features"] = updatedFeatures
        }
    }

    /**
     * WayLines is an object that provides methods to process waypoints and generate WayLines,
     * which are lines that connect the waypoints in a flight plan.
     *
     * It provides methods to remove middle points from a list of waypoints, which is used to
     * generate the WayLines.
     */
    private fun removeMiddlePoints(data: List<Map<String, Any>>): List<Map<String, Any>> {
        val processedData = mutableListOf<Map<String, Any>>()
        var i = 0

        while (i < data.size) {
            val currentAngle = data[i]["angle"] as Int
            val segmentStart = i

            // Find the end of the segment with the same angle
            while (i < data.size && data[i]["angle"] == currentAngle) {
                i++
            }

            val segmentEnd = i

            // If the segment has more than 4 points, keep only the first 2 and the last 2
            if (segmentEnd - segmentStart > 4) {
                processedData.addAll(data.subList(segmentStart, segmentStart + 2))
                processedData.addAll(data.subList(segmentEnd - 2, segmentEnd))
            } else {
                processedData.addAll(data.subList(segmentStart, segmentEnd))
            }
        }

        // Make take_photo = false for all the points
        for (point in processedData) {
            (point as MutableMap<String, Any>)["take_photo"] = false
        }

        return processedData
    }


    /**
     * Creates a GeoJSON Polygon from a JSON string.
     *
     * @param projectAreaJson A JSON string representing the area of interest.
     * @return A GeoJSON Polygon representing the area of interest.
     */
    private fun createPolygonFromJson(projectAreaJson: String): Polygon {
        val geometryFactory = GeometryFactory()

        // Parse the JSON
        val json = JSONObject(projectAreaJson)
        val features = json.getJSONArray("features")
        val feature = features.getJSONObject(0)
        val geometry = feature.getJSONObject("geometry")
        val coordinatesArray = geometry.getJSONArray("coordinates").getJSONArray(0) // First ring

        val coordinates = mutableListOf<Coordinate>()

        for (i in 0 until coordinatesArray.length()) {
            val point = coordinatesArray.getJSONArray(i)
            val lon = point.getDouble(0)
            val lat = point.getDouble(1)
            coordinates.add(Coordinate(lon, lat))
        }

        // Ensure the polygon is closed (first == last point)
        if (coordinates.first() != coordinates.last()) {
            coordinates.add(coordinates.first())
        }

        val shell = geometryFactory.createLinearRing(coordinates.toTypedArray())
        return geometryFactory.createPolygon(shell)
    }


    /**
     * Calculates the Euclidean distance between two points.
     *
     * @param point1 The first point.
     * @param point2 The second point.
     * @return The distance between the two points.
     */
    private fun calculateDistance(point1: Point, point2: Point): Double {
        return sqrt((point1.x - point2.x).pow(2) + (point1.y - point2.y).pow(2))
    }


    /**
     * Adds a buffer to the given area of interest polygon.
     *
     * @param aoiPolygon the area of interest polygon
     * @param bufferDistance the distance to buffer the polygon by
     * @return the buffered polygon
     */
    private fun addBufferToAoi(aoiPolygon: Geometry, bufferDistance: Double): Geometry {
        return BufferOp.bufferOp(aoiPolygon, bufferDistance)
    }


    /**
     * Generates a grid of points within the area of interest.
     *
     * @param aoiPolygon the area of interest polygon
     * @param xSpacing the spacing between points in the x direction
     * @param ySpacing the spacing between points in the y direction
     * @param rotationAngle the angle of rotation of the grid in degrees (clockwise)
     * @return a list of GeoJSON points within the area of interest
     */
    private fun generateGridInAoi(
        aoiPolygon: Geometry,
        xSpacing: Double,
        ySpacing: Double,
        rotationAngle: Double = 0.0
    ): List<Map<String, Any>> {
        val bufferedPolygon = addBufferToAoi(aoiPolygon, xSpacing)
        val centroid = aoiPolygon.centroid


        val affineTransform = AffineTransformation()
        affineTransform.rotate(Math.toRadians(rotationAngle), centroid.x, centroid.y)
        val rotatedPolygon = affineTransform.transform(aoiPolygon) as Polygon

        val rotatedBounds = rotatedPolygon.envelopeInternal
        val originalBounds = aoiPolygon.envelopeInternal

        val minX = minOf(rotatedBounds.minX, originalBounds.minX)
        val minY = minOf(rotatedBounds.minY, originalBounds.minY)
        val maxX = maxOf(rotatedBounds.maxX, originalBounds.maxX)
        val maxY = maxOf(rotatedBounds.maxY, originalBounds.maxY)

        val points = mutableListOf<Map<String, Any>>()
        val xPoints = ((maxX - minX) / xSpacing).toInt() + 1
        val yPoints = ((maxY - minY) / ySpacing).toInt() + 1
        var currentAxis = "x"

        for (yi in 0 until yPoints) {
            for (xi in 0 until xPoints) {
                val x = minX + xi * xSpacing
                val y = minY + yi * ySpacing
                val point = GeometryFactory().createPoint(Coordinate(x, y))

                // Rotate the point
                val rotatedPoint = affineTransform.transform(point)

                val angle = if (currentAxis == "x") -90 else 90

                if (bufferedPolygon.contains(rotatedPoint)) {
                    points.add(mapOf("coordinates" to rotatedPoint, "angle" to angle))
                }
            }
            currentAxis = if (currentAxis == "x") "y" else "x"
        }

        return points
    }


    /**
     * Generates a path from a list of points.
     *
     * @param points A list of points in the format of `Map<String, Any>`.
     * @param forwardSpacing The spacing between each point.
     * @param rotationAngle The angle of rotation of the flight plan in degrees.
     * @param generate3d If true, this will generate 3D points.
     * @param takeOffPoint The coordinates of the takeoff point. If given, the flight plan will be rotated to include this point.
     * @param polygon The AOI polygon. If given, the path will be limited to the polygon.
     * @return A list of points representing the path.
     */
    private fun createPath(
        points: List<Map<String, Any>>,
        forwardSpacing: Double,
        rotationAngle: Double = 0.0,
        generate3d: Boolean = false,
        takeOffPoint: List<Double>? = null,
        polygon: Geometry? = null
    ): List<Map<String, Any>> {
        // Helper function to filter points outside the polygon
        fun filterPointsInPolygon(
            segmentPoints: List<Map<String, Any>>,
            polygon: Geometry?,
            isEdgeSegment: Boolean = false
        ): List<Map<String, Any>> {
            if (polygon == null || isEdgeSegment) {
                return segmentPoints
            }

            val outsidePoints = segmentPoints.filter { point ->
                val coordinates = point["coordinates"] as Point;
                !(polygon.contains(coordinates) || polygon.touches(coordinates))
            }

            return if (outsidePoints.size > 2) {
                segmentPoints.filter { it !in listOf(outsidePoints.first(), outsidePoints.last()) }
            } else {
                segmentPoints
            }
        }

        // Helper function to process angle-based segments
        fun processAngleBasedSegments(coordinatesList: List<Map<String, Any>>): Pair<List<Map<String, Any>>, List<Pair<List<Int>, Int>>> {
            val resultList = coordinatesList.toMutableList()
            val segments = mutableListOf<Pair<List<Int>, Int>>()
            var currentSegment = mutableListOf<Int>()
            var currentAngle: Int? = null

            for (i in coordinatesList.indices) {
                val coord = coordinatesList[i]
                val angle = coord["angle"] as Int

                if (currentAngle == null) {
                    currentAngle = angle
                }

                if (angle == currentAngle) {
                    currentSegment.add(i)
                } else {
                    segments.add(currentSegment to currentAngle)
                    currentSegment = mutableListOf(i)
                    currentAngle = angle
                }
            }

            if (currentSegment.isNotEmpty()) {
                segments.add(currentSegment to currentAngle!!)
            }

            // Reverse segments with angle -90
            for ((segmentIndices, angle) in segments) {
                if (angle == -90) {
                    val segmentValues = segmentIndices.map { coordinatesList[it] }
                    val reversedSegment = segmentValues.reversed()
                    for ((newValue, originalIndex) in reversedSegment.zip(segmentIndices)) {
                        resultList[originalIndex] = newValue
                    }
                }
            }

            return resultList to segments
        }

        // Process the points and get segments information
        val (processedData, segments) = processAngleBasedSegments(points)

        // Initialize new data list
        val newData = mutableListOf<Map<String, Any>>()

        for ((idx, segment) in segments.withIndex()) {
            // Determine if it's a first or last segment
            val isEdgeSegment = idx == 0 || idx == segments.size - 1

            val angle = segment.second
            val segmentIndices = segment.first

            // Get segment points
            val segmentPoints = segmentIndices.map { processedData[it] }

            // Filter points outside the polygon
            val filteredSegmentPoints = filterPointsInPolygon(segmentPoints, polygon, isEdgeSegment)

            // Skip empty segments
            if (filteredSegmentPoints.isEmpty()) continue

            // Calculate extra point before first point
            val firstPoint = filteredSegmentPoints.first()
            val firstPointCoords = firstPoint["coordinates"] as Point
            var startX = firstPointCoords.x
            val startY = firstPointCoords.y

            when (angle) {
                -90 -> startX += forwardSpacing
                90 -> startX -= forwardSpacing
            }

            // Rotate the start point
            val rotatedStartPoint = AffineTransformation()
                .rotate(Math.toRadians(rotationAngle), firstPointCoords.x, firstPointCoords.y)
                .transform(GeometryFactory().createPoint(Coordinate(startX, startY))) as Point

            // Add start point
            newData.add(
                mapOf(
                    "coordinates" to rotatedStartPoint,
                    "angle" to angle,
                    "take_photo" to false,
                    "gimbal_angle" to "-90"
                )
            )

            // Add all points in the segment
            for (point in filteredSegmentPoints) {
                newData.add(
                    mapOf(
                        "coordinates" to point["coordinates"] as Point,
                        "angle" to point["angle"] as Int,
                        "take_photo" to true,
                        "gimbal_angle" to "-90"
                    )
                )
            }

            // Calculate extra point after last point
            val lastPoint = filteredSegmentPoints.last()
            val lastPointCoords = lastPoint["coordinates"] as Point
            var endX = lastPointCoords.x
            val endY = lastPointCoords.y

            when (angle) {
                -90 -> endX -= forwardSpacing
                90 -> endX += forwardSpacing
            }

            // Rotate the end point
            val rotatedEndPoint = AffineTransformation()
                .rotate(Math.toRadians(rotationAngle), lastPointCoords.x, lastPointCoords.y)
                .transform(GeometryFactory().createPoint(Coordinate(endX, endY))) as Point

            // Add end point
            newData.add(
                mapOf(
                    "coordinates" to rotatedEndPoint,
                    "angle" to angle,
                    "take_photo" to false,
                    "gimbal_angle" to "-90"
                )
            )
        }

        return newData
    }


    /**
     * Generates 3D waypoints for a single row of points.
     *
     * @param rowPoints list of points in the row
     * @param rowIndex index of the row
     * @param angle angle of the row
     * @return list of 3D waypoints
     */
    private fun generate3dWaypoints(
        rowPoints: List<Point>,
        rowIndex: Int,
        angle: Int
    ): List<Map<String, Any>> {
        val returnPath = rowPoints.reversed().mapIndexed { index, wp ->
            mapOf(
                "coordinates" to wp,
                "angle" to -angle,
                "take_photo" to (index != 0 && index != rowPoints.lastIndex),
                "gimbal_angle" to if (rowIndex % 2 == 0) "-45" else "45"
            )
        }

        val forwardPath = rowPoints.mapIndexed { index, wp ->
            mapOf(
                "coordinates" to wp,
                "angle" to angle,
                "take_photo" to (index != 0 && index != rowPoints.lastIndex),
                "gimbal_angle" to if (rowIndex % 2 == 0) "45" else "-45"
            )
        }

        return returnPath + forwardPath
    }

    /**
     * Excludes waypoints that are inside a no-fly zone.
     * @param points the list of waypoints to filter
     * @param noFlyZones the list of no-fly zones
     * @return the filtered list of waypoints
     */
    private fun excludeNoFlyZones(
        points: List<Map<String, Any>>,
        noFlyZones: List<Geometry>
    ): List<Map<String, Any>> {
        return points.filter { point ->
            val coordinates = point["coordinates"] as Point
            noFlyZones.none { it.contains(coordinates) }
        }
    }

    /**
     * Transforms a geometry by applying a coordinate transform.
     * @param geometry the geometry to transform
     * @param transform the coordinate transform to apply
     * @return the transformed geometry
     */
    private fun transformGeometry(geometry: Geometry, transform: CoordinateTransform): Geometry {
        val geometryFactory = GeometryFactory()

        fun transformCoordinate(coord: Coordinate): Coordinate {
            val srcCoord = ProjCoordinate(coord.x, coord.y)
            val dstCoord = ProjCoordinate()
            transform.transform(srcCoord, dstCoord)
            return Coordinate(dstCoord.x, dstCoord.y)
        }

        return when (geometry) {
            is Point -> geometryFactory.createPoint(transformCoordinate(geometry.coordinate))

            is LineString -> geometryFactory.createLineString(
                geometry.coordinates.map(::transformCoordinate).toTypedArray()
            )

            is Polygon -> {
                val shell = geometryFactory.createLinearRing(
                    geometry.exteriorRing.coordinates.map(::transformCoordinate).toTypedArray()
                )

                val holes = Array(geometry.numInteriorRing) { i ->
                    geometryFactory.createLinearRing(
                        geometry.getInteriorRingN(i).coordinates.map(::transformCoordinate)
                            .toTypedArray()
                    )
                }

                geometryFactory.createPolygon(shell, holes)
            }

            is MultiPoint -> {
                val transformedPoints = (0 until geometry.numGeometries).map {
                    geometryFactory.createPoint(transformCoordinate((geometry.getGeometryN(it) as Point).coordinate))
                }.toTypedArray()
                geometryFactory.createMultiPointFromCoords(transformedPoints.map { it.coordinate }
                    .toTypedArray())
            }

            is MultiLineString -> {
                val transformedLineStrings = (0 until geometry.numGeometries).map {
                    transformGeometry(geometry.getGeometryN(it), transform) as LineString
                }.toTypedArray()
                geometryFactory.createMultiLineString(transformedLineStrings)
            }

            is MultiPolygon -> {
                val transformedPolygons = (0 until geometry.numGeometries).map {
                    transformGeometry(geometry.getGeometryN(it), transform) as Polygon
                }.toTypedArray()
                geometryFactory.createMultiPolygon(transformedPolygons)
            }

            else -> throw IllegalArgumentException("Unsupported geometry type: ${geometry.geometryType}")
        }
    }


    //    private fun transformGeometry(geometry: Geometry, transform: CoordinateTransform): Geometry {
    //        val geometryFactory = GeometryFactory()
    //        val coordinates = geometry.coordinates
    //
    //        // Transform each coordinate
    //        val transformedCoordinates = coordinates.map { coord ->
    //            val srcCoord = ProjCoordinate(coord.x, coord.y)
    //            val dstCoord = ProjCoordinate()
    //            transform.transform(srcCoord, dstCoord)
    //            Coordinate(dstCoord.x, dstCoord.y)
    //        }.toTypedArray()
    //
    //        // Reconstruct the geometry
    //        return when (geometry) {
    //            is Point -> geometryFactory.createPoint(transformedCoordinates[0])
    //            is Polygon -> geometryFactory.createPolygon(transformedCoordinates)
    //            else -> throw IllegalArgumentException("Unsupported geometry type")
    //        }
    //    }

}