package np.com.naxa.drone_tasking_manager.utils

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns

fun getFileName(context: Context, uri: Uri): String? {
    var fileName: String? = null
    var fileSize: Long? = null

    val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            fileName = it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
            fileSize = it.getLong(it.getColumnIndexOrThrow(OpenableColumns.SIZE))
        }
    }

    return fileName
}