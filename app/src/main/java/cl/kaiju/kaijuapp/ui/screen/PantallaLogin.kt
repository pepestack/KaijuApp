package cl.kaiju.kaijuapp.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.kaiju.kaijuapp.R
import cl.kaiju.kaijuapp.model.Usuario
import cl.kaiju.kaijuapp.repository.DatosDePrueba
import cl.kaiju.kaijuapp.ui.components.etiqueta
import cl.kaiju.kaijuapp.ui.theme.KaijuAppTheme

/** RF-01. Muestra los usuarios de prueba; validar y abrir la sesión le toca al SesionViewModel. */
@Composable
fun PantallaLogin(
    usuarios: List<Usuario>,
    usuarioSeleccionadoId: Int?,
    error: String?,
    onSeleccionarUsuario: (Int) -> Unit,
    onIngresar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.login_titulo),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 32.dp)
            )
            Text(
                text = stringResource(R.string.login_subtitulo),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(
                modifier = Modifier.selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                usuarios.forEach { usuario ->
                    TarjetaUsuario(
                        usuario = usuario,
                        seleccionado = usuario.id == usuarioSeleccionadoId,
                        onClick = { onSeleccionarUsuario(usuario.id) }
                    )
                }
            }
            if (error != null) {
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Button(onClick = onIngresar, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.login_ingresar))
            }
        }
    }
}

@Composable
private fun TarjetaUsuario(usuario: Usuario, seleccionado: Boolean, onClick: () -> Unit) {
    val colorAcento = if (seleccionado) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = seleccionado, onClick = onClick, role = Role.RadioButton),
        border = BorderStroke(
            width = if (seleccionado) 2.dp else 1.dp,
            color = if (seleccionado) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Filled.AccountCircle, contentDescription = null, tint = colorAcento)
            Column(modifier = Modifier.weight(1f)) {
                Text(text = usuario.nombre, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = usuario.rol.etiqueta(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // onClick = null: el clic lo maneja la tarjeta completa
            RadioButton(selected = seleccionado, onClick = null)
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun PantallaLoginPreview() {
    KaijuAppTheme {
        PantallaLogin(
            usuarios = DatosDePrueba.usuarios,
            usuarioSeleccionadoId = 1,
            error = null,
            onSeleccionarUsuario = {},
            onIngresar = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun PantallaLoginErrorPreview() {
    KaijuAppTheme {
        PantallaLogin(
            usuarios = DatosDePrueba.usuarios,
            usuarioSeleccionadoId = null,
            error = "Elige un usuario para ingresar",
            onSeleccionarUsuario = {},
            onIngresar = {}
        )
    }
}
