package cl.kaiju.kaijuapp.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import cl.kaiju.kaijuapp.R

/** Campo de texto con ícono y error visible; el mensaje de error lo decide el ViewModel. */
@Composable
fun CampoFormulario(
    valor: String,
    onValorChange: (String) -> Unit,
    etiqueta: String,
    icono: ImageVector,
    modifier: Modifier = Modifier,
    error: String? = null,
    ayuda: String? = null,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    soloLectura: Boolean = false,
    lineasMaximas: Int = 1,
    // Botón opcional al final del campo (por ejemplo escanear); el ícono de error tiene prioridad.
    accion: (@Composable () -> Unit)? = null
) {
    val textoApoyo = error ?: ayuda
    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(etiqueta) },
        leadingIcon = { Icon(icono, contentDescription = null) },
        trailingIcon = if (error != null) {
            { Icon(Icons.Filled.Warning, contentDescription = stringResource(R.string.campo_con_error)) }
        } else accion,
        isError = error != null,
        supportingText = if (textoApoyo != null) {
            { Text(textoApoyo) }
        } else null,
        readOnly = soloLectura,
        singleLine = lineasMaximas == 1,
        maxLines = lineasMaximas,
        keyboardOptions = KeyboardOptions(keyboardType = tipoTeclado, imeAction = ImeAction.Next)
    )
}
