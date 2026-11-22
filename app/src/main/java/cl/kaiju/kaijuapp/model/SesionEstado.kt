package cl.kaiju.kaijuapp.model

/** Lo escrito en el login, sus errores por campo y la sesión activa son estados distintos. */
data class SesionEstado(
    val correo: String = "",
    val contrasena: String = "",
    val errorCorreo: String? = null,
    val errorContrasena: String? = null,
    // Correo o contraseña incorrectos: marca los dos campos con un solo mensaje, sin revelar
    // cuál falló, así no se puede averiguar qué correos están registrados.
    val errorCredenciales: String? = null,
    // Solo para lo que no es de un campo: sin conexión, demasiados intentos o respuesta inesperada.
    val errorGeneral: String? = null,
    val cargando: Boolean = false,
    val usuarioActivo: Usuario? = null
) {
    val sesionIniciada: Boolean get() = usuarioActivo != null
    val rolActivo: Rol? get() = usuarioActivo?.rol
}
