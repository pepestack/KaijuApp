package cl.kaiju.kaijuapp.viewmodel

import cl.kaiju.kaijuapp.model.Rol
import cl.kaiju.kaijuapp.model.SesionEstado
import cl.kaiju.kaijuapp.model.Usuario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SesionViewModelTest {
    @Test
    fun iniciarSinSeleccionMuestraErrorYSigueSinSesion() {
        val viewModel = SesionViewModel()

        assertFalse(viewModel.iniciarSesion())
        assertNotNull(viewModel.estado.value.errorUsuario)
        assertFalse(viewModel.estado.value.sesionIniciada)
    }

    @Test
    fun seleccionarUsuarioLimpiaErrorSinIniciarSesion() {
        val viewModel = SesionViewModel()
        viewModel.iniciarSesion()
        val usuario = Usuario(1, "Administrador de prueba", Rol.ADMINISTRADOR)

        viewModel.seleccionarUsuario(usuario)

        assertEquals(usuario, viewModel.estado.value.usuarioSeleccionado)
        assertNull(viewModel.estado.value.errorUsuario)
        assertNull(viewModel.estado.value.usuarioActivo)
    }

    @Test
    fun iniciarSesionConservaElUsuarioYRolDeCadaPerfil() {
        for (rol in Rol.values()) {
            val viewModel = SesionViewModel()
            val usuario = Usuario(1, "Usuario de prueba", rol)
            viewModel.seleccionarUsuario(usuario)

            assertTrue(viewModel.iniciarSesion())
            assertEquals(usuario, viewModel.estado.value.usuarioActivo)
            assertEquals(rol, viewModel.estado.value.rolActivo)
            assertTrue(viewModel.estado.value.sesionIniciada)
        }
    }

    @Test
    fun cerrarSesionLimpiaPerfilYSeleccionParaElSiguienteIngreso() {
        val viewModel = SesionViewModel()
        viewModel.seleccionarUsuario(Usuario(1, "Vendedor de prueba", Rol.VENDEDOR))
        viewModel.iniciarSesion()

        viewModel.cerrarSesion()

        assertEquals(SesionEstado(), viewModel.estado.value)
        assertFalse(viewModel.iniciarSesion())
    }
}
