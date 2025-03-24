package np.com.naxa.drone_tasking_manager.features.tasks.utils.flight_plan_creator

/**
 * Package containing classes and functions for generating flight plans.
 *
 * It provides an object `Parameters` that calculates the parameters based on the given
 * overlap percentages, altitude, and other optional parameters. The calculated parameters
 * are used to generate the flight plan.
 */
object Parameters {

    /**
     * Class that contains the parameters for the flight plan creator.
     *
     * It provides the functions to calculate the parameters based on the given
     * overlap percentages, altitude, and other optional parameters.
     */
    data class CalculatedParameters(
        val forwardPhotoHeight: Int,
        val sidePhotoWidth: Int,
        val forwardSpacing: Double,
        val sideSpacing: Double,
        val groundSpeed: Double,
        val altitudeAboveGroundLevel: Double
    )

    /**
     * Calculates the flight parameters based on the given overlap percentages, altitude,
     * and other optional parameters.
     *
     * @param forwardOverlap Forward overlap in percentage.
     * @param sideOverlap Side overlap in percentage.
     * @param agl Altitude above ground level.
     * @param gsd Ground sample distance of the image. Optional.
     * @param imageInterval The time interval at which images should be captured. Default is 2.
     * @return CalculatedParameters object containing calculated values such as photo dimensions,
     * spacing, ground speed, and actual altitude.
     */
    fun calculate(
        forwardOverlap: Double,
        sideOverlap: Double,
        agl: Double,
        gsd: Double? = null,
        imageInterval: Int = 2
    ): CalculatedParameters {

        // Constants (For DJI Mini 4 Pro)
        val verticalFov = 0.71F
        val horizontalFov = 1.26F
        val gsdToAglRatioConst = 29.7F

        val actualAgl = gsd?.let { it * gsdToAglRatioConst } ?: agl

        // Calculations
        val forwardPhotoHeight = actualAgl * verticalFov
        val sidePhotoWidth = actualAgl * horizontalFov
        val forwardOverlapDistance = forwardPhotoHeight * forwardOverlap / 100
        val sideOverlapDistance = sidePhotoWidth * sideOverlap / 100
        val forwardSpacing = forwardPhotoHeight - forwardOverlapDistance
        val sideSpacing = sidePhotoWidth - sideOverlapDistance
        var groundSpeed = forwardSpacing / imageInterval

        // Cap ground speed at 11.5 m/s to avoid problems with the DJI Mini 4 Pro controller.
        // Speeds over 12 m/s cause the controller to change the speed to 2.5 m/s, which is too slow.
        // Keeping it below 12 m/s ensures the flight plan works correctly.

        if (groundSpeed > 12) {
            groundSpeed = 11.5
        }

        return CalculatedParameters(
            forwardPhotoHeight.toInt(),
            sidePhotoWidth.toInt(),
            forwardSpacing,
            sideSpacing,
            groundSpeed,
            actualAgl
        )
    }
}