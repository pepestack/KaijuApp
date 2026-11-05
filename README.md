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

## Funcionalidades implementadas

| Requerimiento | Funcionalidad |
| --- | --- |
| RF-01 | Selección de un usuario de prueba y acceso según su perfil con sus distintos privilegios. |
| RF-02 | Catálogo con nombre, categoría, precio y cantidad disponible. |
| RF-03 | Búsqueda por nombre o código y filtrado por categoría. |
| RF-04 | Detalle de productos con historial de movimientos. |
| RF-05 | Creación y edición de productos mediante un formulario validado. |
| RF-06 | Registro de entradas de mercadería. |
| RF-07 | Registro de salidas por venta o consumo, según el perfil. |
| RF-08 | Ajustes de inventario con motivo obligatorio. |
| RF-09 | Identificación de productos con stock bajo y pantalla de alertas. |
| RF-10 | Persistencia local de productos, usuarios y movimientos al cerrar y volver a abrir la aplicación. |

También se implementan cierre de sesión, lectura de códigos de barras y notificaciones de stock bajo.

### Perfiles disponibles

- **Administrador:** consulta el inventario, crea y edita productos, registra movimientos y revisa alertas.
- **Encargado de inventario:** consulta el inventario, registra movimientos y revisa alertas.
- **Vendedor:** consulta productos y registra salidas por venta.

### Validaciones principales

- Código de producto obligatorio y único.
- Nombre, categoría y precio sujetos a validación.
- Precio mayor que cero y existencias no negativas.
- Control de salidas para evitar descontar más unidades de las disponibles.
- Motivo obligatorio al registrar un ajuste.
- Validación del código de barras cuando se ingresa.
- Restricción de operaciones según el perfil activo.

En un ajuste, la cantidad ingresada corresponde al stock final contado.

## Recursos nativos del dispositivo

| Necesidad del caso | Recurso utilizado | Permisos | Relación con los requerimientos | Alternativa o comportamiento |
| --- | --- | --- | --- | --- |
| Avisar al administrador y al encargado cuando un producto pasa a tener stock bajo. | Notificaciones del sistema mediante NotificationCompat y un canal de notificaciones. | POST_NOTIFICATIONS en Android 13 o superior. | Complementa RF-09. | Si las notificaciones están desactivadas, las alertas siguen disponibles dentro de la aplicación. El permiso puede habilitarse desde los ajustes de Android. |
| Agilizar la identificación de productos y el ingreso de códigos de barras. | Cámara mediante Google Code Scanner, compatible con EAN-13 y UPC-A. | La aplicación no solicita permiso CAMERA: Google Play services administra la interfaz de captura. | Complementa la gestión de productos y el registro de movimientos de RF-05 a RF-08. | El ingreso y la selección manual siguen disponibles. Cancelar el escaneo permite continuar sin modificar los datos. |

Para utilizar el escáner se requiere un dispositivo compatible con Google Play services. La primera utilización puede requerir conexión a Internet para descargar el módulo de escaneo.

## Persistencia local

Los datos se almacenan en una base de datos SQLite administrada mediante Room.

- Los datos de prueba se cargan al crear la base de datos por primera vez.
- Los cambios se conservan al cerrar y volver a abrir la aplicación.
- Las operaciones que modifican existencias y registran movimientos utilizan transacciones para mantener la consistencia.
- No se requiere un servidor para gestionar el inventario local.

Desinstalar la aplicación o borrar sus datos elimina la información almacenada localmente.

## Cómo ejecutar el proyecto

1. Clonar o descargar este repositorio.
2. Abrir la carpeta raíz del proyecto en Android Studio.
3. Esperar la sincronización de Gradle y la descarga de las dependencias.
4. Instalar los componentes del SDK que solicite Android Studio.
5. Conectar un dispositivo con Android 7.0 —API 24— o superior y habilitar la depuración USB, o utilizar un emulador compatible.
6. Seleccionar el módulo `app` y ejecutar la aplicación.
7. En la pantalla inicial, seleccionar un usuario de prueba e ingresar.

Se necesita conexión a Internet para la preparación inicial del proyecto. Para probar el escáner se recomienda utilizar un teléfono con cámara y Google Play services.

## Pruebas realizadas

- Ejecución de 20 pruebas unitarias.
- Ejecución de 4 pruebas instrumentadas de persistencia con Room.
- Comprobación manual en un dispositivo físico con Android 11.
- Revisión de selección de perfiles, navegación, formularios, movimientos, alertas y cierre de sesión.
- Comprobación de persistencia después de cerrar y volver a abrir la aplicación.
- Prueba de lectura de códigos de barras y recepción de notificaciones.
- Pruebas con el emulador integrado de android studio.

## Diseño y planificación
Sketch, wireframe, mockup y demás material de diseño del equipo:
👉 [Carpeta de Drive del equipo](https://drive.google.com/drive/folders/1WHd4rQCI52z48mTNynwqBaFVcs_YqF-e?usp=sharing)

# Integración de API REST externa — Easter Egg de Ragnarok Online

## 1. Descripción de la funcionalidad

Como parte de la actividad EA3 3.1, el equipo decidió desarrollar la **Opción B: integración de una API a elección**.

Para ello, se incorporó a Kaiju una funcionalidad complementaria (*Easter Egg*) relacionada con el videojuego Ragnarok Online.

El objetivo es permitir que el usuario al ingresar una inserción específica de datos, se visualice información de un monstruo seleccionado aleatoriamente, utilizando datos obtenidos desde una API externa.

La información que se muestra incluye:

- ID del monstruo.
- Nombre.
- Nivel.
- Raza.
- Elemento.
- Puntos de vida.
- Imagen del monstruo en formato PNG.

Para implementar esta funcionalidad, se desarrolló un backend propio utilizando Spring Boot, alojado en una instancia de AWS EC2 con Ubuntu.

## 2. API externa seleccionada: Divine Pride

Se seleccionó **Divine pride**, una plataforma que proporciona información sobre Ragnarok Online y ofrece una API REST para consultar datos de monstruos, objetos y otros recursos del juego.

Sitio web: https://www.divine-pride.net/

La API permite consultar un monstruo mediante su identificador:

`GET https://www.divine-pride.net/api/database/Monster/{id}`

Por ejemplo, el monstruo Poring corresponde al ID `1002`.

La respuesta de Divine Pride contiene información detallada en formato JSON, incluyendo estadísticas, habilidades, objetos que puede entregar y ubicaciones donde aparece.

Sin embargo, para nuestra aplicación solamente necesitamos una parte de esos datos.

### Autenticación mediante API key

Para acceder a Divine Pride se obtuvo una API key desde una cuenta propia del servicio.

Esta clave identifica las solicitudes realizadas a la API y debe mantenerse privada.

Por razones de seguridad, decidimos **no incorporar la API key directamente en la aplicación Android**, sino almacenarla en el servidor AWS para que Spring Boot se encargue de utilizarla.

De esta manera, los usuarios pueden consultar información desde Kaiju sin recibir ni conocer la clave privada.

## 3. Configuración del servidor AWS EC2

Para alojar nuestro backend utilizamos una instancia de Amazon EC2 con Ubuntu.

En este servidor instalamos Java 21 y configuramos un proyecto Spring Boot con Maven.

El backend se encarga de recibir las solicitudes realizadas por Kaiju, consultar Divine Pride cuando sea necesario y devolver únicamente la información requerida por la aplicación.

### 3.1. Problema de la dirección IPv4 pública

Durante la configuración identificamos que la dirección IPv4 pública de una instancia EC2 puede cambiar después de detenerla y volver a iniciarla, si no se utiliza una dirección IP estática reservada.

Esto representa un problema para una aplicación móvil, porque no resulta conveniente modificar su URL de conexión cada vez que cambia la IP del servidor.

Para resolverlo, utilizamos un servicio de DNS dinámico.

### 3.2. Configuración de DuckDNS

Se utilizó **DuckDNS**, un servicio de DNS dinámico que permite asociar un nombre de dominio a una dirección IPv4 pública.

Registramos el siguiente subdominio:

`apisnecoarc.duckdns.org`

Este dominio apunta a la dirección pública de nuestra instancia AWS.

De esta manera, Kaiju utiliza un nombre de dominio estable en lugar de conectarse directamente a una dirección IP que podría cambiar.

### 3.3. Actualización automática de la dirección IP

Para evitar actualizar manualmente la dirección IPv4 en DuckDNS, configuramos un script de actualización en Ubuntu.

El procedimiento fue el siguiente:

1. Crear el subdominio en DuckDNS.
2. Asociarlo a la dirección IPv4 pública de la instancia EC2.
3. Configurar un script que consulta la dirección pública y actualiza el registro de DuckDNS.
4. Guardar el token de DuckDNS en un archivo de configuración protegido.
5. Crear un servicio y un temporizador de `systemd` para ejecutar automáticamente la actualización.

El temporizador fue configurado para realizar actualizaciones periódicas, aproximadamente cada cinco minutos, además de ejecutarse tras el arranque.

Finalmente, verificamos que la dirección resuelta mediante DuckDNS coincidiera con la IPv4 pública de nuestra instancia.

Con esta solución, si la dirección cambia, el sistema puede actualizar el dominio sin necesidad de modificar la configuración de la aplicación Android.

## 4. Desarrollo del backend con Spring Boot

Se desarrolló una API REST utilizando **Java 21 y Spring Boot**, con el propósito de actuar como intermediaria entre Kaiju y Divine Pride.

Se organizaron las responsabilidades utilizando distintas capas:

### Controller

`MonsterController` recibe las solicitudes HTTP realizadas desde Postman o la aplicación Android.

Se implementaron dos endpoints principales:

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/monster/test` | Devuelve la información del Poring para comprobar la conexión. |
| GET | `/api/monster/random` | Selecciona aleatoriamente un monstruo de una lista de IDs configurados. |

### Service

`MonsterService` contiene la lógica de selección de monstruos.

Entre sus responsabilidades se encuentran:

- Seleccionar aleatoriamente un ID de monstruo.
- Comprobar si sus datos están almacenados en caché.
- Solicitar información a Divine Pride cuando no existe en caché.
- Seleccionar los atributos necesarios para construir una respuesta simplificada.
- Controlar la frecuencia de solicitudes externas.

### Client

`DivinePrideClient` utiliza `RestClient` de Spring Boot para realizar solicitudes HTTP a Divine Pride.

Este componente obtiene la API key desde una variable de entorno configurada en Ubuntu y la incorpora a las solicitudes dirigidas al proveedor.

También se configuraron tiempos máximos de espera y manejo de errores de comunicación.

### Ejemplo de respuesta de nuestro backend

```json
{
  "id": 1002,
  "nombre": "Poring",
  "nivel": 1,
  "raza": "Plant",
  "elemento": "Water 1",
  "vida": 55,
  "spriteUrl": "https://static.divine-pride.net/images/mobs/png/1002.png"
}
```

Esta respuesta contiene solamente siete propiedades, en lugar de enviar el JSON completo de Divine Pride.

## 5. ¿Por qué utilizamos un backend intermediario?

Aunque Android permite consultar APIs REST directamente mediante Retrofit, decidimos desarrollar un backend propio principalmente por razones de seguridad y control.

**Protección de credenciales:** la API key de Divine Pride permanece en AWS y no se incorpora al APK ni al repositorio público.

**Reducción de información:** Spring Boot selecciona únicamente los atributos que necesita Kaiju, evitando enviar datos innecesarios.

**Caché:** los monstruos consultados se almacenan temporalmente en memoria. Si otro usuario solicita el mismo monstruo, el backend puede devolver sus datos sin repetir la consulta externa.

**Control de solicitudes:** Divine Pride establece restricciones sobre la frecuencia y simultaneidad de las consultas. Nuestro servicio incorpora un intervalo mínimo de 1,5 segundos entre solicitudes externas nuevas y evita ejecutarlas en paralelo dentro de la instancia del servicio. (la restricción de divine pride solicitaba 1 segundo, ya que según dicen, llegaban a tener al rededor de 300 peticiones por segundo)

**Manejo de errores:** se incorporaron mecanismos para controlar errores HTTP, tiempos de espera y respuestas `429 Too Many Requests`, considerando la cabecera `Retry-After` cuando está disponible.

Estas medidas permiten mantener el consumo de la API externa centralizado en nuestro servidor.

## 6. Configuración de HTTPS mediante Caddy

Inicialmente, el backend Spring Boot se encontraba disponible mediante HTTP a través del puerto `8080`.

Posteriormente se decidió configurar HTTPS para cifrar las comunicaciones entre la aplicación Android y nuestro servidor.

Para ello instalamos **Caddy**, un servidor web que funciona como proxy inverso.

Caddy recibe las peticiones HTTPS y las redirige hacia Spring Boot, que continúa escuchando en el puerto `8080`.

Se configuró el dominio `apisnecoarc.duckdns.org` y se obtuvo automáticamente un certificado TLS válido mediante Let's Encrypt.

La instalación quedó administrada mediante `systemd`, permitiendo que Caddy se inicie automáticamente con Ubuntu y gestione la renovación de certificados.

### Flujo de comunicación

```text
Usuario
   |
   v
Kaiju (Android)
   |
   | HTTPS
   v
apisnecoarc.duckdns.org
   |
   v
Caddy (puerto 443)
   |
   | HTTP local
   v
Spring Boot (puerto 8080)
   |
   | API key privada
   | HTTPS
   v
Divine Pride
   |
   v
Respuesta JSON
```

La conexión desde Android hasta Caddy utiliza HTTPS, y la comunicación entre Caddy y Spring Boot se realiza dentro de la misma instancia.

### Endpoint público para Kaiju

`GET https://apisnecoarc.duckdns.org/api/monster/random`

Este endpoint fue probado correctamente mediante Postman, recibiendo respuestas HTTP `200 OK` con información real de monstruos.

**Nota de infraestructura:** actualmente Spring Boot se inicia manualmente mediante un script que carga la API key desde un archivo protegido. El inicio automático del backend y la restricción definitiva del acceso público directo al puerto `8080` quedan como mejoras pendientes.

## 7. Integración con la aplicación Android

**Sección pendiente de completar con la implementación realizada en Kaiju.**

La integración deberá describir las responsabilidades de:

- Retrofit y la interfaz de consumo de la API.
- DTO de monstruos en Kotlin.
- Repository.
- ViewModel y StateFlow.
- Pantalla Jetpack Compose y Material 3.
- Carga de imágenes mediante Coil.
- Estados de carga, éxito, error y reintento.
- Forma de activación del Easter Egg.

El recorrido esperado es:

`Acción del usuario → Pantalla Compose → ViewModel → Repository → Retrofit → Spring Boot → Divine Pride`

Los datos regresan por las mismas capas hasta que el ViewModel actualiza el estado observado por la interfaz.

## 8. Pruebas de funcionamiento

| Prueba | Resultado |
|---|---|
| Consulta del Poring mediante Postman | Exitosa, HTTP 200 |
| Consulta aleatoria mediante Postman | Exitosa, HTTP 200 |
| Consulta mediante HTTPS | Exitosa, HTTP 200 |
| Ruta inexistente `/api/monster/999999` | HTTP 404 |
| Parámetro desconocido `apiKey=PRUEBA` | No alteró la respuesta normal |
| Manejo de errores 429 simulados | Pendiente de verificar |
| Entrada inválida desde Android | Pendiente |
| Falla de conexión y reintento desde Android | Pendiente |

La prueba de una ruta inexistente comprueba la respuesta HTTP 404, pero no sustituye por sí sola la validación de una entrada inválida dentro de la aplicación Android.

## 9. Capturas de pantalla

Agregar capturas de:

1. Respuesta exitosa del endpoint HTTPS en Postman.
2. Pantalla del Easter Egg en Kaiju mostrando un monstruo.
3. Estado de error o conexión fallida con opción de reintento.

## 10. Conclusión

Con esta implementación se integró un servicio externo de datos mediante una arquitectura que combina Android, Spring Boot y AWS.

La utilización de DuckDNS permitió mantener una dirección de acceso estable, mientras que Caddy permitió incorporar HTTPS.

El backend propio permitió proteger la API key de Divine Pride, controlar las solicitudes y transformar las respuestas externas en información más sencilla para la aplicación móvil.

Esta funcionalidad complementa el proyecto Kaiju sin modificar las responsabilidades principales del sistema de inventario.


## Easter egg de Ragnarok Online en la aplicación móvil

Al registrar correctamente un movimiento de **ENTRADA de 67 unidades**, aparece
una tarjeta debajo de **Registrar movimiento**, dentro de la misma pantalla.
Muestra imagen, nombre, ID, nivel, raza, elemento y vida del monstruo recibido.

La tarjeta permanece sin temporizador mientras se este en Movimientos. Otra entrada
confirmada de 67 solicita un monstruo aleatorio y reemplaza la ficha al recibirlo;
el servidor puede devolver el mismo monstruo. Las activaciones durante una consulta
pendiente se ignoran para evitar solicitudes simultaneas.

Cambiar de pestana, volver atras o cerrar sesion limpia la tarjeta y cancela la
consulta pendiente. Al regresar no se muestra una ficha anterior. Rotar el celular
conserva el estado sin repetir la consulta. Minimizar la app no equivale a salir
de Movimientos; el estado vive en el ViewModel y no persiste tras terminar el proceso.

- API: backend propio Spring Boot en AWS, que consulta Divine Pride.
- URL base: https://apisnecoarc.duckdns.org/
- Endpoint: GET api/monster/random (sin parametros).
- Android utiliza Retrofit/Gson para el JSON y Coil para spriteUrl.
- La clave de Divine Pride permanece exclusivamente en AWS.
- Flujo: movimiento confirmado -> EventoUi -> AppNavHost -> MonsterViewModel ->
  MonsterRepository -> MonsterApi -> estado observable -> PantallaMovimiento -> TarjetaMonstruo.
- La consulta se dispara solo despues de guardar correctamente. Escribir 67, crear un
  producto con stock inicial 67, guardar otras cantidades, las salidas y los ajustes
  no activan ni retiran una ficha existente.
- Un fallo del easter egg no modifica ni revierte el movimiento guardado. Reintentar
  consulta la API sin registrar stock. Si habia una ficha previa, se conserva con el aviso.
- Si falla solo la imagen, los atributos permanecen visibles y Reintentar imagen
  reutiliza el JSON sin consultar nuevamente el endpoint de monstruos.



