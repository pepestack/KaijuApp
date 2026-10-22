package cl.kaiju.kaijuapp.notificaciones

import cl.kaiju.kaijuapp.model.Producto

/** El ViewModel decide cuándo avisar sin conocer Context ni las notificaciones de Android. */
fun interface AvisoStock {
    fun stockBajo(producto: Producto)
}
