package cl.kaiju.kaijuapp.viewmodel

import cl.kaiju.kaijuapp.model.Producto
import cl.kaiju.kaijuapp.repository.InventarioRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InventarioViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val polera = Producto("P001", "Polera Kaiju", "", "Poleras", "Vestuario", 10000, 5, 5)
    private val poleron = polera.copy(codigo = "P002", nombre = "Polerón", categoria = "Polerones", stockActual = 10)
    private val agotado = polera.copy(codigo = "P003", nombre = "Polera Goku", stockActual = 0)

    @Before fun preparar() { Dispatchers.setMain(dispatcher) }
    @After fun limpiar() { Dispatchers.resetMain() }

    @Test fun busquedaPorNombreOCodigoSeCombinaConCategoria() = runTest(dispatcher) {
        val vm = InventarioViewModel(InventarioRepository(listOf(polera, poleron, agotado)))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.estadoCatalogo.collect {} }
        vm.actualizarBusqueda("  kaIJU  ")
        runCurrent()
        assertEquals(listOf(polera), vm.estadoCatalogo.value.productos)
        vm.actualizarBusqueda("p002")
        vm.seleccionarCategoria("Poleras")
        runCurrent()
        assertTrue(vm.estadoCatalogo.value.productos.isEmpty())
        vm.seleccionarCategoria(null)
        runCurrent()
        assertEquals(listOf(poleron), vm.estadoCatalogo.value.productos)
        vm.limpiarFiltros()
        runCurrent()
        assertEquals(3, vm.estadoCatalogo.value.productos.size)
        assertEquals("", vm.estadoCatalogo.value.busqueda)
    }

    @Test fun alertasIncluyenElUmbralYNoDependenDeLaBusqueda() = runTest(dispatcher) {
        val vm = InventarioViewModel(InventarioRepository(listOf(polera, poleron, agotado)))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.estadoCatalogo.collect {} }
        vm.actualizarBusqueda("inexistente")
        runCurrent()
        assertTrue(vm.estadoCatalogo.value.productos.isEmpty())
        assertEquals(listOf(polera, agotado), vm.estadoCatalogo.value.productosEnAlerta)
    }

    @Test fun cambiosDelRepositorioActualizanProductosCategoriasYAlertas() = runTest(dispatcher) {
        val repo = InventarioRepository(listOf(polera, poleron))
        val vm = InventarioViewModel(repo)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.estadoCatalogo.collect {} }
        runCurrent()
        assertEquals(listOf("Poleras", "Polerones"), vm.estadoCatalogo.value.categorias)
        repo.actualizarProducto(polera.copy(stockActual = 6))
        repo.agregarProducto(polera.copy(codigo = "P004", categoria = "Accesorios", stockActual = 9))
        runCurrent()
        assertEquals(3, vm.estadoCatalogo.value.productos.size)
        assertTrue(vm.estadoCatalogo.value.productosEnAlerta.isEmpty())
        assertEquals(listOf("Accesorios", "Poleras", "Polerones"), vm.estadoCatalogo.value.categorias)
    }

    @Test fun inventarioVacioExponeListasVacias() {
        val vm = InventarioViewModel(InventarioRepository(emptyList()))
        assertTrue(vm.estadoCatalogo.value.productos.isEmpty())
        assertTrue(vm.estadoCatalogo.value.categorias.isEmpty())
        assertTrue(vm.estadoCatalogo.value.productosEnAlerta.isEmpty())
    }
}
