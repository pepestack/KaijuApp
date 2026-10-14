package cl.kaiju.kaijuapp.repository

import cl.kaiju.kaijuapp.model.Movimiento
import cl.kaiju.kaijuapp.model.Producto
import cl.kaiju.kaijuapp.model.TipoMovimiento
import cl.kaiju.kaijuapp.model.Usuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update


class InventarioRepository(
    productosIniciales: List<Producto> = DatosDePrueba.productos,
    movimientosIniciales: List<Movimiento> = emptyList(),
    val usuarios: List<Usuario> = DatosDePrueba.usuarios
) {
    private val _productos = MutableStateFlow(productosIniciales)
    val productos: StateFlow<List<Producto>> = _productos.asStateFlow()

    private val _movimientos = MutableStateFlow(movimientosIniciales)
    val movimientos: StateFlow<List<Movimiento>> = _movimientos.asStateFlow()

    private var ultimoIdMovimiento = movimientosIniciales.maxOfOrNull { it.id } ?: 0

    fun buscarProducto(codigo: String): Producto? =
        _productos.value.find { it.codigo == codigo }


    fun historialDe(codigoProducto: String): Flow<List<Movimiento>> =
        movimientos.map { lista ->
            lista.filter { it.codigoProducto == codigoProducto }
                .sortedByDescending { it.fecha }
        }

    fun agregarProducto(producto: Producto) {
        _productos.update { it + producto }
    }

    fun actualizarProducto(producto: Producto) {
        _productos.update { lista ->
            lista.map { if (it.codigo == producto.codigo) producto else it }
        }
    }

    fun registrarMovimiento(
        codigoProducto: String,
        tipo: TipoMovimiento,
        cantidad: Int,
        motivo: String?,
        usuarioId: Int,
        nuevoStock: Int
    ): Movimiento {
        val movimiento = Movimiento(
            id = ++ultimoIdMovimiento,
            codigoProducto = codigoProducto,
            tipo = tipo,
            cantidad = cantidad,
            motivo = motivo,
            usuario = usuarioId
        )
        _movimientos.update { it + movimiento }
        _productos.update { lista ->
            lista.map { if (it.codigo == codigoProducto) it.copy(stockActual = nuevoStock) else it }
        }
        return movimiento
    }
}
