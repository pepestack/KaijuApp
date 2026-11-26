package cl.kaiju.kaijuapp.viewmodel

import cl.kaiju.kaijuapp.model.EventoUi
import cl.kaiju.kaijuapp.model.Producto
import cl.kaiju.kaijuapp.model.Rol
import cl.kaiju.kaijuapp.model.TipoMovimiento
import cl.kaiju.kaijuapp.model.Usuario
import cl.kaiju.kaijuapp.notificaciones.AvisoStock
import cl.kaiju.kaijuapp.repository.InventarioRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class InventarioViewModelSpec : FunSpec({

    val dispatcher = StandardTestDispatcher()
    beforeTest { Dispatchers.setMain(dispatcher) }
    afterTest { Dispatchers.resetMain() }

    val admin = Usuario(1, "Admin de prueba", Rol.ADMINISTRADOR)
    val encargado = Usuario(2, "Encargado de prueba", Rol.ENCARGADO_INVENTARIO)
    val vendedor = Usuario(3, "Vendedor de prueba", Rol.VENDEDOR)
    val polera = Producto(
        codigo = "POL-001", nombre = "Polera básica", descripcion = "Algodón",
        categoria = "Poleras", tipo = "Prenda", precio = 9990, stockActual = 5, stockMinimo = 3
    )

    fun repositorioFalso(): InventarioRepository {
        val repo = mockk<InventarioRepository>()
        every { repo.productos } returns flowOf(listOf(polera))
        every { repo.usuarios } returns flowOf(listOf(admin, encargado, vendedor))
        coEvery { repo.buscarProducto(any()) } returns null
        coEvery { repo.buscarProducto("POL-001") } returns polera
        coEvery { repo.buscarUsuario(any()) } answers {
            listOf(admin, encargado, vendedor).find { it.id == firstArg<Int>() }
        }
        return repo
    }

    test("RN-01: una salida mayor al stock disponible se rechaza y no se guarda nada") {
        val repo = repositorioFalso()
        val vm = InventarioViewModel(repo)
        vm.actualizarProductoMovimiento("POL-001")
        vm.actualizarTipoMovimiento(TipoMovimiento.SALIDA_VENTA)
        vm.actualizarCantidad("8")

        runTest(dispatcher) {
            vm.registrarMovimiento(vendedor)
            advanceUntilIdle() // deja terminar la corrutina de viewModelScope
        }

        vm.estadoFormularioMovimiento.value.errores.cantidad shouldBe "La salida supera el stock disponible"
        coVerify(exactly = 0) { repo.registrarMovimiento(any(), any(), any(), any(), any(), any(), any()) }
    }

    test("RN-04: un ajuste sin motivo se rechaza") {
        val vm = InventarioViewModel(repositorioFalso())
        vm.actualizarProductoMovimiento("POL-001")
        vm.actualizarTipoMovimiento(TipoMovimiento.AJUSTE)
        vm.actualizarCantidad("4")
        vm.actualizarMotivo("   ") // solo espacios cuenta como vacío

        vm.validarFormularioMovimiento(encargado) shouldBe false
        vm.estadoFormularioMovimiento.value.errores.motivo shouldBe "El ajuste exige un motivo"
    }

    test("RF-07: el vendedor solo puede registrar salidas por venta") {
        val vm = InventarioViewModel(repositorioFalso())
        vm.tiposPermitidos(Rol.VENDEDOR) shouldBe listOf(TipoMovimiento.SALIDA_VENTA)

        vm.actualizarProductoMovimiento("POL-001")
        vm.actualizarTipoMovimiento(TipoMovimiento.ENTRADA)
        vm.actualizarCantidad("10")
        vm.validarFormularioMovimiento(vendedor) shouldBe false
        vm.estadoFormularioMovimiento.value.errores.tipo shouldBe "Tu perfil no permite este movimiento"
    }

    test("RN-02 y RN-03: un producto con código repetido y precio cero se rechaza") {
        val vm = InventarioViewModel(repositorioFalso())
        vm.prepararNuevoProducto()
        vm.actualizarCodigo("POL-001") // ya existe en el Repository falso
        vm.actualizarNombre("Polera nueva")
        vm.actualizarCategoria("Poleras")
        vm.actualizarPrecio("0")
        vm.actualizarStockMinimo("2")

        vm.validarFormularioProducto() shouldBe false
        val errores = vm.estadoFormularioProducto.value.errores
        errores.codigo shouldBe "El código ya existe"
        errores.precio shouldBe "El precio debe ser un entero mayor que cero"
        // Los campos que estaban bien no muestran error.
        errores.nombre.shouldBeNull()
        errores.categoria.shouldBeNull()
    }

    test("RF-06: una entrada válida suma al stock y llega al Repository una sola vez") {
        val repo = repositorioFalso()
        coEvery { repo.registrarMovimiento(any(), any(), any(), any(), any(), any(), any()) } returns true
        val vm = InventarioViewModel(repo)
        vm.actualizarProductoMovimiento("POL-001")
        vm.actualizarTipoMovimiento(TipoMovimiento.ENTRADA)
        vm.actualizarCantidad("10")

        runTest(dispatcher) {
            vm.registrarMovimiento(encargado)
            advanceUntilIdle()
        }

        coVerify(exactly = 1) {
            repo.registrarMovimiento(
                "POL-001", TipoMovimiento.ENTRADA, 10, null, encargado.id,
                /* stockEsperado = */ 5, /* nuevoStock = */ 15
            )
        }
        vm.evento.value shouldBe EventoUi.MovimientoRegistrado(TipoMovimiento.ENTRADA, 10, entroEnAlerta = false)
        vm.estadoFormularioMovimiento.value.cantidad shouldBe "" // el formulario quedó limpio
    }

    test("RN-06: una salida que deja el stock en el mínimo o bajo él avisa una vez") {
        val repo = repositorioFalso()
        coEvery { repo.registrarMovimiento(any(), any(), any(), any(), any(), any(), any()) } returns true
        val aviso = mockk<AvisoStock>()
        every { aviso.stockBajo(any()) } just Runs
        val vm = InventarioViewModel(repo, aviso)
        vm.actualizarProductoMovimiento("POL-001")
        vm.actualizarTipoMovimiento(TipoMovimiento.SALIDA_CONSUMO)
        vm.actualizarCantidad("3") // 5 - 3 = 2, que es <= al mínimo (3)

        runTest(dispatcher) {
            vm.registrarMovimiento(encargado)
            advanceUntilIdle()
        }

        verify(exactly = 1) { aviso.stockBajo(polera.copy(stockActual = 2)) }
        vm.evento.value shouldBe EventoUi.MovimientoRegistrado(TipoMovimiento.SALIDA_CONSUMO, 3, entroEnAlerta = true)
    }
})
