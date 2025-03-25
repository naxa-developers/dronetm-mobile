package np.com.naxa.drone_tasking_manager.features.tasks.utils.flight_plan_creator

import android.content.Context
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.File
import java.io.FileWriter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import kotlin.collections.get


/**
 * DroneFlightPlan is responsible for generating flight plans based on given parameters.
 *
 * This object provides a method to create flight plans for drone operations by specifying
 * the area of interest, overlaps, altitude, and other parameters. It supports optional
 * elevation data and can output the flight plan in a specified format.
 */
object DroneFlightPlan {

    /**
     * Generate a flight plan with the given parameters
     *
     * @param projectArea The area of interest. The area of interest is defined as a GeoJSON Polygon.
     * @param parameters The parameters of the flight plan.
     * @param outfile The path to the output file. If not given, the output file will be created at /tmp/output.kmz.
     * @param generate3d If true, it will generate waypoints at each point, not at intervals.
     * @param rotationAngle The angle of rotation of the flight plan in degrees.
     * @param takeOffPoint The coordinates of the takeoff point. If given, the flight plan will be rotated to include this point.
     * @param rasterDemFilePath The path to the raster DEM file.
     * @param elevatedWaypointsFilePath The path to the elevated waypoints file.
     * @param noFlyZones Areas where flight is restricted; waypoints are excluded from these zones.
     * @param mode The mode of flight planning.
     * @param wayLinesFileExt The file extension of the waylines file.
     * @return The path to the generated flight plan
     */
    fun create(
        projectArea: String,
        parameters: Parameters.CalculatedParameters,
        outfile: String,
        generate3d: Boolean = false,
        rotationAngle: Double = 0.0,
        takeOffPoint: List<Double>? = null,
        rasterDemFilePath: String? = null,
        elevatedWaypointsFilePath: String? = null,
        noFlyZones: String? = null,
        mode: Mode = Mode.WayPoints,
        wayLinesFileExt: String = "kml"
    ): String {

        // Generate waypoints
        val waypoints = WaypointsOrLines.create(
            projectArea,
            parameters,
            rotationAngle,
            generate3d,
            takeOffPoint = takeOffPoint,
            rasterDemFilePath = rasterDemFilePath,
            elevatedWaypointsFilePath = elevatedWaypointsFilePath,
            noFlyZones = noFlyZones,
            mode = mode
        )

        // Parse waypoints with elevation
        val waypointsGeoJson = jacksonObjectMapper().readValue<Map<String, Any>>(waypoints)

        // Create WPML file
        return createWpml(waypointsGeoJson, outfile, wayLinesFileExt)
    }

    /**
     * Zips all files in a given directory to a zip file.
     *
     * @param directoryPath the path to the directory containing the files to be zipped
     * @param zipPath the path where the zip file should be written
     */
    private fun zipDirectory(directoryPath: String, zipPath: String) {
        val zipFile = File(zipPath)
        val sourceDir = File(directoryPath)

        ZipOutputStream(zipFile.outputStream()).use { zipOut ->
            sourceDir.walk().forEach { file ->
                if (file.isFile) {
                    val relativePath = file.relativeTo(sourceDir.parentFile).path
                    zipOut.putNextEntry(ZipEntry(relativePath))
                    file.inputStream().use { it.copyTo(zipOut) }
                    zipOut.closeEntry()
                }
            }
        }
    }


    /**
     * Creates a zip file with the given waypoint file and returns the path to the zip file.
     *
     * @param wayLinesPathUid the path to the waypoint file
     * @return the path to the generated zip file
     */
    private fun createZipFile(wayLinesPathUid: String, wayLinesFileExt: String = "kml"): String {
        val wpmzPath = "$wayLinesPathUid/wpmz"
        File(wpmzPath).mkdirs()

        // Copy waylines.wpml to the wpmz folder
        val wpmlContent = File("$wayLinesPathUid/waylines.${wayLinesFileExt}").readText()
        File("$wpmzPath/waylines.${wayLinesFileExt}").writeText(wpmlContent)

        // Create a ZIP file
        val outputFileName = "$wayLinesPathUid/output.kmz"
        zipDirectory(wpmzPath, outputFileName)

        return outputFileName
    }


    /**
     * Creates a "take photo" action in an XML document for a flight plan.
     *
     * @param doc The XML document where the action will be created.
     * @param actionGroupElement The parent element to which the action will be appended.
     * @param index A string representing the index of the action, used for identification.
     */
    private fun takePhotoAction(doc: Document, actionGroupElement: Element, index: String) {
        val action = doc.createElement("wpml:action")
        val actionId = doc.createElement("wpml:actionId")
        actionId.textContent = index
        action.appendChild(actionId)

        val actionActuatorFunc = doc.createElement("wpml:actionActuatorFunc")
        actionActuatorFunc.textContent = "takePhoto"
        action.appendChild(actionActuatorFunc)

        val actionActuatorFuncParam = doc.createElement("wpml:actionActuatorFuncParam")
        val payloadPositionIndex = doc.createElement("wpml:payloadPositionIndex")
        payloadPositionIndex.textContent = "0"
        actionActuatorFuncParam.appendChild(payloadPositionIndex)
        action.appendChild(actionActuatorFuncParam)

        actionGroupElement.appendChild(action)
    }


    /**
     * Creates a "gimbal rotate" action in an XML document for a flight plan.
     *
     * @param doc The XML document where the action will be created.
     * @param actionGroupElement The parent element to which the action will be appended.
     * @param index A string representing the index of the action, used for identification.
     * @param gimbalAngle The angle to which the gimbal should be rotated, in degrees.
     */
    private fun gimbalRotateAction(doc: Document, actionGroupElement: Element, index: String, gimbalAngle: String) {
        val action = doc.createElement("wpml:action")
        val actionId = doc.createElement("wpml:actionId")
        actionId.textContent = index
        action.appendChild(actionId)

        val actionActuatorFunc = doc.createElement("wpml:actionActuatorFunc")
        actionActuatorFunc.textContent = "gimbalRotate"
        action.appendChild(actionActuatorFunc)

        val actionActuatorFuncParam = doc.createElement("wpml:actionActuatorFuncParam")
        val gimbalHeadingYawBase = doc.createElement("wpml:gimbalHeadingYawBase")
        gimbalHeadingYawBase.textContent = "aircraft"
        actionActuatorFuncParam.appendChild(gimbalHeadingYawBase)

        val gimbalRotateMode = doc.createElement("wpml:gimbalRotateMode")
        gimbalRotateMode.textContent = "absoluteAngle"
        actionActuatorFuncParam.appendChild(gimbalRotateMode)

        val gimbalPitchRotateEnable = doc.createElement("wpml:gimbalPitchRotateEnable")
        gimbalPitchRotateEnable.textContent = "1"
        actionActuatorFuncParam.appendChild(gimbalPitchRotateEnable)

        val gimbalPitchRotateAngle = doc.createElement("wpml:gimbalPitchRotateAngle")
        gimbalPitchRotateAngle.textContent = if (gimbalAngle == "45") "-90" else gimbalAngle
        actionActuatorFuncParam.appendChild(gimbalPitchRotateAngle)

        val gimbalRollRotateEnable = doc.createElement("wpml:gimbalRollRotateEnable")
        gimbalRollRotateEnable.textContent = if (gimbalAngle == "45") "1" else "0"
        actionActuatorFuncParam.appendChild(gimbalRollRotateEnable)

        val gimbalRollRotateAngle = doc.createElement("wpml:gimbalRollRotateAngle")
        gimbalRollRotateAngle.textContent = if (gimbalAngle == "45") "-45" else "0"
        actionActuatorFuncParam.appendChild(gimbalRollRotateAngle)

        val gimbalYawRotateEnable = doc.createElement("wpml:gimbalYawRotateEnable")
        gimbalYawRotateEnable.textContent = "0"
        actionActuatorFuncParam.appendChild(gimbalYawRotateEnable)

        val gimbalYawRotateAngle = doc.createElement("wpml:gimbalYawRotateAngle")
        gimbalYawRotateAngle.textContent = "0"
        actionActuatorFuncParam.appendChild(gimbalYawRotateAngle)

        val gimbalRotateTimeEnable = doc.createElement("wpml:gimbalRotateTimeEnable")
        gimbalRotateTimeEnable.textContent = "0"
        actionActuatorFuncParam.appendChild(gimbalRotateTimeEnable)

        val gimbalRotateTime = doc.createElement("wpml:gimbalRotateTime")
        gimbalRotateTime.textContent = "0"
        actionActuatorFuncParam.appendChild(gimbalRotateTime)

        val payloadPositionIndex = doc.createElement("wpml:payloadPositionIndex")
        payloadPositionIndex.textContent = "0"
        actionActuatorFuncParam.appendChild(payloadPositionIndex)

        action.appendChild(actionActuatorFuncParam)
        actionGroupElement.appendChild(action)
    }


    /**
     * Create a KML element for a folder.
     *
     * @param doc The KML document to create the element in.
     * @param placeMarks The place marks to create the folder for.
     * @return The created element.
     */
    private fun createFolder(doc: Document, placeMarks: List<*>): Element {
        val folder = doc.createElement("Folder")

        val templateId = doc.createElement("wpml:templateId")
        templateId.textContent = "0"
        folder.appendChild(templateId)

        val executeHeightMode = doc.createElement("wpml:executeHeightMode")
        executeHeightMode.textContent = "relativeToStartPoint"
        folder.appendChild(executeHeightMode)

        val wayLineId = doc.createElement("wpml:waylineId")
        wayLineId.textContent = "0"
        folder.appendChild(wayLineId)

        val distance = doc.createElement("wpml:distance")
        distance.textContent = "0"
        folder.appendChild(distance)

        val duration = doc.createElement("wpml:duration")
        duration.textContent = "0"
        folder.appendChild(duration)

        val globalWaypointTurnMode = doc.createElement("wpml:globalWaypointTurnMode")
        globalWaypointTurnMode.textContent = "toPointAndStopWithDiscontinuityCurvature"
        folder.appendChild(globalWaypointTurnMode)

        val straightLine = doc.createElement("wpml:globalUseStraightLine")
        straightLine.textContent = "0"
        folder.appendChild(straightLine)

        val autoFlightSpeed = doc.createElement("wpml:autoFlightSpeed")
        autoFlightSpeed.textContent = "2.5"
        folder.appendChild(autoFlightSpeed)

        for (placemark in placeMarks) {
            val placemarkElement = createPlaceMark(doc, placemark as Map<String, Any>)
            folder.appendChild(placemarkElement)
        }

        return folder
    }


    /**
     * Create a KML element for a single placemark.
     *
     * @param doc The KML document to create the element in.
     * @param placeMark The placemark to create the element for. This is a dictionary with the following keys:
     * - "geometry": A GeoJSON geometry (i.e. a dictionary with keys "type" and "coordinates").
     * - "properties": A dictionary with keys "index", "speed", and "gimbalAngle".
     * @return The created element.
     */
    private fun createPlaceMark(doc: Document, placeMark: Map<String, Any>): Element {
        val placeMarkElement = doc.createElement("Placemark")

        val point = doc.createElement("Point")
        val coordinates = doc.createElement("coordinates")
        coordinates.textContent =
            "${placeMark["geometry"]?.let { (it as Map<*, *>)["coordinates"]?.let { (it as List<*>)[0] } }},${placeMark["geometry"]?.let { (it as Map<*, *>)["coordinates"]?.let { (it as List<*>)[1] } }}"
        point.appendChild(coordinates)
        placeMarkElement.appendChild(point)

        val wpmlIndex = doc.createElement("wpml:index")
        wpmlIndex.textContent = placeMark["properties"]?.let { (it as Map<*, *>)["index"]?.toString() }
        placeMarkElement.appendChild(wpmlIndex)

        val executeHeight = doc.createElement("wpml:executeHeight")
        executeHeight.textContent =
            placeMark["geometry"]?.let { (it as Map<*, *>)["coordinates"]?.let { (it as List<*>)[2]?.toString() } }
        placeMarkElement.appendChild(executeHeight)

        val waypointSpeed = doc.createElement("wpml:waypointSpeed")
        waypointSpeed.textContent = placeMark["properties"]?.let { (it as Map<*, *>)["speed"]?.toString() }
        placeMarkElement.appendChild(waypointSpeed)

        val waypointHeadingParam = doc.createElement("wpml:waypointHeadingParam")
        val waypointHeadingMode = doc.createElement("wpml:waypointHeadingMode")
        waypointHeadingMode.textContent = "followWayline"
        waypointHeadingParam.appendChild(waypointHeadingMode)

        val waypointHeadingAngle = doc.createElement("wpml:waypointHeadingAngle")
        waypointHeadingAngle.textContent = placeMark["properties"]?.let { (it as Map<*, *>)["heading"]?.toString() }
        waypointHeadingParam.appendChild(waypointHeadingAngle)

        val waypointPoiPoint = doc.createElement("wpml:waypointPoiPoint")
        waypointPoiPoint.textContent = "0.000000,0.000000,0.000000"
        waypointHeadingParam.appendChild(waypointPoiPoint)

        val waypointHeadingAngleEnable = doc.createElement("wpml:waypointHeadingAngleEnable")
        waypointHeadingAngleEnable.textContent = "1"
        waypointHeadingParam.appendChild(waypointHeadingAngleEnable)

        placeMarkElement.appendChild(waypointHeadingParam)

        val actionGroup1 = doc.createElement("wpml:actionGroup")
        val actionGroup1Id = doc.createElement("wpml:actionGroupId")
        actionGroup1Id.textContent = "1"
        actionGroup1.appendChild(actionGroup1Id)

        val actionGroup1Start = doc.createElement("wpml:actionGroupStartIndex")
        actionGroup1Start.textContent = placeMark["properties"]?.let { (it as Map<*, *>)["index"]?.toString() }
        actionGroup1.appendChild(actionGroup1Start)

        val actionGroup1End = doc.createElement("wpml:actionGroupEndIndex")
        actionGroup1End.textContent = placeMark["properties"]?.let { (it as Map<*, *>)["index"]?.toString() }
        actionGroup1.appendChild(actionGroup1End)

        val actionGroup1Mode = doc.createElement("wpml:actionGroupMode")
        actionGroup1Mode.textContent = "parallel"
        actionGroup1.appendChild(actionGroup1Mode)

        val actionGroup1Trigger = doc.createElement("wpml:actionTrigger")
        val actionGroup1TriggerType = doc.createElement("wpml:actionTriggerType")
        actionGroup1TriggerType.textContent = "reachPoint"
        actionGroup1Trigger.appendChild(actionGroup1TriggerType)
        actionGroup1.appendChild(actionGroup1Trigger)

        if (placeMark["properties"]?.let {
                (it as Map<*, *>)["take_photo"] as Boolean
            } == true) {
            takePhotoAction(doc, actionGroup1, "1")
        } else {
            gimbalRotateAction(
                doc,
                actionGroup1,
                "1",
                placeMark["properties"]?.let { (it as Map<*, *>)["gimbal_angle"]?.toString() } ?: "-90")
        }

        placeMarkElement.appendChild(actionGroup1)

        return placeMarkElement
    }


    /**
     * Creates a mission configuration element for a flight plan.
     *
     * @param doc The XML document where the mission configuration will be created.
     * @param finishActionValue A string representing the finish action for the mission.
     * @param globalHeight A string representing the global return-to-home height.
     * @return An XML element representing the mission configuration.
     */
    private fun createMissionConfig(doc: Document, finishActionValue: String, globalHeight: String): Element {
        val missionConfig = doc.createElement("wpml:missionConfig")

        val flyToWayLineMode = doc.createElement("wpml:flyToWaylineMode")
        flyToWayLineMode.textContent = "safely"
        missionConfig.appendChild(flyToWayLineMode)

        val finishAction = doc.createElement("wpml:finishAction")
        finishAction.textContent = finishActionValue
        missionConfig.appendChild(finishAction)

        val exitOnRcLost = doc.createElement("wpml:exitOnRCLost")
        exitOnRcLost.textContent = "executeLostAction"
        missionConfig.appendChild(exitOnRcLost)

        val executeRcLostAction = doc.createElement("wpml:executeRCLostAction")
        executeRcLostAction.textContent = "hover"
        missionConfig.appendChild(executeRcLostAction)

        val globalTransitionalSpeed = doc.createElement("wpml:globalTransitionalSpeed")
        globalTransitionalSpeed.textContent = "2.5"
        missionConfig.appendChild(globalTransitionalSpeed)

        val globalRthHeight = doc.createElement("wpml:globalRTHHeight")
        globalRthHeight.textContent = globalHeight
        missionConfig.appendChild(globalRthHeight)

        val droneInfo = doc.createElement("wpml:droneInfo")
        val droneEnumValue = doc.createElement("wpml:droneEnumValue")
        droneEnumValue.textContent = "68"
        droneInfo.appendChild(droneEnumValue)

        val droneSubEnumValue = doc.createElement("wpml:droneSubEnumValue")
        droneSubEnumValue.textContent = "0"
        droneInfo.appendChild(droneSubEnumValue)

        missionConfig.appendChild(droneInfo)

        return missionConfig
    }


    /**
     * Create a KML document from a mission configuration and a folder.
     *
     * @param doc The XML document where the KML will be created.
     * @param missionConfig The mission configuration element.
     * @param folder The folder element.
     * @return The KML document.
     */
    private fun createKml(doc: Document, missionConfig: Element, folder: Element): Element {
        val kml = doc.createElement("kml")
        kml.setAttribute("xmlns", "http://www.opengis.net/kml/2.2")
        kml.setAttribute("xmlns:wpml", "http://www.dji.com/wpmz/1.0.2")

        val document = doc.createElement("Document")
        document.appendChild(missionConfig)
        document.appendChild(folder)

        kml.appendChild(document)

        return kml
    }


    /**
     * Create a Xml file from a list of place marks.
     *
     * @param placeMarks A list of place marks to be converted to a WPML file.
     * @param finishAction A string representing the finish action for the mission.
     * @param globalHeight A string representing the global return-to-home height.
     * @param outputFilePath A string representing the path to the output WPML file.
     * @return A string representing the path to the generated WPML file.
     */
    private fun createXml(
        placeMarks: List<*>,
        finishAction: String,
        globalHeight: String,
        outputFilePath: String,
        wayLinesFileExt: String = "kml"
    ): String {
        val docFactory = DocumentBuilderFactory.newInstance()
        val docBuilder = docFactory.newDocumentBuilder()
        val doc = docBuilder.newDocument()

        val missionConfig = createMissionConfig(doc, finishAction, globalHeight)
        val folder = createFolder(doc, placeMarks)
        val kml = createKml(doc, missionConfig, folder)

        val transformerFactory = TransformerFactory.newInstance()
        val transformer = transformerFactory.newTransformer()
        val source = DOMSource(kml)

        val folderName = "flight"
        File("$outputFilePath/$folderName").mkdirs()
        val wayLinesPath = "$outputFilePath/$folderName/waylines.${wayLinesFileExt}"

        FileWriter(wayLinesPath).use { writer ->
            val result = StreamResult(writer)
            transformer.transform(source, result)
        }

        return createZipFile("$outputFilePath/$folderName", wayLinesFileExt)
    }


    /**
     * Create a WPML file from a GeoJSON string.
     *
     * Takes a GeoJSON string, parses it into a list of placeMarks, and creates a WPML file containing the
     * placeMarks. The WPML file is saved as a zip file in the given output directory.
     *
     * @param placeMarkGeoJson The GeoJSON string to parse.
     * @param outputFilePath The path to the directory where the WPML file should be saved. Defaults to /tmp/.
     * @return The path to the generated WPML file.
     */
    private fun createWpml(
        placeMarkGeoJson: Map<String, Any>,
        outputFilePath: String,
        wayLinesFileExt: String = "kml"
    ): String {
        val finishAction = "goHome"
        val globalHeight =
            placeMarkGeoJson["features"]?.let { (it as List<*>)[0]?.let { (it as Map<*, *>)["geometry"]?.let { (it as Map<*, *>)["coordinates"]?.let { (it as List<*>)[2]?.toString() } } } }
                ?: "100"
        val placeMarks = placeMarkGeoJson["features"] as List<*>

        return createXml(placeMarks, finishAction, globalHeight, outputFilePath, wayLinesFileExt)
    }

    /**
     * Validates and parses a coordinate string into a list of doubles representing longitude and latitude.
     *
     * The input string should be in the format "longitude,latitude". Longitude must be within the range
     * -180.0 to 180.0 and latitude within -90.0 to 90.0. If the input is not in the correct format or
     * the values are out of range, an IllegalArgumentException is thrown.
     *
     * @param value The coordinate string in the format "longitude,latitude".
     * @return A list of two doubles, where the first element is the longitude and the second is the latitude.
     * @throws IllegalArgumentException If the input string is not in the correct format or the values
     *                                  are out of the valid range.
     */
    fun validateCoordinates(value: String): List<Double> {
        return try {
            val (lon, lat) = value.split(",").map { it.toDouble() }
            if (lon !in -180.0..180.0 || lat !in -90.0..90.0) {
                throw IllegalArgumentException("Coordinates must be in the format 'longitude,latitude' and within valid ranges.")
            }
            listOf(lon, lat)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid format. Coordinates must be in 'longitude,latitude' format.")
        }
    }
}