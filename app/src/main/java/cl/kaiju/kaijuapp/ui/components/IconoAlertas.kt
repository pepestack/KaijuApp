package cl.kaiju.kaijuapp.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.rotate

/**
 * Ícono de la pestaña Alertas con la cantidad de productos en alerta. Cada vez que
 * [sacudidas] aumenta, la campana se balancea para avisar que entró un producto nuevo.
 */
@Composable
fun IconoAlertas(cantidad: Int, sacudidas: Int, modifier: Modifier = Modifier) {
    BadgedBox(
        modifier = modifier,
        badge = { if (cantidad > 0) Badge { Text(cantidad.toString()) } }
    ) {
        Icon(Icons.Filled.Notifications, contentDescription = null, modifier = Modifier.balanceo(sacudidas))
    }
}

/**
 * Balancea el contenido una vez cada vez que cambia [disparador]. Con [alAparecer] también al
 * entrar en pantalla; sin él, volver a mostrar la barra (al salir del detalle) no repite el aviso.
 */
fun Modifier.balanceo(disparador: Any, alAparecer: Boolean = false): Modifier = composed {
    val giro = remember { Animatable(0f) }
    val inicial = remember { disparador }
    LaunchedEffect(disparador) {
        if (disparador == inicial && !alAparecer) return@LaunchedEffect
        listOf(16f, -16f, 10f, -6f, 0f).forEach { giro.animateTo(it, tween(90)) }
    }
    rotate(giro.value)
}
