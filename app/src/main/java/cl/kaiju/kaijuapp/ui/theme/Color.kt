package cl.kaiju.kaijuapp.ui.theme

import androidx.compose.ui.graphics.Color

// --- BRAND CORE COLORS ---
// Extracted from Cyber Kaiju Banner & Prints
val KaijuBlack = Color(0xFF0B0A0E)          // Deepest void black (App Background)
val KaijuCharcoal = Color(0xFF16151D)       // T-shirt fabric dark grey (Cards / Surfaces)
val KaijuSurfaceVariant = Color(0xFF24222F) // Elevated surfaces, text fields, dividers

val CyberMagenta = Color(0xFFFF007F)        // Primary brand accent (Banner neon pink)
val CyberMagentaDark = Color(0xFF99004C)    // Containers / pressed states
val CyberMagentaDeep = Color(0xFFC2005F)    // Light mode primary (AA contrast on white)
val CyberMagentaLight = Color(0xFFFF80BF)   // Subtle highlights

val AcidLime = Color(0xFFD4FF00)            // Secondary accent (Banner discount slash / Logo yellow-green)
val AcidLimeDark = Color(0xFF4D5E00)        // Secondary container

val TokusatsuBlue = Color(0xFF0055FF)       // Tertiary accent (Godzilla lightning blue)
val TokusatsuBlueLight = Color(0xFF6699FF)  // Readable blue on dark surfaces

val SilksongCrimson = Color(0xFFE6194B)     // Logo ring & Silksong red (Error / Out of Stock)
val SilksongCrimsonDark = Color(0xFF5C0A1E) // Error container on dark surfaces
val SilksongCrimsonLight = Color(0xFFFFD9DE)// Error container on light surfaces

// --- NEUTRALS ---
val StarkWhite = Color(0xFFF7F7FA)          // Primary text & garment print white
val MutedGrey = Color(0xFF9E9BA8)           // Secondary text, SKU labels, timestamps
val LightBackground = Color(0xFFF4F4F8)     // Clean warehouse daylight mode background
val LightSurface = Color(0xFFFFFFFF)        // Daylight mode cards

// --- INVENTORY SEMANTIC COLORS (Stock Levels) ---
// Thresholds are per product (see Producto.enAlerta), not fixed unit counts.
val StockHealthy = Color(0xFF00E676)        // Sufficient stock
val StockLow = Color(0xFFFFEA00)            // At or below stockMinimo
val StockEmpty = Color(0xFFFF1744)          // Out of stock (0 units)
