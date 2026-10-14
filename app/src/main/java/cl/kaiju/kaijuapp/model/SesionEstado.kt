package cl.kaiju.kaijuapp.model

/** La selección del formulario y la sesión activa son estados distintos. */
data class SesionEstado(
    val usuarioSeleccionado: Usuario? = null,
    val usuarioActivo: Usuario? = null,
    val errorUsuario: String? = null
) {
    val sesionIniciada: Boolean get() = usuarioActivo != null
    val rolActivo: Rol? get() = usuarioActivo?.rol
}
