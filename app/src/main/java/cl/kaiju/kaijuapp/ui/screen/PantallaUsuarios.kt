package cl.kaiju.kaijuapp.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.kaiju.kaijuapp.R
import cl.kaiju.kaijuapp.model.CuentaUsuario
import cl.kaiju.kaijuapp.model.ListaUsuariosEstado
import cl.kaiju.kaijuapp.model.Rol
import cl.kaiju.kaijuapp.ui.components.EstadoVacio
import cl.kaiju.kaijuapp.ui.components.etiqueta
import cl.kaiju.kaijuapp.ui.theme.KaijuAppTheme

/**
 * Gestión de usuarios del backend, solo para el Administrador. El usuario con la sesión
 * abierta no se puede eliminar a sí mismo, así que su tarjeta no muestra el botón.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaUsuarios(
    estado: ListaUsuariosEstado,
    usuarioActivoId: Int?,
    guardando: Boolean,
    onRecargar: () -> Unit,
    onNuevo: () -> Unit,
    onEditar: (CuentaUsuario) -> Unit,
    onEliminar: (CuentaUsuario) -> Unit,
    modifier: Modifier = Modifier
) {
    // el usuario que se quiere eliminar espera la confirmacion en un dialogo
    var porEliminar by remember { mutableStateOf<CuentaUsuario?>(null) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.usuarios_titulo)) },
                actions = {
                    IconButton(onClick = onRecargar, enabled = !estado.cargando) {
                        Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.usuarios_recargar))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNuevo) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.usuarios_nuevo))
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                estado.usuarios.isEmpty() && estado.cargando ->
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                estado.usuarios.isEmpty() && estado.error != null ->
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        EstadoVacio(icono = Icons.Filled.Warning, mensaje = estado.error)
                        OutlinedButton(onClick = onRecargar) { Text(stringResource(R.string.reintentar)) }
                    }

                estado.usuarios.isEmpty() ->
                    EstadoVacio(icono = Icons.Filled.Person, mensaje = stringResource(R.string.usuarios_vacio))

                else -> LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(estado.usuarios, key = { it.id }) { usuario ->
                        TarjetaUsuario(
                            usuario = usuario,
                            esUsuarioActivo = usuario.id == usuarioActivoId,
                            habilitada = !guardando,
                            onClick = { onEditar(usuario) },
                            onEliminar = { porEliminar = usuario },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }

    porEliminar?.let { usuario ->
        AlertDialog(
            onDismissRequest = { porEliminar = null },
            title = { Text(stringResource(R.string.usuarios_eliminar_titulo)) },
            text = { Text(stringResource(R.string.usuarios_eliminar_mensaje, usuario.nombre)) },
            confirmButton = {
                TextButton(onClick = {
                    porEliminar = null
                    onEliminar(usuario)
                }) { Text(stringResource(R.string.usuarios_eliminar)) }
            },
            dismissButton = {
                TextButton(onClick = { porEliminar = null }) { Text(stringResource(R.string.cancelar)) }
            }
        )
    }
}

@Composable
private fun TarjetaUsuario(
    usuario: CuentaUsuario,
    esUsuarioActivo: Boolean,
    habilitada: Boolean,
    onClick: () -> Unit,
    onEliminar: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(onClick = onClick, enabled = habilitada, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Filled.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(modifier = Modifier.weight(1f)) {
                Text(text = usuario.nombre, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = usuario.correo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (esUsuarioActivo) stringResource(R.string.usuarios_rol_tu, usuario.rol.etiqueta())
                        else usuario.rol.etiqueta(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            if (!esUsuarioActivo) {
                IconButton(onClick = onEliminar, enabled = habilitada) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.usuarios_eliminar_de, usuario.nombre),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

private val usuariosDePrueba = listOf(
    CuentaUsuario(1, "Alejandro Carrasco", "admin.kaiju@duoc.cl", Rol.ADMINISTRADOR),
    CuentaUsuario(2, "Esteban Hernandez", "encargado.kaiju@duoc.cl", Rol.ENCARGADO_INVENTARIO),
    CuentaUsuario(3, "Ignacia Diaz", "vendedor.kaiju@duoc.cl", Rol.VENDEDOR)
)

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun PantallaUsuariosPreview() {
    KaijuAppTheme {
        PantallaUsuarios(
            estado = ListaUsuariosEstado(usuarios = usuariosDePrueba),
            usuarioActivoId = 1, guardando = false,
            onRecargar = {}, onNuevo = {}, onEditar = {}, onEliminar = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 400)
@Composable
private fun PantallaUsuariosErrorPreview() {
    KaijuAppTheme {
        PantallaUsuarios(
            estado = ListaUsuariosEstado(error = "No se pudo conectar con el servidor."),
            usuarioActivoId = 1, guardando = false,
            onRecargar = {}, onNuevo = {}, onEditar = {}, onEliminar = {}
        )
    }
}
