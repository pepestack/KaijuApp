package cl.kaiju.kaijuapp.repository

import cl.kaiju.kaijuapp.data.model.ErrorDto
import cl.kaiju.kaijuapp.data.model.LoginRequest
import cl.kaiju.kaijuapp.data.model.UsuarioDto
import cl.kaiju.kaijuapp.data.model.UsuarioRequest
import cl.kaiju.kaijuapp.data.remote.UsuarioApi
import cl.kaiju.kaijuapp.database.InventarioDao
import cl.kaiju.kaijuapp.model.CuentaUsuario
import cl.kaiju.kaijuapp.model.Rol
import cl.kaiju.kaijuapp.model.Usuario
import com.google.gson.Gson
import com.google.gson.JsonParseException
import retrofit2.HttpException
import retrofit2.Response

/** Lo que respondió el backend cuando rechaza algo: status, mensaje y el mensaje de cada campo. */
class ErrorBackend(
    val status: Int,
    mensaje: String,
    val detalles: Map<String, String> = emptyMap()
) : Exception(mensaje)

// Los usuarios viven en el backend; cada usuario que llega se copia en Room solo para que el
// historial muestre su nombre. Sin conexión se lanza la IOException de Retrofit tal cual.
class UsuarioRepository(private val api: UsuarioApi, private val dao: InventarioDao) {
    // El token dura lo que dura la sesión: al cerrar la app hay que volver a ingresar.
    @Volatile private var token: String? = null

    suspend fun iniciarSesion(correo: String, contrasena: String): Usuario {
        val respuesta = llamar { api.iniciarSesion(LoginRequest(correo, contrasena)) }
        val usuario = respuesta.usuario.aCuenta().comoUsuario()
        token = "Bearer ${respuesta.accessToken}"
        dao.guardarUsuarios(listOf(usuario))
        return usuario
    }

    fun cerrarSesion() {
        token = null
    }

    suspend fun listar(): List<CuentaUsuario> =
        llamar { api.listar(token()) }.map { it.aCuenta() }.also { lista ->
            dao.guardarUsuarios(lista.map { it.comoUsuario() })
        }

    suspend fun crear(nombre: String, correo: String, contrasena: String, rol: Rol): CuentaUsuario =
        llamar { api.crear(token(), UsuarioRequest(nombre, correo, contrasena, rol.textoBackend)) }
            .aCuenta().also { dao.guardarUsuarios(listOf(it.comoUsuario())) }

    // contrasena null deja la contraseña actual.
    suspend fun actualizar(id: Int, nombre: String, correo: String, contrasena: String?, rol: Rol): CuentaUsuario =
        llamar { api.actualizar(token(), id, UsuarioRequest(nombre, correo, contrasena, rol.textoBackend)) }
            .aCuenta().also { dao.guardarUsuarios(listOf(it.comoUsuario())) }

    // El backend lo desactiva; la copia en Room se queda para no perder el nombre en el historial.
    suspend fun eliminar(id: Int) {
        val respuesta = llamar { api.eliminar(token(), id) }
        if (!respuesta.isSuccessful) throw errorDe(respuesta)
    }

    private fun token() = token ?: throw ErrorBackend(401, "Debes iniciar sesión")

    private suspend fun <T> llamar(peticion: suspend () -> T): T = try {
        peticion()
    } catch (e: HttpException) {
        throw errorDe(e.response())
    }

    private fun errorDe(respuesta: Response<*>?): ErrorBackend {
        val status = respuesta?.code() ?: 0
        val cuerpo = try {
            respuesta?.errorBody()?.string()?.let { Gson().fromJson(it, ErrorDto::class.java) }
        } catch (e: JsonParseException) {
            null
        }
        return ErrorBackend(
            status,
            cuerpo?.error ?: "El servidor respondió con un error (HTTP $status)",
            cuerpo?.detalles.orEmpty()
        )
    }

    private fun UsuarioDto.aCuenta(): CuentaUsuario {
        val rol = Rol.desdeBackend(rol) ?: throw ErrorBackend(0, "El servidor respondió un rol desconocido")
        return CuentaUsuario(id, nombre, correo, rol)
    }
}
