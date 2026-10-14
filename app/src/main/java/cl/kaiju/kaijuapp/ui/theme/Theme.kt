package cl.kaiju.kaijuapp.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val KaijuDarkColorScheme = darkColorScheme(
    primary = CyberMagenta,
    onPrimary = KaijuBlack, // White on neon magenta fails AA contrast
    primaryContainer = CyberMagentaDark,
    onPrimaryContainer = StarkWhite,

    secondary = AcidLime,
    onSecondary = KaijuBlack,
    secondaryContainer = AcidLimeDark,
    onSecondaryContainer = AcidLime,

    tertiary = TokusatsuBlueLight,
    onTertiary = KaijuBlack,
    tertiaryContainer = TokusatsuBlue,
    onTertiaryContainer = StarkWhite,

    background = KaijuBlack,
    onBackground = StarkWhite,

    surface = KaijuCharcoal,
    onSurface = StarkWhite,
    surfaceVariant = KaijuSurfaceVariant,
    onSurfaceVariant = MutedGrey,
    // Cards use surfaceContainerHighest, menus and sheets the lower tones. It must not equal
    // surfaceVariant: contentColorFor() would match that first and give cards grey text.
    surfaceContainerLowest = KaijuBlack,
    surfaceContainerLow = Color(0xFF121118),
    surfaceContainer = KaijuCharcoal,
    surfaceContainerHigh = Color(0xFF1D1C26),
    surfaceContainerHighest = Color(0xFF22202C),

    error = SilksongCrimson,
    onError = Color.White,
    errorContainer = SilksongCrimsonDark,
    onErrorContainer = StarkWhite
)

private val KaijuLightColorScheme = lightColorScheme(
    primary = CyberMagentaDeep,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFD8E8),
    onPrimaryContainer = CyberMagentaDark,

    secondary = Color(0xFF5A6E00), // Darker lime for contrast on white
    onSecondary = Color.White,
    secondaryContainer = AcidLime,
    onSecondaryContainer = KaijuBlack,

    tertiary = TokusatsuBlue,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFDCE4FF),
    onTertiaryContainer = Color(0xFF001A55),

    background = LightBackground,
    onBackground = KaijuBlack,

    surface = LightSurface,
    onSurface = KaijuBlack,
    surfaceVariant = Color(0xFFE6E4F0),
    onSurfaceVariant = Color(0xFF4A4754),
    surfaceContainerLowest = LightSurface,
    surfaceContainerLow = Color(0xFFF9F9FC),
    surfaceContainer = Color(0xFFF1F0F6),
    surfaceContainerHigh = Color(0xFFEBEAF2),
    surfaceContainerHighest = Color(0xFFE8E6F1),

    error = SilksongCrimson,
    onError = Color.White,
    errorContainer = SilksongCrimsonLight,
    onErrorContainer = SilksongCrimsonDark
)

@Composable
fun KaijuAppTheme(
    // Dark by default so the Cyber Kaiju aesthetic shines out of the box
    darkTheme: Boolean = true,
    // Dynamic color would replace the brand palette, so it is opt-in
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> KaijuDarkColorScheme
        else -> KaijuLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = KaijuTypography,
        content = content
    )
}
