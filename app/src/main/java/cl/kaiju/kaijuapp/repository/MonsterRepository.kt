package cl.kaiju.kaijuapp.repository

import cl.kaiju.kaijuapp.data.remote.MonsterApi
import java.io.IOException

class MonsterRepository(private val api: MonsterApi) {
    suspend fun obtenerAleatorio() = api.obtenerMonstruoAleatorio().also {
        if (it.id <= 0 || it.nombre.isNullOrBlank() || it.spriteUrl.isNullOrBlank() ||
            !it.spriteUrl.startsWith("https://")) throw RespuestaMonstruoInvalida()
    }
}
class RespuestaMonstruoInvalida : IOException()
