package cl.kaiju.kaijuapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.kaiju.kaijuapp.model.ReglasUsuario
import cl.kaiju.kaijuapp.model.SesionEstado
import cl.kaiju.kaijuapp.repository.ErrorBackend
import cl.kaiju.kaijuapp.repository.UsuarioRepository
import com.google.gson.JsonParseException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

// RF-01 contra el backend. Los errores de formato quedan en su campo; si el backend rechaza las
// credenciales se marcan ambos campos con un mensaje que no dice cuál falló.
class SesionViewModel(private val repository: UsuarioRepository) : ViewModel() {
    private val _estado = MutableStateFlow(SesionEstado())
    val estado: StateFlow<SesionEstado> = _estado.asStateFlow()

    fun actualizarCorreo(correo: String) {
        _estado.update { it.copy(correo = correo, errorCorreo = null, errorCredenciales = null, errorGeneral = null) }
    }

    fun actualizarContrasena(contrasena: String) {
        _estado.update {
            it.copy(contrasena = contrasena, errorContrasena = null, errorCredenciales = null, errorGeneral = null)
        }
    }

    fun iniciarSesion() {
        val actual = _estado.value
        if (actual.cargando) return
        val correo = ReglasUsuario.normalizarCorreo(actual.correo)
        val errorCorreo = when {
            correo.isEmpty() -> "Ingresa tu correo"
            !ReglasUsuario.correoValido(correo) -> "Ingresa un correo válido"
            else -> null
        }
        val errorContrasena = if (actual.contrasena.isEmpty()) "Ingresa tu contraseña" else null
        if (errorCorreo != null || errorContrasena != null) {
            _estado.update {
                it.copy(errorCorreo = errorCorreo, errorContrasena = errorContrasena, errorCredenciales = null)
            }
            return
        }

        _estado.update { it.copy(cargando = true, errorGeneral = null) }
        viewModelScope.launch {
            try {
                val usuario = repository.iniciarSesion(correo, actual.contrasena)
                // La contraseña no se guarda en el estado más de lo necesario.
                _estado.update { it.copy(cargando = false, contrasena = "", usuarioActivo = usuario) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: ErrorBackend) {
                when (e.status) {
                    // Cualquier 401 es "credenciales inválidas", aunque el backend diga qué campo falló.
                    401 -> _estado.update {
                        it.copy(cargando = false, errorCredenciales = "Correo o contraseña incorrectos")
                    }
                    429 -> fallaGeneral("Demasiados intentos. Espera un momento e intenta nuevamente.")
                    // 400: formato inválido según el backend; ahí sí se indica el campo.
                    else -> _estado.update {
                        it.copy(
                            cargando = false,
                            errorCorreo = e.detalles["correo"],
                            errorContrasena = e.detalles["contrasena"],
                            errorGeneral = if (e.detalles.isEmpty()) e.message else null
                        )
                    }
                }
            } catch (e: IOException) {
                fallaGeneral("No se pudo conectar con el servidor. Revisa tu conexión e intenta nuevamente.")
            } catch (e: JsonParseException) {
                fallaGeneral("El servidor respondió en un formato inesperado.")
            }
        }
    }

    fun cerrarSesion() {
        repository.cerrarSesion()
        _estado.value = SesionEstado()
    }

    private fun fallaGeneral(mensaje: String) {
        _estado.update { it.copy(cargando = false, errorGeneral = mensaje) }
    }

    companion object {
        fun factory(repository: UsuarioRepository) = viewModelFactory {
            initializer { SesionViewModel(repository) }
        }
    }
}
