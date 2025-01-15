package np.com.naxa.drone_tasking_manager.core.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import np.com.naxa.drone_tasking_manager.R

val AppFontFamily = FontFamily(
    Font(R.font.avenir_next_lt_pro_regular, FontWeight.Normal),
    Font(R.font.avenir_next_lt_pro_regular, FontWeight.Medium),
    Font(R.font.avenir_next_lt_pro_regular, FontWeight.SemiBold),
    Font(R.font.avenir_next_lt_pro_bold, FontWeight.Bold),
    Font(R.font.avenir_next_lt_pro_bold, FontWeight.ExtraBold),
    Font(R.font.avenir_next_lt_pro_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.avenir_next_lt_pro_italic, FontWeight.Medium, FontStyle.Italic),
    Font(R.font.avenir_next_lt_pro_italic, FontWeight.SemiBold, FontStyle.Italic),
    Font(R.font.avenir_next_lt_pro_italic, FontWeight.Bold, FontStyle.Italic),
)

val LightTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = AppFontFamily,
        color = Color.DarkGray,
        fontSize = 24.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.15.sp,
        lineHeight = 32.sp
    ),
    displayMedium = TextStyle(
        fontFamily = AppFontFamily,
        color = Color.DarkGray,
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.1.sp,
        lineHeight = 36.sp
    ),
    displayLarge = TextStyle(
        fontFamily = AppFontFamily,
        color = Color.Black,
        fontSize = 32.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 0.sp,
        lineHeight = 40.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = AppFontFamily,
        color = Color.DarkGray,
        fontSize = 20.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.15.sp,
        lineHeight = 28.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = AppFontFamily,
        color = Color.DarkGray,
        fontSize = 22.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.1.sp,
        lineHeight = 30.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = AppFontFamily,
        color = Color.Black,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.05.sp,
        lineHeight = 32.sp
    ),
    titleSmall = TextStyle(
        fontFamily = AppFontFamily,
        color = Color.Gray,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.1.sp,
        lineHeight = 20.sp
    ),
    titleMedium = TextStyle(
        fontFamily = AppFontFamily,
        color = Color.Gray,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.05.sp,
        lineHeight = 24.sp
    ),
    titleLarge = TextStyle(
        fontFamily = AppFontFamily,
        color = Color.DarkGray,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.sp,
        lineHeight = 28.sp
    ),
    bodySmall = TextStyle(
        fontFamily = AppFontFamily,
        color = Color.Black,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.4.sp,
        lineHeight = 16.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = AppFontFamily,
        color = Color.Black,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.25.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = AppFontFamily,
        color = Color.DarkGray,
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.15.sp,
        lineHeight = 24.sp
    ),
    labelSmall = TextStyle(
        fontFamily = AppFontFamily,
        color = Color.White,
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.5.sp,
        lineHeight = 12.sp
    ),
    labelMedium = TextStyle(
        fontFamily = AppFontFamily,
        color = Color.White,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.25.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = AppFontFamily,
        color = Color.White,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.1.sp,
        lineHeight = 20.sp
    )
)


val DarkTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = AppFontFamily,
        color = Color.LightGray,
        fontSize = 24.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.15.sp,
        lineHeight = 32.sp
    ),
    displayMedium = TextStyle(
        fontFamily = AppFontFamily,
        color = Color.LightGray,
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.1.sp,
        lineHeight = 36.sp
    ),
    displayLarge = TextStyle(
        fontFamily = AppFontFamily,
        color = Color.White,
        fontSize = 32.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 0.sp,
        lineHeight = 40.sp
    ),

    headlineSmall = TextStyle(
        fontFamily = AppFontFamily,
        color = Color.LightGray,
        fontSize = 20.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.15.sp,
        lineHeight = 28.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = AppFontFamily,
        color = Color.LightGray,
        fontSize = 22.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.1.sp,
        lineHeight = 30.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = AppFontFamily,
        color = Color.White,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.05.sp,
        lineHeight = 32.sp
    ),

    titleSmall = TextStyle(
        fontFamily = AppFontFamily,
        color = Color.Gray,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.1.sp,
        lineHeight = 20.sp
    ),
    titleMedium = TextStyle(
        fontFamily = AppFontFamily,
        color = Color.Gray,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.05.sp,
        lineHeight = 24.sp
    ),
    titleLarge = TextStyle(
        fontFamily = AppFontFamily,
        color = Color.LightGray,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.sp,
        lineHeight = 28.sp
    ),

    bodySmall = TextStyle(
        fontFamily = AppFontFamily,
        color = Color.White,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.4.sp,
        lineHeight = 16.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = AppFontFamily,
        color = Color.LightGray,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.25.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = AppFontFamily,
        color = Color.LightGray,
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.15.sp,
        lineHeight = 24.sp
    ),

    labelSmall = TextStyle(
        fontFamily = AppFontFamily,
        color = Color.White,
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.5.sp,
        lineHeight = 12.sp
    ),
    labelMedium = TextStyle(
        fontFamily = AppFontFamily,
        color = Color.White,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.25.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = AppFontFamily,
        color = Color.White,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.1.sp,
        lineHeight = 20.sp
    )
)