package cl.kaiju.kaijuapp.ui.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import cl.kaiju.kaijuapp.R

/**
 * Aviso que aparece solo mientras las notificaciones de stock bajo están desactivadas y sigue el
 * flujo de la Guía 11: verifica, explica si Android lo sugiere, pide el permiso y, si el usuario
 * dice que no, la app sigue (las alertas se ven igual en su pestaña). Si ya no se puede pedir
 * (rechazó dos veces, o Android 12 o menos con los avisos apagados) lleva a Ajustes.
 */
// el permiso solo se pide cuando sePuedePedir ya confirmo android 13 o mas, lint no alcanza a verlo dentro de los lambdas
@SuppressLint("InlinedApi")
@Composable
fun AvisoNotificaciones(
    onConcedido: () -> Unit,
    onSinPermiso: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    // en android 13 o mas es false si falta el permiso; en versiones anteriores refleja el interruptor de ajustes
    var activas by remember { mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled()) }
    var mostrarExplicacion by rememberSaveable { mutableStateOf(false) }
    // tras rechazar dos veces android ya no muestra el dialogo, desde ahi solo queda ir a ajustes
    var denegadoPermanente by rememberSaveable { mutableStateOf(false) }

    // se vuelve a revisar cada vez que la app pasa a primer plano por ejemplo al volver de ajustes
    // asi el aviso desaparece si el usuario los activo alla, o vuelve si los quito
    LifecycleResumeEffect(Unit) {
        activas = NotificationManagerCompat.from(context).areNotificationsEnabled()
        onPauseOrDispose { }
    }

    // el launcher recibe la respuesta del dialogo del sistema
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) {
            activas = true
            onConcedido()
        } else {
            // si despues de rechazar android ya no sugiere explicar es porque no va a volver a preguntar
            denegadoPermanente = activity != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.POST_NOTIFICATIONS)
            onSinPermiso()
        }
    }

    if (activas) return

    // el permiso solo existe desde android 13; antes los avisos se apagan a mano en ajustes
    val sePuedePedir = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !denegadoPermanente &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
        PackageManager.PERMISSION_GRANTED

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(stringResource(R.string.avisos_titulo), style = MaterialTheme.typography.titleSmall)
            Text(stringResource(R.string.avisos_texto), style = MaterialTheme.typography.bodyMedium)
            Button(onClick = {
                when {
                    // la pantalla de notificaciones de la app existe desde android 8, antes se abre la ficha de la app
                    !sePuedePedir -> context.startActivity(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        } else {
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                        }
                    )
                    // android sugiere explicar antes porque el usuario ya rechazo una vez
                    activity != null && ActivityCompat.shouldShowRequestPermissionRationale(
                        activity, Manifest.permission.POST_NOTIFICATIONS
                    ) -> mostrarExplicacion = true
                    else -> launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }) {
                Text(stringResource(if (sePuedePedir) R.string.avisos_activar else R.string.avisos_abrir_ajustes))
            }
        }
    }

    // explicacion propia el dialogo del sistema no se puede cambiar pero este si, siempre con opcion de decir que no
    if (mostrarExplicacion) {
        AlertDialog(
            onDismissRequest = { mostrarExplicacion = false },
            title = { Text(stringResource(R.string.avisos_explicacion_titulo)) },
            text = { Text(stringResource(R.string.avisos_explicacion_texto)) },
            confirmButton = {
                TextButton(onClick = {
                    mostrarExplicacion = false
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }) { Text(stringResource(R.string.avisos_continuar)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarExplicacion = false
                    onSinPermiso()
                }) { Text(stringResource(R.string.avisos_ahora_no)) }
            }
        )
    }
}
