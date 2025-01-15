package np.com.naxa.drone_tasking_manager

/**
 * Represents the available navigation routes in the application.
 *
 * @property label Human-readable name for the route
 * @property path URL path pattern for the route
 */
enum class Routes(
    val label: String,
    val path: String,
) {
    /**
     * Route for displaying the splash screen.
     * Path: /splash
     */
    Splash(
        label = "Splash",
        path = "splash"
    ),

    /**
     * Route for displaying the home screen.
     * Path: /home
     */
    Home(
        label = "Home",
        path = "home"
    ),

    /**
     * Route for displaying the download and transfer contents of a specific device.
     * Path: /download-and-transfer/{deviceId}
     * @param {deviceId} The unique identifier of the device
     */
    DownloadAndTransfer(
        label = "Download And Transfer",
        path = "download-and-transfer/{deviceId}"
    ),

    /**
     * Route for displaying the Login screen.
     * Path: /login
     */
    Login(
        label = "Login",
        path = "login"
    ),
}