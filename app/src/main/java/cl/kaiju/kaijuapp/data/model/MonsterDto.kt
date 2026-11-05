package cl.kaiju.kaijuapp.data.model

// JSON reducido de AWS. La clave de Divine Pride permanece en el servidor.
data class MonsterDto(val id: Int, val nombre: String, val nivel: Int,
    val raza: String, val elemento: String, val vida: Int, val spriteUrl: String)
