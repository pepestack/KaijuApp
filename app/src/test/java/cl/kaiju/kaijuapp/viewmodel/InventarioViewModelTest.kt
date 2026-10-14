package cl.kaiju.kaijuapp.viewmodel

import cl.kaiju.kaijuapp.model.Producto
import cl.kaiju.kaijuapp.model.Rol
import cl.kaiju.kaijuapp.model.Usuario
import cl.kaiju.kaijuapp.model.TipoMovimiento
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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

    private val administrador = Usuario(100, "Administrador ficticio", Rol.ADMINISTRADOR)
    private val vendedor = Usuario(101, "Vendedor ficticio", Rol.VENDEDOR)
    private fun repositorio() = InventarioRepository(
        listOf(polera, poleron), usuarios = listOf(administrador, vendedor)
    )
    private fun completarProducto(vm: InventarioViewModel, codigo: String = "P010") {
        vm.prepararNuevoProducto()
        vm.actualizarCodigo(codigo)
        vm.actualizarNombre("Nueva polera")
        vm.actualizarCategoria("Poleras")
        vm.actualizarPrecio("12000")
        vm.actualizarStockMinimo("2")
        vm.actualizarStockActual("4")
    }

    @Test fun productoInvalidoNoSeGuardaYExponeErroresPorCampo() {
        val repo = repositorio()
        val vm = InventarioViewModel(repo)
        completarProducto(vm, " P001 ")
        vm.actualizarNombre("a")
        vm.actualizarCategoria("Inexistente")
        vm.actualizarPrecio("0")
        vm.actualizarStockMinimo("-1")
        vm.actualizarStockActual("1.5")
        assertFalse(vm.guardarProducto(administrador))
        val errores = vm.estadoFormularioProducto.value.errores
        assertNotNull(errores.codigo)
        assertNotNull(errores.nombre)
        assertNotNull(errores.categoria)
        assertNotNull(errores.precio)
        assertNotNull(errores.stockMinimo)
        assertNotNull(errores.stockActual)
        assertEquals(2, repo.productos.value.size)
        assertTrue(repo.movimientos.value.isEmpty())
    }

    @Test fun crearProductoRegistraStockInicialYRechazaSegundoCodigoIgual() {
        val repo = repositorio()
        val vm = InventarioViewModel(repo)
        completarProducto(vm)
        assertFalse(vm.guardarProducto(vendedor))
        assertTrue(vm.guardarProducto(administrador))
        assertEquals(4, repo.buscarProducto("P010")!!.stockActual)
        assertEquals(TipoMovimiento.ENTRADA, repo.movimientos.value.single().tipo)
        assertEquals(administrador.id, repo.movimientos.value.single().usuario)
        completarProducto(vm)
        assertFalse(vm.guardarProducto(administrador))
        assertEquals(3, repo.productos.value.size)
    }

    @Test fun editarNoCambiaCodigoNiSobrescribeStockActualizado() {
        val repo = repositorio()
        val vm = InventarioViewModel(repo)
        assertTrue(vm.prepararEdicionProducto("P001"))
        vm.actualizarCodigo("OTRO")
        vm.actualizarStockActual("99")
        vm.actualizarNombre("Nombre editado")
        repo.actualizarProducto(polera.copy(stockActual = 3))
        assertTrue(vm.guardarProducto(administrador))
        assertEquals("Nombre editado", repo.buscarProducto("P001")!!.nombre)
        assertEquals(3, repo.buscarProducto("P001")!!.stockActual)
        assertEquals(2, repo.productos.value.size)
    }

    @Test fun salidaExcesivaNoAlteraDatosYSalidaExactaAgotaStock() {
        val repo = repositorio()
        val vm = InventarioViewModel(repo)
        vm.actualizarProductoMovimiento("P001")
        vm.actualizarTipoMovimiento(TipoMovimiento.SALIDA_VENTA)
        vm.actualizarCantidad("6")
        assertFalse(vm.registrarMovimiento(vendedor))
        assertNotNull(vm.estadoFormularioMovimiento.value.errores.cantidad)
        assertEquals(5, repo.buscarProducto("P001")!!.stockActual)
        assertTrue(repo.movimientos.value.isEmpty())
        vm.actualizarCantidad("5")
        assertTrue(vm.registrarMovimiento(vendedor))
        assertEquals(0, repo.buscarProducto("P001")!!.stockActual)
        assertEquals(vendedor.id, repo.movimientos.value.single().usuario)
        assertEquals("", vm.estadoFormularioMovimiento.value.codigoProducto)
    }

    @Test fun ajusteRequiereMotivoYEstableceStockContadoPositivo() {
        val repo = repositorio()
        val vm = InventarioViewModel(repo)
        vm.actualizarProductoMovimiento("P001")
        vm.actualizarTipoMovimiento(TipoMovimiento.AJUSTE)
        vm.actualizarCantidad("2")
        assertFalse(vm.registrarMovimiento(administrador))
        assertNotNull(vm.estadoFormularioMovimiento.value.errores.motivo)
        assertTrue(repo.movimientos.value.isEmpty())
        vm.actualizarMotivo("  Conteo físico  ")
        vm.actualizarCantidad("0")
        assertFalse(vm.registrarMovimiento(administrador))
        vm.actualizarCantidad("2")
        assertFalse(vm.registrarMovimiento(vendedor))
        assertTrue(vm.registrarMovimiento(administrador))
        assertEquals(2, repo.buscarProducto("P001")!!.stockActual)
        assertEquals("Conteo físico", repo.movimientos.value.single().motivo)
    }

    @Test fun entradaValidaSumaStockYRechazaDesbordamiento() {
        val repo = repositorio()
        val vm = InventarioViewModel(repo)
        vm.actualizarProductoMovimiento("P001")
        vm.actualizarTipoMovimiento(TipoMovimiento.ENTRADA)
        vm.actualizarCantidad("3")
        assertFalse(vm.registrarMovimiento(null))
        assertTrue(vm.registrarMovimiento(administrador))
        assertEquals(8, repo.buscarProducto("P001")!!.stockActual)
        vm.actualizarProductoMovimiento("P001")
        vm.actualizarTipoMovimiento(TipoMovimiento.ENTRADA)
        vm.actualizarCantidad(Int.MAX_VALUE.toString())
        assertFalse(vm.registrarMovimiento(administrador))
        assertEquals(8, repo.buscarProducto("P001")!!.stockActual)
        assertEquals(1, repo.movimientos.value.size)
    }

    @Test fun movimientoIncompletoONumeroInvalidoNoModificaInventario() {
        val repo = repositorio()
        val vm = InventarioViewModel(repo)
        assertFalse(vm.registrarMovimiento(administrador))
        assertNotNull(vm.estadoFormularioMovimiento.value.errores.producto)
        assertNotNull(vm.estadoFormularioMovimiento.value.errores.tipo)
        vm.actualizarProductoMovimiento("P001")
        vm.actualizarTipoMovimiento(TipoMovimiento.SALIDA_CONSUMO)
        for (cantidad in listOf("0", "-1", "1.5", "texto")) {
            vm.actualizarCantidad(cantidad)
            assertFalse(vm.registrarMovimiento(administrador))
        }
        assertEquals(5, repo.buscarProducto("P001")!!.stockActual)
        assertTrue(repo.movimientos.value.isEmpty())
    }
}
