package cl.kaiju.kaijuapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Tip: for extra polish, bundle "Chakra Petch" or "Space Grotesk" in res/font
// for Display/Headline, and "JetBrains Mono" for Label/SKU styles.
val KaijuDisplayFont = FontFamily.SansSerif
val KaijuBodyFont = FontFamily.Default
val KaijuMonoFont = FontFamily.Monospace // Ideal for SKUs, barcodes, and stock quantities

val KaijuTypography = Typography(
    // Screen Titles (e.g., "INVENTARIO", "STOCK CRÍTICO")
    headlineLarge = TextStyle(
        fontFamily = KaijuDisplayFont,
        fontWeight = FontWeight.Black,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = 1.sp
    ),
    // Category / Collection Headers (e.g., "COLECCIÓN TOKUSATSU", "HOLLOW KNIGHT")
    headlineMedium = TextStyle(
        fontFamily = KaijuDisplayFont,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.5.sp
    ),
    // Top app bar titles
    titleLarge = TextStyle(
        fontFamily = KaijuDisplayFont,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    // Product Card Titles (e.g., "Polera Godzilla King of the Monsters")
    titleMedium = TextStyle(
        fontFamily = KaijuBodyFont,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.15.sp
    ),
    // General descriptions, garment details (e.g., "Serigrafía 4 tintas, Algodón 100%")
    bodyMedium = TextStyle(
        fontFamily = KaijuBodyFont,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    // Product codes on cards (e.g., "Código: PRD-001")
    bodySmall = TextStyle(
        fontFamily = KaijuMonoFont,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    // SKU Codes, Barcodes, Size Matrix Badges (e.g., "SKU: KJ-GDZ-001-XL")
    labelMedium = TextStyle(
        fontFamily = KaijuMonoFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    // Small tags & timestamps
    labelSmall = TextStyle(
        fontFamily = KaijuMonoFont,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    )
)

// Large Stock Count Display on Item Cards (e.g., "42 UNIDADES").
// Kept out of titleLarge so top app bar titles stay in the display font.
val StockCountStyle = TextStyle(
    fontFamily = KaijuMonoFont,
    fontWeight = FontWeight.Bold,
    fontSize = 22.sp,
    lineHeight = 28.sp,
    letterSpacing = 0.sp
)
