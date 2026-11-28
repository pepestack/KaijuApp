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
import cl.kaiju.kaijuapp.model.MonsterUiState
import cl.kaiju.kaijuapp.ui.components.TarjetaMonstruo
import cl.kaiju.kaijuapp.ui.theme.KaijuAppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TarjetaMonstruoTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun muestraErrorDeConexionYAlReintentarPasaACargando() {
        val contexto = InstrumentationRegistry.getInstrumentation().targetContext
        val mensaje = "No se pudo conectar. Revisa internet e intenta nuevamente."
        val reintentar = contexto.getString(R.string.easter_egg_reintentar)
        var estado by mutableStateOf(MonsterUiState(error = mensaje))
        var reintentos = 0

        composeRule.setContent {
            KaijuAppTheme(dynamicColor = false) {
                TarjetaMonstruo(
                    estado = estado,
                    onImagenFallida = { _, _ -> },
                    // Hace lo mismo que MonsterViewModel al reintentar: carga y borra el error.
                    onReintentar = {
                        reintentos++
                        estado = MonsterUiState(cargando = true)
                    },
                    onReintentarImagen = {}
                )
            }
        }

        composeRule.onNodeWithText(mensaje).assertIsDisplayed()
        composeRule.onNodeWithText(reintentar)
            .assertIsDisplayed()
            .performClick()

        composeRule.runOnIdle {
            assertEquals(1, reintentos)
        }
        // Mientras carga se ve el aviso y desaparecen el error y el botón.
        composeRule.onNodeWithText(contexto.getString(R.string.easter_egg_cargando)).assertIsDisplayed()
        composeRule.onNodeWithText(mensaje).assertDoesNotExist()
        composeRule.onNodeWithText(reintentar).assertDoesNotExist()
    }
}
