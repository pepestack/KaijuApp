package cl.kaiju.kaijuapp

import android.app.Application
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import cl.kaiju.kaijuapp.repository.InventarioRepository

// android crea esta clase una sola vez al abrir la app y vive mientras la app este abierta
// esta registrada en el manifest con android:name=".KaijuApplication" sin eso android no la usa
// no recibe nada por constructor porque android la crea sin parametros
// es la raiz que crea las cosas y las reparte hacia abajo (inyeccion de dependencias manual)
class KaijuApplication : Application() {

    // el unico repository de toda la app asi todas las pantallas ven los mismos datos
    // aqui y no con remember porque remember se pierde al girar el celular
    // by lazy lo crea recien la primera vez que alguien lo usa y despues devuelve siempre el mismo
    val repository by lazy { InventarioRepository() }

    // se ejecuta una vez al iniciar la app
    override fun onCreate() {
        super.onCreate()
        // toda notificacion pertenece a un canal y el usuario puede silenciarlo en ajustes
        // el canal debe existir antes de la primera notificacion crearlo de nuevo no hace nada
        // se usa la version compat porque minSdk es 24 y los canales existen desde la api 26
        // la version compat revisa la version de android sola
        val canal = NotificationChannelCompat.Builder(
            CANAL_ALERTAS_STOCK,
            NotificationManagerCompat.IMPORTANCE_DEFAULT
        )
            // nombre que ve el usuario en ajustes
            .setName(getString(R.string.canal_alertas_nombre))
            .build()
        NotificationManagerCompat.from(this).createNotificationChannel(canal)
    }

    // id del canal como constante para que el notificador use el mismo sin repetir el string
    companion object {
        const val CANAL_ALERTAS_STOCK = "alertas_stock"
    }
}
