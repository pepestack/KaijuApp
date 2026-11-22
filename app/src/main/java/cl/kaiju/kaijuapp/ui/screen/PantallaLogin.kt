package cl.kaiju.kaijuapp.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.kaiju.kaijuapp.R
import cl.kaiju.kaijuapp.model.SesionEstado
import cl.kaiju.kaijuapp.ui.components.CampoFormulario
import cl.kaiju.kaijuapp.ui.theme.KaijuAppTheme

/**
 * RF-01. Correo y contraseña contra el backend de usuarios. Los errores de formato aparecen bajo
 * su campo; si las credenciales no coinciden se marcan los dos con un solo mensaje que no dice
 * cuál falló. El mensaje general queda para fallas de conexión. Validar le toca al SesionViewModel.
 */
@Composable
fun PantallaLogin(
    estado: SesionEstado,
    onCorreoChange: (String) -> Unit,
    onContrasenaChange: (String) -> Unit,
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
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                CampoFormulario(
                    valor = estado.correo,
                    onValorChange = onCorreoChange,
                    etiqueta = stringResource(R.string.campo_correo),
                    icono = Icons.Filled.Email,
                    error = estado.errorCorreo,
                    conError = estado.errorCorreo != null || estado.errorCredenciales != null,
                    tipoTeclado = KeyboardType.Email,
                    soloLectura = estado.cargando
                )
                CampoFormulario(
                    valor = estado.contrasena,
                    onValorChange = onContrasenaChange,
                    etiqueta = stringResource(R.string.campo_contrasena),
                    icono = Icons.Filled.Lock,
                    // el mensaje de credenciales va bajo la contraseña, junto al boton
                    error = estado.errorContrasena ?: estado.errorCredenciales,
                    esContrasena = true,
                    soloLectura = estado.cargando
                )
            }
            if (estado.errorGeneral != null) {
                Text(
                    text = estado.errorGeneral,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Button(
                onClick = onIngresar,
                enabled = !estado.cargando,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (estado.cargando) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.login_ingresar))
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun PantallaLoginPreview() {
    KaijuAppTheme {
        PantallaLogin(
            estado = SesionEstado(correo = "admin.kaiju@duoc.cl"),
            onCorreoChange = {},
            onContrasenaChange = {},
            onIngresar = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun PantallaLoginErrorPreview() {
    KaijuAppTheme {
        PantallaLogin(
            estado = SesionEstado(
                correo = "nadie@duoc.cl",
                contrasena = "1234",
                errorCredenciales = "Correo o contraseña incorrectos"
            ),
            onCorreoChange = {},
            onContrasenaChange = {},
            onIngresar = {}
        )
    }
}
