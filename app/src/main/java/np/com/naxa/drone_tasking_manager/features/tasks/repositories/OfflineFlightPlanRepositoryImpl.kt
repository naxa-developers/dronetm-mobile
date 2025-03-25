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
import np.com.naxa.drone_tasking_manager.core.utils.DownloadResponse
import np.com.naxa.drone_tasking_manager.core.utils.Response
import np.com.naxa.drone_tasking_manager.core.utils.responsevalidator.ErrorResponse
import np.com.naxa.drone_tasking_manager.core.utils.responsevalidator.getErrorMessage
import np.com.naxa.drone_tasking_manager.features.projects.dto.project.NoFlyZones
import np.com.naxa.drone_tasking_manager.features.projects.dto.project.toGeoJsonStr
import np.com.naxa.drone_tasking_manager.features.projects.models.toGeoJsonStr
import np.com.naxa.drone_tasking_manager.features.tasks.models.ProjectTask
import np.com.naxa.drone_tasking_manager.features.tasks.utils.flight_plan_creator.DroneFlightPlan
import np.com.naxa.drone_tasking_manager.features.tasks.utils.flight_plan_creator.Mode
import np.com.naxa.drone_tasking_manager.features.tasks.utils.flight_plan_creator.Parameters
import np.com.naxa.drone_tasking_manager.features.tasks.utils.flight_plan_creator.WaypointsOrLines
import np.com.naxa.drone_tasking_manager.utils.getFileFromMediaStorage
import org.maplibre.geojson.FeatureCollection
import retrofit2.HttpException
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject

class OfflineFlightPlanRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) :
    OfflineFlightPlanRepository {

    override suspend fun taskWayPointsOrLines(
        task: ProjectTask,
        noFlyZones: NoFlyZones?,
        rotationAngle: Int,
        takeOffPoint: List<Double>?,
        mode: Mode
    ): Flow<Response<FeatureCollection>> {
        return flow {
            emit(Response.Loading())
            try {
                val parameters = Parameters.calculate(
                    forwardOverlap = task.frontOverlap?.toDouble() ?: 75.0,
                    sideOverlap = task.sideOverlap?.toDouble() ?: 70.0,
                    agl = 118.8,
                    gsd = task.gsdCmPx?.toDouble() ?: 4.0,
                    imageInterval = 2
                )

                val response = WaypointsOrLines.create(
                    projectArea = task.geometry!!.toGeoJsonStr(),
                    noFlyZones = noFlyZones?.toGeoJsonStr(),
                    parameters = parameters,
                    rotationAngle = 360 - rotationAngle.toDouble(),
                    takeOffPoint = takeOffPoint,
                    mode = mode,
                )

                emit(Response.Success(FeatureCollection.fromJson(response)))

            } catch (e: Exception) {
                emit(
                    Response.Error(
                        e.message ?: e.cause?.message ?: "Error generating waypoints."
                    )
                )
            }
        }
    }

    override suspend fun generateFlightPlanFile(
        task: ProjectTask,
        noFlyZones: NoFlyZones?,
        rotationAngle: Int,
        takeOffPoint: List<Double>?,
        mode: Mode,
    ): Flow<DownloadResponse> {
        return flow {
            emit(DownloadResponse.Downloading(0f))
            try {

                val taskId = task.id!!

                val parameters = Parameters.calculate(
                    forwardOverlap = task.frontOverlap?.toDouble() ?: 75.0,
                    sideOverlap = task.sideOverlap?.toDouble() ?: 70.0,
                    agl = 118.8,
                    gsd = task.gsdCmPx?.toDouble() ?: 4.0,
                    imageInterval = 2
                )

                val generated = DroneFlightPlan.create(
                    projectArea = task.geometry!!.toGeoJsonStr(),
                    noFlyZones = noFlyZones?.toGeoJsonStr(),
                    outfile = context.filesDir.absolutePath,
                    parameters = parameters,
                    rotationAngle = 360 - rotationAngle.toDouble(),
                    takeOffPoint = takeOffPoint,
                    mode = mode,
                )

                val generatedFile = File(generated)
                val fileName = "$taskId.${generatedFile.extension}"

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
                            FileInputStream(generatedFile).use { input ->
                                try {
                                    val totalBytes = generatedFile.length()
                                    val bufferSize =
                                        totalBytes.coerceAtMost(1024 * 1024).toInt() // Max 1 MB
                                    val buffer = ByteArray(bufferSize)
                                    var bytesTransferred = 0L
                                    var length: Int
                                    while (input.read(buffer).also { length = it } > 0) {
                                        output.write(
                                            buffer,
                                            0,
                                            length
                                        )
                                        bytesTransferred += length

                                        val progress =
                                            bytesTransferred.toFloat() / totalBytes.toFloat()

                                        // Update progress
                                        emit(DownloadResponse.Downloading(progress))
                                    }
                                } catch (_: Exception) {
                                    return@flow emit(
                                        DownloadResponse.Error(
                                            ErrorResponse.parseErrorBody(
                                                "Download failed"
                                            ).getErrorMessage()
                                        )
                                    )
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
                            FileInputStream(generatedFile).use { input ->
                                val totalBytes = generatedFile.length()
                                val bufferSize =
                                    totalBytes.coerceAtMost(1024 * 1024).toInt() // Max 1 MB
                                val buffer = ByteArray(bufferSize)
                                var bytesTransferred = 0L
                                var length: Int
                                while (input.read(buffer).also { length = it } > 0) {
                                    output.write(
                                        buffer,
                                        0,
                                        length
                                    )
                                    bytesTransferred += length

                                    val progress =
                                        bytesTransferred.toFloat() / totalBytes.toFloat()

                                    // Update progress
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