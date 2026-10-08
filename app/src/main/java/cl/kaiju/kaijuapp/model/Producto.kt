package cl.kaiju.kaijuapp.model

data class Producto(
    val codigo: String,
    val nombre: String,
    val descripcion: String,
    val categoria: String,
    val tipo: String,
    val precio: Int,
    val stockActual: Int,
    val stockMinimo: Int,
    val detalle: String? = null
) {
    val enAlerta: Boolean get() = stockActual <= stockMinimo
}