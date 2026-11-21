package cl.kaiju.kaijuapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cl.kaiju.kaijuapp.R

/**
 * Insignia que aparece al guardar un movimiento: un check, o una campana si el producto
 * entró en alerta de stock. Es solo visual; quien la usa decide cuándo mostrarla y la oculta.
 */
@Composable
fun ResultadoMovimientoAnimado(
    visible: Boolean,
    alerta: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(tween(150)) + scaleIn(
            initialScale = 0.3f,
            animationSpec = spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMediumLow)
        ),
        exit = fadeOut(tween(250)) + scaleOut(targetScale = 0.8f, animationSpec = tween(250))
    ) {
        // la campana se balancea una vez al aparecer para llamar la atención
        val giro = remember { Animatable(0f) }
        LaunchedEffect(alerta) {
            if (alerta) {
                giro.animateTo(14f, tween(80))
                giro.animateTo(-14f, tween(120))
                giro.animateTo(8f, tween(100))
                giro.animateTo(0f, tween(80))
            }
        }
        val color = if (alerta) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(MaterialTheme.colorScheme.surface, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (alerta) Icons.Filled.Notifications else Icons.Filled.CheckCircle,
                contentDescription = stringResource(
                    if (alerta) R.string.resultado_alerta_stock else R.string.movimiento_registrado
                ),
                tint = color,
                modifier = Modifier
                    .size(64.dp)
                    .rotate(giro.value)
            )
        }
    }
}
