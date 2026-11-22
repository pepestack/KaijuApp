package cl.kaiju.kaijuapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.kaiju.kaijuapp.model.CuentaUsuario
import cl.kaiju.kaijuapp.model.EventoUsuarios
import cl.kaiju.kaijuapp.model.FormularioUsuarioErrores
import cl.kaiju.kaijuapp.model.FormularioUsuarioEstado
import cl.kaiju.kaijuapp.model.ListaUsuariosEstado
import cl.kaiju.kaijuapp.model.ReglasUsuario
import cl.kaiju.kaijuapp.model.Rol
import cl.kaiju.kaijuapp.repository.ErrorBackend
import cl.kaiju.kaijuapp.repository.UsuarioRepository
import com.google.gson.JsonParseException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

// CRUD de usuarios del backend, solo para el Administrador. El backend vuelve a validar todo.
class UsuariosViewModel(private val repository: UsuarioRepository) : ViewModel() {
    private val _lista = MutableStateFlow(ListaUsuariosEstado())
    val lista = _lista.asStateFlow()
    private val _formulario = MutableStateFlow(FormularioUsuarioEstado())
    val formulario = _formulario.asStateFlow()
    private val _guardando = MutableStateFlow(false)
    val guardando = _guardando.asStateFlow()
    // Errores que no son de un campo (por ejemplo cambiar el propio rol); la vista los muestra en el snackbar.
    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje = _mensaje.asStateFlow()
    private val _evento = MutableStateFlow<EventoUsuarios?>(null)
    val evento = _evento.asStateFlow()

    fun consumirEvento() {
        _evento.value = null
    }

    fun mensajeMostrado() {
        _mensaje.value = null
    }

    fun cargar() {
        if (_lista.value.cargando) return
        _lista.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            try {
                _lista.value = ListaUsuariosEstado(usuarios = repository.listar())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _lista.update { it.copy(cargando = false, error = mensajeDe(e)) }
            }
        }
    }

    // Al cerrar sesión no queda la lista ni un formulario a medias para el siguiente perfil.
    fun limpiar() {
        _lista.value = ListaUsuariosEstado()
        _formulario.value = FormularioUsuarioEstado()
        _mensaje.value = null
        _evento.value = null
    }

    fun prepararNuevo() {
        _formulario.value = FormularioUsuarioEstado()
    }

    fun prepararEdicion(usuario: CuentaUsuario) {
        _formulario.value = FormularioUsuarioEstado(
            id = usuario.id, nombre = usuario.nombre, correo = usuario.correo, rol = usuario.rol
        )
    }

    fun actualizarNombre(valor: String) = _formulario.update {
        it.copy(nombre = valor, errores = it.errores.copy(nombre = null))
    }

    fun actualizarCorreo(valor: String) = _formulario.update {
        it.copy(correo = valor, errores = it.errores.copy(correo = null))
    }

    fun actualizarContrasena(valor: String) = _formulario.update {
        it.copy(contrasena = valor, errores = it.errores.copy(contrasena = null))
    }

    fun actualizarRol(valor: Rol) = _formulario.update {
        it.copy(rol = valor, errores = it.errores.copy(rol = null))
    }

    fun guardar() {
        if (_guardando.value) return
        val estado = _formulario.value
        val errores = validar(estado)
        _formulario.update { it.copy(errores = errores) }
        if (errores.hayErrores) return

        val nombre = estado.nombre.trim()
        val correo = ReglasUsuario.normalizarCorreo(estado.correo)
        val rol = estado.rol!!
        _guardando.value = true
        viewModelScope.launch {
            try {
                val guardado = if (estado.id == null) {
                    repository.crear(nombre, correo, estado.contrasena, rol)
                } else {
                    repository.actualizar(estado.id, nombre, correo, estado.contrasena.ifEmpty { null }, rol)
                }
                // Se actualiza la lista sin volver a pedirla al backend.
                _lista.update { lista ->
                    lista.copy(usuarios = (lista.usuarios.filter { it.id != guardado.id } + guardado).sortedBy { it.id })
                }
                _formulario.value = FormularioUsuarioEstado()
                _evento.value = EventoUsuarios.Guardado
            } catch (e: CancellationException) {
                throw e
            } catch (e: ErrorBackend) {
                val porCampo = FormularioUsuarioErrores(
                    nombre = e.detalles["nombre"], correo = e.detalles["correo"],
                    contrasena = e.detalles["contrasena"], rol = e.detalles["rol"]
                )
                _formulario.update { it.copy(errores = porCampo) }
                if (!porCampo.hayErrores) _mensaje.value = mensajeDe(e)
            } catch (e: Exception) {
                _mensaje.value = mensajeDe(e)
            } finally {
                _guardando.value = false
            }
        }
    }

    fun eliminar(usuario: CuentaUsuario) {
        if (_guardando.value) return
        _guardando.value = true
        viewModelScope.launch {
            try {
                repository.eliminar(usuario.id)
                _lista.update { lista -> lista.copy(usuarios = lista.usuarios.filter { it.id != usuario.id }) }
                _evento.value = EventoUsuarios.Eliminado(usuario.nombre)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _mensaje.value = mensajeDe(e)
            } finally {
                _guardando.value = false
            }
        }
    }

    private fun validar(estado: FormularioUsuarioEstado): FormularioUsuarioErrores {
        val nombre = estado.nombre.trim()
        val correo = ReglasUsuario.normalizarCorreo(estado.correo)
        val contrasena = estado.contrasena
        return FormularioUsuarioErrores(
            nombre = when {
                nombre.isEmpty() -> "Ingresa el nombre"
                nombre.length > ReglasUsuario.LARGO_MAXIMO -> "Máximo ${ReglasUsuario.LARGO_MAXIMO} caracteres"
                else -> null
            },
            correo = when {
                correo.isEmpty() -> "Ingresa el correo"
                !ReglasUsuario.correoValido(correo) -> "Ingresa un correo válido"
                else -> null
            },
            // Al editar, vacía significa que no se cambia.
            contrasena = when {
                contrasena.isEmpty() && !estado.esEdicion -> "Ingresa una contraseña"
                contrasena.isNotEmpty() && contrasena.length !in ReglasUsuario.largoContrasena ->
                    "Debe tener entre ${ReglasUsuario.largoContrasena.first} y ${ReglasUsuario.largoContrasena.last} caracteres"
                else -> null
            },
            rol = if (estado.rol == null) "Selecciona un rol" else null
        )
    }

    private fun mensajeDe(error: Exception): String = when {
        error is ErrorBackend && error.status == 401 -> "Tu sesión expiró. Cierra sesión y vuelve a ingresar."
        error is ErrorBackend && error.status == 403 -> "Solo el Administrador puede gestionar usuarios."
        error is ErrorBackend -> error.message ?: "El servidor rechazó la operación."
        error is IOException -> "No se pudo conectar con el servidor. Revisa tu conexión e intenta nuevamente."
        error is JsonParseException -> "El servidor respondió en un formato inesperado."
        else -> "Ocurrió un error inesperado. Intenta nuevamente."
    }

    companion object {
        fun factory(repository: UsuarioRepository) = viewModelFactory {
            initializer { UsuariosViewModel(repository) }
        }
    }
}
