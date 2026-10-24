package cl.kaiju.kaijuapp.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.kaiju.kaijuapp.R
import cl.kaiju.kaijuapp.model.Producto
import cl.kaiju.kaijuapp.database.DatosDePrueba
import cl.kaiju.kaijuapp.ui.components.EstadoVacio
import cl.kaiju.kaijuapp.ui.components.TarjetaProducto
import cl.kaiju.kaijuapp.ui.theme.KaijuAppTheme

/**
 * RF-09. Recibe solo los productos en alerta (RN-06 se calcula en Producto.enAlerta / ViewModel).
 * [encabezado] va sobre la lista; ahí el NavHost pone el aviso de notificaciones, así esta
 * pantalla no toca permisos y los previews no dependen de una Activity. Recibe el Modifier con
 * el margen para que, si no dibuja nada, no quede un hueco arriba de la lista.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAlertas(
    productos: List<Producto>,
    onProductoClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    encabezado: @Composable (Modifier) -> Unit = {}
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.alertas_titulo)) }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            encabezado(Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp))
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (productos.isEmpty()) {
                    item {
                        EstadoVacio(icono = Icons.Filled.CheckCircle, mensaje = stringResource(R.string.alertas_vacio))
                    }
                } else {
                    item {
                        Text(
                            text = pluralStringResource(R.plurals.alertas_cantidad, productos.size, productos.size),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    items(productos, key = { it.codigo }) { producto ->
                        TarjetaProducto(producto = producto, onClick = { onProductoClick(producto.codigo) })
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun PantallaAlertasPreview() {
    KaijuAppTheme {
        PantallaAlertas(productos = DatosDePrueba.productos.filter { it.enAlerta }, onProductoClick = {})
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 400)
@Composable
private fun PantallaAlertasVaciaPreview() {
    KaijuAppTheme {
        PantallaAlertas(productos = emptyList(), onProductoClick = {})
    }
}
