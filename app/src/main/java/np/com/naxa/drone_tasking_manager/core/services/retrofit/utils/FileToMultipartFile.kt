package np.com.naxa.drone_tasking_manager.core.services.retrofit.utils

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File


object FileToMultipartFileUtils {
    fun getMultipartBodyPart(filePath: String?, partName: String): MultipartBody.Part? {
        return getFileFromPath(filePath)?.let {
            val requestFile = it.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            MultipartBody.Part.createFormData(partName, it.name, requestFile)
        }
    }


    private fun getFileFromPath(filePath: String?): File? {
        return filePath?.let {
            File(it)
        }
    }
}