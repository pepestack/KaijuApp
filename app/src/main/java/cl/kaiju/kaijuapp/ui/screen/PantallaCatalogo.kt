package cl.kaiju.kaijuapp.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.kaiju.kaijuapp.R
import cl.kaiju.kaijuapp.model.Producto
import cl.kaiju.kaijuapp.repository.DatosDePrueba
import cl.kaiju.kaijuapp.ui.components.EstadoVacio
import cl.kaiju.kaijuapp.ui.components.TarjetaProducto
import cl.kaiju.kaijuapp.ui.theme.KaijuAppTheme

/**
 * RF-02, RF-03, RF-09. Recibe la lista ya filtrada por el ViewModel y avisa los cambios de búsqueda
 * y filtro. Si el NavHost la envuelve en otro Scaffold con el menú inferior, debe pasarle
 * Modifier.padding(inner).consumeWindowInsets(inner) para no duplicar los márgenes del sistema.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCatalogo(
    productos: List<Producto>,
    busqueda: String,
    onBusquedaChange: (String) -> Unit,
    categorias: List<String>,
    categoriaSeleccionada: String?,
    onCategoriaChange: (String?) -> Unit,
    puedeCrear: Boolean,
    onProductoClick: (String) -> Unit,
    onNuevoProducto: () -> Unit,
    onCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.catalogo_titulo)) }) },
        floatingActionButton = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FloatingActionButton(
                    onClick = onCerrarSesion,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_logout),
                        contentDescription = stringResource(R.string.cerrar_sesion),
                        modifier = Modifier.size(24.dp)
                    )
                }
                if (puedeCrear) {
                    FloatingActionButton(onClick = onNuevoProducto) {
                        Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.catalogo_nuevo_producto))
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            // Deja espacio para los dos botones del administrador o el único de otros perfiles.
            contentPadding = PaddingValues(
                start = 16.dp, top = 8.dp, end = 16.dp,
                bottom = if (puedeCrear) 156.dp else 88.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = busqueda,
                    onValueChange = onBusquedaChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.catalogo_buscar)) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = if (busqueda.isNotEmpty()) {
                        {
                            IconButton(onClick = { onBusquedaChange("") }) {
                                Icon(
                                    Icons.Filled.Clear,
                                    contentDescription = stringResource(R.string.catalogo_limpiar_busqueda)
                                )
                            }
                        }
                    } else null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
                )
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = categoriaSeleccionada == null,
                            onClick = { onCategoriaChange(null) },
                            label = { Text(stringResource(R.string.catalogo_todas)) }
                        )
                    }
                    items(categorias) { categoria ->
                        FilterChip(
                            selected = categoria == categoriaSeleccionada,
                            onClick = { onCategoriaChange(categoria) },
                            label = { Text(categoria) }
                        )
                    }
                }
            }
            item {
                Text(
                    text = pluralStringResource(R.plurals.catalogo_resultados, productos.size, productos.size),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (productos.isEmpty()) {
                item {
                    EstadoVacio(icono = Icons.Filled.Search, mensaje = stringResource(R.string.catalogo_vacio))
                }
            } else {
                items(productos, key = { it.codigo }) { producto ->
                    TarjetaProducto(producto = producto, onClick = { onProductoClick(producto.codigo) })
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun PantallaCatalogoPreview() {
    KaijuAppTheme {
        PantallaCatalogo(
            productos = DatosDePrueba.productos,
            busqueda = "",
            onBusquedaChange = {},
            categorias = DatosDePrueba.productos.map { it.categoria }.distinct(),
            categoriaSeleccionada = null,
            onCategoriaChange = {},
            puedeCrear = true,
            onProductoClick = {},
            onNuevoProducto = {},
            onCerrarSesion = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 480)
@Composable
private fun PantallaCatalogoVaciaPreview() {
    KaijuAppTheme {
        PantallaCatalogo(
            productos = emptyList(),
            busqueda = "godzilla",
            onBusquedaChange = {},
            categorias = listOf("Poleras", "Polerones"),
            categoriaSeleccionada = "Poleras",
            onCategoriaChange = {},
            puedeCrear = false,
            onProductoClick = {},
            onNuevoProducto = {},
            onCerrarSesion = {}
        )
    }
}
