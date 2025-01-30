package np.com.naxa.drone_tasking_manager.features.tasks.models

import androidx.compose.ui.graphics.Color

enum class ProjectTaskState(
    val colorHex: String,
    val label: String
) {
    RequestForMapping("#F3C5C5", "Request For Mapping"),
    UnlockedToMap("rgba(255, 255, 255, 0.3)", "Unlocked To Map"),
    LockedForMapping("#98BBC8", "Locked For Mapping"),
    UnlockedToValidate("#176149", "Unlocked To Validate"),
    LockedForValidation("#FFFFFF", "Locked For Validation"),
    UnlockedDone("#FFFFFF", "Unlocked Done"),
    UnflyableTask("#9EA5AD", "Unflyable Task"),
    ImageUploaded("#9EC7FF", "Image Uploaded"),
    ImageProcessingFailed("#f00000", "Image Processing Failed"),
    ImageProcessingStarted("#9C77B2", "Image Processing Started"),
    ImageProcessingFinished("#ACD2C4", "Image Processed");

    val key: String = name
        .replace(Regex("(?<!^)([A-Z])"), "_$1")
        .lowercase()

    val color: Color = try {
        if (colorHex.startsWith("rgba")) {
            val rgba = colorHex.substring(5, colorHex.length - 1).split(",")
            val red = rgba[0].trim().toInt()
            val green = rgba[1].trim().toInt()
            val blue = rgba[2].trim().toInt()
            val alpha = rgba[3].trim().toInt()

            Color(red, green, blue, alpha)
        } else {
            val red = colorHex.substring(1, 3).toInt(16)
            val green = colorHex.substring(3, 5).toInt(16)
            val blue = colorHex.substring(5, 7).toInt(16)
            val alpha = if (colorHex.length == 9) colorHex.substring(7, 9).toInt(16) else 0xFF
            Color(red, green, blue, alpha)
        }
    } catch (e: Exception) {
        Color.White
    }

    companion object {
        private val keyLookup by lazy { entries.associateBy { it.key } }
        fun fromString(state: String?) = state?.trim()?.lowercase()?.let { keyLookup[it] }
    }
}