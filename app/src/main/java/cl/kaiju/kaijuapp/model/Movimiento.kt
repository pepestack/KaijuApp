package cl.kaiju.kaijuapp.model

import java.time.LocalDateTime

enum class TipoMovimiento { ENTRADA, SALIDA_VENTA, SALIDA_CONSUMO, AJUSTE }

data class Movimiento (
    val id: Int,
    val codigoProducto: String,
    val tipo: TipoMovimiento,
    val cantidad: Int,
    val motivo: String? = null,
    val fecha: Long = System.currentTimeMillis(),
    val usuario: Int
)