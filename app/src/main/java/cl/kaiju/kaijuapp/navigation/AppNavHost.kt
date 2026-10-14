package cl.kaiju.kaijuapp.navigation

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.kaiju.kaijuapp.R
import cl.kaiju.kaijuapp.model.FormularioMovimientoEstado
import cl.kaiju.kaijuapp.model.FormularioProductoEstado
import cl.kaiju.kaijuapp.model.Rol
import cl.kaiju.kaijuapp.model.TipoMovimiento
import cl.kaiju.kaijuapp.repository.InventarioRepository
import cl.kaiju.kaijuapp.ui.screen.PantallaAlertas
import cl.kaiju.kaijuapp.ui.screen.PantallaCatalogo
import cl.kaiju.kaijuapp.ui.screen.PantallaDetalleProducto
import cl.kaiju.kaijuapp.ui.screen.PantallaFormularioProducto
import cl.kaiju.kaijuapp.ui.screen.PantallaLogin
import cl.kaiju.kaijuapp.ui.screen.PantallaMovimiento
import cl.kaiju.kaijuapp.viewmodel.InventarioViewModel
import cl.kaiju.kaijuapp.viewmodel.SesionViewModel

/** Una pestaña de la barra inferior. */
private data class ItemBarra(val ruta: Rutas, val etiqueta: Int, val icono: ImageVector)

@Composable
fun AppNavHost(repository: InventarioRepository) {
    val navController = rememberNavController()

    // Se crean UNA sola vez aquí y se comparten entre pantallas
    val sesionViewModel: SesionViewModel = viewModel()
    val inventarioViewModel: InventarioViewModel =
        viewModel(factory = InventarioViewModel.factory(repository))

    val sesion by sesionViewModel.estado.collectAsState()
    val catalogo by inventarioViewModel.estadoCatalogo.collectAsState()
    val productos by repository.productos.collectAsState()

    val rol = sesion.rolActivo
    val esAdmin = rol == Rol.ADMINISTRADOR

    //las alertas son solo para el encargado y el administrador
    val itemsBarra = listOfNotNull(
        ItemBarra(Rutas.Catalogo, R.string.nav_catalogo, Icons.Filled.Home),
        ItemBarra(Rutas.Movimiento, R.string.nav_movimiento, Icons.Filled.ShoppingCart),
        if (rol == Rol.ADMINISTRADOR || rol == Rol.ENCARGADO_INVENTARIO)
            ItemBarra(Rutas.Alertas, R.string.nav_alertas, Icons.Filled.Notifications)
        else null
    )

    val backStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = backStackEntry?.destination?.route
    val mostrarBarra = itemsBarra.any { it.ruta.ruta == rutaActual }

    Scaffold(
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
            // Las pantallas tienen su propio Scaffold: así no se duplican los márgenes
            modifier = Modifier.padding(inner).consumeWindowInsets(inner)
        ) {
            composable(Rutas.Login.ruta) {
                PantallaLogin(
                    usuarios = repository.usuarios,
                    usuarioSeleccionadoId = sesion.usuarioSeleccionado?.id,
                    error = sesion.errorUsuario,
                    onSeleccionarUsuario = { id ->
                        sesionViewModel.seleccionarUsuario(repository.usuarios.find { it.id == id })
                    },
                    onIngresar = {
                        if (sesionViewModel.iniciarSesion()) {
                            navController.navigate(Rutas.Catalogo.ruta) {
                                // Saca el Login de la pila: "atrás" ya no vuelve a él
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
                    onBusquedaChange = inventarioViewModel::actualizarBusqueda,
                    categorias = catalogo.categorias,
                    categoriaSeleccionada = catalogo.categoriaSeleccionada,
                    onCategoriaChange = inventarioViewModel::seleccionarCategoria,
                    puedeCrear = esAdmin,
                    onProductoClick = { codigo -> navController.navigate(Rutas.Detalle.crearRuta(codigo)) },
                    onNuevoProducto = { navController.navigate(Rutas.Formulario.crearRuta()) }
                )
            }

            composable(
                route = Rutas.Detalle.ruta,
                arguments = listOf(navArgument("codigo") { type = NavType.StringType })
            ) { entry ->
                val codigo = entry.arguments?.getString("codigo") ?: return@composable
                val producto = productos.find { it.codigo == codigo } ?: return@composable
                val historial by remember(codigo) { repository.historialDe(codigo) }
                    .collectAsState(initial = emptyList())

                PantallaDetalleProducto(
                    producto = producto,
                    movimientos = historial,
                    nombresUsuarios = repository.usuarios.associate { it.id to it.nombre },
                    puedeEditar = esAdmin, // RF-05
                    onEditar = { navController.navigate(Rutas.Formulario.crearRuta(codigo)) },
                    onVolver = { navController.popBackStack() }
                )
            }

            composable(
                route = Rutas.Formulario.ruta,
                arguments = listOf(navArgument("codigo") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { entry ->
                val codigo = entry.arguments?.getString("codigo")

                // Mientras tanto la pantalla se muestra, pero no guarda nada.
                PantallaFormularioProducto(
                    estado = FormularioProductoEstado(codigo = codigo.orEmpty()),
                    esEdicion = codigo != null,
                    categorias = catalogo.categorias,
                    onCodigoChange = {},
                    onNombreChange = {},
                    onDescripcionChange = {},
                    onCategoriaChange = {},
                    onTipoChange = {},
                    onPrecioChange = {},
                    onStockActualChange = {},
                    onStockMinimoChange = {},
                    onDetalleChange = {},
                    onGuardar = {},
                    onVolver = { navController.popBackStack() }
                )
            }

            composable(Rutas.Movimiento.ruta) {

                // Mientras tanto la pantalla se muestra, pero no registra nada.
                PantallaMovimiento(
                    estado = FormularioMovimientoEstado(),
                    productos = productos,
                    tiposPermitidos = tiposPermitidos(rol),
                    onProductoChange = {},
                    onTipoChange = {},
                    onCantidadChange = {},
                    onMotivoChange = {},
                    onRegistrar = {}
                )
            }

            composable(Rutas.Alertas.ruta) {
                PantallaAlertas(
                    productos = catalogo.productosEnAlerta,
                    onProductoClick = { codigo -> navController.navigate(Rutas.Detalle.crearRuta(codigo)) }
                )
            }
        }
    }
}

/** Cambia de pestaña sin apilar copias de la misma pantalla. */
private fun navegarAPestana(navController: NavHostController, ruta: Rutas) {
    navController.navigate(ruta.ruta) {
        // El catálogo es la raíz después del login: las pestañas no se apilan sobre él
        popUpTo(Rutas.Catalogo.ruta) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** RF-06, RF-07, RF-08: qué movimientos puede registrar cada perfil. */
private fun tiposPermitidos(rol: Rol?): List<TipoMovimiento> = when (rol) {
    Rol.ADMINISTRADOR -> listOf(TipoMovimiento.ENTRADA, TipoMovimiento.AJUSTE)
    Rol.VENDEDOR -> listOf(TipoMovimiento.SALIDA_VENTA, TipoMovimiento.SALIDA_CONSUMO)
    Rol.ENCARGADO_INVENTARIO -> TipoMovimiento.entries
    null -> emptyList()
}
