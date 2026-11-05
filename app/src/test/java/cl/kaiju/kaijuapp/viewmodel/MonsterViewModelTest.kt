package cl.kaiju.kaijuapp.viewmodel

import cl.kaiju.kaijuapp.data.model.MonsterDto
import cl.kaiju.kaijuapp.data.remote.MonsterApi
import cl.kaiju.kaijuapp.repository.MonsterRepository
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class MonsterViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val poring = MonsterDto(1002, "Poring", 1, "Plant", "Water 1", 55, "https://example.org/1002.png")
    @Before fun preparar() { Dispatchers.setMain(dispatcher) }
    @After fun limpiar() { Dispatchers.resetMain() }

    private class Api(private val respuesta: suspend () -> MonsterDto) : MonsterApi {
        var consultas = 0
        override suspend fun obtenerMonstruoAleatorio(): MonsterDto {
            consultas++
            return respuesta()
        }
    }

    @Test fun fichaPermaneceYUnaNuevaActivacionLaReemplazaSinConsultasSimultaneas() = runTest(dispatcher) {
        var respuesta = CompletableDeferred<MonsterDto>()
        val api = Api { respuesta.await() }
        val vm = MonsterViewModel(MonsterRepository(api))
        vm.activar()
        vm.activar()
        runCurrent()
        assertTrue(vm.estado.value.cargando)
        assertEquals(1, api.consultas)
        respuesta.complete(poring)
        runCurrent()
        advanceTimeBy(60_000)
        assertEquals(poring, vm.estado.value.monstruo)
        assertEquals(1, api.consultas)

        respuesta = CompletableDeferred()
        vm.activar()
        runCurrent()
        assertTrue(vm.estado.value.cargando)
        assertEquals(poring, vm.estado.value.monstruo)
        val spore = poring.copy(id = 1014, nombre = "Spore", spriteUrl = "https://example.org/1014.png")
        respuesta.complete(spore)
        runCurrent()
        assertEquals(spore, vm.estado.value.monstruo)
        assertFalse(vm.estado.value.cargando)
        assertEquals(2, api.consultas)
    }

    @Test fun falloDeRedPermiteReintentarYFalloDeImagenReutilizaFicha() = runTest(dispatcher) {
        var sinConexion = true
        val api = Api { if (sinConexion) throw IOException() else poring }
        val vm = MonsterViewModel(MonsterRepository(api))
        vm.activar()
        runCurrent()
        assertNotNull(vm.estado.value.error)
        assertFalse(vm.estado.value.cargando)
        sinConexion = false
        vm.reintentar()
        runCurrent()
        assertEquals(poring, vm.estado.value.monstruo)
        vm.imagenFallida(poring.spriteUrl, 0)
        assertTrue(vm.estado.value.errorImagen)
        assertEquals(poring, vm.estado.value.monstruo)
        vm.reintentarImagen()
        assertNull(vm.estado.value.error)
        assertFalse(vm.estado.value.errorImagen)
        assertEquals(1, vm.estado.value.intentoImagen)
        assertEquals(2, api.consultas)
        vm.cerrar()
        assertNull(vm.estado.value.monstruo)
    }

    @Test fun cerrarCancelaConsultaPendienteYRespuestaInvalidaMuestraError() = runTest(dispatcher) {
        val respuesta = CompletableDeferred<MonsterDto>()
        val vm = MonsterViewModel(MonsterRepository(Api { respuesta.await() }))
        vm.activar()
        runCurrent()
        vm.cerrar()
        respuesta.complete(poring)
        runCurrent()
        assertNull(vm.estado.value.monstruo)
        assertFalse(vm.estado.value.cargando)
        val invalido = MonsterViewModel(MonsterRepository(Api { poring.copy(spriteUrl = "") }))
        invalido.activar()
        runCurrent()
        assertNotNull(invalido.estado.value.error)
        assertNull(invalido.estado.value.monstruo)
    }

    @Test fun errorAlActualizarConservaFichaYReintentarConsultaOtraVez() = runTest(dispatcher) {
        var falla = false
        val api = Api { if (falla) throw IOException() else poring }
        val vm = MonsterViewModel(MonsterRepository(api))
        vm.activar()
        runCurrent()
        falla = true
        vm.activar()
        runCurrent()
        assertEquals(poring, vm.estado.value.monstruo)
        assertNotNull(vm.estado.value.error)
        assertFalse(vm.estado.value.cargando)
        falla = false
        vm.reintentar()
        runCurrent()
        assertNull(vm.estado.value.error)
        assertEquals(3, api.consultas)
    }

    @Test fun salirLimpiaFichaYErroresSinRecuperarlaAlVolver() = runTest(dispatcher) {
        val api = Api { poring }
        val vm = MonsterViewModel(MonsterRepository(api))
        vm.activar()
        runCurrent()
        vm.imagenFallida(poring.spriteUrl, 0)
        vm.cerrar()
        advanceTimeBy(60_000)
        runCurrent()
        assertEquals(cl.kaiju.kaijuapp.model.MonsterUiState(), vm.estado.value)
        assertEquals(1, api.consultas)
        vm.activar()
        runCurrent()
        assertEquals(poring, vm.estado.value.monstruo)
        assertEquals(2, api.consultas)
    }

    @Test fun callbackDeImagenAnteriorNoMarcaLaNuevaFichaComoFallida() = runTest(dispatcher) {
        var monstruo = poring
        val vm = MonsterViewModel(MonsterRepository(Api { monstruo }))
        vm.activar()
        runCurrent()
        monstruo = poring.copy(id = 1014, spriteUrl = "https://example.org/1014.png")
        vm.activar()
        runCurrent()
        vm.imagenFallida(poring.spriteUrl, 0)
        assertFalse(vm.estado.value.errorImagen)
        vm.imagenFallida(monstruo.spriteUrl, 0)
        vm.reintentarImagen()
        vm.imagenFallida(monstruo.spriteUrl, 0)
        assertFalse(vm.estado.value.errorImagen)
    }
}
