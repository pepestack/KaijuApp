package cl.kaiju.kaijuapp.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object MonsterApiClient {
    val api: MonsterApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://apisnecoarc.duckdns.org/")
            .client(OkHttpClient.Builder().callTimeout(20, TimeUnit.SECONDS).build())
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(MonsterApi::class.java)
    }
}
