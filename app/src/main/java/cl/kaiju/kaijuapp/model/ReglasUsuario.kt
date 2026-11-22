package cl.kaiju.kaijuapp.model

/** Mismas reglas que el backend de Kaiju; validar antes evita un viaje al servidor. */
object ReglasUsuario {
    private val formatoCorreo = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
    const val LARGO_MAXIMO = 100
    val largoContrasena = 4..10

    fun correoValido(correo: String) = correo.length <= LARGO_MAXIMO && formatoCorreo.matches(correo)

    fun normalizarCorreo(correo: String) = correo.trim().lowercase()
}
