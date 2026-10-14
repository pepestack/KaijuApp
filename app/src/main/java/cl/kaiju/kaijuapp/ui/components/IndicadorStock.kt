package cl.kaiju.kaijuapp.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.kaiju.kaijuapp.R
import cl.kaiju.kaijuapp.ui.theme.KaijuAppTheme

/** Muestra el estado recibido; la regla de alerta permanece en Producto.enAlerta. */
@Composable
fun IndicadorStock(enAlerta: Boolean, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = if (enAlerta) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.secondaryContainer,
        contentColor = if (enAlerta) MaterialTheme.colorScheme.onErrorContainer
            else MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Text(
            text = stringResource(
                if (enAlerta) R.string.stock_bajo else R.string.stock_suficiente
            ),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun IndicadorStockBajoPreview() {
    KaijuAppTheme(dynamicColor = false) { IndicadorStock(enAlerta = true) }
}

@Preview(showBackground = true)
@Composable
private fun IndicadorStockSuficientePreview() {
    KaijuAppTheme(dynamicColor = false) { IndicadorStock(enAlerta = false) }
}
