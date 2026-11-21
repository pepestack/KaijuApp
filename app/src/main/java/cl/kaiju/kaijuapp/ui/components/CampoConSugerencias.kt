package cl.kaiju.kaijuapp.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import cl.kaiju.kaijuapp.R

/**
 * Campo de texto libre que sugiere valores existentes mientras se escribe.
 * A diferencia de SelectorDesplegable acepta valores nuevos; validar le toca al ViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoConSugerencias(
    valor: String,
    onValorChange: (String) -> Unit,
    sugerencias: List<String>,
    etiqueta: String,
    icono: ImageVector,
    modifier: Modifier = Modifier,
    error: String? = null,
    ayuda: String? = null
) {
    var expandido by remember { mutableStateOf(false) }
    val textoApoyo = error ?: ayuda
    // Sin escribir nada se muestran todas; si el texto ya es una sugerencia exacta no se repite.
    val filtradas = sugerencias.filter {
        it.contains(valor.trim(), ignoreCase = true) && !it.equals(valor.trim(), ignoreCase = true)
    }

    ExposedDropdownMenuBox(
        expanded = expandido && filtradas.isNotEmpty(),
        onExpandedChange = { expandido = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = valor,
            onValueChange = {
                onValorChange(it)
                expandido = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable),
            label = { Text(etiqueta) },
            leadingIcon = { Icon(icono, contentDescription = null) },
            // El ícono de error convive con la flecha del menú; antes la flecha lo reemplazaba.
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (error != null) {
                        Icon(Icons.Filled.Warning, contentDescription = stringResource(R.string.campo_con_error))
                    }
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido && filtradas.isNotEmpty())
                }
            },
            isError = error != null,
            supportingText = if (textoApoyo != null) {
                { Text(textoApoyo) }
            } else null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )
        ExposedDropdownMenu(
            expanded = expandido && filtradas.isNotEmpty(),
            onDismissRequest = { expandido = false }
        ) {
            filtradas.forEach { sugerencia ->
                DropdownMenuItem(
                    text = { Text(sugerencia) },
                    onClick = {
                        onValorChange(sugerencia)
                        expandido = false
                    }
                )
            }
        }
    }
}
