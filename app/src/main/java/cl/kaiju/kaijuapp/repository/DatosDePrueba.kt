package cl.kaiju.kaijuapp.repository

import cl.kaiju.kaijuapp.model.Producto
import cl.kaiju.kaijuapp.model.Rol
import cl.kaiju.kaijuapp.model.Usuario

object DatosDePrueba {

    val usuarios = listOf(
        Usuario(id = 1, nombre = "Alejandro Carrasco", rol = Rol.ADMINISTRADOR),
        Usuario(id = 2, nombre = "Esteban Hernandez", rol = Rol.ENCARGADO_INVENTARIO),
        Usuario(id = 1, nombre = "Ignacia Diaz", rol = Rol.VENDEDOR)
    )

    val productos = listOf(
        Producto(
            codigo = "P001",
            nombre = "Polera Kaiju",
            descripcion = "Polera de algodón",
            categoria = "Ropa",
            tipo = "Vestuario",
            precio = 12990,
            stockActual = 15,
            stockMinimo = 5,
            detalle = "Talla M, negra"
        ),
        Producto(
            codigo = "P002",
            nombre = "Polera Kaiju",
            descripcion = "Polera de algodón",
            categoria = "Ropa",
            tipo = "Vestuario",
            precio = 12990,
            stockActual = 15,
            stockMinimo = 5,
            detalle = "Talla L, negra"
        ),
        Producto(
            codigo = "P003",
            nombre = "Polera Kaiju",
            descripcion = "Polera de algodón",
            categoria = "Ropa",
            tipo = "Vestuario",
            precio = 12990,
            stockActual = 15,
            stockMinimo = 5,
            detalle = "Talla XL, negra"
        ),
        Producto(
            codigo = "P004",
            nombre = "Polera Kaiju",
            descripcion = "Polera de algodón",
            categoria = "Ropa",
            tipo = "Vestuario",
            precio = 12990,
            stockActual = 15,
            stockMinimo = 5,
            detalle = "Talla S, negra"
        ),
        Producto(
            codigo = "P005",
            nombre = "Poleron Kaiju",
            descripcion = "Poleron de algodón con estampado de kaneki de tokyo ghoul",
            categoria = "Ropa",
            tipo = "Vestuario",
            precio = 12990,
            stockActual = 15,
            stockMinimo = 5,
            detalle = "Talla S, negra"
        ),
        Producto(
            codigo = "P006",
            nombre = "Polera Kaiju",
            descripcion = "Poleron de algodón con estampado de kaneki de tokyo ghoul",
            categoria = "Ropa",
            tipo = "Vestuario",
            precio = 12990,
            stockActual = 15,
            stockMinimo = 5,
            detalle = "Talla M, negra"
        ),
        Producto(
            codigo = "P007",
            nombre = "Polera Kaiju",
            descripcion = "Poleron de algodón con estampado de kaneki de tokyo ghoul",
            categoria = "Ropa",
            tipo = "Vestuario",
            precio = 12990,
            stockActual = 15,
            stockMinimo = 5,
            detalle = "Talla L, negra"
        ),
        Producto(
            codigo = "P008",
            nombre = "Polera Kaiju",
            descripcion = "Poleron de algodón con estampado de kaneki de tokyo ghoul",
            categoria = "Ropa",
            tipo = "Vestuario",
            precio = 12990,
            stockActual = 15,
            stockMinimo = 5,
            detalle = "Talla XL, negra"
        ),
        Producto(
            codigo = "P009",
            nombre = "Polera Goku migatte no gokui",
            descripcion = "Polera de algodón con diseño de goku",
            categoria = "Ropa",
            tipo = "Vestuario",
            precio = 12990,
            stockActual = 15,
            stockMinimo = 5,
            detalle = "Talla L oversize, negra"
        ),
        Producto(
            codigo = "P010",
            nombre = "Polera Kaiju",
            descripcion = "Polera de algodón",
            categoria = "Ropa",
            tipo = "Vestuario",
            precio = 12990,
            stockActual = 15,
            stockMinimo = 5,
            detalle = "Talla XL oversize, negra "
        ),

    )

}
