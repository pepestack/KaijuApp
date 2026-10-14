package cl.kaiju.kaijuapp.viewmodel

import androidx.lifecycle.ViewModel
import cl.kaiju.kaijuapp.model.SesionEstado
import cl.kaiju.kaijuapp.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


class SesionViewModel : ViewModel() {
    private val _estado = MutableStateFlow(SesionEstado())
    val estado: StateFlow<SesionEstado> = _estado.asStateFlow()

    fun seleccionarUsuario(usuario: Usuario?) {
        _estado.update {
            it.copy(usuarioSeleccionado = usuario, errorUsuario = null)
        }
    }

    fun iniciarSesion(): Boolean {
        val usuario = _estado.value.usuarioSeleccionado
        if (usuario == null) {
            _estado.update { it.copy(errorUsuario = "Selecciona un usuario") }
            return false
        }

        _estado.update {
            it.copy(usuarioActivo = usuario, errorUsuario = null)
        }
        return true
    }

    fun cerrarSesion() {
        _estado.value = SesionEstado()
    }
}
