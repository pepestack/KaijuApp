package cl.kaiju.kaijuapp.model

enum class Rol { ADMINISTRADOR, VENDEDOR, ENCARGADO_INVENTARIO }

data class Usuario (
    val id: Int,
    val nombre: String,
    val rol: Rol
)