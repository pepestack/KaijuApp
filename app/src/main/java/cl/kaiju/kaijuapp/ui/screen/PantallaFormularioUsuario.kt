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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.kaiju.kaijuapp.R
import cl.kaiju.kaijuapp.model.FormularioUsuarioErrores
import cl.kaiju.kaijuapp.model.FormularioUsuarioEstado
import cl.kaiju.kaijuapp.model.Rol
import cl.kaiju.kaijuapp.ui.components.CampoFormulario
import cl.kaiju.kaijuapp.ui.components.SelectorDesplegable
import cl.kaiju.kaijuapp.ui.components.etiqueta
import cl.kaiju.kaijuapp.ui.theme.KaijuAppTheme

/** Crear o editar un usuario del backend. Validar y guardar le toca al UsuariosViewModel. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaFormularioUsuario(
    estado: FormularioUsuarioEstado,
    guardando: Boolean,
    onNombreChange: (String) -> Unit,
    onCorreoChange: (String) -> Unit,
    onContrasenaChange: (String) -> Unit,
    onRolChange: (Rol) -> Unit,
    onGuardar: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val errores = estado.errores
    // etiqueta() es composable; se arman los textos antes de entregarlos al selector
    val textosRol = Rol.entries.associateWith { it.etiqueta() }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (estado.esEdicion) R.string.formulario_usuario_editar
                            else R.string.formulario_usuario_nuevo
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.volver))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CampoFormulario(
                valor = estado.nombre,
                onValorChange = onNombreChange,
                etiqueta = stringResource(R.string.campo_nombre),
                icono = Icons.Filled.Person,
                error = errores.nombre
            )
            CampoFormulario(
                valor = estado.correo,
                onValorChange = onCorreoChange,
                etiqueta = stringResource(R.string.campo_correo),
                icono = Icons.Filled.Email,
                error = errores.correo,
                tipoTeclado = KeyboardType.Email
            )
            CampoFormulario(
                valor = estado.contrasena,
                onValorChange = onContrasenaChange,
                etiqueta = stringResource(R.string.campo_contrasena),
                icono = Icons.Filled.Lock,
                error = errores.contrasena,
                ayuda = stringResource(
                    if (estado.esEdicion) R.string.campo_contrasena_ayuda_edicion
                    else R.string.campo_contrasena_ayuda
                ),
                esContrasena = true
            )
            SelectorDesplegable(
                opciones = Rol.entries,
                seleccion = estado.rol,
                textoDe = { textosRol.getValue(it) },
                onSeleccion = onRolChange,
                etiqueta = stringResource(R.string.campo_rol),
                icono = Icons.Filled.Star,
                error = errores.rol
            )
            Button(
                onClick = onGuardar,
                enabled = !guardando,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Text(
                    text = stringResource(
                        if (estado.esEdicion) R.string.formulario_usuario_guardar
                        else R.string.formulario_usuario_crear
                    ),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun PantallaFormularioUsuarioPreview() {
    KaijuAppTheme {
        PantallaFormularioUsuario(
            estado = FormularioUsuarioEstado(
                nombre = "Vendedor Nuevo", correo = "nuevo@", rol = Rol.VENDEDOR,
                errores = FormularioUsuarioErrores(correo = "Ingresa un correo válido")
            ),
            guardando = false,
            onNombreChange = {}, onCorreoChange = {}, onContrasenaChange = {}, onRolChange = {},
            onGuardar = {}, onVolver = {}
        )
    }
}
