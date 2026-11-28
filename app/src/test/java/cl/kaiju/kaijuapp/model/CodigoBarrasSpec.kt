package cl.kaiju.kaijuapp.model

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe


class CodigoBarrasSpec : FunSpec({

    test("un UPC-A de 12 dígitos se guarda como EAN-13 con un 0 adelante") {
        CodigoBarras.normalizar("036000291452") shouldBe "0036000291452"
        CodigoBarras.normalizar(" 036000291452 ") shouldBe "0036000291452" // espacios del escáner o del teclado
        // Normalizar un código ya normalizado no lo cambia: guardar y buscar dan el mismo texto.
        CodigoBarras.normalizar("0036000291452") shouldBe "0036000291452"
    }

    test("un código inválido se rechaza: dígito verificador, largo o caracteres") {
        CodigoBarras.normalizar("4006381333931") shouldBe "4006381333931" // control: EAN-13 válido
        CodigoBarras.normalizar("4006381333932").shouldBeNull() // dígito verificador incorrecto
        CodigoBarras.normalizar("03600029145").shouldBeNull() // 11 dígitos
        CodigoBarras.normalizar("40063813339310").shouldBeNull() // 14 dígitos
        CodigoBarras.normalizar("40063813339A1").shouldBeNull() // contiene una letra
        CodigoBarras.normalizar("").shouldBeNull()
    }
})
