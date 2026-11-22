package cl.kaiju.kaijuapp.data.remote

import cl.kaiju.kaijuapp.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object UsuarioApiClient {
    // La URL sale de local.properties (kaiju.api.url); sin ella apunta al backend local del emulador.
    val api: UsuarioApi by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.KAIJU_API_URL)
            .client(OkHttpClient.Builder().callTimeout(20, TimeUnit.SECONDS).build())
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(UsuarioApi::class.java)
    }
}
