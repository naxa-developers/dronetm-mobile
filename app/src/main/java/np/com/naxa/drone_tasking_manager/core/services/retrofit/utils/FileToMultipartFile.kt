package np.com.naxa.drone_tasking_manager.core.services.retrofit.utils

import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File


object MultipartFileUtils {
    fun getMultipartBodyPart(file: File?, partName: String): MultipartBody.Part? {
        return file?.let {
            try {
                Log.d(
                    "FileToMultipartFileUtils",
                    "Preparing multipart for file: ${it.absolutePath}"
                )

                if (!it.exists()) {
                    Log.e("FileToMultipartFileUtils", "File does not exist: ${it.absolutePath}")
                    return@let null
                }

                val requestFile = it.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                MultipartBody.Part.createFormData(partName, it.name, requestFile)
            } catch (e: Exception) {
                Log.e("FileToMultipartFileUtils", "Error creating multipart body: ${e.message}")
                null
            }
        } ?: run {
            Log.w("FileToMultipartFileUtils", "Null file provided for multipart body")
            null
        }
    }


    private fun getFileFromPath(filePath: String?): File? {
        return filePath?.let {
            File(it)
        }
    }


    fun createPartFromString(data: String): RequestBody {
        return data.toRequestBody("multipart/form-data".toMediaTypeOrNull())
    }

    fun createPartFromInt(data: Int): RequestBody {
        return data.toString().toRequestBody("multipart/form-data".toMediaTypeOrNull())
    }

    fun createPartFromBoolean(data: Boolean): RequestBody {
        return data.toString().toRequestBody("multipart/form-data".toMediaTypeOrNull())
    }
}