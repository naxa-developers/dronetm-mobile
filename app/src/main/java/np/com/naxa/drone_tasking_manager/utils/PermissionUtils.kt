package np.com.naxa.drone_tasking_manager.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat

object PermissionUtils {

    /**
     * Checks if the application has the necessary file access permissions.
     *
     * This function determines if the application has either all file access permissions
     * or media file access permissions.
     *
     * @param context The context of the application or activity.
     * @return True if the application has the required file access permissions, false otherwise.
     */
    fun hasFileAccessPermissions(context: Context): Boolean {
        return hasAllFileAccessPermission() || hasMediaFileAccessPermissions(context)
    }

    /**
     * Method to check if has read and write external storage permission (media files only)
     */
    fun hasMediaFileAccessPermissions(context: Context): Boolean {
        // Else just requesting permission to handle media files only
        val readPermission = Manifest.permission.READ_EXTERNAL_STORAGE
        val writePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE

        val hasReadPermission = ContextCompat.checkSelfPermission(
            context,
            readPermission
        ) == PackageManager.PERMISSION_GRANTED

        val hasWritePermission = ContextCompat.checkSelfPermission(
            context,
            writePermission
        ) == PackageManager.PERMISSION_GRANTED

        return hasReadPermission && hasWritePermission
    }

    /**
     * Method to check if device has all files access permission
     */
    fun hasAllFileAccessPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager()
        }
        return false
    }

    /**
     * Method to check if the application has coarse location permission.
     *
     * @param context The context of the application or activity.
     * @return True if the application has coarse location permission, false otherwise.
     */
    fun hasCoarseLocationPermission(context: Context): Boolean {
        val coarseLocationPermission = Manifest.permission.ACCESS_COARSE_LOCATION
        return ContextCompat.checkSelfPermission(
            context,
            coarseLocationPermission
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Method to check if the application has fine location permission.
     *
     * @param context The context of the application or activity.
     * @return True if the application has fine location permission, false otherwise.
     */
    fun hasFineLocationPermission(context: Context): Boolean {
        val fineLocationPermission = Manifest.permission.ACCESS_FINE_LOCATION
        return ContextCompat.checkSelfPermission(
            context,
            fineLocationPermission
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Method to check if the application has either coarse or fine location permission.
     *
     * @param context The context of the application or activity.
     * @return True if the application has either coarse or fine location permission, false otherwise.
     */
    fun hasLocationPermissions(context: Context): Boolean {
        return hasCoarseLocationPermission(context) || hasFineLocationPermission(context)
    }
}