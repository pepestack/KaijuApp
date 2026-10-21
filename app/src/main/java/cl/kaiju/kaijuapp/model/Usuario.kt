package cl.kaiju.kaijuapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Rol { ADMINISTRADOR, VENDEDOR, ENCARGADO_INVENTARIO }

@Entity(tableName = "usuarios")
data class Usuario (
    @PrimaryKey val id: Int,
    val nombre: String,
    val rol: Rol
)
