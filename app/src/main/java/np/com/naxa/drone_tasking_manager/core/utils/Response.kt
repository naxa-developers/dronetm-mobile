package np.com.naxa.drone_tasking_manager.core.utils


/**
 * A sealed class representing the different states of a resource operation.
 * It can be in a [Success] state, an [Error] state, or a [Loading] state.
 *
 * This class is typically used to handle the result of operations that can
 * either succeed, fail, or be in progress, such as network requests or
 * database queries. Using a sealed class provides type safety and ensures
 * that all possible states are handled.
 */
sealed class Response<T>(open val data: T? = null, open val message: String? = null) {
    /**
     * Represents a successful operation with optional data.
     *
     * @param data The data payload of the success state
     */
    data class Success<T>(override val data: T?) : Response<T>()

    /**
     * Represents an error state with a message and optional data.
     *
     * @param message The error message describing what went wrong
     */
    data class Error<T>(override val message: String) : Response<T>()

    /**
     * Represents a loading state of the resource operation.
     *
     */
    class Loading<T> : Response<T>()
}