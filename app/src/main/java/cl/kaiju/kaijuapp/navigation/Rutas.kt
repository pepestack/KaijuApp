package cl.kaiju.kaijuapp.navigation

sealed class Rutas (val ruta: String) {

    object Catalogo : Rutas("catalogo")
    object Login : Rutas("login")
    object Alertas : Rutas("alertas")
    object Detalle : Rutas("detalle/{codigo}") {
        fun crearRuta(codigo: String) = "detalle/$codigo"
    }
    object Movimiento : Rutas("movimiento/{codigo}")

    object Formulario : Rutas("formulario?codigo={codigo}") {
        fun crearRuta(codigo: String? = null) = if(codigo == null) "formulario" else "formulario?codigo=$codigo"
    }

}