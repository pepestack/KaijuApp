package cl.kaiju.kaijuapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TipoMovimiento { ENTRADA, SALIDA_VENTA, SALIDA_CONSUMO, AJUSTE }

@Entity(tableName = "movimientos")
data class Movimiento (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val codigoProducto: String,
    val tipo: TipoMovimiento,
    val cantidad: Int,
    val motivo: String? = null,
    val fecha: Long = System.currentTimeMillis(),
    val usuario: Int
)
