package cl.kaiju.kaijuapp.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.kaiju.kaijuapp.R
import cl.kaiju.kaijuapp.model.Movimiento
import cl.kaiju.kaijuapp.model.Producto
import cl.kaiju.kaijuapp.model.TipoMovimiento
import cl.kaiju.kaijuapp.database.DatosDePrueba
import cl.kaiju.kaijuapp.ui.components.EstadoVacio
import cl.kaiju.kaijuapp.ui.components.IndicadorStock
import cl.kaiju.kaijuapp.ui.components.etiqueta
import cl.kaiju.kaijuapp.ui.components.rememberFormatoFecha
import cl.kaiju.kaijuapp.ui.components.rememberFormatoPrecio
import cl.kaiju.kaijuapp.ui.theme.KaijuAppTheme
import cl.kaiju.kaijuapp.ui.theme.StockCountStyle
import java.text.DateFormat
import java.util.Date

/** RF-04. Muestra el historial en el orden recibido; el repositorio ya lo entrega del más reciente al más antiguo. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalleProducto(
    producto: Producto,
    movimientos: List<Movimiento>,
    nombresUsuarios: Map<Int, String>,
    puedeEditar: Boolean,
    onEditar: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formatoFecha = rememberFormatoFecha()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(producto.nombre, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.volver))
                    }
                },
                actions = {
                    if (puedeEditar) {
                        IconButton(onClick = onEditar) {
                            Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.detalle_editar))
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { DatosProducto(producto) }
            item {
                Text(
                    text = stringResource(R.string.detalle_historial),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            if (movimientos.isEmpty()) {
                item {
                    EstadoVacio(
                        icono = Icons.AutoMirrored.Filled.List,
                        mensaje = stringResource(R.string.detalle_historial_vacio)
                    )
                }
            } else {
                items(movimientos, key = { it.id }) { movimiento ->
                    FilaMovimiento(
                        movimiento = movimiento,
                        nombreUsuario = nombresUsuarios[movimiento.usuario]
                            ?: stringResource(R.string.detalle_usuario_desconocido, movimiento.usuario),
                        formatoFecha = formatoFecha
                    )
                }
            }
        }
    }
}

@Composable
private fun DatosProducto(producto: Producto) {
    val formatoPrecio = rememberFormatoPrecio()
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.producto_codigo, producto.codigo),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            producto.codigoBarras?.let {
                Text(
                    text = stringResource(R.string.producto_codigo_barras, it),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = stringResource(R.string.producto_categoria, producto.categoria),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = stringResource(R.string.detalle_tipo, producto.tipo),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(text = producto.descripcion, style = MaterialTheme.typography.bodyMedium)
            producto.detalle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = formatoPrecio.format(producto.precio),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.detalle_disponible),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${producto.stockActual} ${stringResource(R.string.detalle_unidades)}",
                        style = StockCountStyle
                    )
                    Text(
                        text = stringResource(R.string.detalle_stock_minimo, producto.stockMinimo),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IndicadorStock(enAlerta = producto.enAlerta)
            }
        }
    }
}

@Composable
private fun FilaMovimiento(movimiento: Movimiento, nombreUsuario: String, formatoFecha: DateFormat) {
    // Solo presentación: el signo indica si el movimiento sumó o restó unidades
    val (signo, color) = when (movimiento.tipo) {
        TipoMovimiento.ENTRADA -> "+" to MaterialTheme.colorScheme.secondary
        TipoMovimiento.SALIDA_VENTA, TipoMovimiento.SALIDA_CONSUMO -> "−" to MaterialTheme.colorScheme.error
        TipoMovimiento.AJUSTE -> "" to MaterialTheme.colorScheme.tertiary
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        ListItem(
            headlineContent = { Text(movimiento.tipo.etiqueta(), style = MaterialTheme.typography.titleMedium) },
            supportingContent = {
                Column {
                    Text(
                        text = "${formatoFecha.format(Date(movimiento.fecha))} · $nombreUsuario",
                        style = MaterialTheme.typography.labelSmall
                    )
                    movimiento.motivo?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            text = stringResource(R.string.detalle_motivo, it),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            },
            trailingContent = {
                Text(text = "$signo${movimiento.cantidad}", style = StockCountStyle, color = color)
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
}

private val movimientosDePrueba = listOf(
    Movimiento(4, "P008", TipoMovimiento.AJUSTE, 1, "Prenda con falla de estampado", 1_760_400_000_000, 2),
    Movimiento(3, "P008", TipoMovimiento.SALIDA_VENTA, 10, null, 1_760_300_000_000, 3),
    Movimiento(2, "P008", TipoMovimiento.SALIDA_CONSUMO, 2, "Muestra para feria", 1_760_200_000_000, 2),
    Movimiento(1, "P008", TipoMovimiento.ENTRADA, 15, null, 1_760_100_000_000, 1)
)

@Preview(showBackground = true, widthDp = 360, heightDp = 900)
@Composable
private fun PantallaDetalleProductoPreview() {
    KaijuAppTheme {
        PantallaDetalleProducto(
            producto = DatosDePrueba.productos.first { it.codigo == "P008" },
            movimientos = movimientosDePrueba,
            nombresUsuarios = DatosDePrueba.usuarios.associate { it.id to it.nombre },
            puedeEditar = true,
            onEditar = {},
            onVolver = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun PantallaDetalleProductoSinHistorialPreview() {
    KaijuAppTheme {
        PantallaDetalleProducto(
            producto = DatosDePrueba.productos.first(),
            movimientos = emptyList(),
            nombresUsuarios = emptyMap(),
            puedeEditar = false,
            onEditar = {},
            onVolver = {}
        )
    }
}
