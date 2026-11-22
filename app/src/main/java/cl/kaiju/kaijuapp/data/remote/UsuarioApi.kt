package cl.kaiju.kaijuapp.data.remote

import cl.kaiju.kaijuapp.data.model.LoginRequest
import cl.kaiju.kaijuapp.data.model.LoginResponse
import cl.kaiju.kaijuapp.data.model.UsuarioDto
import cl.kaiju.kaijuapp.data.model.UsuarioRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// El CRUD exige "Authorization: Bearer <token>" de un Administrador de Kaiju.
interface UsuarioApi {
    @POST("api/v1/kaiju/auth/login")
    suspend fun iniciarSesion(@Body datos: LoginRequest): LoginResponse

    @GET("api/v1/kaiju/usuarios")
    suspend fun listar(@Header("Authorization") token: String): List<UsuarioDto>

    @POST("api/v1/kaiju/usuarios")
    suspend fun crear(@Header("Authorization") token: String, @Body datos: UsuarioRequest): UsuarioDto

    @PUT("api/v1/kaiju/usuarios/{id}")
    suspend fun actualizar(
        @Header("Authorization") token: String, @Path("id") id: Int, @Body datos: UsuarioRequest
    ): UsuarioDto

    // Responde 204 sin cuerpo; el backend solo desactiva al usuario.
    @DELETE("api/v1/kaiju/usuarios/{id}")
    suspend fun eliminar(@Header("Authorization") token: String, @Path("id") id: Int): Response<Unit>
}
