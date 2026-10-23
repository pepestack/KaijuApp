package cl.kaiju.kaijuapp.ui.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import cl.kaiju.kaijuapp.R
import cl.kaiju.kaijuapp.ui.theme.KaijuAppTheme
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/** Animacion de entrada; al terminar avisa con onTerminar y AppNavHost decide a donde ir. */
@Composable
fun PantallaInicio(
    onTerminar: () -> Unit,
    modifier: Modifier = Modifier
) {
    // la animacion es estado de la vista no del negocio por eso vive aqui y no en un viewmodel
    val escala = remember { Animatable(0f) }
    val progreso = remember { Animatable(0f) }

    // Unit hace que corra una sola vez cuando la pantalla aparece
    LaunchedEffect(Unit) {
        // pop el resorte se pasa un poco de 1 y rebota
        escala.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMediumLow))
        // el logo se corre a la izquierda mientras aparece el titulo
        progreso.animateTo(1f, tween(durationMillis = 500, easing = FastOutSlowInEasing))
        delay(700)
        onTerminar()
    }

    ContenidoInicio(escala = escala.value, progreso = progreso.value, modifier = modifier)
}

// dibuja la pantalla con valores fijos asi los previews pueden mostrar cualquier momento de la animacion
@Composable
private fun ContenidoInicio(
    escala: Float,
    progreso: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.necoarc_splash),
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .graphicsLayer {
                        scaleX = escala
                        scaleY = escala
                    }
            )
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Visible,
                modifier = Modifier
                    .clipToBounds()
                    // mide el titulo completo pero solo ocupa una parte de su ancho segun el progreso
                    // con progreso 0 la fila solo tiene el logo y queda centrado
                    // al crecer la fila se ensancha y como sigue centrada el logo se mueve solo a la izquierda
                    .layout { medible, restricciones ->
                        val separacion = 16.dp.roundToPx()
                        val texto = medible.measure(restricciones.copy(minWidth = 0, maxWidth = Constraints.Infinity))
                        val ancho = ((texto.width + separacion) * progreso).roundToInt()
                        layout(ancho, texto.height) {
                            texto.placeRelative(separacion, 0)
                        }
                    }
                    .graphicsLayer { alpha = progreso }
            )
        }
    }
}

@Preview(name = "Inicio - pop", showBackground = true, backgroundColor = 0xFF0B0A0E, widthDp = 360, heightDp = 640)
@Composable
private fun PantallaInicioPopPreview() {
    KaijuAppTheme {
        ContenidoInicio(escala = 1f, progreso = 0f)
    }
}

@Preview(name = "Inicio - mitad", showBackground = true, backgroundColor = 0xFF0B0A0E, widthDp = 360, heightDp = 640)
@Composable
private fun PantallaInicioMitadPreview() {
    KaijuAppTheme {
        ContenidoInicio(escala = 1f, progreso = 0.5f)
    }
}

@Preview(name = "Inicio - final", showBackground = true, backgroundColor = 0xFF0B0A0E, widthDp = 360, heightDp = 640)
@Composable
private fun PantallaInicioFinalPreview() {
    KaijuAppTheme {
        ContenidoInicio(escala = 1f, progreso = 1f)
    }
}

// la animacion real se ve con el modo interactivo del preview de android studio
@Preview(name = "Inicio - animada", showBackground = true, backgroundColor = 0xFF0B0A0E, widthDp = 360, heightDp = 640)
@Composable
private fun PantallaInicioAnimadaPreview() {
    KaijuAppTheme {
        PantallaInicio(onTerminar = {})
    }
}
