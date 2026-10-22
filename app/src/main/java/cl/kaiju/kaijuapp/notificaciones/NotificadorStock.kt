package cl.kaiju.kaijuapp.notificaciones

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import cl.kaiju.kaijuapp.KaijuApplication
import cl.kaiju.kaijuapp.R
import cl.kaiju.kaijuapp.model.Producto

// implementacion real del aviso publica la notificacion en el canal que crea KaijuApplication
class NotificadorStock(private val context: Context) : AvisoStock {

    // el permiso se revisa a mano al inicio lint no alcanza a verlo por la condicion de version
    @SuppressLint("MissingPermission")
    override fun stockBajo(producto: Producto) {
        // desde android 13 hay que tener el permiso sin el la notificacion no se publica y la app sigue normal
        val sinPermiso = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        val manager = NotificationManagerCompat.from(context)
        if (sinPermiso || !manager.areNotificationsEnabled()) return

        // el intent del launcher trae al frente la tarea que ya existe asi no se pierde la sesion
        val abrirApp = context.packageManager.getLaunchIntentForPackage(context.packageName)?.let {
            PendingIntent.getActivity(context, 0, it, PendingIntent.FLAG_IMMUTABLE)
        }
        val notificacion = NotificationCompat.Builder(context, KaijuApplication.CANAL_ALERTAS_STOCK)
            .setSmallIcon(R.drawable.ic_notificacion_stock)
            .setContentTitle(context.getString(R.string.notificacion_stock_titulo, producto.nombre))
            .setContentText(context.resources.getQuantityString(
                R.plurals.notificacion_stock_texto, producto.stockActual, producto.stockActual, producto.stockMinimo
            ))
            .setContentIntent(abrirApp)
            // se borra sola al tocarla
            .setAutoCancel(true)
            .build()
        // un id por producto si vuelve a avisar reemplaza la anterior en vez de apilar otra
        manager.notify(producto.codigo.hashCode(), notificacion)
    }
}
