package cl.kaiju.kaijuapp.model

data class FormularioProductoEstado(
    val codigo: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val categoria: String = "",
    val tipo: String = "",
    val precio: String = "",
    val stockActual: String = "",
    val stockMinimo: String = "",
    val detalle: String = "",
    val errores: FormularioProductoErrores = FormularioProductoErrores()
)

data class FormularioProductoErrores(
    val codigo: String? = null,
    val nombre: String? = null,
    val categoria: String? = null,
    val precio: String? = null,
    val stockMinimo: String? = null
)
