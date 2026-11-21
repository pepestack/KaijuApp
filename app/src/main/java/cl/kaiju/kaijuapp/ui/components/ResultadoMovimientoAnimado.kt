package cl.kaiju.kaijuapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cl.kaiju.kaijuapp.R
import kotlinx.coroutines.delay

/**
 * Insignia que aparece al guardar un movimiento. El check siempre indica que se guardó bien;
 * si el producto entró en alerta, una campana chica aparece después en la esquina como aviso
 * aparte, para que no se lea como un error. Quien la usa decide cuándo mostrarla y la oculta.
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
        // la campana entra cuando el check ya rebotó así se leen como dos mensajes en orden
        var mostrarCampana by remember { mutableStateOf(false) }
        LaunchedEffect(alerta) {
            if (alerta) {
                delay(350)
                mostrarCampana = true
            }
        }
        Box(modifier = Modifier.size(112.dp)) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .align(Alignment.Center)
                    .background(MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = stringResource(R.string.movimiento_registrado),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )
            }
            AnimatedVisibility(
                visible = mostrarCampana,
                modifier = Modifier.align(Alignment.TopEnd).offset(x = (-4).dp, y = 4.dp),
                enter = scaleIn(spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMedium))
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.tertiaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = stringResource(R.string.resultado_alerta_stock),
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier
                            .size(22.dp)
                            .balanceo(Unit, alAparecer = true)
                    )
                }
            }
        }
    }
}
