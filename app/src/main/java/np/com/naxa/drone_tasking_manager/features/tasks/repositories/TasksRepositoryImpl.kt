package np.com.naxa.drone_tasking_manager.features.tasks.repositories

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import np.com.naxa.drone_tasking_manager.core.services.ApiService
import np.com.naxa.drone_tasking_manager.core.utils.DownloadResponse
import np.com.naxa.drone_tasking_manager.core.utils.Response
import np.com.naxa.drone_tasking_manager.core.utils.responsevalidator.ErrorResponse
import np.com.naxa.drone_tasking_manager.core.utils.responsevalidator.getErrorMessage
import np.com.naxa.drone_tasking_manager.features.tasks.dto.TakeOffPointUpdateRequestBody
import np.com.naxa.drone_tasking_manager.features.tasks.dto.TaskEventRequestBody
import np.com.naxa.drone_tasking_manager.features.tasks.mapper.toProjectTask
import np.com.naxa.drone_tasking_manager.features.tasks.mapper.toTaskLockUnlockResponse
import np.com.naxa.drone_tasking_manager.features.tasks.mapper.toTaskUnFlyableResponse
import np.com.naxa.drone_tasking_manager.features.tasks.models.ProjectTask
import np.com.naxa.drone_tasking_manager.features.tasks.models.TaskLockUnlockResponse
import np.com.naxa.drone_tasking_manager.features.tasks.models.TaskUnFlyableResponse
import np.com.naxa.drone_tasking_manager.utils.DateUtils
import np.com.naxa.drone_tasking_manager.utils.fileName
import np.com.naxa.drone_tasking_manager.utils.getFileFromMediaStorage
import org.maplibre.geojson.FeatureCollection
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TasksRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: ApiService,
) :
    TasksRepository {
    override suspend fun fetchTaskById(
        id: String,
        forceRefresh: Boolean
    ): Flow<Response<ProjectTask>> {
        return flow {
            emit(Response.Loading())

            try {
                val response = apiService.fetchTaskById(
                    id = id,
                    forceRefresh = forceRefresh
                )

                emit(
                    Response.Success(
                        response.toProjectTask().copy(
                            id = id,
                        )
                    )
                )

            } catch (e: HttpException) {
                return@flow emit(
                    Response.Error(
                        ErrorResponse.parseErrorBody(
                            e.response()?.errorBody()?.string()
                        ).getErrorMessage()
                    )
                )
            } catch (e: Exception) {
                emit(Response.Error(e.message ?: e.cause?.message ?: "Error fetching task"))
            }
        }
    }


    override suspend fun lockTask(
        taskId: String,
        projectId: String
    ): Flow<Response<TaskLockUnlockResponse>> {
        return flow {
            emit(Response.Loading())

            try {
                val response = apiService.lockOrUnlockTask(
                    projectId = projectId,
                    taskId = taskId,
                    body = TaskEventRequestBody(
                        event = "request",
                        updatedAt = DateUtils.currentDateAsStr(),
                    ),
                )

                emit(Response.Success(response.toTaskLockUnlockResponse()))

            } catch (e: HttpException) {
                return@flow emit(
                    Response.Error(
                        ErrorResponse.parseErrorBody(
                            e.response()?.errorBody()?.string()
                        ).getErrorMessage()
                    )
                )
            } catch (e: Exception) {
                emit(Response.Error(e.message ?: e.cause?.message ?: "Error locking task $taskId"))
            }
        }
    }

    override suspend fun unlockTask(
        taskId: String,
        projectId: String
    ): Flow<Response<TaskLockUnlockResponse>> {
        return flow {
            emit(Response.Loading())

            try {
                val response = apiService.lockOrUnlockTask(
                    projectId = projectId,
                    taskId = taskId,
                    body = TaskEventRequestBody(
                        event = "unlock",
                        updatedAt = DateUtils.currentDateAsStr(),
                    ),
                )

                emit(Response.Success(response.toTaskLockUnlockResponse()))

            } catch (e: HttpException) {
                return@flow emit(
                    Response.Error(
                        ErrorResponse.parseErrorBody(
                            e.response()?.errorBody()?.string()
                        ).getErrorMessage()
                    )
                )
            } catch (e: Exception) {
                emit(
                    Response.Error(
                        e.message ?: e.cause?.message ?: "Error unlocking task $taskId"
                    )
                )
            }
        }
    }

    override suspend fun taskUnFlyable(
        taskId: String,
        projectId: String,
        comment: String?
    ): Flow<Response<TaskUnFlyableResponse>> {
        return flow {
            emit(Response.Loading())

            try {
                val response = apiService.unFlyableTask(
                    projectId = projectId,
                    taskId = taskId,
                    body = TaskEventRequestBody(
                        event = "comment",
                        comment = comment,
                        updatedAt = DateUtils.currentDateAsStr(),
                    ),
                )

                emit(Response.Success(response.toTaskUnFlyableResponse()))

            } catch (e: HttpException) {
                return@flow emit(
                    Response.Error(
                        ErrorResponse.parseErrorBody(
                            e.response()?.errorBody()?.string()
                        ).getErrorMessage()
                    )
                )
            } catch (e: Exception) {
                emit(
                    Response.Error(
                        e.message ?: e.cause?.message ?: "Error requesting task $taskId un-flyable"
                    )
                )
            }
        }
    }

    override suspend fun taskWayPoints(
        taskId: String,
        projectId: String,
        rotationAngle: Int,
        download: Boolean,
        forceRefresh: Boolean
    ): Flow<Response<FeatureCollection>> {
        return flow {
            emit(Response.Loading())
            try {
                val response = apiService.taskWayPointsOrWayLines(
                    projectId = projectId,
                    taskId = taskId,
                    rotationAngle = 360 - rotationAngle, //FE and BE uses 360 - angle to generate new files
                    download = download,
                    mode = "waypoints",
                    forceRefresh = forceRefresh
                )

                emit(Response.Success(response))

            } catch (e: HttpException) {
                return@flow emit(
                    Response.Error(
                        ErrorResponse.parseErrorBody(
                            e.response()?.errorBody()?.string()
                        ).getErrorMessage()
                    )
                )
            } catch (e: Exception) {
                emit(
                    Response.Error(
                        e.message ?: e.cause?.message ?: "Error fetching task: $taskId"
                    )
                )
            }
        }
    }

    override suspend fun taskWayLines(
        taskId: String,
        projectId: String,
        rotationAngle: Int,
        download: Boolean,
        forceRefresh: Boolean
    ): Flow<Response<FeatureCollection>> {
        return flow {
            emit(Response.Loading())

            try {
                val response = apiService.taskWayPointsOrWayLines(
                    projectId = projectId,
                    taskId = taskId,
                    rotationAngle = 360 - rotationAngle, //FE and BE uses 360 - angle to generate new files
                    download = download,
                    mode = "waylines",
                    forceRefresh = forceRefresh
                )

                emit(Response.Success(response))

            } catch (e: HttpException) {
                return@flow emit(
                    Response.Error(
                        ErrorResponse.parseErrorBody(
                            e.response()?.errorBody()?.string()
                        ).getErrorMessage()
                    )
                )
            } catch (e: Exception) {
                emit(
                    Response.Error(
                        e.message ?: e.cause?.message ?: "Error fetching task: $taskId"
                    )
                )
            }
        }
    }

    override suspend fun updateTakeOffPoint(
        taskId: String,
        projectId: String,
        rotationAngle: Int,
        download: Boolean,
        mode: String,
        forceRefresh: Boolean,
        latitude: Double,
        longitude: Double
    ): Flow<Response<FeatureCollection>> {
        return flow {
            emit(Response.Loading())

            try {
                val response = apiService.updateTakeOffPoint(
                    projectId = projectId,
                    taskId = taskId,
                    rotationAngle = rotationAngle,
                    download = download,
                    mode = mode,
                    forceRefresh = forceRefresh,
                    body = TakeOffPointUpdateRequestBody(
                        latitude = latitude,
                        longitude = longitude,
                    )
                )

                emit(Response.Success(response))

            } catch (e: HttpException) {
                return@flow emit(
                    Response.Error(
                        ErrorResponse.parseErrorBody(
                            e.response()?.errorBody()?.string()
                        ).getErrorMessage()
                    )
                )
            } catch (e: Exception) {
                emit(
                    Response.Error(
                        e.message ?: e.cause?.message
                        ?: "Error updating takeoff point of task: $taskId"
                    )
                )
            }
        }
    }

    override suspend fun downloadFlightPlanFile(
        taskId: String,
        projectId: String,
        mode: String,
        rotationAngle: Int
    ): Flow<DownloadResponse> {
        return flow {
            emit(DownloadResponse.Downloading(0f))

            try {
                val response = apiService.downloadFlightPlan(
                    projectId = projectId,
                    taskId = taskId,
                    rotationAngle = 360-rotationAngle, //FE and BE uses 360 - angle to generate new files
                    download = true,
                    mode = mode,
                    forceRefresh = true,
                )

                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        val fileName = response.fileName() ?: "$taskId.kmz"
                        val totalBytes = body.contentLength()
                        val inputStream = body.byteStream()
                        val bufferSize = 8192
                        var bytesWritten = 0L

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            val resolver = context.contentResolver

                            // First, check and delete existing file
                            val selection =
                                "${MediaStore.Downloads.DISPLAY_NAME} = ? AND ${MediaStore.Downloads.RELATIVE_PATH} = ?"
                            val selectionArgs = arrayOf(fileName, "Download/DroneTM/$taskId/")

                            try {
                                resolver.query(
                                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                                    arrayOf(MediaStore.Downloads._ID),
                                    selection,
                                    selectionArgs,
                                    null
                                )?.use { cursor ->
                                    while (cursor.moveToNext()) {
                                        val id =
                                            cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID))
                                        val deleteUri = ContentUris.withAppendedId(
                                            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                                            id
                                        )
                                        resolver.delete(deleteUri, null, null)
                                    }
                                }
                            } catch (e: Exception) {
                                return@flow emit(
                                    DownloadResponse.Error(
                                        ErrorResponse.parseErrorBody(
                                            "Failed to delete existing file: ${e.message}"
                                        ).getErrorMessage()
                                    )
                                )
                            }

                            // Now proceed with creating the new file
                            val contentValues = ContentValues().apply {
                                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                                put(
                                    MediaStore.Downloads.MIME_TYPE,
                                    "application/vnd.google-earth.kmz"
                                ) // MIME type for .kmz
                                put(
                                    MediaStore.Downloads.RELATIVE_PATH,
                                    "Download/DroneTM/$taskId"
                                )
                            }

                            val uri = resolver.insert(
                                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                                contentValues
                            )

                            uri?.let {
                                resolver.openOutputStream(it)?.use { output ->
                                    inputStream.use { input ->
                                        val buffer = ByteArray(bufferSize)
                                        var bytes = input.read(buffer)

                                        // Read and write the content in chunks
                                        while (bytes >= 0) {
                                            output.write(buffer, 0, bytes)
                                            bytesWritten += bytes
                                            bytes = input.read(buffer)

                                            // Emit progress of the download
                                            val progress = if (totalBytes > 0) {
                                                bytesWritten.toFloat() / totalBytes.toFloat()
                                            } else {
                                                -1f
                                            }
                                            emit(DownloadResponse.Downloading(progress))
                                        }
                                    }
                                } ?: return@flow emit(
                                    DownloadResponse.Error(
                                        ErrorResponse.parseErrorBody(
                                            "Download failed"
                                        ).getErrorMessage()
                                    )
                                )
                            } ?: return@flow emit(
                                DownloadResponse.Error(
                                    ErrorResponse.parseErrorBody(
                                        "Download failed"
                                    ).getErrorMessage()
                                )
                            )

                            uri.getFileFromMediaStorage(context, fileName)?.let {
                                return@flow emit(
                                    DownloadResponse.Completed(it)
                                )
                            } ?: return@flow emit(
                                DownloadResponse.Error(
                                    ErrorResponse.parseErrorBody(
                                        "Download failed"
                                    ).getErrorMessage()
                                )
                            )
                        } else {
                            val downloadsDir =
                                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                            val droneTmDirectory = File(downloadsDir, "DroneTM")
                            if (!droneTmDirectory.exists()) droneTmDirectory.mkdir()
                            val droneTmTaskDirectory = File(droneTmDirectory, taskId)
                            if (!droneTmTaskDirectory.exists()) droneTmTaskDirectory.mkdir()
                            val file = File(droneTmTaskDirectory, fileName)

                            // Delete existing file if it exists
                            if (file.exists()) {
                                try {
                                    val deleted = file.delete()
                                    if (!deleted) {
                                        return@flow emit(
                                            DownloadResponse.Error(
                                                ErrorResponse.parseErrorBody(
                                                    "Failed to delete existing file"
                                                ).getErrorMessage()
                                            )
                                        )
                                    }
                                } catch (e: Exception) {
                                    return@flow emit(
                                        DownloadResponse.Error(
                                            ErrorResponse.parseErrorBody(
                                                "Error deleting existing file: ${e.message}"
                                            ).getErrorMessage()
                                        )
                                    )
                                }
                            }

                            // Write the downloaded content to the file
                            try {
                                FileOutputStream(file).use { output ->
                                    inputStream.use { input ->
                                        val buffer = ByteArray(bufferSize)
                                        var bytes = input.read(buffer)

                                        // Read and write the content in chunks
                                        while (bytes >= 0) {
                                            output.write(buffer, 0, bytes)
                                            bytesWritten += bytes
                                            bytes = input.read(buffer)

                                            // Emit progress of the download
                                            val progress = if (totalBytes > 0) {
                                                bytesWritten.toFloat() / totalBytes.toFloat()
                                            } else {
                                                -1f
                                            }
                                            emit(DownloadResponse.Downloading(progress))
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                return@flow emit(
                                    DownloadResponse.Error(
                                        ErrorResponse.parseErrorBody(
                                            "Error writing file: ${e.message}"
                                        ).getErrorMessage()
                                    )
                                )
                            }

                            return@flow emit(
                                DownloadResponse.Completed(file)
                            )
                        }
                    } ?: return@flow emit(
                        DownloadResponse.Error(
                            ErrorResponse.parseErrorBody(
                                "Response body is null"
                            ).getErrorMessage()
                        )
                    )
                } else return@flow emit(
                    DownloadResponse.Error(
                        ErrorResponse.parseErrorBody(
                            "Download failed"
                        ).getErrorMessage()
                    )
                )


            } catch (e: HttpException) {
                return@flow emit(
                    DownloadResponse.Error(
                        ErrorResponse.parseErrorBody(
                            e.response()?.errorBody()?.string()
                        ).getErrorMessage()
                    )
                )
            } catch (e: Exception) {
                return@flow emit(
                    DownloadResponse.Error(
                        e.message ?: e.cause?.message ?: "Error downloading flight plan"
                    )
                )
            }
        }
    }
}