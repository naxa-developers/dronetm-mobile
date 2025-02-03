package np.com.naxa.drone_tasking_manager.navigation.routes

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

    /**
     * Route for displaying the User Profile screen.
     * Path: /profile
     */
    ProfileScreen(
        label = "Profile",
        path = "profile"),
    /**
     * Route for displaying the Projects screen.
     * Path: /projects
     */
    Projects(
        label = "Projects",
        path = "projects"
    ),

    /**
     * Route for displaying the Projects List screen.
     * Path: /projects-list
     */
    ProjectsList(
        label = "Projects",
        path = "projects-list"
    ),

    /**
     * Route for displaying the Projects Map screen.
     * Path: /projects-map
     */
    ProjectsMap(
        label = "Map",
        path = "projects-map"
    ),

    /**
     * Route for displaying the Project Details screen.
     * Path: /projects
     */
    ProjectDetails(
        label = "Details",
        path = "projects-details/{id}"
    ),

    /**
     * Route for displaying the Task Details screen.
     * Path: /task-details/{id}/{project}
     */
    TaskDetails(
        label = "Task Details",
        path = "task-details/{id}/{project}"
    ),
}