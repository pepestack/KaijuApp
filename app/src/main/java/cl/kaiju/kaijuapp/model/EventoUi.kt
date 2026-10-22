package cl.kaiju.kaijuapp.model

/** Resultado de una operación que la vista debe atender una sola vez (navegar o avisar). */
sealed interface EventoUi {
    data object ProductoGuardado : EventoUi
    data object MovimientoRegistrado : EventoUi
    data class EdicionLista(val codigo: String) : EventoUi
}
