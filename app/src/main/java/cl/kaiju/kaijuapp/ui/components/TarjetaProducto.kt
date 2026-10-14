package cl.kaiju.kaijuapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.kaiju.kaijuapp.R
import cl.kaiju.kaijuapp.model.Producto
import cl.kaiju.kaijuapp.ui.theme.KaijuAppTheme

/** Recibe datos y avisa del clic; la pantalla decide cómo abrir el detalle. */
@Composable
fun TarjetaProducto(
    producto: Producto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formatoPrecio = rememberFormatoPrecio()

    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = producto.nombre, style = MaterialTheme.typography.titleMedium)
            Text(
                text = stringResource(R.string.producto_codigo, producto.codigo),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.producto_categoria, producto.categoria),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = formatoPrecio.format(producto.precio),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.producto_stock, producto.stockActual),
                style = MaterialTheme.typography.bodyMedium
            )
            IndicadorStock(enAlerta = producto.enAlerta)
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Preview(showBackground = true, widthDp = 360, fontScale = 1.5f)
@Composable
private fun TarjetaProductoPreview() {
    KaijuAppTheme(dynamicColor = false) {
        TarjetaProducto(
            producto = Producto(
                codigo = "PRD-001",
                nombre = "Polera Kaiju",
                descripcion = "Producto ficticio para la vista previa",
                categoria = "Ropa",
                tipo = "Polera",
                precio = 12990,
                stockActual = 3,
                stockMinimo = 5
            ),
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
