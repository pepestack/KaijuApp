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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.kaiju.kaijuapp.R
import cl.kaiju.kaijuapp.model.EventoUi
import cl.kaiju.kaijuapp.model.Rol
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

// las pantallas no saben nada de viewmodels reciben datos por parametro y avisan eventos con lambdas
@Composable
fun AppNavHost(inventarioFactory: ViewModelProvider.Factory) {
    // control remoto de la navegacion sabe en que pantalla estas y te mueve entre ellas
    val navController = rememberNavController()

    // los viewmodels se crean una sola vez aqui y se pasan a cada pantalla (regla de la g09)
    // si cada pantalla creara el suyo no verian los mismos datos
    // viewModel() busca uno que ya exista (sobrevive al girar) y solo lo crea si no hay
    // inventarioViewModel necesita la factory porque recibe el repository en su constructor
    // la factory llega desde MainActivity asi el navhost (la vista) nunca toca el repository
    val sesionViewModel: SesionViewModel = viewModel()
    val inventarioViewModel: InventarioViewModel =
        viewModel(factory = inventarioFactory)

    // collectAsState convierte el stateflow en estado que compose observa cuando cambia la pantalla se redibuja
    // by permite usar sesion.algo directo sin escribir .value
    val sesion by sesionViewModel.estado.collectAsState()
    val catalogo by inventarioViewModel.estadoCatalogo.collectAsState()
    val productos by inventarioViewModel.productos.collectAsState()
    val usuarios by inventarioViewModel.usuarios.collectAsState()
    val formularioProducto by inventarioViewModel.estadoFormularioProducto.collectAsState()
    val esEdicion by inventarioViewModel.esEdicion.collectAsState()
    val formularioMovimiento by inventarioViewModel.estadoFormularioMovimiento.collectAsState()
    val errorOperacion by inventarioViewModel.errorOperacion.collectAsState()
    val guardando by inventarioViewModel.guardando.collectAsState()
    val evento by inventarioViewModel.evento.collectAsState()

    // snackbar mensajes cortos abajo de la pantalla para errores del viewmodel y confirmaciones
    // scope lanza el snackbar de confirmacion aparte asi no se corta cuando el evento se consume
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val mensajeRegistrado = stringResource(R.string.movimiento_registrado)
    // LaunchedEffect ejecuta el bloque cada vez que errorOperacion cambia si no es null lo muestra
    // al terminar avisa al viewmodel que ya se vio asi no reaparece al girar y el mismo error puede volver a mostrarse
    LaunchedEffect(errorOperacion) {
        errorOperacion?.let {
            snackbarHostState.showSnackbar(it)
            inventarioViewModel.errorMostrado()
        }
    }

    // el viewmodel termina la operacion y deja un evento la vista navega o avisa y lo consume una sola vez
    // se consume antes de actuar asi girar el celular no repite la navegacion
    LaunchedEffect(evento) {
        val actual = evento ?: return@LaunchedEffect
        inventarioViewModel.consumirEvento()
        when (actual) {
            EventoUi.ProductoGuardado -> navController.popBackStack()
            EventoUi.MovimientoRegistrado -> scope.launch { snackbarHostState.showSnackbar(mensajeRegistrado) }
            is EventoUi.EdicionLista -> navController.navigate(Rutas.Formulario.crearRuta(actual.codigo))
        }
    }

    // rol del usuario que inicio sesion se usa para mostrar u ocultar cosas
    val rol = sesion.rolActivo
    val esAdmin = rol == Rol.ADMINISTRADOR

    // desde android 13 las notificaciones de stock bajo necesitan permiso y pedirlo requiere la activity por eso va en la vista
    // el permiso es del celular no del usuario se pide solo a quien ve las alertas para que un vendedor no lo rechace por todos
    // rememberSaveable evita volver a preguntar en la misma sesion de la app aunque se gire o cambie de usuario
    val context = LocalContext.current
    val pedirPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    var permisoPedido by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(rol) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !permisoPedido &&
            (rol == Rol.ADMINISTRADOR || rol == Rol.ENCARGADO_INVENTARIO) &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            permisoPedido = true
            pedirPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)
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

    // ruta de la pantalla actual se usa para marcar la pestana activa
    // la barra solo se muestra en catalogo movimiento y alertas no en login detalle ni formulario
    val backStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = backStackEntry?.destination?.route
    val mostrarBarra = itemsBarra.any { it.ruta.ruta == rutaActual }

    // si android mato el proceso en segundo plano al volver restaura la pila (por ejemplo el catalogo)
    // pero el sesionViewModel nace vacio, sin sesion no hay nada que mostrar asi que se vuelve al login
    // rutaActual es null mientras el navhost aun no arma su grafo
    LaunchedEffect(sesion.usuarioActivo, rutaActual) {
        if (sesion.usuarioActivo == null && rutaActual != null && rutaActual != Rutas.Login.ruta) {
            volverAlLogin(navController)
        }
    }

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
                    usuarios = usuarios,
                    usuarioSeleccionadoId = sesion.usuarioSeleccionado?.id,
                    error = sesion.errorUsuario,
                    // la pantalla entrega el id y find busca el usuario completo que pide el viewmodel
                    onSeleccionarUsuario = { id ->
                        sesionViewModel.seleccionarUsuario(usuarios.find { it.id == id })
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
                    },
                    //De acá para abajo se modificó para la implementación del botón de salir
                    onCerrarSesion = {
                        inventarioViewModel.limpiarFiltros()
                        inventarioViewModel.prepararNuevoProducto()
                        inventarioViewModel.limpiarFormularioMovimiento()
                        sesionViewModel.cerrarSesion()
                        snackbarHostState.currentSnackbarData?.dismiss()
                        volverAlLogin(navController)
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
                val historial by remember(codigo) { inventarioViewModel.historialDe(codigo) }
                    .collectAsState(initial = emptyList())

                PantallaDetalleProducto(
                    producto = producto,
                    movimientos = historial,
                    nombresUsuarios = usuarios.associate { it.id to it.nombre },
                    puedeEditar = esAdmin, // rf-05
                    // carga los datos del producto en el formulario y el evento EdicionLista hace la navegacion
                    onEditar = { inventarioViewModel.prepararEdicionProducto(codigo) },
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
                    guardando = guardando,
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
                    // guardarProducto valida (rn-02 rn-03) y si sale bien el evento ProductoGuardado vuelve atras
                    onGuardar = { inventarioViewModel.guardarProducto(sesion.usuarioActivo) },
                    onVolver = { navController.popBackStack() }
                )
            }

            composable(Rutas.Movimiento.ruta) {
                PantallaMovimiento(
                    estado = formularioMovimiento,
                    productos = productos,
                    // que tipos de movimiento puede usar cada rol lo decide el viewmodel
                    tiposPermitidos = inventarioViewModel.tiposPermitidos(rol),
                    guardando = guardando,
                    onProductoChange = inventarioViewModel::actualizarProductoMovimiento,
                    onTipoChange = inventarioViewModel::actualizarTipoMovimiento,
                    onCantidadChange = inventarioViewModel::actualizarCantidad,
                    onMotivoChange = inventarioViewModel::actualizarMotivo,
                    // registrarMovimiento valida (rn-01 rn-04) y si sale bien el evento MovimientoRegistrado muestra el mensaje
                    onRegistrar = { inventarioViewModel.registrarMovimiento(sesion.usuarioActivo) }
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

// lo usan el boton salir y la redireccion cuando no hay sesion
private fun volverAlLogin(navController: NavHostController) {
    // Descarta también las pilas guardadas al cambiar de pestaña.
    // El siguiente perfil conserva el inventario, pero no la navegación anterior.
    listOf(Rutas.Catalogo, Rutas.Movimiento, Rutas.Alertas).forEach {
        navController.clearBackStack(it.ruta)
    }
    navController.navigate(Rutas.Login.ruta) {
        popUpTo(navController.graph.id) { inclusive = false }
        launchSingleTop = true
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
