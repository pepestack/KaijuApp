package cl.kaiju.kaijuapp

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cl.kaiju.kaijuapp.navigation.AppNavHost
import cl.kaiju.kaijuapp.ui.theme.KaijuAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Light system bar icons over the dark Kaiju background
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        setContent {
            KaijuAppTheme {
                // la activity solo carga el navhost toda la navegacion vive ahi
                // application es la KaijuApplication que creo android y el as le dice a kotlin de que tipo es
                // asi se le puede pedir el repository unico
                AppNavHost(repository = (application as KaijuApplication).repository)
            }
        }
    }
}
