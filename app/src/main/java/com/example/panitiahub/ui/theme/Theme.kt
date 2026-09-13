package com.example.panitiahub.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val PanitiaDarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = DeepSpace,
    primaryContainer = SurfaceElevated,
    onPrimaryContainer = ElectricBlueBright,

    secondary = CyberGreen,
    onSecondary = DeepSpace,
    secondaryContainer = SurfaceElevated2,
    onSecondaryContainer = CyberGreenBright,

    tertiary = NeonPink,
    onTertiary = DeepSpace,
    tertiaryContainer = SurfaceElevated2,
    onTertiaryContainer = NeonPink,

    background = DeepSpace,
    onBackground = TextPrimary,

    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    surfaceTint = ElectricBlue,

    outline = OutlineDark,
    outlineVariant = OutlineDark,

    error = ExpenseRed,
    onError = DeepSpace,
    errorContainer = SurfaceElevated2,
    onErrorContainer = ExpenseRed,

    scrim = ScrimDark
)

private val PanitiaLightColorScheme = lightColorScheme(
    primary = ElectricBlue,
    onPrimary = Color.White,
    primaryContainer = ElectricBlue.copy(alpha = 0.1f),
    onPrimaryContainer = ElectricBlue,

    secondary = CyberGreen,
    onSecondary = Color.White,
    secondaryContainer = CyberGreen.copy(alpha = 0.1f),
    onSecondaryContainer = CyberGreen,

    tertiary = NeonPink,
    onTertiary = Color.White,
    tertiaryContainer = NeonPink.copy(alpha = 0.1f),
    onTertiaryContainer = NeonPink,

    background = Color(0xFFF8F9FA),
    onBackground = Color(0xFF1E1E1E),

    surface = Color.White,
    onSurface = Color(0xFF1E1E1E),
    surfaceVariant = Color(0xFFF0F0F0),
    onSurfaceVariant = Color(0xFF757575),
    surfaceTint = ElectricBlue,

    outline = Color(0xFFE0E0E0),
    outlineVariant = Color(0xFFE0E0E0),

    error = ExpenseRed,
    onError = Color.White,
    errorContainer = ExpenseRed.copy(alpha = 0.1f),
    onErrorContainer = ExpenseRed,

    scrim = Color.Black.copy(alpha = 0.3f)
)

private val PanitiaShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun PanitiaHubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        PanitiaDarkColorScheme
    } else {
        PanitiaLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PanitiaTypography,
        shapes = PanitiaShapes,
        content = content
    )
}