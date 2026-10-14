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
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ShoppingCart
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
import cl.kaiju.kaijuapp.model.FormularioProductoErrores
import cl.kaiju.kaijuapp.model.FormularioProductoEstado
import cl.kaiju.kaijuapp.ui.components.CampoFormulario
import cl.kaiju.kaijuapp.ui.components.SelectorDesplegable
import cl.kaiju.kaijuapp.ui.theme.KaijuAppTheme

/**
 * RF-05. Solo dibuja el estado del formulario; validar (RN-02, RN-03) y guardar le toca al
 * InventarioViewModel. Al editar, el código y el stock actual quedan bloqueados: el stock
 * cambia solo con movimientos para no perder la trazabilidad (RN-05).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaFormularioProducto(
    estado: FormularioProductoEstado,
    esEdicion: Boolean,
    categorias: List<String>,
    onCodigoChange: (String) -> Unit,
    onNombreChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onCategoriaChange: (String) -> Unit,
    onTipoChange: (String) -> Unit,
    onPrecioChange: (String) -> Unit,
    onStockActualChange: (String) -> Unit,
    onStockMinimoChange: (String) -> Unit,
    onDetalleChange: (String) -> Unit,
    onGuardar: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val errores = estado.errores

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (esEdicion) R.string.formulario_producto_editar
                            else R.string.formulario_producto_nuevo
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
                valor = estado.codigo,
                onValorChange = onCodigoChange,
                etiqueta = stringResource(R.string.campo_codigo),
                icono = Icons.Filled.Info,
                error = errores.codigo,
                ayuda = if (esEdicion) stringResource(R.string.campo_codigo_bloqueado) else null,
                soloLectura = esEdicion
            )
            CampoFormulario(
                valor = estado.nombre,
                onValorChange = onNombreChange,
                etiqueta = stringResource(R.string.campo_nombre),
                icono = Icons.Filled.Create,
                error = errores.nombre
            )
            CampoFormulario(
                valor = estado.descripcion,
                onValorChange = onDescripcionChange,
                etiqueta = stringResource(R.string.campo_descripcion),
                icono = Icons.AutoMirrored.Filled.List,
                lineasMaximas = 3
            )
            SelectorDesplegable(
                opciones = categorias,
                seleccion = estado.categoria.ifBlank { null },
                textoDe = { it },
                onSeleccion = onCategoriaChange,
                etiqueta = stringResource(R.string.campo_categoria),
                icono = Icons.Filled.Menu,
                modifier = Modifier.fillMaxWidth(),
                error = errores.categoria
            )
            CampoFormulario(
                valor = estado.tipo,
                onValorChange = onTipoChange,
                etiqueta = stringResource(R.string.campo_tipo),
                icono = Icons.Filled.Build
            )
            CampoFormulario(
                valor = estado.precio,
                onValorChange = onPrecioChange,
                etiqueta = stringResource(R.string.campo_precio),
                icono = Icons.Filled.ShoppingCart,
                error = errores.precio,
                tipoTeclado = KeyboardType.Number
            )
            CampoFormulario(
                valor = estado.stockActual,
                onValorChange = onStockActualChange,
                etiqueta = stringResource(R.string.campo_stock_actual),
                icono = Icons.Filled.Home,
                ayuda = if (esEdicion) stringResource(R.string.campo_stock_actual_bloqueado) else null,
                tipoTeclado = KeyboardType.Number,
                soloLectura = esEdicion
            )
            CampoFormulario(
                valor = estado.stockMinimo,
                onValorChange = onStockMinimoChange,
                etiqueta = stringResource(R.string.campo_stock_minimo),
                icono = Icons.Filled.Notifications,
                error = errores.stockMinimo,
                tipoTeclado = KeyboardType.Number
            )
            CampoFormulario(
                valor = estado.detalle,
                onValorChange = onDetalleChange,
                etiqueta = stringResource(R.string.campo_detalle),
                icono = Icons.Filled.Edit
            )
            Button(
                onClick = onGuardar,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Text(
                    text = stringResource(
                        if (esEdicion) R.string.formulario_producto_guardar
                        else R.string.formulario_producto_crear
                    ),
                    modifier = Modifier.padding(start = ButtonDefaults.IconSpacing)
                )
            }
        }
    }
}

@Composable
private fun FormularioProductoPreview(estado: FormularioProductoEstado, esEdicion: Boolean) {
    KaijuAppTheme {
        PantallaFormularioProducto(
            estado = estado,
            esEdicion = esEdicion,
            categorias = listOf("Poleras", "Polerones"),
            onCodigoChange = {},
            onNombreChange = {},
            onDescripcionChange = {},
            onCategoriaChange = {},
            onTipoChange = {},
            onPrecioChange = {},
            onStockActualChange = {},
            onStockMinimoChange = {},
            onDetalleChange = {},
            onGuardar = {},
            onVolver = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 900)
@Composable
private fun PantallaFormularioProductoErroresPreview() {
    FormularioProductoPreview(
        estado = FormularioProductoEstado(
            codigo = "P001",
            nombre = "Po",
            precio = "0",
            stockMinimo = "-1",
            errores = FormularioProductoErrores(
                codigo = "Ya existe un producto con ese código",
                nombre = "El nombre debe tener al menos 3 caracteres",
                categoria = "Elige una categoría",
                precio = "El precio debe ser mayor que cero",
                stockMinimo = "El stock mínimo no puede ser negativo"
            )
        ),
        esEdicion = false
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 900)
@Composable
private fun PantallaFormularioProductoEdicionPreview() {
    FormularioProductoPreview(
        estado = FormularioProductoEstado(
            codigo = "P008",
            nombre = "Poleron Kaiju",
            descripcion = "Poleron de algodón con estampado",
            categoria = "Polerones",
            tipo = "Vestuario",
            precio = "17990",
            stockActual = "2",
            stockMinimo = "5",
            detalle = "Talla XL, negra"
        ),
        esEdicion = true
    )
}
