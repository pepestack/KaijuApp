package cl.kaiju.kaijuapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.kaiju.kaijuapp.R
import cl.kaiju.kaijuapp.data.model.MonsterDto
import cl.kaiju.kaijuapp.model.MonsterUiState
import cl.kaiju.kaijuapp.ui.theme.KaijuAppTheme
import coil3.compose.AsyncImage

// La pantalla entrega estado y callbacks; la tarjeta no consulta el servicio.
@Composable
fun TarjetaMonstruo(
    estado: MonsterUiState,
    onImagenFallida: (String, Int) -> Unit,
    onReintentar: () -> Unit,
    onReintentarImagen: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!estado.cargando && estado.monstruo == null && estado.error == null) return
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(stringResource(R.string.monstruo_titulo), style = MaterialTheme.typography.titleMedium)
            estado.monstruo?.let { monstruo ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (estado.errorImagen) {
                        Text(
                            stringResource(R.string.monstruo_sin_imagen),
                            modifier = Modifier.size(96.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        key(monstruo.spriteUrl, estado.intentoImagen) {
                            AsyncImage(
                                model = monstruo.spriteUrl,
                                contentDescription = monstruo.nombre,
                                modifier = Modifier.size(96.dp),
                                contentScale = ContentScale.Fit,
                                onError = { onImagenFallida(monstruo.spriteUrl, estado.intentoImagen) }
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(monstruo.nombre, style = MaterialTheme.typography.titleLarge)
                        Text(stringResource(R.string.monstruo_id, monstruo.id),
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.monstruo_nivel, monstruo.nivel))
                        Text(stringResource(R.string.monstruo_raza, monstruo.raza))
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.monstruo_vida, monstruo.vida))
                        Text(stringResource(R.string.monstruo_elemento, monstruo.elemento))
                    }
                }
                if (estado.errorImagen) {
                    TextButton(onClick = onReintentarImagen) {
                        Text(stringResource(R.string.monstruo_reintentar_imagen))
                    }
                }
            }
            if (estado.cargando) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Text(stringResource(R.string.easter_egg_cargando))
                }
            }
            estado.error?.let { mensaje ->
                Text(mensaje, color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = onReintentar, enabled = !estado.cargando) {
                    Text(stringResource(R.string.easter_egg_reintentar))
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 320)
@Composable
private fun TarjetaMonstruoPreview() {
    KaijuAppTheme {
        TarjetaMonstruo(
            estado = MonsterUiState(
                monstruo = MonsterDto(1002, "Poring", 1, "Plant", "Water 1", 55, ""),
                errorImagen = true
            ),
            onImagenFallida = { _, _ -> },
            onReintentar = {},
            onReintentarImagen = {}
        )
    }
}
