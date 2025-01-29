package np.com.naxa.drone_tasking_manager.core.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import np.com.naxa.drone_tasking_manager.R

val AppFontFamily = FontFamily(
    Font(R.font.barlowcondensed_extralight, FontWeight.ExtraLight),
    Font(R.font.barlowcondensed_light, FontWeight.Light),
    Font(R.font.barlowcondensed_regular, FontWeight.Normal),
    Font(R.font.barlowcondensed_medium, FontWeight.Medium),
    Font(R.font.barlowcondensed_semibold, FontWeight.SemiBold),
    Font(R.font.barlowcondensed_bold, FontWeight.Bold),
    Font(R.font.barlowcondensed_extrabold, FontWeight.ExtraBold),
    Font(R.font.barlowcondensed_extralightitalic, FontWeight.ExtraLight, FontStyle.Italic),
    Font(R.font.barlowcondensed_lightitalic, FontWeight.Light, FontStyle.Italic),
    Font(R.font.barlowcondensed_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.barlowcondensed_mediumitalic, FontWeight.Medium, FontStyle.Italic),
    Font(R.font.barlowcondensed_semibolditalic, FontWeight.SemiBold, FontStyle.Italic),
    Font(R.font.barlowcondensed_bolditalic, FontWeight.Bold, FontStyle.Italic),
)

val LightTypography = buildTypography(primaryColor = Color.DarkGray, secondaryColor = Color.Black)
val DarkTypography = buildTypography(primaryColor = Color.LightGray, secondaryColor = Color.White)

/**
 * Creates a [TextStyle] with predefined font family and customizable properties.
 *
 * This function simplifies the creation of [TextStyle] objects by setting the
 * fontFamily to [AppFontFamily] and allowing customization of other properties
 * like [fontSize], [fontWeight], [color], [letterSpacing], and [lineHeight].
 *
 * @param fontSize The size of the font.
 * @param fontWeight The weight of the font (e.g., [FontWeight.Bold], [FontWeight.Normal]).
 * @param color The color of the text.
 * @param letterSpacing The amount of space to add between each letter. Defaults to `0.sp`.
 * @param lineHeight The height of each line of text. Defaults to [TextUnit.Unspecified].
 * @return A [TextStyle] object with the specified properties.
 *
 * @sample
 * ```
 * val myTextStyle = textStyle(
 *     fontSize = 16.sp,
 *     fontWeight = FontWeight.Medium,
 *     color = Color.Black,
 *     letterSpacing = 0.5.sp,
 *     lineHeight = 24.sp
 * )
 * ```
 */
fun textStyle(
    fontSize: TextUnit,
    fontWeight: FontWeight,
    color: Color,
    letterSpacing: TextUnit = 0.sp,
    lineHeight: TextUnit = TextUnit.Unspecified
): TextStyle {
    return TextStyle(
        fontFamily = AppFontFamily,
        fontSize = fontSize,
        fontWeight = fontWeight,
        color = color,
        letterSpacing = letterSpacing,
        lineHeight = lineHeight
    )
}

/**
 * Builds a [Typography] object with predefined text styles using the provided primary and secondary colors.
 *
 * This function defines a complete set of text styles for various text roles (display, headline, title, body, label)
 * in small, medium, and large sizes. Each text style is configured with specific font size, font weight, color,
 * letter spacing, and line height.
 *
 * @param primaryColor The primary color used for the majority of the text styles.
 * @param secondaryColor The secondary color used for some of the text styles, providing visual contrast.
 * @return A [Typography] object containing the defined text styles.
 *
 */
fun buildTypography(primaryColor: Color, secondaryColor: Color): Typography {
    return Typography(
        displaySmall = textStyle(24.sp, FontWeight.Medium, primaryColor, 0.15.sp, 32.sp),
        displayMedium = textStyle(28.sp, FontWeight.Bold, primaryColor, 0.1.sp, 36.sp),
        displayLarge = textStyle(32.sp, FontWeight.ExtraBold, secondaryColor, 0.sp, 40.sp),
        headlineSmall = textStyle(20.sp, FontWeight.Normal, primaryColor, 0.15.sp, 28.sp),
        headlineMedium = textStyle(22.sp, FontWeight.Medium, primaryColor, 0.1.sp, 30.sp),
        headlineLarge = textStyle(24.sp, FontWeight.Bold, secondaryColor, 0.05.sp, 32.sp),
        titleSmall = textStyle(14.sp, FontWeight.SemiBold, primaryColor, 0.1.sp, 20.sp),
        titleMedium = textStyle(16.sp, FontWeight.Bold, primaryColor, 0.05.sp, 24.sp),
        titleLarge = textStyle(18.sp, FontWeight.Bold, primaryColor, 0.sp, 28.sp),
        bodySmall = textStyle(12.sp, FontWeight.Normal, secondaryColor, 0.4.sp, 16.sp),
        bodyMedium = textStyle(14.sp, FontWeight.Normal, secondaryColor, 0.25.sp, 20.sp),
        bodyLarge = textStyle(16.sp, FontWeight.Normal, primaryColor, 0.15.sp, 24.sp),
        labelSmall = textStyle(10.sp, FontWeight.Medium, primaryColor, 1.5.sp, 12.sp),
        labelMedium = textStyle(12.sp, FontWeight.SemiBold, primaryColor, 1.25.sp, 16.sp),
        labelLarge = textStyle(14.sp, FontWeight.Bold, primaryColor, 1.1.sp, 20.sp)
    )
}