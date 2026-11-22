package cl.kaiju.kaijuapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// textoBackend es el nombre del rol en la tabla kaiju_roles; Room guarda el name del enum.
enum class Rol(val textoBackend: String) {
    ADMINISTRADOR("Administrador"),
    VENDEDOR("Vendedor"),
    ENCARGADO_INVENTARIO("Encargado de inventario");

    companion object {
        fun desdeBackend(texto: String): Rol? = entries.find { it.textoBackend == texto }
    }
}

// Los usuarios viven en el backend. Room guarda una copia con id, nombre y rol para mostrar
// quién hizo cada movimiento del historial, que sí vive en el teléfono.
@Entity(tableName = "usuarios")
data class Usuario (
    @PrimaryKey val id: Int,
    val nombre: String,
    val rol: Rol
)

/** Usuario tal como lo administra el backend; el correo es su credencial de ingreso. */
data class CuentaUsuario(val id: Int, val nombre: String, val correo: String, val rol: Rol) {
    fun comoUsuario() = Usuario(id, nombre, rol)
}
