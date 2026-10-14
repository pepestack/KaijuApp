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

// fuente de datos de la app solo guarda y entrega datos no valida reglas de negocio (eso es del viewmodel)
// es class y no object para poder crear uno nuevo en cada test sin estado global
// los parametros tienen valor por defecto asi en la app se usa InventarioRepository() y en tests se le pasan otros datos
class InventarioRepository(
    productosIniciales: List<Producto> = DatosDePrueba.productos,
    movimientosIniciales: List<Movimiento> = emptyList(),
    val usuarios: List<Usuario> = DatosDePrueba.usuarios
) {
    // _productos es privado y mutable solo el repository lo cambia
    // productos es publico y de solo lectura los demas solo lo observan
    // stateflow avisa a quien lo observe cada vez que cambia por eso el catalogo se actualiza solo
    private val _productos = MutableStateFlow(productosIniciales)
    val productos: StateFlow<List<Producto>> = _productos.asStateFlow()

    // mismo patron que productos pero para el historial de movimientos
    private val _movimientos = MutableStateFlow(movimientosIniciales)
    val movimientos: StateFlow<List<Movimiento>> = _movimientos.asStateFlow()

    // ultimo id usado parte desde el mayor id existente o desde 0 si no hay movimientos
    private var ultimoIdMovimiento = movimientosIniciales.maxOfOrNull { it.id } ?: 0

    // busca un producto por codigo devuelve null si no existe
    fun buscarProducto(codigo: String): Producto? =
        _productos.value.find { it.codigo == codigo }

    // historial de un producto del mas nuevo al mas viejo (rf-04)
    // map transforma el flujo cada vez que cambian los movimientos el historial se recalcula solo
    fun historialDe(codigoProducto: String): Flow<List<Movimiento>> =
        movimientos.map { lista ->
            lista.filter { it.codigoProducto == codigoProducto }
                .sortedByDescending { it.fecha }
        }

    // update no modifica la lista crea una lista nueva con el producto agregado y la reemplaza
    // ese reemplazo es lo que dispara el aviso del stateflow
    fun agregarProducto(producto: Producto) {
        _productos.update { it + producto }
    }

    // recorre la lista y cambia solo el producto con el mismo codigo los demas quedan igual
    fun actualizarProducto(producto: Producto) {
        _productos.update { lista ->
            lista.map { if (it.codigo == producto.codigo) producto else it }
        }
    }

    // recibe nuevoStock ya calculado porque el viewmodel valida y calcula y el repository solo guarda
    // guarda el movimiento y el stock juntos para que nunca queden desincronizados
    // no hay funciones para editar ni borrar movimientos por la rn-05 (trazabilidad)
    fun registrarMovimiento(
        codigoProducto: String,
        tipo: TipoMovimiento,
        cantidad: Int,
        motivo: String?,
        usuarioId: Int,
        nuevoStock: Int
    ): Movimiento {
        // ++ultimoIdMovimiento suma 1 y despues lo usa asi cada movimiento tiene id unico
        // la fecha no se pasa porque el modelo la pone sola con la hora actual
        val movimiento = Movimiento(
            id = ++ultimoIdMovimiento,
            codigoProducto = codigoProducto,
            tipo = tipo,
            cantidad = cantidad,
            motivo = motivo,
            usuario = usuarioId
        )
        _movimientos.update { it + movimiento }
        // copy crea un producto nuevo igual al anterior pero con otro stock (las data class son inmutables)
        _productos.update { lista ->
            lista.map { if (it.codigo == codigoProducto) it.copy(stockActual = nuevoStock) else it }
        }
        return movimiento
    }
}
