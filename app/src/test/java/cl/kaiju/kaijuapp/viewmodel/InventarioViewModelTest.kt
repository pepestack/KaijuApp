package cl.kaiju.kaijuapp.viewmodel

import cl.kaiju.kaijuapp.database.FakeInventarioDao
import kotlinx.coroutines.flow.first
import cl.kaiju.kaijuapp.model.EventoUi
import cl.kaiju.kaijuapp.model.Producto
import cl.kaiju.kaijuapp.model.Rol
import cl.kaiju.kaijuapp.model.Usuario
import cl.kaiju.kaijuapp.model.TipoMovimiento
import cl.kaiju.kaijuapp.repository.InventarioRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
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
        val vm = InventarioViewModel(InventarioRepository(FakeInventarioDao(listOf(polera, poleron, agotado))))
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
        val vm = InventarioViewModel(InventarioRepository(FakeInventarioDao(listOf(polera, poleron, agotado))))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.estadoCatalogo.collect {} }
        vm.actualizarBusqueda("inexistente")
        runCurrent()
        assertTrue(vm.estadoCatalogo.value.productos.isEmpty())
        assertEquals(listOf(polera, agotado), vm.estadoCatalogo.value.productosEnAlerta)
    }

    @Test fun cambiosDelRepositorioActualizanProductosCategoriasYAlertas() = runTest(dispatcher) {
        val repo = InventarioRepository(FakeInventarioDao(listOf(polera, poleron)))
        val vm = InventarioViewModel(repo)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.estadoCatalogo.collect {} }
        runCurrent()
        assertEquals(listOf("Poleras", "Polerones"), vm.estadoCatalogo.value.categorias)
        repo.registrarMovimiento("P001", TipoMovimiento.ENTRADA, 1, null, 1, 5, 6)
        repo.agregarProducto(polera.copy(codigo = "P004", categoria = "Accesorios", stockActual = 9), 1)
        runCurrent()
        assertEquals(3, vm.estadoCatalogo.value.productos.size)
        assertTrue(vm.estadoCatalogo.value.productosEnAlerta.isEmpty())
        assertEquals(listOf("Accesorios", "Poleras", "Polerones"), vm.estadoCatalogo.value.categorias)
    }

    @Test fun inventarioVacioExponeListasVacias() = runTest(dispatcher) {
        val vm = InventarioViewModel(InventarioRepository(FakeInventarioDao()))
        assertTrue(vm.estadoCatalogo.value.productos.isEmpty())
        assertTrue(vm.estadoCatalogo.value.categorias.isEmpty())
        assertTrue(vm.estadoCatalogo.value.productosEnAlerta.isEmpty())
    }

    private val administrador = Usuario(100, "Administrador ficticio", Rol.ADMINISTRADOR)
    private val vendedor = Usuario(101, "Vendedor ficticio", Rol.VENDEDOR)
    private fun repositorio() = InventarioRepository(
        FakeInventarioDao(listOf(polera, poleron), listOf(administrador, vendedor))
    )
    // Las operaciones del ViewModel corren en viewModelScope: se lanzan, se deja correr el
    // dispatcher de prueba y el evento indica si terminaron bien.
    private fun TestScope.guardar(vm: InventarioViewModel, usuario: Usuario?): Boolean {
        vm.consumirEvento()
        vm.guardarProducto(usuario)
        advanceUntilIdle()
        return vm.evento.value == EventoUi.ProductoGuardado
    }
    private fun TestScope.registrar(vm: InventarioViewModel, usuario: Usuario?): Boolean {
        vm.consumirEvento()
        vm.registrarMovimiento(usuario)
        advanceUntilIdle()
        return vm.evento.value == EventoUi.MovimientoRegistrado
    }
    private fun TestScope.prepararEdicion(vm: InventarioViewModel, codigo: String): Boolean {
        vm.consumirEvento()
        vm.prepararEdicionProducto(codigo)
        advanceUntilIdle()
        return vm.evento.value == EventoUi.EdicionLista(codigo)
    }
    private fun completarProducto(vm: InventarioViewModel, codigo: String = "P010") {
        vm.prepararNuevoProducto()
        vm.actualizarCodigo(codigo)
        vm.actualizarNombre("Nueva polera")
        vm.actualizarCategoria("Poleras")
        vm.actualizarPrecio("12000")
        vm.actualizarStockMinimo("2")
        vm.actualizarStockActual("4")
    }

    @Test fun productoInvalidoNoSeGuardaYExponeErroresPorCampo() = runTest(dispatcher) {
        val repo = repositorio()
        val vm = InventarioViewModel(repo)
        completarProducto(vm, " P001 ")
        vm.actualizarNombre("a")
        vm.actualizarCategoria("Inexistente")
        vm.actualizarPrecio("0")
        vm.actualizarStockMinimo("-1")
        vm.actualizarStockActual("1.5")
        assertFalse(guardar(vm, administrador))
        val errores = vm.estadoFormularioProducto.value.errores
        assertNotNull(errores.codigo)
        assertNotNull(errores.nombre)
        assertNotNull(errores.categoria)
        assertNotNull(errores.precio)
        assertNotNull(errores.stockMinimo)
        assertNotNull(errores.stockActual)
        assertEquals(2, repo.productos.first().size)
        assertTrue(repo.movimientos.first().isEmpty())
    }

    @Test fun crearProductoRegistraStockInicialYRechazaSegundoCodigoIgual() = runTest(dispatcher) {
        val repo = repositorio()
        val vm = InventarioViewModel(repo)
        completarProducto(vm)
        assertFalse(guardar(vm, vendedor))
        assertTrue(guardar(vm, administrador))
        assertEquals(4, repo.buscarProducto("P010")!!.stockActual)
        assertEquals(TipoMovimiento.ENTRADA, repo.movimientos.first().single().tipo)
        assertEquals(administrador.id, repo.movimientos.first().single().usuario)
        completarProducto(vm)
        assertFalse(guardar(vm, administrador))
        assertEquals(3, repo.productos.first().size)
    }

    @Test fun editarNoCambiaCodigoNiSobrescribeStockActualizado() = runTest(dispatcher) {
        val repo = repositorio()
        val vm = InventarioViewModel(repo)
        assertTrue(prepararEdicion(vm, "P001"))
        vm.actualizarCodigo("OTRO")
        vm.actualizarStockActual("99")
        vm.actualizarNombre("Nombre editado")
        repo.registrarMovimiento("P001", TipoMovimiento.SALIDA_VENTA, 2, null, administrador.id, 5, 3)
        assertTrue(guardar(vm, administrador))
        assertEquals("Nombre editado", repo.buscarProducto("P001")!!.nombre)
        assertEquals(3, repo.buscarProducto("P001")!!.stockActual)
        assertEquals(2, repo.productos.first().size)
    }

    @Test fun salidaExcesivaNoAlteraDatosYSalidaExactaAgotaStock() = runTest(dispatcher) {
        val repo = repositorio()
        val vm = InventarioViewModel(repo)
        vm.actualizarProductoMovimiento("P001")
        vm.actualizarTipoMovimiento(TipoMovimiento.SALIDA_VENTA)
        vm.actualizarCantidad("6")
        assertFalse(registrar(vm, vendedor))
        assertNotNull(vm.estadoFormularioMovimiento.value.errores.cantidad)
        assertEquals(5, repo.buscarProducto("P001")!!.stockActual)
        assertTrue(repo.movimientos.first().isEmpty())
        vm.actualizarCantidad("5")
        assertTrue(registrar(vm, vendedor))
        assertEquals(0, repo.buscarProducto("P001")!!.stockActual)
        assertEquals(vendedor.id, repo.movimientos.first().single().usuario)
        assertEquals("", vm.estadoFormularioMovimiento.value.codigoProducto)
    }

    @Test fun ajusteRequiereMotivoYEstableceStockContado() = runTest(dispatcher) {
        val repo = repositorio()
        val vm = InventarioViewModel(repo)
        vm.actualizarProductoMovimiento("P001")
        vm.actualizarTipoMovimiento(TipoMovimiento.AJUSTE)
        vm.actualizarCantidad("2")
        assertFalse(registrar(vm, administrador))
        assertNotNull(vm.estadoFormularioMovimiento.value.errores.motivo)
        assertTrue(repo.movimientos.first().isEmpty())
        vm.actualizarMotivo("  Conteo físico  ")
        vm.actualizarCantidad("-1")
        assertFalse(registrar(vm, administrador))
        vm.actualizarCantidad("2")
        assertFalse(registrar(vm, vendedor))
        assertTrue(registrar(vm, administrador))
        assertEquals(2, repo.buscarProducto("P001")!!.stockActual)
        assertEquals("Conteo físico", repo.movimientos.first().single().motivo)
    }

    @Test fun ajustePuedeDejarStockEnCeroPeroOtrosTiposNo() = runTest(dispatcher) {
        val repo = repositorio()
        val vm = InventarioViewModel(repo)
        vm.actualizarProductoMovimiento("P001")
        vm.actualizarTipoMovimiento(TipoMovimiento.ENTRADA)
        vm.actualizarCantidad("0")
        assertFalse(registrar(vm, administrador))
        assertNotNull(vm.estadoFormularioMovimiento.value.errores.cantidad)
        vm.actualizarProductoMovimiento("P001")
        vm.actualizarTipoMovimiento(TipoMovimiento.AJUSTE)
        vm.actualizarCantidad("0")
        vm.actualizarMotivo("Productos dañados")
        assertTrue(registrar(vm, administrador))
        assertEquals(0, repo.buscarProducto("P001")!!.stockActual)
        assertEquals(TipoMovimiento.AJUSTE, repo.movimientos.first().single().tipo)
    }

    @Test fun entradaValidaSumaStockYRechazaDesbordamiento() = runTest(dispatcher) {
        val repo = repositorio()
        val vm = InventarioViewModel(repo)
        vm.actualizarProductoMovimiento("P001")
        vm.actualizarTipoMovimiento(TipoMovimiento.ENTRADA)
        vm.actualizarCantidad("3")
        assertFalse(registrar(vm, null))
        assertTrue(registrar(vm, administrador))
        assertEquals(8, repo.buscarProducto("P001")!!.stockActual)
        vm.actualizarProductoMovimiento("P001")
        vm.actualizarTipoMovimiento(TipoMovimiento.ENTRADA)
        vm.actualizarCantidad(Int.MAX_VALUE.toString())
        assertFalse(registrar(vm, administrador))
        assertEquals(8, repo.buscarProducto("P001")!!.stockActual)
        assertEquals(1, repo.movimientos.first().size)
    }

    @Test fun movimientoIncompletoONumeroInvalidoNoModificaInventario() = runTest(dispatcher) {
        val repo = repositorio()
        val vm = InventarioViewModel(repo)
        assertFalse(registrar(vm, administrador))
        assertNotNull(vm.estadoFormularioMovimiento.value.errores.producto)
        assertNotNull(vm.estadoFormularioMovimiento.value.errores.tipo)
        vm.actualizarProductoMovimiento("P001")
        vm.actualizarTipoMovimiento(TipoMovimiento.SALIDA_CONSUMO)
        for (cantidad in listOf("0", "-1", "1.5", "texto")) {
            vm.actualizarCantidad(cantidad)
            assertFalse(registrar(vm, administrador))
        }
        assertEquals(5, repo.buscarProducto("P001")!!.stockActual)
        assertTrue(repo.movimientos.first().isEmpty())
    }

    @Test fun guardadoEsperaLaEscrituraYRechazaOtroClicMientrasTanto() = runTest(dispatcher) {
        val permitirEscritura = CompletableDeferred<Unit>()
        val dao = object : FakeInventarioDao(listOf(polera), listOf(administrador)) {
            override suspend fun crearProductoConEntrada(producto: Producto, entrada: cl.kaiju.kaijuapp.model.Movimiento?) {
                permitirEscritura.await()
                super.crearProductoConEntrada(producto, entrada)
            }
        }
        val repo = InventarioRepository(dao)
        val vm = InventarioViewModel(repo)
        completarProducto(vm)
        vm.guardarProducto(administrador)
        runCurrent()
        assertTrue(vm.guardando.value)
        assertEquals("P010", vm.estadoFormularioProducto.value.codigo)
        vm.guardarProducto(administrador)
        runCurrent()
        assertEquals(null, vm.evento.value)
        permitirEscritura.complete(Unit)
        advanceUntilIdle()
        assertEquals(EventoUi.ProductoGuardado, vm.evento.value)
        assertFalse(vm.guardando.value)
        assertEquals("", vm.estadoFormularioProducto.value.codigo)
        assertEquals(1, repo.historialDe("P010").first().size)
    }

    @Test fun falloDeEscrituraConservaFormularioYDevuelveError() = runTest(dispatcher) {
        val dao = object : FakeInventarioDao(listOf(polera), listOf(administrador)) {
            override suspend fun crearProductoConEntrada(producto: Producto, entrada: cl.kaiju.kaijuapp.model.Movimiento?) {
                throw IllegalStateException("Fallo de almacenamiento simulado")
            }
        }
        val repo = InventarioRepository(dao)
        val vm = InventarioViewModel(repo)
        completarProducto(vm)
        assertFalse(guardar(vm, administrador))
        assertNotNull(vm.errorOperacion.value)
        assertEquals("P010", vm.estadoFormularioProducto.value.codigo)
        assertEquals(1, repo.productos.first().size)
        assertTrue(repo.movimientos.first().isEmpty())
    }

    @Test fun historialSoloIncluyeMovimientosDelProductoPedido() = runTest(dispatcher) {
        val repo = repositorio()
        val vm = InventarioViewModel(repo)
        repo.registrarMovimiento("P001", TipoMovimiento.ENTRADA, 1, null, administrador.id, 5, 6)
        repo.registrarMovimiento("P002", TipoMovimiento.ENTRADA, 2, null, administrador.id, 10, 12)
        repo.registrarMovimiento("P001", TipoMovimiento.SALIDA_VENTA, 3, null, vendedor.id, 6, 3)
        val historial = vm.historialDe("P001").first()
        assertEquals(2, historial.size)
        assertTrue(historial.all { it.codigoProducto == "P001" })
        assertTrue(vm.historialDe("P999").first().isEmpty())
    }
}
