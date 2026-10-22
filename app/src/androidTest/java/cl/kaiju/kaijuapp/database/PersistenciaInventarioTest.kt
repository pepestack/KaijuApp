package cl.kaiju.kaijuapp.database

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cl.kaiju.kaijuapp.model.Movimiento
import cl.kaiju.kaijuapp.model.Producto
import cl.kaiju.kaijuapp.model.Rol
import cl.kaiju.kaijuapp.model.TipoMovimiento
import cl.kaiju.kaijuapp.repository.InventarioRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Archivo de pruebas separado: nunca abre ni borra kaiju.db. */
@RunWith(AndroidJUnit4::class)
class PersistenciaInventarioTest {
    private lateinit var context: Context
    private lateinit var database: KaijuDatabase
    private val nombre = "kaiju-persistencia-test.db"

    @Before fun preparar() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(nombre)
        database = KaijuDatabase.crear(context, nombre)
    }

    @After fun limpiar() {
        database.close()
        context.deleteDatabase(nombre)
    }

    @Test fun reabrirConservaProductosMovimientosUsuariosYNoReinsertaSemillas() = runBlocking {
        var repo = InventarioRepository(database.inventarioDao())
        val usuario = repo.usuarios.first().first { it.rol == Rol.ADMINISTRADOR }
        val producto = Producto("TEST-001", "Prueba persistente", "", "Poleras", "Vestuario", 15000, 7, 2)
        repo.agregarProducto(producto, usuario.id)
        assertTrue(repo.registrarMovimiento(producto.codigo, TipoMovimiento.SALIDA_VENTA, 2,
            null, usuario.id, 7, 5))
        val semilla = DatosDePrueba.productos.first()
        assertTrue(repo.registrarMovimiento(semilla.codigo, TipoMovimiento.AJUSTE, 1,
            "Conteo de prueba", usuario.id, semilla.stockActual, 1))
        database.close()

        database = KaijuDatabase.crear(context, nombre)
        repo = InventarioRepository(database.inventarioDao())
        assertEquals(5, repo.buscarProducto(producto.codigo)!!.stockActual)
        assertEquals(1, repo.buscarProducto(semilla.codigo)!!.stockActual)
        assertEquals(DatosDePrueba.productos.size + 1, repo.productos.first().size)
        assertEquals(DatosDePrueba.usuarios, repo.usuarios.first())
        val historial = repo.historialDe(producto.codigo).first()
        assertEquals(listOf(TipoMovimiento.SALIDA_VENTA, TipoMovimiento.ENTRADA), historial.map { it.tipo })
        assertTrue(historial.all { it.usuario == usuario.id && it.id > 0 })
        val ultimoId = repo.movimientos.first().maxOf { it.id }
        assertTrue(repo.registrarMovimiento(producto.codigo, TipoMovimiento.ENTRADA, 1,
            null, usuario.id, 5, 6))
        assertTrue(repo.movimientos.first().maxOf { it.id } > ultimoId)
    }

    @Test fun stockDesactualizadoNoRegistraOtraSalida() = runBlocking {
        val repo = InventarioRepository(database.inventarioDao())
        val producto = DatosDePrueba.productos.first()
        val stock = producto.stockActual
        assertTrue(repo.registrarMovimiento(producto.codigo, TipoMovimiento.SALIDA_VENTA,
            1, null, 1, stock, stock - 1))
        assertFalse(repo.registrarMovimiento(producto.codigo, TipoMovimiento.SALIDA_VENTA,
            1, null, 1, stock, stock - 1))
        assertEquals(stock - 1, repo.buscarProducto(producto.codigo)!!.stockActual)
        assertEquals(1, repo.historialDe(producto.codigo).first().size)
    }

    @Test fun falloAlInsertarMovimientoRevierteElStock() = runBlocking {
        val dao = database.inventarioDao()
        val producto = dao.obtenerProductos().first()
        val movimiento = Movimiento(id = 100, codigoProducto = producto.codigo,
            tipo = TipoMovimiento.SALIDA_VENTA, cantidad = 1, usuario = 1)
        dao.insertarMovimiento(movimiento)
        try {
            dao.guardarMovimiento(movimiento, producto.stockActual, producto.stockActual - 1)
            fail("Se esperaba el rechazo del ID duplicado")
        } catch (esperado: android.database.sqlite.SQLiteConstraintException) {
            assertEquals(producto.stockActual, dao.buscarProducto(producto.codigo)!!.stockActual)
            assertEquals(1, dao.observarMovimientos().first().size)
        }
    }

    @Test fun codigoDuplicadoNoSobrescribeProductoNiCreaEntrada() = runBlocking {
        val repo = InventarioRepository(database.inventarioDao())
        val original = repo.productos.first().first()
        try {
            repo.agregarProducto(original.copy(nombre = "No debe reemplazar"), 1)
            fail("Se esperaba el rechazo del código duplicado")
        } catch (esperado: android.database.sqlite.SQLiteConstraintException) {
            assertEquals(original, repo.buscarProducto(original.codigo))
            assertTrue(repo.movimientos.first().isEmpty())
        }
    }
}
