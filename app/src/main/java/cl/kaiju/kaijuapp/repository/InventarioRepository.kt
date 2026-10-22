package cl.kaiju.kaijuapp.repository

import cl.kaiju.kaijuapp.database.InventarioDao
import cl.kaiju.kaijuapp.model.Movimiento
import cl.kaiju.kaijuapp.model.Producto
import cl.kaiju.kaijuapp.model.TipoMovimiento

// Room es la fuente de verdad. Los Flow notifican los cambios sin mantener otra lista en memoria.
class InventarioRepository(private val dao: InventarioDao) {
    val productos = dao.observarProductos()
    val movimientos = dao.observarMovimientos()
    val usuarios = dao.observarUsuarios()

    suspend fun buscarProducto(codigo: String) = dao.buscarProducto(codigo)
    suspend fun buscarPorCodigoBarras(codigoBarras: String) = dao.buscarPorCodigoBarras(codigoBarras)
    suspend fun obtenerCategorias() = dao.obtenerCategorias()
    suspend fun buscarUsuario(id: Int) = dao.buscarUsuario(id)

    fun historialDe(codigoProducto: String) = dao.observarHistorial(codigoProducto)

    // Producto y entrada inicial se confirman juntos; SQLite asigna los IDs de movimientos.
    suspend fun agregarProducto(producto: Producto, usuarioId: Int) {
        val entrada = if (producto.stockActual > 0) Movimiento(
            codigoProducto = producto.codigo, tipo = TipoMovimiento.ENTRADA,
            cantidad = producto.stockActual, motivo = "Stock inicial", usuario = usuarioId
        ) else null
        dao.crearProductoConEntrada(producto, entrada)
    }

    suspend fun actualizarProducto(producto: Producto): Boolean = dao.actualizarDatosProducto(
        producto.codigo, producto.nombre, producto.descripcion, producto.categoria,
        producto.tipo, producto.precio, producto.stockMinimo, producto.detalle, producto.codigoBarras
    ) == 1

    // El ViewModel calcula el resultado. El DAO verifica que el stock leído siga vigente.
    suspend fun registrarMovimiento(
        codigoProducto: String, tipo: TipoMovimiento, cantidad: Int, motivo: String?,
        usuarioId: Int, stockEsperado: Int, nuevoStock: Int
    ): Boolean = dao.guardarMovimiento(
        Movimiento(codigoProducto = codigoProducto, tipo = tipo, cantidad = cantidad,
            motivo = motivo, usuario = usuarioId), stockEsperado, nuevoStock
    )
}
