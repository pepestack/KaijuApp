package cl.kaiju.kaijuapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import cl.kaiju.kaijuapp.model.Movimiento
import cl.kaiju.kaijuapp.model.Producto
import cl.kaiju.kaijuapp.model.Usuario
import kotlinx.coroutines.flow.Flow

@Dao
abstract class InventarioDao {
    @Query("SELECT * FROM productos ORDER BY codigo")
    abstract fun observarProductos(): Flow<List<Producto>>

    @Query("SELECT * FROM movimientos ORDER BY fecha DESC, id DESC")
    abstract fun observarMovimientos(): Flow<List<Movimiento>>

    @Query("SELECT * FROM usuarios ORDER BY id")
    abstract fun observarUsuarios(): Flow<List<Usuario>>

    @Query("SELECT * FROM productos WHERE codigo = :codigo")
    abstract suspend fun buscarProducto(codigo: String): Producto?

    @Query("SELECT * FROM productos ORDER BY codigo")
    abstract suspend fun obtenerProductos(): List<Producto>

    @Query("SELECT * FROM usuarios WHERE id = :id")
    abstract suspend fun buscarUsuario(id: Int): Usuario?

    @Insert
    abstract suspend fun insertarProducto(producto: Producto)

    @Insert
    abstract suspend fun insertarMovimiento(movimiento: Movimiento): Long

    // Editar datos descriptivos nunca sobrescribe el stock actualizado por un movimiento.
    @Query("""UPDATE productos SET nombre = :nombre, descripcion = :descripcion,
        categoria = :categoria, tipo = :tipo, precio = :precio,
        stockMinimo = :stockMinimo, detalle = :detalle WHERE codigo = :codigo""")
    abstract suspend fun actualizarDatosProducto(
        codigo: String, nombre: String, descripcion: String, categoria: String,
        tipo: String, precio: Int, stockMinimo: Int, detalle: String?
    ): Int

    // Control de concurrencia en SQLite: no se escribe sobre un stock que ya cambió.
    @Query("""UPDATE productos SET stockActual = :nuevoStock
        WHERE codigo = :codigo AND stockActual = :stockEsperado AND :nuevoStock >= 0""")
    abstract suspend fun actualizarStock(codigo: String, stockEsperado: Int, nuevoStock: Int): Int

    // RN-05: si falla cualquiera de las escrituras, Room revierte la transacción completa.
    @Transaction
    open suspend fun crearProductoConEntrada(producto: Producto, entrada: Movimiento?) {
        insertarProducto(producto)
        if (entrada != null) insertarMovimiento(entrada)
    }

    @Transaction
    open suspend fun guardarMovimiento(
        movimiento: Movimiento, stockEsperado: Int, nuevoStock: Int
    ): Boolean {
        if (actualizarStock(movimiento.codigoProducto, stockEsperado, nuevoStock) != 1) return false
        insertarMovimiento(movimiento)
        return true
    }
}
