package cl.kaiju.kaijuapp.model

/** Lista de usuarios del backend que ve el Administrador. */
data class ListaUsuariosEstado(
    val usuarios: List<CuentaUsuario> = emptyList(),
    val cargando: Boolean = false,
    val error: String? = null
)

data class FormularioUsuarioErrores(
    val nombre: String? = null,
    val correo: String? = null,
    val contrasena: String? = null,
    val rol: String? = null
) {
    val hayErrores: Boolean get() = listOf(nombre, correo, contrasena, rol).any { it != null }
}

/** Sin id es crear; con id es editar y la contraseña vacía no se cambia. */
data class FormularioUsuarioEstado(
    val id: Int? = null,
    val nombre: String = "",
    val correo: String = "",
    val contrasena: String = "",
    val rol: Rol? = null,
    val errores: FormularioUsuarioErrores = FormularioUsuarioErrores()
) {
    val esEdicion: Boolean get() = id != null
}

/** Lo que la vista atiende una sola vez tras guardar o eliminar un usuario. */
sealed interface EventoUsuarios {
    data object Guardado : EventoUsuarios
    data class Eliminado(val nombre: String) : EventoUsuarios
}
