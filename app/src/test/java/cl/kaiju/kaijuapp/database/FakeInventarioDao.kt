package cl.kaiju.kaijuapp.database

import cl.kaiju.kaijuapp.model.Movimiento
import cl.kaiju.kaijuapp.model.Producto
import cl.kaiju.kaijuapp.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** DAO de prueba según la guía: no necesita Context ni un dispositivo Android. */
open class FakeInventarioDao(
    productosIniciales: List<Producto> = emptyList(),
    usuariosIniciales: List<Usuario> = DatosDePrueba.usuarios
) : InventarioDao() {
    private val productos = MutableStateFlow(productosIniciales.sortedBy { it.codigo })
    private val movimientos = MutableStateFlow<List<Movimiento>>(emptyList())
    private val usuarios = MutableStateFlow(usuariosIniciales)

    override fun observarProductos() = productos
    override fun observarMovimientos() = movimientos
    override fun observarUsuarios() = usuarios
    override fun observarHistorial(codigo: String) =
        movimientos.map { lista -> lista.filter { it.codigoProducto == codigo } }
    override suspend fun buscarProducto(codigo: String) = productos.value.find { it.codigo == codigo }
    override suspend fun obtenerProductos() = productos.value
    override suspend fun obtenerCategorias() = productos.value.map { it.categoria }.distinct()
    override suspend fun buscarUsuario(id: Int) = usuarios.value.find { it.id == id }

    override suspend fun insertarProducto(producto: Producto) {
        check(buscarProducto(producto.codigo) == null) { "Código duplicado" }
        productos.value = (productos.value + producto).sortedBy { it.codigo }
    }

    override suspend fun insertarMovimiento(movimiento: Movimiento): Long {
        val id = (movimientos.value.maxOfOrNull { it.id } ?: 0) + 1
        movimientos.value = (movimientos.value + movimiento.copy(id = id))
            .sortedWith(compareByDescending<Movimiento> { it.fecha }.thenByDescending { it.id })
        return id.toLong()
    }

    override suspend fun actualizarDatosProducto(
        codigo: String, nombre: String, descripcion: String, categoria: String,
        tipo: String, precio: Int, stockMinimo: Int, detalle: String?
    ): Int {
        val anterior = buscarProducto(codigo) ?: return 0
        reemplazarProducto(anterior.copy(nombre = nombre, descripcion = descripcion,
            categoria = categoria, tipo = tipo, precio = precio, stockMinimo = stockMinimo, detalle = detalle))
        return 1
    }

    private fun reemplazarProducto(producto: Producto) {
        productos.value = productos.value.map { if (it.codigo == producto.codigo) producto else it }
    }

    override suspend fun actualizarStock(codigo: String, stockEsperado: Int, nuevoStock: Int): Int {
        val anterior = buscarProducto(codigo) ?: return 0
        if (anterior.stockActual != stockEsperado || nuevoStock < 0) return 0
        reemplazarProducto(anterior.copy(stockActual = nuevoStock))
        return 1
    }
}
