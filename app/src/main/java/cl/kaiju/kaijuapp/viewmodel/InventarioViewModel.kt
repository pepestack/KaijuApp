package cl.kaiju.kaijuapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewModelScope
import cl.kaiju.kaijuapp.model.CatalogoEstado
import cl.kaiju.kaijuapp.model.Producto
import cl.kaiju.kaijuapp.model.FormularioProductoEstado
import cl.kaiju.kaijuapp.model.FormularioProductoErrores
import cl.kaiju.kaijuapp.model.FormularioMovimientoEstado
import cl.kaiju.kaijuapp.model.FormularioMovimientoErrores
import cl.kaiju.kaijuapp.model.Rol
import cl.kaiju.kaijuapp.model.TipoMovimiento
import cl.kaiju.kaijuapp.model.Usuario
import cl.kaiju.kaijuapp.repository.InventarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InventarioViewModel(private val repository: InventarioRepository) : ViewModel() {
    private val busqueda = MutableStateFlow("")
    private val categoriaSeleccionada = MutableStateFlow<String?>(null)
    private val _estadoFormularioProducto = MutableStateFlow(FormularioProductoEstado())
    val estadoFormularioProducto = _estadoFormularioProducto.asStateFlow()
    private val _estadoFormularioMovimiento = MutableStateFlow(FormularioMovimientoEstado())
    val estadoFormularioMovimiento = _estadoFormularioMovimiento.asStateFlow()
    private val _esEdicion = MutableStateFlow(false)
    val esEdicion = _esEdicion.asStateFlow()
    private var codigoEnEdicion: String? = null
    private val _errorOperacion = MutableStateFlow<String?>(null)
    val errorOperacion = _errorOperacion.asStateFlow()

    // producto que acaba de pasar a alerta (rn-06) o null si no hay nada que avisar
    // el viewmodel no notifica porque no tiene context solo avisa y la ui muestra la notificacion
    private val _productoEnAlerta = MutableStateFlow<Producto?>(null)
    val productoEnAlerta = _productoEnAlerta.asStateFlow()

    fun prepararNuevoProducto() {
        codigoEnEdicion = null
        _esEdicion.value = false
        _estadoFormularioProducto.value = FormularioProductoEstado(stockActual = "0")
        _errorOperacion.value = null
    }

    fun prepararEdicionProducto(codigo: String): Boolean {
        val producto = repository.buscarProducto(codigo)
        if (producto == null) {
            _errorOperacion.value = "El producto ya no existe"
            return false
        }
        codigoEnEdicion = codigo
        _esEdicion.value = true
        _estadoFormularioProducto.value = FormularioProductoEstado(
            codigo = producto.codigo, nombre = producto.nombre, descripcion = producto.descripcion,
            categoria = producto.categoria, tipo = producto.tipo, precio = producto.precio.toString(),
            stockActual = producto.stockActual.toString(), stockMinimo = producto.stockMinimo.toString(),
            detalle = producto.detalle.orEmpty()
        )
        _errorOperacion.value = null
        return true
    }

    // copy conserva los otros campos; los errores se recalculan al intentar guardar.
    fun actualizarCodigo(valor: String) {
        if (!_esEdicion.value) _estadoFormularioProducto.update { it.copy(codigo = valor) }
    }
    fun actualizarNombre(valor: String) = _estadoFormularioProducto.update { it.copy(nombre = valor) }
    fun actualizarDescripcion(valor: String) = _estadoFormularioProducto.update { it.copy(descripcion = valor) }
    fun actualizarCategoria(valor: String) = _estadoFormularioProducto.update { it.copy(categoria = valor) }
    fun actualizarTipo(valor: String) = _estadoFormularioProducto.update { it.copy(tipo = valor) }
    fun actualizarPrecio(valor: String) = _estadoFormularioProducto.update { it.copy(precio = valor) }
    fun actualizarStockActual(valor: String) {
        if (!_esEdicion.value) _estadoFormularioProducto.update { it.copy(stockActual = valor) }
    }
    fun actualizarStockMinimo(valor: String) = _estadoFormularioProducto.update { it.copy(stockMinimo = valor) }
    fun actualizarDetalle(valor: String) = _estadoFormularioProducto.update { it.copy(detalle = valor) }

    fun validarFormularioProducto(): Boolean {
        val estado = _estadoFormularioProducto.value
        val codigo = estado.codigo.trim()
        val errores = FormularioProductoErrores(
            codigo = when {
                codigo.isBlank() -> "El código es obligatorio"
                codigo != codigoEnEdicion && repository.buscarProducto(codigo) != null -> "El código ya existe"
                else -> null
            },
            nombre = if (estado.nombre.trim().length < 3) "El nombre debe tener al menos 3 caracteres" else null,
            categoria = if (estado.categoria !in repository.productos.value.map { it.categoria }) "Elige una categoría de la lista" else null,
            precio = if ((estado.precio.trim().toIntOrNull() ?: 0) <= 0) "El precio debe ser un entero mayor que cero" else null,
            stockMinimo = validarStock(estado.stockMinimo),
            stockActual = if (_esEdicion.value) null else validarStock(estado.stockActual)
        )
        _estadoFormularioProducto.update { it.copy(errores = errores) }
        return errores == FormularioProductoErrores()
    }

    private fun validarStock(valor: String): String? {
        val numero = valor.trim().toIntOrNull()
        return if (numero == null || numero < 0) "Debe ser un entero mayor o igual a cero" else null
    }

    fun guardarProducto(usuario: Usuario?): Boolean {
        _errorOperacion.value = null
        if (!usuarioValido(usuario) || usuario?.rol != Rol.ADMINISTRADOR) {
            _errorOperacion.value = "Solo un administrador activo puede guardar productos"
            return false
        }
        if (!validarFormularioProducto()) return false
        val estado = _estadoFormularioProducto.value
        val anterior = codigoEnEdicion?.let(repository::buscarProducto)
        if (_esEdicion.value && anterior == null) {
            _errorOperacion.value = "El producto ya no existe"
            return false
        }
        val producto = Producto(
            codigo = anterior?.codigo ?: estado.codigo.trim(), nombre = estado.nombre.trim(),
            descripcion = estado.descripcion.trim(), categoria = estado.categoria,
            tipo = estado.tipo.trim(), precio = estado.precio.trim().toInt(),
            // RN-05: editar datos no altera el stock, aunque haya cambiado durante la edición.
            stockActual = anterior?.stockActual ?: estado.stockActual.trim().toInt(),
            stockMinimo = estado.stockMinimo.trim().toInt(), detalle = estado.detalle.trim().ifBlank { null }
        )
        if (anterior != null) {
            repository.actualizarProducto(producto)
        } else {
            repository.agregarProducto(producto.copy(stockActual = 0))
            if (producto.stockActual > 0) {
                repository.registrarMovimiento(producto.codigo, TipoMovimiento.ENTRADA,
                    producto.stockActual, "Stock inicial", usuario!!.id, producto.stockActual)
            }
        }
        prepararNuevoProducto()
        return true
    }

    fun limpiarFormularioMovimiento() {
        _estadoFormularioMovimiento.value = FormularioMovimientoEstado()
        _errorOperacion.value = null
    }
    fun actualizarProductoMovimiento(valor: String) = _estadoFormularioMovimiento.update { it.copy(codigoProducto = valor) }
    fun actualizarTipoMovimiento(valor: TipoMovimiento) = _estadoFormularioMovimiento.update { it.copy(tipo = valor) }
    fun actualizarCantidad(valor: String) = _estadoFormularioMovimiento.update { it.copy(cantidad = valor) }
    fun actualizarMotivo(valor: String) = _estadoFormularioMovimiento.update { it.copy(motivo = valor) }

    fun tiposPermitidos(rol: Rol?): List<TipoMovimiento> = when (rol) {
        Rol.ADMINISTRADOR, Rol.ENCARGADO_INVENTARIO -> TipoMovimiento.entries
        Rol.VENDEDOR -> listOf(TipoMovimiento.SALIDA_VENTA)
        null -> emptyList()
    }

    private fun usuarioValido(usuario: Usuario?): Boolean =
        usuario != null && repository.usuarios.any { it == usuario }

    fun validarFormularioMovimiento(usuario: Usuario?): Boolean {
        val estado = _estadoFormularioMovimiento.value
        val producto = repository.buscarProducto(estado.codigoProducto)
        val cantidad = estado.cantidad.trim().toIntOrNull()
        val salida = estado.tipo == TipoMovimiento.SALIDA_VENTA || estado.tipo == TipoMovimiento.SALIDA_CONSUMO
        val errores = FormularioMovimientoErrores(
            producto = if (producto == null) "Elige un producto existente" else null,
            tipo = when {
                estado.tipo == null -> "Elige un tipo de movimiento"
                estado.tipo !in tiposPermitidos(usuario?.rol) -> "Tu perfil no permite este movimiento"
                else -> null
            },
            cantidad = when {
                cantidad == null -> "La cantidad debe ser un número entero"
                cantidad <= 0 -> "La cantidad debe ser mayor que cero"
                salida && producto != null && cantidad > producto.stockActual -> "La salida supera el stock disponible"
                estado.tipo == TipoMovimiento.ENTRADA && producto != null && cantidad.toLong() + producto.stockActual > Int.MAX_VALUE -> "La entrada supera el stock máximo admitido"
                else -> null
            },
            motivo = if (estado.tipo == TipoMovimiento.AJUSTE && estado.motivo.isBlank()) "El ajuste exige un motivo" else null
        )
        _estadoFormularioMovimiento.update { it.copy(errores = errores) }
        return errores == FormularioMovimientoErrores()
    }

    fun registrarMovimiento(usuario: Usuario?): Boolean {
        _errorOperacion.value = null
        if (!usuarioValido(usuario)) {
            _errorOperacion.value = "Inicia sesión con un usuario de prueba válido"
            return false
        }
        if (!validarFormularioMovimiento(usuario)) return false
        val estado = _estadoFormularioMovimiento.value
        val producto = repository.buscarProducto(estado.codigoProducto)!!
        val cantidad = estado.cantidad.trim().toInt()
        val tipo = estado.tipo!!
        val nuevoStock = when (tipo) {
            TipoMovimiento.ENTRADA -> producto.stockActual + cantidad
            TipoMovimiento.SALIDA_VENTA, TipoMovimiento.SALIDA_CONSUMO -> producto.stockActual - cantidad
            TipoMovimiento.AJUSTE -> cantidad
        }
        // Llamada síncrona desde UI: sin launch ni suspensiones entre validar y registrar.
        repository.registrarMovimiento(producto.codigo, tipo, cantidad,
            estado.motivo.trim().ifBlank { null }, usuario!!.id, nuevoStock)
        // producto es el antes y actualizado es el despues del movimiento
        // solo se avisa cuando pasa de no alerta a alerta asi no se repite en cada salida
        val actualizado = repository.buscarProducto(producto.codigo)
        if (!producto.enAlerta && actualizado != null && actualizado.enAlerta) {
            _productoEnAlerta.value = actualizado
        }
        limpiarFormularioMovimiento()
        return true
    }

    // la ui la llama despues de mostrar la notificacion
    // vuelve a null para que al girar el celular o redibujar no se notifique dos veces
    fun notificacionMostrada() {
        _productoEnAlerta.value = null
    }

    val estadoCatalogo: StateFlow<CatalogoEstado> = combine(
        repository.productos, busqueda, categoriaSeleccionada
    ) { productos, texto, categoria ->
        crearEstado(productos, texto, categoria)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = crearEstado(repository.productos.value, "", null)
    )

    fun actualizarBusqueda(valor: String) {
        busqueda.value = valor
    }

    fun seleccionarCategoria(categoria: String?) {
        categoriaSeleccionada.value = categoria
    }

    fun limpiarFiltros() {
        busqueda.value = ""
        categoriaSeleccionada.value = null
    }

    private fun crearEstado(
        productos: List<Producto>, texto: String, categoria: String?
    ): CatalogoEstado {
        val consulta = texto.trim()
        return CatalogoEstado(
            productos = productos.filter { producto ->
                (categoria == null || producto.categoria == categoria) &&
                    (consulta.isEmpty() ||
                        producto.nombre.contains(consulta, ignoreCase = true) ||
                        producto.codigo.contains(consulta, ignoreCase = true))
            },
            busqueda = texto,
            categoriaSeleccionada = categoria,
            categorias = productos.map { it.categoria }.filter { it.isNotBlank() }
                .distinct().sorted(),
            // Las alertas abarcan todo el inventario, independiente del filtro del catálogo.
            productosEnAlerta = productos.filter { it.enAlerta }
        )
    }

    companion object {
        fun factory(repository: InventarioRepository) = viewModelFactory {
            initializer { InventarioViewModel(repository) }
        }
    }
}
