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
| RF-01 | Selección de un usuario de prueba y acceso según su perfil. |
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
