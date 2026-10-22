package cl.kaiju.kaijuapp.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.common.moduleinstall.InstallStatusListener
import com.google.android.gms.common.moduleinstall.ModuleInstall
import com.google.android.gms.common.moduleinstall.ModuleInstallRequest
import com.google.android.gms.common.moduleinstall.ModuleInstallStatusUpdate
import com.google.android.gms.common.moduleinstall.ModuleInstallStatusUpdate.InstallState
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

/**
 * Escáner de Google Play services: trae su propia pantalla de cámara, así que la app no pide
 * el permiso CAMERA ni depende de que el teléfono tenga un lector propio. Solo lee UPC-A y
 * EAN-13. Devuelve una función que abre el escáner y entrega el texto leído; cancelar no hace nada.
 *
 * El escáner es un módulo que Play services descarga la primera vez que se usa. Si falta, se
 * avisa con onInstalando y al terminar con onListo; no se abre solo para no aparecer de sorpresa
 * si el usuario ya cambió de pantalla.
 */
@Composable
fun rememberEscanerCodigoBarras(
    onInstalando: () -> Unit,
    onListo: () -> Unit,
    onError: () -> Unit
): ((String) -> Unit) -> Unit {
    val context = LocalContext.current
    val onInstalandoActual by rememberUpdatedState(onInstalando)
    val onListoActual by rememberUpdatedState(onListo)
    val onErrorActual by rememberUpdatedState(onError)
    val escaner = remember(context) {
        val opciones = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_EAN_13, Barcode.FORMAT_UPC_A)
            .enableAutoZoom()
            .build()
        GmsBarcodeScanning.getClient(context, opciones)
    }
    val instalador = remember(context) { ModuleInstall.getClient(context) }

    return remember(escaner, instalador) {
        { onCodigo ->
            val escanear = {
                escaner.startScan()
                    .addOnSuccessListener { barcode -> barcode.rawValue?.let(onCodigo) }
                    .addOnFailureListener { onErrorActual() }
                Unit
            }
            // Play services avisa el avance de la descarga; solo interesa saber cuándo termina.
            val alTerminar = object : InstallStatusListener {
                override fun onInstallStatusUpdated(estado: ModuleInstallStatusUpdate) {
                    when (estado.installState) {
                        InstallState.STATE_COMPLETED -> {
                            instalador.unregisterListener(this)
                            onListoActual()
                        }
                        InstallState.STATE_FAILED, InstallState.STATE_CANCELED -> {
                            instalador.unregisterListener(this)
                            onErrorActual()
                        }
                    }
                }
            }
            instalador.areModulesAvailable(escaner)
                .addOnSuccessListener { disponibilidad ->
                    if (disponibilidad.areModulesAvailable()) {
                        escanear()
                    } else {
                        val solicitud = ModuleInstallRequest.newBuilder()
                            .addApi(escaner)
                            .setListener(alTerminar)
                            .build()
                        instalador.installModules(solicitud)
                            .addOnSuccessListener { respuesta ->
                                // Pudo terminar de instalarse entre la consulta y la solicitud.
                                if (respuesta.areModulesAlreadyInstalled()) {
                                    instalador.unregisterListener(alTerminar)
                                    escanear()
                                } else {
                                    onInstalandoActual()
                                }
                            }
                            .addOnFailureListener {
                                instalador.unregisterListener(alTerminar)
                                onErrorActual()
                            }
                    }
                }
                .addOnFailureListener { onErrorActual() }
        }
    }
}
