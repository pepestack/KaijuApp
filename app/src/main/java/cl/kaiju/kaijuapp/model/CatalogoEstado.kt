package cl.kaiju.kaijuapp.model

data class CatalogoEstado(
    val productos: List<Producto> = emptyList(),
    val busqueda: String = "",
    val categoriaSeleccionada: String? = null,
    val categorias: List<String> = emptyList(),
    val productosEnAlerta: List<Producto> = emptyList()
)
