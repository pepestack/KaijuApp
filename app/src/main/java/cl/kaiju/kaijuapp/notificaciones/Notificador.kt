package cl.kaiju.kaijuapp.notificaciones

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import cl.kaiju.kaijuapp.KaijuApplication
import cl.kaiju.kaijuapp.MainActivity
import cl.kaiju.kaijuapp.R
import cl.kaiju.kaijuapp.model.Producto

// muestra las notificaciones locales de stock bajo (rf-09)
// esta fuera del viewmodel porque necesita context y el viewmodel no debe tenerlo
// (un context guardado en el viewmodel puede dejar viva una activity que ya se cerro)
class Notificador(private val context: Context) {

    fun alertaStock(producto: Producto) {
        // desde android 13 (api 33) notificar exige el permiso post_notifications
        // si el usuario lo rechazo no se hace nada y la alternativa es la pantalla de alertas
        // en versiones anteriores el permiso no existe y se puede notificar siempre
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        // al tocar la notificacion se abre la app
        // pendingintent es un intent que otra app (el sistema) ejecuta en nombre de la nuestra
        // flag_immutable es obligatorio desde android 12 asi nadie puede modificarlo
        val intent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val alTocar = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // el builder compat funciona igual en todas las versiones
        // usa el mismo id de canal que se creo en KaijuApplication
        val notificacion = NotificationCompat.Builder(context, KaijuApplication.CANAL_ALERTAS_STOCK)
            // icono chico de la barra de estado es obligatorio sin el la app se cae
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(context.getString(R.string.notificacion_stock_titulo, producto.nombre))
            .setContentText(
                context.getString(R.string.notificacion_stock_texto, producto.stockActual, producto.stockMinimo)
            )
            .setContentIntent(alTocar)
            // se borra sola al tocarla
            .setAutoCancel(true)
            .build()

        // el id es el hash del codigo asi cada producto tiene su propia notificacion
        // si el mismo producto vuelve a alerta reemplaza la anterior en vez de apilar otra
        NotificationManagerCompat.from(context).notify(producto.codigo.hashCode(), notificacion)
    }
}
