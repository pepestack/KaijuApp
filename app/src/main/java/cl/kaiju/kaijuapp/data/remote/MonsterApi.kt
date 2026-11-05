package cl.kaiju.kaijuapp.data.remote

import cl.kaiju.kaijuapp.data.model.MonsterDto
import retrofit2.http.GET

interface MonsterApi {
    @GET("api/monster/random")
    suspend fun obtenerMonstruoAleatorio(): MonsterDto
}
