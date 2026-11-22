package cl.kaiju.kaijuapp.viewmodel

import cl.kaiju.kaijuapp.data.model.LoginRequest
import cl.kaiju.kaijuapp.data.model.LoginResponse
import cl.kaiju.kaijuapp.data.model.UsuarioDto
import cl.kaiju.kaijuapp.data.model.UsuarioRequest
import cl.kaiju.kaijuapp.data.remote.UsuarioApi
import cl.kaiju.kaijuapp.database.FakeInventarioDao
import cl.kaiju.kaijuapp.model.Rol
import cl.kaiju.kaijuapp.model.SesionEstado
import cl.kaiju.kaijuapp.model.Usuario
import cl.kaiju.kaijuapp.repository.UsuarioRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class SesionViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val api = FakeUsuarioApi()
    private val dao = FakeInventarioDao(usuariosIniciales = emptyList())
    private val viewModel = SesionViewModel(UsuarioRepository(api, dao))

    @Before fun preparar() { Dispatchers.setMain(dispatcher) }
    @After fun limpiar() { Dispatchers.resetMain() }

    @Test fun camposVaciosMarcanErrorEnCadaCampoSinConsultarAlBackend() = runTest(dispatcher) {
        viewModel.iniciarSesion()
        advanceUntilIdle()

        assertNotNull(viewModel.estado.value.errorCorreo)
        assertNotNull(viewModel.estado.value.errorContrasena)
        assertNull(viewModel.estado.value.errorGeneral)
        assertEquals(0, api.intentosLogin)
    }

    @Test fun correoInexistenteMuestraElErrorEnElCampoCorreo() = runTest(dispatcher) {
        api.respuestaLogin = { throw errorHttp(401, """{"error":"Usuario no encontrado","detalles":{"correo":"No existe un usuario con este correo"}}""") }

        ingresar("nadie@duoc.cl", "1234")

        assertEquals("No existe un usuario con este correo", viewModel.estado.value.errorCorreo)
        assertNull(viewModel.estado.value.errorContrasena)
        assertNull(viewModel.estado.value.errorGeneral)
        assertFalse(viewModel.estado.value.sesionIniciada)
    }

    @Test fun contrasenaIncorrectaMuestraElErrorEnElCampoContrasena() = runTest(dispatcher) {
        api.respuestaLogin = { throw errorHttp(401, """{"error":"Contraseña incorrecta","detalles":{"contrasena":"Contraseña incorrecta"}}""") }

        ingresar("admin.kaiju@duoc.cl", "mala")

        assertNull(viewModel.estado.value.errorCorreo)
        assertEquals("Contraseña incorrecta", viewModel.estado.value.errorContrasena)
        assertFalse(viewModel.estado.value.sesionIniciada)
    }

    @Test fun loginCorrectoAbreLaSesionYCopiaElUsuarioEnRoom() = runTest(dispatcher) {
        api.respuestaLogin = {
            LoginResponse("token", UsuarioDto(2, "Esteban Hernandez", it.correo, "Encargado de inventario"))
        }

        ingresar("  Encargado.Kaiju@duoc.cl ", "1234")

        val esperado = Usuario(2, "Esteban Hernandez", Rol.ENCARGADO_INVENTARIO)
        assertEquals("encargado.kaiju@duoc.cl", api.ultimoLogin?.correo)
        assertEquals(esperado, viewModel.estado.value.usuarioActivo)
        assertEquals(Rol.ENCARGADO_INVENTARIO, viewModel.estado.value.rolActivo)
        assertEquals("", viewModel.estado.value.contrasena)
        assertEquals(esperado, dao.buscarUsuario(2))
    }

    @Test fun sinConexionMuestraUnErrorGeneral() = runTest(dispatcher) {
        api.respuestaLogin = { throw IOException("sin red") }

        ingresar("admin.kaiju@duoc.cl", "1234")

        assertNotNull(viewModel.estado.value.errorGeneral)
        assertNull(viewModel.estado.value.errorCorreo)
        assertFalse(viewModel.estado.value.cargando)
    }

    @Test fun cerrarSesionLimpiaElFormularioYLaSesion() = runTest(dispatcher) {
        api.respuestaLogin = { LoginResponse("token", UsuarioDto(1, "Admin", it.correo, "Administrador")) }
        ingresar("admin.kaiju@duoc.cl", "1234")
        assertTrue(viewModel.estado.value.sesionIniciada)

        viewModel.cerrarSesion()

        assertEquals(SesionEstado(), viewModel.estado.value)
    }

    private fun kotlinx.coroutines.test.TestScope.ingresar(correo: String, contrasena: String) {
        viewModel.actualizarCorreo(correo)
        viewModel.actualizarContrasena(contrasena)
        viewModel.iniciarSesion()
        advanceUntilIdle()
    }

    private fun errorHttp(status: Int, cuerpo: String) =
        HttpException(Response.error<Any>(status, cuerpo.toResponseBody("application/json".toMediaType())))
}

/** API de prueba: el login responde lo que diga cada test; el resto no se usa aquí. */
private class FakeUsuarioApi : UsuarioApi {
    var respuestaLogin: suspend (LoginRequest) -> LoginResponse = { error("login no configurado") }
    var intentosLogin = 0
    var ultimoLogin: LoginRequest? = null

    override suspend fun iniciarSesion(datos: LoginRequest): LoginResponse {
        intentosLogin++
        ultimoLogin = datos
        return respuestaLogin(datos)
    }

    override suspend fun listar(token: String): List<UsuarioDto> = TODO()
    override suspend fun crear(token: String, datos: UsuarioRequest): UsuarioDto = TODO()
    override suspend fun actualizar(token: String, id: Int, datos: UsuarioRequest): UsuarioDto = TODO()
    override suspend fun eliminar(token: String, id: Int): Response<Unit> = TODO()
}
