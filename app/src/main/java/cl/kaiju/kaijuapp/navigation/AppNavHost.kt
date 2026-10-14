package cl.kaiju.kaijuapp.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.kaiju.kaijuapp.R
import cl.kaiju.kaijuapp.model.Rol
import cl.kaiju.kaijuapp.notificaciones.Notificador
import cl.kaiju.kaijuapp.repository.InventarioRepository
import cl.kaiju.kaijuapp.ui.screen.PantallaAlertas
import cl.kaiju.kaijuapp.ui.screen.PantallaCatalogo
import cl.kaiju.kaijuapp.ui.screen.PantallaDetalleProducto
import cl.kaiju.kaijuapp.ui.screen.PantallaFormularioProducto
import cl.kaiju.kaijuapp.ui.screen.PantallaLogin
import cl.kaiju.kaijuapp.ui.screen.PantallaMovimiento
import cl.kaiju.kaijuapp.viewmodel.InventarioViewModel
import cl.kaiju.kaijuapp.viewmodel.SesionViewModel
import kotlinx.coroutines.launch

// una pestana de la barra inferior con su ruta su texto y su icono
private data class ItemBarra(val ruta: Rutas, val etiqueta: Int, val icono: ImageVector)

// conecta todo las pantallas de jose con los viewmodels de apablaza
// las pantallas no saben nada de viewmodels reciben datos por parametro y avisan eventos con lambdas
@Composable
fun AppNavHost(repository: InventarioRepository) {
    // control remoto de la navegacion sabe en que pantalla estas y te mueve entre ellas
    val navController = rememberNavController()

    // los viewmodels se crean una sola vez aqui y se pasan a cada pantalla (regla de la g09)
    // si cada pantalla creara el suyo no verian los mismos datos
    // viewModel() busca uno que ya exista (sobrevive al girar) y solo lo crea si no hay
    // inventarioViewModel necesita la factory porque recibe el repository en su constructor
    val sesionViewModel: SesionViewModel = viewModel()
    val inventarioViewModel: InventarioViewModel =
        viewModel(factory = InventarioViewModel.factory(repository))

    // collectAsState convierte el stateflow en estado que compose observa cuando cambia la pantalla se redibuja
    // by permite usar sesion.algo directo sin escribir .value
    val sesion by sesionViewModel.estado.collectAsState()
    val catalogo by inventarioViewModel.estadoCatalogo.collectAsState()
    val productos by repository.productos.collectAsState()
    val formularioProducto by inventarioViewModel.estadoFormularioProducto.collectAsState()
    val esEdicion by inventarioViewModel.esEdicion.collectAsState()
    val formularioMovimiento by inventarioViewModel.estadoFormularioMovimiento.collectAsState()
    val errorOperacion by inventarioViewModel.errorOperacion.collectAsState()

    // snackbar mensajes cortos abajo de la pantalla para errores del viewmodel y confirmaciones
    // scope sirve para lanzar el snackbar desde un clic porque showSnackbar es suspend
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val mensajeRegistrado = stringResource(R.string.movimiento_registrado)
    // LaunchedEffect ejecuta el bloque cada vez que errorOperacion cambia si no es null lo muestra
    LaunchedEffect(errorOperacion) {
        errorOperacion?.let { snackbarHostState.showSnackbar(it) }
    }

    // rol del usuario que inicio sesion se usa para mostrar u ocultar cosas
    val rol = sesion.rolActivo
    val esAdmin = rol == Rol.ADMINISTRADOR

    // LocalContext.current es el context de la pantalla
    // se usa applicationContext porque vive toda la app y no se pierde al girar
    // remember crea un solo notificador y no uno nuevo en cada redibujo
    val context = LocalContext.current
    val notificador = remember { Notificador(context.applicationContext) }
    val productoEnAlerta by inventarioViewModel.productoEnAlerta.collectAsState()

    // cuando el viewmodel avisa que un producto paso a alerta se notifica
    // y luego se consume el aviso para no notificarlo de nuevo
    LaunchedEffect(productoEnAlerta) {
        productoEnAlerta?.let {
            notificador.alertaStock(it)
            inventarioViewModel.notificacionMostrada()
        }
    }

    // launcher que abre el dialogo del sistema para pedir el permiso
    // el bloque recibe true si lo acepto y false si lo rechazo
    // si lo rechaza no se bloquea nada solo se le recuerda la alternativa (pantalla de alertas)
    val mensajePermisoRechazado = stringResource(R.string.permiso_notificaciones_rechazado)
    val lanzadorPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { aceptado ->
        if (!aceptado) scope.launch { snackbarHostState.showSnackbar(mensajePermisoRechazado) }
    }

    // se pide al iniciar sesion y no al abrir la app asi el usuario entiende para que es
    // solo para encargado y admin que son los que necesitan las alertas
    // solo en android 13 o mas porque antes el permiso no existe
    // si ya lo tiene no se vuelve a pedir y si lo rechazo dos veces android ya no muestra el dialogo
    LaunchedEffect(rol) {
        val necesitaAlertas = rol == Rol.ADMINISTRADOR || rol == Rol.ENCARGADO_INVENTARIO
        if (necesitaAlertas &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            lanzadorPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // las alertas son solo para el encargado y el administrador (rf-09)
    // si no corresponde el item queda null y listOfNotNull lo descarta
    val itemsBarra = listOfNotNull(
        ItemBarra(Rutas.Catalogo, R.string.nav_catalogo, Icons.Filled.Home),
        ItemBarra(Rutas.Movimiento, R.string.nav_movimiento, Icons.Filled.ShoppingCart),
        if (rol == Rol.ADMINISTRADOR || rol == Rol.ENCARGADO_INVENTARIO)
            ItemBarra(Rutas.Alertas, R.string.nav_alertas, Icons.Filled.Notifications)
        else null
    )

    // ruta de la pantalla actual se usa para marcar la pestaña activa
    // la barra solo se muestra en catalogo movimiento y alertas no en login detalle ni formulario
    val backStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = backStackEntry?.destination?.route
    val mostrarBarra = itemsBarra.any { it.ruta.ruta == rutaActual }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (mostrarBarra) {
                NavigationBar {
                    itemsBarra.forEach { item ->
                        NavigationBarItem(
                            selected = rutaActual == item.ruta.ruta,
                            onClick = { navegarAPestana(navController, item.ruta) },
                            icon = { Icon(item.icono, contentDescription = null) },
                            label = { Text(stringResource(item.etiqueta)) }
                        )
                    }
                }
            }
        }
    ) { inner ->
        NavHost(
            navController = navController,
            startDestination = Rutas.Login.ruta,
            // la app parte en el login
            // las pantallas tienen su propio scaffold asi no se duplican los margenes de la barra
            modifier = Modifier.padding(inner).consumeWindowInsets(inner)
        ) {
            // cada composable une una ruta con su pantalla
            composable(Rutas.Login.ruta) {
                PantallaLogin(
                    usuarios = repository.usuarios,
                    usuarioSeleccionadoId = sesion.usuarioSeleccionado?.id,
                    error = sesion.errorUsuario,
                    // la pantalla entrega el id y find busca el usuario completo que pide el viewmodel
                    onSeleccionarUsuario = { id ->
                        sesionViewModel.seleccionarUsuario(repository.usuarios.find { it.id == id })
                    },
                    // solo navega si el viewmodel dice true la pantalla no decide nada
                    onIngresar = {
                        if (sesionViewModel.iniciarSesion()) {
                            navController.navigate(Rutas.Catalogo.ruta) {
                                // saca el login de la pila asi el boton atras ya no vuelve a el
                                popUpTo(Rutas.Login.ruta) { inclusive = true }
                            }
                        }
                    }
                )
            }

            composable(Rutas.Catalogo.ruta) {
                PantallaCatalogo(
                    productos = catalogo.productos,
                    busqueda = catalogo.busqueda,
                    // :: es referencia a funcion igual que { inventarioViewModel.actualizarBusqueda(it) }
                    onBusquedaChange = inventarioViewModel::actualizarBusqueda,
                    categorias = catalogo.categorias,
                    categoriaSeleccionada = catalogo.categoriaSeleccionada,
                    onCategoriaChange = inventarioViewModel::seleccionarCategoria,
                    puedeCrear = esAdmin, // solo el admin crea productos (rf-05)
                    onProductoClick = { codigo -> navController.navigate(Rutas.Detalle.crearRuta(codigo)) },
                    onNuevoProducto = {
                        // el formulario se prepara antes de navegar
                        // si se preparara al abrir la pantalla girar el celular borraria lo escrito
                        inventarioViewModel.prepararNuevoProducto()
                        navController.navigate(Rutas.Formulario.crearRuta())
                    }
                )
            }

            // navArgument declara el hueco {codigo} de la ruta como texto
            composable(
                route = Rutas.Detalle.ruta,
                arguments = listOf(navArgument("codigo") { type = NavType.StringType })
            ) { entry ->
                // saca el codigo de la ruta detalle/P008 da "P008"
                // si falta el codigo o el producto no dibuja nada en vez de caerse
                val codigo = entry.arguments?.getString("codigo") ?: return@composable
                val producto = productos.find { it.codigo == codigo } ?: return@composable
                // remember(codigo) crea el flujo del historial una vez por producto y no en cada redibujo
                val historial by remember(codigo) { repository.historialDe(codigo) }
                    .collectAsState(initial = emptyList())

                PantallaDetalleProducto(
                    producto = producto,
                    movimientos = historial,
                    nombresUsuarios = repository.usuarios.associate { it.id to it.nombre },
                    puedeEditar = esAdmin, // rf-05
                    // carga los datos del producto en el formulario y recien ahi navega
                    onEditar = {
                        if (inventarioViewModel.prepararEdicionProducto(codigo)) {
                            navController.navigate(Rutas.Formulario.crearRuta(codigo))
                        }
                    },
                    // popBackStack vuelve a la pantalla anterior
                    onVolver = { navController.popBackStack() }
                )
            }

            // el codigo es opcional por eso nullable y por defecto null
            composable(
                route = Rutas.Formulario.ruta,
                arguments = listOf(navArgument("codigo") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) {
                // el viewmodel ya sabe si es creacion o edicion porque se preparo al navegar
                // cada campo avisa al viewmodel y el viewmodel actualiza el estado (flujo unidireccional)
                // si estas lambdas no hicieran nada lo escrito no apareceria
                PantallaFormularioProducto(
                    estado = formularioProducto,
                    esEdicion = esEdicion,
                    categorias = catalogo.categorias,
                    onCodigoChange = inventarioViewModel::actualizarCodigo,
                    onNombreChange = inventarioViewModel::actualizarNombre,
                    onDescripcionChange = inventarioViewModel::actualizarDescripcion,
                    onCategoriaChange = inventarioViewModel::actualizarCategoria,
                    onTipoChange = inventarioViewModel::actualizarTipo,
                    onPrecioChange = inventarioViewModel::actualizarPrecio,
                    onStockActualChange = inventarioViewModel::actualizarStockActual,
                    onStockMinimoChange = inventarioViewModel::actualizarStockMinimo,
                    onDetalleChange = inventarioViewModel::actualizarDetalle,
                    // guardarProducto valida (rn-02 rn-03) y si sale bien vuelve atras
                    onGuardar = {
                        if (inventarioViewModel.guardarProducto(sesion.usuarioActivo)) {
                            navController.popBackStack()
                        }
                    },
                    onVolver = { navController.popBackStack() }
                )
            }

            composable(Rutas.Movimiento.ruta) {
                PantallaMovimiento(
                    estado = formularioMovimiento,
                    productos = productos,
                    // que tipos de movimiento puede usar cada rol lo decide el viewmodel
                    tiposPermitidos = inventarioViewModel.tiposPermitidos(rol),
                    onProductoChange = inventarioViewModel::actualizarProductoMovimiento,
                    onTipoChange = inventarioViewModel::actualizarTipoMovimiento,
                    onCantidadChange = inventarioViewModel::actualizarCantidad,
                    onMotivoChange = inventarioViewModel::actualizarMotivo,
                    // registrarMovimiento valida (rn-01 rn-04) y si sale bien muestra el mensaje
                    onRegistrar = {
                        if (inventarioViewModel.registrarMovimiento(sesion.usuarioActivo)) {
                            scope.launch { snackbarHostState.showSnackbar(mensajeRegistrado) }
                        }
                    }
                )
            }

            composable(Rutas.Alertas.ruta) {
                PantallaAlertas(
                    // productos con stock menor o igual al minimo (rn-06)
                    productos = catalogo.productosEnAlerta,
                    onProductoClick = { codigo -> navController.navigate(Rutas.Detalle.crearRuta(codigo)) }
                )
            }
        }
    }
}

// cambia de pestana sin apilar copias de la misma pantalla
private fun navegarAPestana(navController: NavHostController, ruta: Rutas) {
    navController.navigate(ruta.ruta) {
        // el catalogo es la raiz despues del login las pestanas no se apilan sobre el
        // saveState y restoreState recuerdan donde estabas en cada pestana
        popUpTo(Rutas.Catalogo.ruta) { saveState = true }
        // launchSingleTop evita abrir dos veces la misma pantalla
        launchSingleTop = true
        restoreState = true
    }
}
