package cl.kaiju.kaijuapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.kaiju.kaijuapp.model.MonsterUiState
import cl.kaiju.kaijuapp.repository.MonsterRepository
import cl.kaiju.kaijuapp.repository.RespuestaMonstruoInvalida
import com.google.gson.JsonParseException
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

class MonsterViewModel(private val repository: MonsterRepository) : ViewModel() {
    private val _estado = MutableStateFlow(MonsterUiState())
    val estado = _estado.asStateFlow()
    private var consulta: Job? = null

    fun activar() {
        if (consulta?.isActive == true) return
        // Conserva la ficha anterior mientras llega el siguiente visitante.
        _estado.update { it.copy(cargando = true, error = null) }
        consulta = viewModelScope.launch {
            try {
                _estado.value = MonsterUiState(monstruo = repository.obtenerAleatorio())
            } catch (e: CancellationException) {
                throw e
            } catch (e: HttpException) {
                error(if (e.code() == 429) "Demasiadas solicitudes. Espera antes de reintentar."
                    else "No se pudo consultar el monstruo (HTTP "+e.code()+").")
            } catch (e: RespuestaMonstruoInvalida) {
                error("La respuesta del monstruo no es valida.")
            } catch (e: JsonParseException) {
                error("La respuesta del monstruo no tiene el formato esperado.")
            } catch (e: IOException) {
                error("No se pudo conectar. Revisa internet e intenta nuevamente.")
            }
        }
    }

    fun imagenFallida(url: String, intento: Int) {
        // Ignora callbacks de una imagen retirada al reemplazar o cerrar la ficha.
        _estado.update {
            if (it.monstruo?.spriteUrl == url && it.intentoImagen == intento)
                it.copy(errorImagen = true) else it
        }
    }

    fun reintentar() {
        if (_estado.value.error != null) activar()
    }

    fun reintentarImagen() {
        if (!_estado.value.errorImagen) return
        // Conserva el JSON y vuelve a intentar solo la descarga de imagen.
        _estado.update { it.copy(errorImagen = false, intentoImagen = it.intentoImagen + 1) }
    }

    fun cerrar() {
        consulta?.cancel()
        consulta = null
        _estado.value = MonsterUiState()
    }

    private fun error(mensaje: String) {
        _estado.update { it.copy(cargando = false, error = mensaje) }
    }

    companion object {
        fun factory(repository: MonsterRepository) = viewModelFactory {
            initializer { MonsterViewModel(repository) }
        }
    }
}
