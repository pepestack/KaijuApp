package cl.kaiju.kaijuapp.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import cl.kaiju.kaijuapp.R
import cl.kaiju.kaijuapp.model.Rol
import cl.kaiju.kaijuapp.model.TipoMovimiento
import java.text.DateFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Currency
import java.util.Locale

/** Textos y formatos de presentación que comparten las pantallas. */

private val LocaleChile = Locale.forLanguageTag("es-CL")

@Composable
fun Rol.etiqueta(): String = stringResource(
    when (this) {
        Rol.ADMINISTRADOR -> R.string.rol_administrador
        Rol.VENDEDOR -> R.string.rol_vendedor
        Rol.ENCARGADO_INVENTARIO -> R.string.rol_encargado_inventario
    }
)

@Composable
fun TipoMovimiento.etiqueta(): String = stringResource(
    when (this) {
        TipoMovimiento.ENTRADA -> R.string.tipo_entrada
        TipoMovimiento.SALIDA_VENTA -> R.string.tipo_salida_venta
        TipoMovimiento.SALIDA_CONSUMO -> R.string.tipo_salida_consumo
        TipoMovimiento.AJUSTE -> R.string.tipo_ajuste
    }
)

/** Pesos chilenos sin decimales, por ejemplo "$12.990". */
@Composable
fun rememberFormatoPrecio(): NumberFormat = remember {
    NumberFormat.getCurrencyInstance(LocaleChile).apply {
        currency = Currency.getInstance("CLP")
        minimumFractionDigits = 0
        maximumFractionDigits = 0
    }
}

/** SimpleDateFormat en vez de java.time porque minSdk 24 no lo trae sin desugaring. */
@Composable
fun rememberFormatoFecha(): DateFormat = remember {
    SimpleDateFormat("dd-MM-yyyy HH:mm", LocaleChile)
}
