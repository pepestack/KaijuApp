package cl.kaiju.kaijuapp.model

import cl.kaiju.kaijuapp.data.model.MonsterDto

data class MonsterUiState(
    val cargando: Boolean = false,
    val monstruo: MonsterDto? = null,
    val error: String? = null,
    val errorImagen: Boolean = false,
    val intentoImagen: Int = 0
)
