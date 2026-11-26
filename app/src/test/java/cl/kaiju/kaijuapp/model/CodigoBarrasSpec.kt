package cl.kaiju.kaijuapp.model

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe


class CodigoBarrasSpec : FunSpec({

    test("un UPC-A de 12 dígitos se guarda como EAN-13 con un 0 adelante") {
        CodigoBarras.normalizar("036000291452") shouldBe "0036000291452"
    }

    test("un código con el dígito verificador incorrecto se rechaza") {
        CodigoBarras.normalizar("4006381333931") shouldBe "4006381333931"
        CodigoBarras.normalizar("4006381333932").shouldBeNull()
    }
})
