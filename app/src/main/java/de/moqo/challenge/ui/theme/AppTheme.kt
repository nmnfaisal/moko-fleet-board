package de.moqo.challenge.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import de.moqo.challenge.R

/**
 * Colours are resolved from XML resources rather than hardcoded, so the 50+ white label flavours in
 * the production app all work without touching Compose code.
 */
data class AppColors(
    val accent: Color,
    val textDark: Color,
    val textMuted: Color,
    val success: Color,
)

private val LocalAppColors: ProvidableCompositionLocal<AppColors> = staticCompositionLocalOf {
    error("AppColors not provided, wrap the content in AppTheme")
}

object AppTheme {
    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current
}

private val AppTypography = Typography(
    titleLarge = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, letterSpacing = 0.sp),
    bodySmall = TextStyle(fontSize = 14.sp, letterSpacing = 0.sp),
    labelLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.sp),
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val colors = AppColors(
        accent = colorResource(R.color.accent),
        textDark = colorResource(R.color.text_dark),
        textMuted = colorResource(R.color.text_muted),
        success = colorResource(R.color.status_active),
    )
    CompositionLocalProvider(LocalAppColors provides colors) {
        MaterialTheme(
            colorScheme = lightColorScheme(
                primary = colors.accent,
                onPrimary = Color.White,
                surface = Color.White,
                onSurface = colors.textDark,
            ),
            typography = AppTypography,
            content = content,
        )
    }
}
