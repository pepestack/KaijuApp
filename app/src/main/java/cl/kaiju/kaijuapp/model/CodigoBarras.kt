package cl.kaiju.kaijuapp.model

/**
 * Un UPC-A (12 dígitos) es el mismo código que un EAN-13 con un 0 adelante, y el escáner
 * devuelve el UPC-A con 12. Todo pasa por aquí para guardar y buscar siempre el mismo texto.
 */
object CodigoBarras {
    /** Devuelve el EAN-13 de 13 dígitos, o null si no es un UPC-A o EAN-13 válido. */
    fun normalizar(texto: String): String? {
        val digitos = texto.trim()
        if (!digitos.all { it.isDigit() }) return null
        val ean = when (digitos.length) {
            12 -> "0$digitos"
            13 -> digitos
            else -> return null
        }
        // Dígito verificador: pesos 1 y 3 alternados sobre los primeros 12 dígitos.
        val suma = ean.take(12).mapIndexed { i, c -> c.digitToInt() * if (i % 2 == 0) 1 else 3 }.sum()
        return if ((10 - suma % 10) % 10 == ean.last().digitToInt()) ean else null
    }
}
