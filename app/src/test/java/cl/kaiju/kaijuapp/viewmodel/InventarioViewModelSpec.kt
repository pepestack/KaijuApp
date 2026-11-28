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
import kotlinx.coroutines.delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
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

    test("RN-01: una salida mayor al stock se rechaza, pero vender todo el stock sí se permite") {
        val repo = repositorioFalso()
        val vm = InventarioViewModel(repo)
        vm.actualizarProductoMovimiento("POL-001")
        vm.actualizarTipoMovimiento(TipoMovimiento.SALIDA_VENTA)
        vm.actualizarCantidad("8") // hay 5 disponibles

        runTest(dispatcher) {
            vm.registrarMovimiento(vendedor)
            advanceUntilIdle() // deja terminar la corrutina de viewModelScope
        }

        vm.estadoFormularioMovimiento.value.errores.cantidad shouldBe "La salida supera el stock disponible"
        coVerify(exactly = 0) { repo.registrarMovimiento(any(), any(), any(), any(), any(), any(), any()) }

        // El límite: vender exactamente las 5 unidades deja el stock en 0, que sí es válido.
        vm.actualizarCantidad("5")
        vm.validarFormularioMovimiento(vendedor) shouldBe true
    }

    test("RN-04: un ajuste sin motivo se rechaza; con motivo fija el stock contado, incluso 0") {
        val repo = repositorioFalso()
        coEvery { repo.registrarMovimiento(any(), any(), any(), any(), any(), any(), any()) } returns true
        val vm = InventarioViewModel(repo)
        vm.actualizarProductoMovimiento("POL-001")
        vm.actualizarTipoMovimiento(TipoMovimiento.AJUSTE)
        vm.actualizarCantidad("4")
        vm.actualizarMotivo("   ") // solo espacios cuenta como vacío

        vm.validarFormularioMovimiento(encargado) shouldBe false
        vm.estadoFormularioMovimiento.value.errores.motivo shouldBe "El ajuste exige un motivo"

        // Con motivo, el ajuste guarda la cantidad contada como stock final (0 = todo perdido).
        vm.actualizarCantidad("0")
        vm.actualizarMotivo("  Conteo físico  ")
        runTest(dispatcher) {
            vm.registrarMovimiento(encargado)
            advanceUntilIdle()
        }

        coVerify(exactly = 1) {
            repo.registrarMovimiento(
                "POL-001", TipoMovimiento.AJUSTE, 0, "Conteo físico", encargado.id,
                /* stockEsperado = */ 5, /* nuevoStock = */ 0
            )
        }
    }

    test("RF-07: el vendedor solo puede registrar salidas por venta") {
        val repo = repositorioFalso()
        val vm = InventarioViewModel(repo)
        vm.tiposPermitidos(Rol.VENDEDOR) shouldBe listOf(TipoMovimiento.SALIDA_VENTA)

        vm.actualizarProductoMovimiento("POL-001")
        vm.actualizarTipoMovimiento(TipoMovimiento.ENTRADA)
        vm.actualizarCantidad("2")
        vm.validarFormularioMovimiento(vendedor) shouldBe false
        vm.estadoFormularioMovimiento.value.errores.tipo shouldBe "Tu perfil no permite este movimiento"

        // Aunque intente guardar igual, la entrada nunca llega al Repository.
        runTest(dispatcher) {
            vm.registrarMovimiento(vendedor)
            advanceUntilIdle()
        }
        coVerify(exactly = 0) { repo.registrarMovimiento(any(), any(), any(), any(), any(), any(), any()) }

        // Lo que sí le corresponde se acepta.
        vm.actualizarTipoMovimiento(TipoMovimiento.SALIDA_VENTA)
        vm.validarFormularioMovimiento(vendedor) shouldBe true
    }

    test("RN-02 y RN-03: un producto con código repetido y precio cero se rechaza") {
        val repo = repositorioFalso()
        val vm = InventarioViewModel(repo)
        vm.prepararNuevoProducto()
        vm.actualizarCodigo("POL-001") // ya existe en el Repository falso
        vm.actualizarNombre("Polera nueva")
        vm.actualizarCategoria("Poleras")
        vm.actualizarPrecio("0")
        vm.actualizarStockMinimo("2")

        runTest(dispatcher) {
            vm.guardarProducto(admin) // el mismo camino que el botón Guardar
            advanceUntilIdle()
        }

        val errores = vm.estadoFormularioProducto.value.errores
        errores.codigo shouldBe "El código ya existe"
        errores.precio shouldBe "El precio debe ser un entero mayor que cero"
        // Los campos que estaban bien no muestran error.
        errores.nombre.shouldBeNull()
        errores.categoria.shouldBeNull()
        errores.stockMinimo.shouldBeNull()
        // Y el producto inválido nunca se guarda.
        coVerify(exactly = 0) { repo.agregarProducto(any(), any()) }
    }

    test("RF-06: una entrada válida suma al stock y llega al Repository una sola vez") {
        val repo = repositorioFalso()
        // El guardado tarda un poco, como SQLite, para poder mirar el estado intermedio.
        coEvery { repo.registrarMovimiento(any(), any(), any(), any(), any(), any(), any()) } coAnswers {
            delay(100)
            true
        }
        val vm = InventarioViewModel(repo)
        vm.actualizarProductoMovimiento("POL-001")
        vm.actualizarTipoMovimiento(TipoMovimiento.ENTRADA)
        vm.actualizarCantidad("10")

        runTest(dispatcher) {
            vm.registrarMovimiento(encargado)
            runCurrent() // avanza hasta que el Repository está escribiendo
            vm.guardando.value shouldBe true // los botones quedan desactivados
            advanceUntilIdle()
            vm.guardando.value shouldBe false
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

    test("RN-06: avisa una sola vez al llegar al mínimo y no repite el aviso en salidas posteriores") {
        val repo = repositorioFalso()
        coEvery { repo.registrarMovimiento(any(), any(), any(), any(), any(), any(), any()) } returns true
        val aviso = mockk<AvisoStock>()
        every { aviso.stockBajo(any()) } just Runs
        val vm = InventarioViewModel(repo, aviso)
        vm.actualizarProductoMovimiento("POL-001")
        vm.actualizarTipoMovimiento(TipoMovimiento.SALIDA_CONSUMO)
        vm.actualizarCantidad("2") // 5 - 2 = 3, justo el mínimo

        runTest(dispatcher) {
            vm.registrarMovimiento(encargado)
            advanceUntilIdle()
        }

        verify(exactly = 1) { aviso.stockBajo(polera.copy(stockActual = 3)) }
        vm.evento.value shouldBe EventoUi.MovimientoRegistrado(TipoMovimiento.SALIDA_CONSUMO, 2, entroEnAlerta = true)

        // El producto ya está en alerta: otra salida no vuelve a avisar.
        coEvery { repo.buscarProducto("POL-001") } returns polera.copy(stockActual = 3)
        vm.actualizarProductoMovimiento("POL-001")
        vm.actualizarTipoMovimiento(TipoMovimiento.SALIDA_CONSUMO)
        vm.actualizarCantidad("1")
        runTest(dispatcher) {
            vm.registrarMovimiento(encargado)
            advanceUntilIdle()
        }

        verify(exactly = 1) { aviso.stockBajo(any()) } // sigue siendo el primer aviso
        vm.evento.value shouldBe EventoUi.MovimientoRegistrado(TipoMovimiento.SALIDA_CONSUMO, 1, entroEnAlerta = false)
    }
})
