package np.com.naxa.drone_tasking_manager.core.widgets

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp

@Composable
fun SuperscriptText(
    modifier: Modifier = Modifier,
    text: String,
    superscriptText: String,
    style: TextStyle? = null,
    superscriptStyle: SpanStyle? = null
) {
    Text(
        modifier = modifier,
        text = buildAnnotatedString {
            append(text)
            withStyle(
                style = superscriptStyle?.copy(
                    baselineShift = BaselineShift.Superscript
                ) ?: SpanStyle(
                    fontSize = 12.sp,
                    baselineShift = BaselineShift.Superscript
                )
            ) {
                append(superscriptText)
            }
        },
        style = style ?: TextStyle(fontSize = 16.sp)
    )
}