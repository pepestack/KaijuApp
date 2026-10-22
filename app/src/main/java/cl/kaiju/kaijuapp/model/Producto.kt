package cl.kaiju.kaijuapp.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey

// El índice único impide repetir un código de barras; SQLite permite varios NULL.
@Entity(tableName = "productos", indices = [Index(value = ["codigoBarras"], unique = true)])
data class Producto(
    @PrimaryKey val codigo: String,
    val nombre: String,
    val descripcion: String,
    val categoria: String,
    val tipo: String,
    val precio: Int,
    val stockActual: Int,
    val stockMinimo: Int,
    val detalle: String? = null,
    // Siempre EAN-13 normalizado (ver CodigoBarras); null si el producto no tiene.
    val codigoBarras: String? = null
) {
    @get:Ignore
    val enAlerta: Boolean get() = stockActual <= stockMinimo
}
