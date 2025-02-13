package np.com.naxa.drone_tasking_manager.utils.internetutils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/**
 * Object providing utility functions for checking internet connectivity.
 */
object CheckInternetConnectionUtils {
    /**
     * Checks if the device has an active internet connection.
     *
     * @param context The application context.
     * @return True if the device has internet connectivity, false otherwise.
     */
    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities =
            connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
