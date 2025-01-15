package np.com.naxa.drone_tasking_manager

/**
 * A sealed class representing different states of a resource operation.
 *
 * @param T The type of data being handled
 * @property data Optional data payload
 * @property message Optional message, typically used for error descriptions
 */
sealed class Resources<T> (val data: T? = null, val message: String? = null) {
    /**
     * Represents a successful operation with optional data.
     *
     * @param data The data payload of the success state
     */
    class Success<T>(data: T?) : Resources<T>(data)

    /**
     * Represents an error state with a message and optional data.
     *
     * @param message The error message describing what went wrong
     * @param data Optional data that might be available even in error state
     */
    class Error<T>(message: String, data: T? = null) : Resources<T>(data, message)

    /**
     * Represents a loading state of the resource operation.
     *
     * @property isLoading Boolean flag indicating if the loading is in progress
     */
    class Loading<T>() : Resources<T>(null)
}