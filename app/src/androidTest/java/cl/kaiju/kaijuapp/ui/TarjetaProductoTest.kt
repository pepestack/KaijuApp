package cl.kaiju.kaijuapp.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cl.kaiju.kaijuapp.R
import cl.kaiju.kaijuapp.model.Producto
import cl.kaiju.kaijuapp.ui.components.TarjetaProducto
import cl.kaiju.kaijuapp.ui.theme.KaijuAppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TarjetaProductoTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val contexto = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun tarjetaConStockBajoMuestraLaAlertaYRespondeAlToque() {
        val producto = Producto(
            codigo = "POL-001", nombre = "Polera básica", descripcion = "Algodón",
            categoria = "Poleras", tipo = "Prenda", precio = 9990,
            stockActual = 2, stockMinimo = 5
        )
        var toques = 0

        composeRule.setContent {
            KaijuAppTheme(dynamicColor = false) {
                TarjetaProducto(producto = producto, onClick = { toques++ })
            }
        }

        composeRule.onNodeWithText("Polera básica").assertIsDisplayed()
        composeRule.onNodeWithText(contexto.getString(R.string.producto_stock, 2)).assertIsDisplayed()
        composeRule.onNodeWithText(contexto.getString(R.string.stock_bajo)).assertIsDisplayed()

        composeRule.onNodeWithText("Polera básica").performClick()
        assertEquals(1, toques)
    }
}
