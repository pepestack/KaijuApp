package cl.kaiju.kaijuapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewModelScope
import cl.kaiju.kaijuapp.model.CatalogoEstado
import cl.kaiju.kaijuapp.model.Producto
import cl.kaiju.kaijuapp.repository.InventarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** Recibe el repositorio compartido del equipo; no crea otra copia de los datos. */
class InventarioViewModel(private val repository: InventarioRepository) : ViewModel() {
    private val busqueda = MutableStateFlow("")
    private val categoriaSeleccionada = MutableStateFlow<String?>(null)

    // combine recalcula el catálogo cuando cambian datos o filtros, sin listas duplicadas.
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

    /** null representa todas las categorías; las opciones vienen de los productos. */
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
        /** Permite a AppNavHost obtener el ViewModel con el repositorio compartido. */
        fun factory(repository: InventarioRepository) = viewModelFactory {
            initializer { InventarioViewModel(repository) }
        }
    }
}
