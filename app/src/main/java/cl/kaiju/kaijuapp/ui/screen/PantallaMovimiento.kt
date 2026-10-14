package cl.kaiju.kaijuapp.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import cl.kaiju.kaijuapp.model.FormularioMovimientoErrores
import cl.kaiju.kaijuapp.model.FormularioMovimientoEstado
import cl.kaiju.kaijuapp.model.Producto
import cl.kaiju.kaijuapp.model.TipoMovimiento
import cl.kaiju.kaijuapp.repository.DatosDePrueba
import cl.kaiju.kaijuapp.ui.components.CampoFormulario
import cl.kaiju.kaijuapp.ui.components.SelectorDesplegable
import cl.kaiju.kaijuapp.ui.components.etiqueta
import cl.kaiju.kaijuapp.ui.theme.KaijuAppTheme

/**
 * RF-06, RF-07, RF-08. Los tipos que puede usar el perfil llegan en tiposPermitidos; validar
 * la cantidad (RN-01) y el motivo (RN-04) y actualizar el stock le toca al InventarioViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PantallaMovimiento(
    estado: FormularioMovimientoEstado,
    productos: List<Producto>,
    tiposPermitidos: List<TipoMovimiento>,
    onProductoChange: (String) -> Unit,
    onTipoChange: (TipoMovimiento) -> Unit,
    onCantidadChange: (String) -> Unit,
    onMotivoChange: (String) -> Unit,
    onRegistrar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val errores = estado.errores
    val productoSeleccionado = productos.find { it.codigo == estado.codigoProducto }

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.movimiento_titulo)) }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SelectorDesplegable(
                opciones = productos,
                seleccion = productoSeleccionado,
                // Varios productos comparten nombre, así que se muestra también el código y el detalle
                textoDe = { listOfNotNull(it.codigo, it.nombre, it.detalle).joinToString(" · ") },
                onSeleccion = { onProductoChange(it.codigo) },
                etiqueta = stringResource(R.string.campo_producto),
                icono = Icons.Filled.ShoppingCart,
                modifier = Modifier.fillMaxWidth(),
                error = errores.producto,
                ayuda = productoSeleccionado?.let { stringResource(R.string.producto_stock, it.stockActual) }
            )

            Text(
                text = stringResource(R.string.campo_tipo_movimiento),
                style = MaterialTheme.typography.labelMedium,
                color = if (errores.tipo != null) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tiposPermitidos.forEach { tipo ->
                    val seleccionado = tipo == estado.tipo
                    FilterChip(
                        selected = seleccionado,
                        onClick = { onTipoChange(tipo) },
                        label = { Text(tipo.etiqueta()) },
                        leadingIcon = if (seleccionado) {
                            {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        } else null
                    )
                }
            }
            if (errores.tipo != null) {
                Text(
                    text = errores.tipo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            CampoFormulario(
                valor = estado.cantidad,
                onValorChange = onCantidadChange,
                etiqueta = stringResource(R.string.campo_cantidad),
                icono = Icons.Filled.Add,
                error = errores.cantidad,
                tipoTeclado = KeyboardType.Number
            )
            CampoFormulario(
                valor = estado.motivo,
                onValorChange = onMotivoChange,
                // Solo cambia el texto de la etiqueta; la regla RN-04 la revisa el ViewModel
                etiqueta = stringResource(
                    if (estado.tipo == TipoMovimiento.AJUSTE) R.string.campo_motivo_obligatorio
                    else R.string.campo_motivo_opcional
                ),
                icono = Icons.Filled.Edit,
                error = errores.motivo,
                lineasMaximas = 3
            )
            Button(
                onClick = onRegistrar,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Text(
                    text = stringResource(R.string.movimiento_registrar),
                    modifier = Modifier.padding(start = ButtonDefaults.IconSpacing)
                )
            }
        }
    }
}

@Composable
private fun MovimientoPreview(estado: FormularioMovimientoEstado, tiposPermitidos: List<TipoMovimiento>) {
    KaijuAppTheme {
        PantallaMovimiento(
            estado = estado,
            productos = DatosDePrueba.productos,
            tiposPermitidos = tiposPermitidos,
            onProductoChange = {},
            onTipoChange = {},
            onCantidadChange = {},
            onMotivoChange = {},
            onRegistrar = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun PantallaMovimientoPreview() {
    MovimientoPreview(
        estado = FormularioMovimientoEstado(codigoProducto = "P001", tipo = TipoMovimiento.ENTRADA, cantidad = "10"),
        tiposPermitidos = TipoMovimiento.entries
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun PantallaMovimientoErroresPreview() {
    MovimientoPreview(
        estado = FormularioMovimientoEstado(
            codigoProducto = "P008",
            tipo = TipoMovimiento.AJUSTE,
            cantidad = "5",
            errores = FormularioMovimientoErrores(
                cantidad = "Solo hay 2 unidades disponibles",
                motivo = "El ajuste necesita un motivo"
            )
        ),
        tiposPermitidos = TipoMovimiento.entries
    )
}
