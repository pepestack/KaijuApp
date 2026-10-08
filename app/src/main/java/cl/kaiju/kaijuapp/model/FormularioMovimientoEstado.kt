package cl.kaiju.kaijuapp.model

data class FormularioMovimientoEstado(
    val codigoProducto: String = "",
    val tipo: TipoMovimiento? = null,
    val cantidad: String = "",
    val motivo: String = "",
    val errores: FormularioMovimientoErrores = FormularioMovimientoErrores()
)

data class FormularioMovimientoErrores(
    val producto: String? = null,
    val tipo: String? = null,
    val cantidad: String? = null,
    val motivo: String? = null
)
