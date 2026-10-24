# KaijuApp — Grupo Vio Coders

## Integrantes participantes

| Nombre completo | Correo |
| --- | --- |
| José Valentín Calderón González | rjvc97@hotmail.com y jv.calderon@duocuc.cl |
| Sebastian Andres Apablaza Perez | se.apablazap@duocuc.cl |
| Sebastian Moises Navarro Gonzales | seba.navarro@duocuc.cl |

**Caso:**

Kaiju desarrolla actividades vinculadas al comercio, los servicios y la gestión de productos. La necesidad principal es mejorar el control y trazabilidad de inventario. La solución propuesta es un MVP de una aplicación movil de control de inventario con perfiles y espacio a futura escalabilidad e integración con servicios externos.

**Arquitectura:** Kotlin + Jetpack Compose, patrón MVVM.

**Recursos:** La aplicación implementa dos recursos, notificaciones y un escaner de codigo de barra.

| Necesidad del caso | Recurso | Permiso y tipo | RF que cumple | Qué pasa si el usuario dice que no |
|---|---|---|---|---|
| El encargado de inventario y el Administrador requieren ser avisados de alertas de stock critico | NotificationChannelCompat, NotificationCompat.Builder y NotificationManagerCompat.notify(), PendingIntent abre la app al tocar el aviso | POST_NOTIFICATIONS, permiso en tiempo de ejecución | [RF-xx] | La aplicación funciona normal y las alertas igual se muestran dentro de la App. El usuario tiene que ir a Ajustes en Android para activarlas |
| Registrar movimientos y cargar productos rapidamente leyendo el código de barras del producto (EAN-13 / UPC-A) | Google code scanner (play-services-code-scanner, GmsBarcodeScanning). La interfaz de cámara proviene de Google Play services. | Ninguno, porque la cámara la usa Google Play services y no la app. | [] | Si se cancela el escaneo, o aun no esta disponible el módulo, se muestra un snackbar con mensaje. Siempre está habilitada la opción de ingresar el código a mano |


## Diseño y planificación
Sketch, wireframe, mockup y demás material de diseño del equipo:
👉 [Carpeta de Drive del equipo](https://drive.google.com/drive/folders/1WHd4rQCI52z48mTNynwqBaFVcs_YqF-e?usp=sharing)
