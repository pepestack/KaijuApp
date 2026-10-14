package cl.kaiju.kaijuapp

import android.app.Application
import cl.kaiju.kaijuapp.repository.InventarioRepository

class KaijuApplication(): Application() {


    val repository by lazy { InventarioRepository() }

}