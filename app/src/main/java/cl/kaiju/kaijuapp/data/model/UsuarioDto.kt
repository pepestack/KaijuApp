package cl.kaiju.kaijuapp.data.model

// JSON del backend de usuarios (/api/v1/kaiju). El rol llega como texto: "Administrador",
// "Encargado de inventario" o "Vendedor".
data class UsuarioDto(val id: Int, val nombre: String, val correo: String, val rol: String)

data class LoginRequest(val correo: String, val contrasena: String)

data class LoginResponse(val accessToken: String, val usuario: UsuarioDto)

// Gson no escribe los null: en PUT sin contraseña el backend no la cambia.
data class UsuarioRequest(val nombre: String, val correo: String, val contrasena: String?, val rol: String)

// Formato de error del backend; detalles trae el mensaje de cada campo que falló.
data class ErrorDto(val error: String?, val detalles: Map<String, String>?)
