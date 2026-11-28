package cl.kaiju.kaijuapp.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
    fun tarjetaMuestraAlertaEnElMinimoLaQuitaSobreElYRespondeAlToque() {
        // RN-06: con el stock justo en el mínimo el producto ya está en alerta.
        var producto by mutableStateOf(
            Producto(
                codigo = "POL-001", nombre = "Polera básica", descripcion = "Algodón",
                categoria = "Poleras", tipo = "Prenda", precio = 9990,
                stockActual = 5, stockMinimo = 5
            )
        )
        var toques = 0

        composeRule.setContent {
            KaijuAppTheme(dynamicColor = false) {
                TarjetaProducto(producto = producto, onClick = { toques++ })
            }
        }

        composeRule.onNodeWithText("Polera básica").assertIsDisplayed()
        composeRule.onNodeWithText(contexto.getString(R.string.producto_categoria, "Poleras")).assertIsDisplayed()
        composeRule.onNodeWithText(contexto.getString(R.string.producto_stock, 5)).assertIsDisplayed()
        composeRule.onNodeWithText(contexto.getString(R.string.stock_bajo)).assertIsDisplayed()
        composeRule.onNodeWithText(contexto.getString(R.string.stock_suficiente)).assertDoesNotExist()

        composeRule.onNodeWithText("Polera básica").performClick()
        composeRule.runOnIdle { assertEquals(1, toques) }

        // Una unidad sobre el mínimo: la tarjeta deja de marcar la alerta.
        composeRule.runOnIdle { producto = producto.copy(stockActual = 6) }

        composeRule.onNodeWithText(contexto.getString(R.string.producto_stock, 6)).assertIsDisplayed()
        composeRule.onNodeWithText(contexto.getString(R.string.stock_suficiente)).assertIsDisplayed()
        composeRule.onNodeWithText(contexto.getString(R.string.stock_bajo)).assertDoesNotExist()
    }
}
