package cl.kaiju.kaijuapp.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "productos")
data class Producto(
    @PrimaryKey val codigo: String,
    val nombre: String,
    val descripcion: String,
    val categoria: String,
    val tipo: String,
    val precio: Int,
    val stockActual: Int,
    val stockMinimo: Int,
    val detalle: String? = null
) {
    @get:Ignore
    val enAlerta: Boolean get() = stockActual <= stockMinimo
}
