package cl.kaiju.kaijuapp.navigation

import android.net.Uri

// todas las rutas de la app en un solo lugar
// sealed class hace que si escribes mal una ruta el error salga al compilar y no con la app corriendo
sealed class Rutas (val ruta: String) {

    // rutas sin argumento
    data object Catalogo : Rutas("catalogo")
    data object Login : Rutas("login")
    data object Alertas : Rutas("alertas")

    // {codigo} es un hueco obligatorio el patron detalle/{codigo} acepta detalle/P008
    // crearRuta arma el string concreto para que nadie lo escriba a mano
    // Uri.encode escapa / ? & # % del codigo asi la ruta no se rompe y navigation lo decodifica solo
    data object Detalle : Rutas("detalle/{codigo}") {
        fun crearRuta(codigo: String) = "detalle/${Uri.encode(codigo)}"
    }

    // sin argumento porque la pantalla de movimiento tiene su propio selector de producto
    data object Movimiento : Rutas("movimiento")

    // ?codigo={codigo} es un argumento opcional
    // sin codigo es crear producto y con codigo es editar ese producto
    // si es null no se agrega nada porque si no quedaria el texto "null" en la ruta
    data object Formulario : Rutas("formulario?codigo={codigo}") {
        fun crearRuta(codigo: String? = null) = if(codigo == null) "formulario" else "formulario?codigo=${Uri.encode(codigo)}"
    }

}
