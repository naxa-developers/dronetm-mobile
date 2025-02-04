package np.com.naxa.drone_tasking_manager.features.tasks.views.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.google.gson.JsonObject
import np.com.naxa.drone_tasking_manager.utils.round
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskWaypointInfoBottomSheet(
    modifier: Modifier = Modifier,
    infoJsonObject: JsonObject?,
    infoSheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
    show: Boolean,
    onDismiss: () -> Unit = {},
) {

    if (show) {
        ModalBottomSheet(
            modifier = modifier
                .wrapContentHeight(),
            sheetState = infoSheetState,
            onDismissRequest = {
                onDismiss.invoke()
            }
        ) {
            infoJsonObject?.let {
                val keys = remember { it.keySet().toList().sortedBy { if (it == "index") 0 else 1 } }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    for (i in 0 until it.size()) {
                        val key = keys[i]
                        val value = it.get(key)
                        val valueStr = when {
                            value.isJsonPrimitive -> {
                                val primitive = value.asJsonPrimitive
                                when {
                                    primitive.isString -> primitive.asString
                                    primitive.isNumber -> primitive.asNumber.toString()
                                    primitive.isBoolean -> primitive.asBoolean.toString()
                                    else -> ""
                                }
                            }
                            else -> ""
                        }

                        if (key == "index") {
                            val point =
                                valueStr.toIntOrNull() ?: valueStr.toFloatOrNull()?.toInt() ?: 0
                            Text(
                                modifier = Modifier.padding(bottom = 16.dp),
                                text = "Point #${point} ${if (point == 0) "(Take Off Point)" else ""}",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            continue
                        }

                        KeyValueText(
                            key = key.split("_").joinToString(separator = " ") { word ->
                                word.replaceFirstChar {
                                    if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString()
                                }
                            },
                            value = valueStr
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KeyValueText(key: String, value: String) = Row(
    modifier = Modifier.padding(bottom = 12.dp),
    verticalAlignment = Alignment.CenterVertically
) {
    Text(
        modifier = Modifier.weight(1f),
        text = "$key :",
        style = MaterialTheme.typography.bodyLarge
    )
    Text(
        modifier = Modifier.weight(2f),
        text = buildAnnotatedString {
            append(value.toDoubleOrNull()?.round(2)?.toString() ?: value)
            append(" ")
            withStyle(
                MaterialTheme.typography.bodyLarge.toSpanStyle().copy(
                    fontWeight = FontWeight.Normal
                )
            ) {
                if (key.lowercase().trim() == "speed") append("m/s")
                if (key.lowercase().trim() == "altitude" || key.lowercase()
                        .trim() == "elevation"
                ) append("meter")
                if (key.lowercase().trim() == "gimbal angle" || key.lowercase()
                        .trim() == "heading"
                ) append("degree")
            }
        },
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Start,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}