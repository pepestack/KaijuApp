package cl.kaiju.kaijuapp.database

import cl.kaiju.kaijuapp.model.Producto
import cl.kaiju.kaijuapp.model.Rol
import cl.kaiju.kaijuapp.model.Usuario

object DatosDePrueba {

    val usuarios = listOf(
        Usuario(id = 1, nombre = "Alejandro Carrasco", rol = Rol.ADMINISTRADOR),
        Usuario(id = 2, nombre = "Esteban Hernandez", rol = Rol.ENCARGADO_INVENTARIO),
        Usuario(id = 3, nombre = "Ignacia Diaz", rol = Rol.VENDEDOR)
    )

    val productos = listOf(
        Producto(
            codigo = "P001",
            nombre = "Polera Kaiju",
            descripcion = "Polera de algodón",
            categoria = "Poleras",
            tipo = "Vestuario",
            precio = 12990,
            stockActual = 15,
            stockMinimo = 5,
            detalle = "Talla M, negra",
            codigoBarras = "2000000000015"
        ),
        Producto(
            codigo = "P002",
            nombre = "Polera Kaiju",
            descripcion = "Polera de algodón",
            categoria = "Poleras",
            tipo = "Vestuario",
            precio = 12990,
            stockActual = 15,
            stockMinimo = 5,
            detalle = "Talla L, negra",
            codigoBarras = "2000000000022"
        ),
        Producto(
            codigo = "P003",
            nombre = "Polera Kaiju",
            descripcion = "Polera de algodón",
            categoria = "Poleras",
            tipo = "Vestuario",
            precio = 12990,
            stockActual = 15,
            stockMinimo = 5,
            detalle = "Talla XL, negra",
            codigoBarras = "2000000000039"
        ),
        Producto(
            codigo = "P004",
            nombre = "Polera Kaiju",
            descripcion = "Polera de algodón",
            categoria = "Poleras",
            tipo = "Vestuario",
            precio = 12990,
            stockActual = 15,
            stockMinimo = 5,
            detalle = "Talla S, negra",
            codigoBarras = "2000000000046"
        ),
        Producto(
            codigo = "P005",
            nombre = "Poleron Kaiju",
            descripcion = "Poleron de algodón con estampado de kaneki de tokyo ghoul",
            categoria = "Polerones",
            tipo = "Vestuario",
            precio = 15990,
            stockActual = 15,
            stockMinimo = 5,
            detalle = "Talla S, negra",
            codigoBarras = "2000000000053"
        ),
        Producto(
            codigo = "P006",
            nombre = "Poleron Kaiju",
            descripcion = "Poleron de algodón con estampado de kaneki de tokyo ghoul",
            categoria = "Polerones",
            tipo = "Vestuario",
            precio = 15990,
            stockActual = 15,
            stockMinimo = 5,
            detalle = "Talla M, negra",
            codigoBarras = "2000000000060"
        ),
        Producto(
            codigo = "P007",
            nombre = "Poleron Kaiju",
            descripcion = "Poleron de algodón con estampado de kaneki de tokyo ghoul",
            categoria = "Polerones",
            tipo = "Vestuario",
            precio = 15990,
            stockActual = 15,
            stockMinimo = 5,
            detalle = "Talla L, negra",
            codigoBarras = "2000000000077"
        ),
        Producto(
            codigo = "P008",
            nombre = "Poleron Kaiju",
            descripcion = "Poleron de algodón con estampado de kaneki de tokyo ghoul",
            categoria = "Polerones",
            tipo = "Vestuario",
            precio = 17990,
            stockActual = 2,
            stockMinimo = 5,
            detalle = "Talla XL, negra",
            codigoBarras = "2000000000084"
        ),
        Producto(
            codigo = "P009",
            nombre = "Polera Goku migatte no gokui",
            descripcion = "Polera de algodón con diseño de goku",
            categoria = "Poleras",
            tipo = "Vestuario",
            precio = 13990,
            stockActual = 0,
            stockMinimo = 5,
            detalle = "Talla L oversize, negra",
            codigoBarras = "2000000000091"
        ),
        Producto(
            codigo = "P010",
            nombre = "Polera Kaiju",
            descripcion = "Polera de algodón",
            categoria = "Poleras",
            tipo = "Vestuario",
            precio = 13990,
            stockActual = 3,
            stockMinimo = 5,
            detalle = "Talla XL oversize, negra",
            codigoBarras = "2000000000107"
        ),

    )

}
