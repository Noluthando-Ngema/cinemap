package com.example.cinemap.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.content.Context
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File

object RetrofitClient {
    const val TMDB_API_KEY = "9755fb7dbfc1eec99511ea7c8538ea3b"
    private const val BASE_URL = "https://api.themoviedb.org/3/"
    private var retrofit: Retrofit? = null

    val tmdbApi: TmdbApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TmdbApi::class.java)
    }

    // second client for FREE cinema map
    val overpassApi: TmdbApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://overpass-api.de/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TmdbApi::class.java)
    }

    fun getInstance(context: Context): Retrofit {
        if (retrofit == null) {
            // 10 MB cache
            val cacheSize = 10 * 1024 * 1024L
            val cacheDir = File(context.cacheDir, "http_cache")
            val cache = Cache(cacheDir, cacheSize)

            val prefs = context.getSharedPreferences("CineMapPrefs", Context.MODE_PRIVATE)
            val isOffline = prefs.getBoolean("offline_mode", false)

            val client = OkHttpClient.Builder()
                .cache(cache)
                .addInterceptor { chain ->
                    var request = chain.request()
                    if (isOffline) {
                        // Force cache when offline toggle is ON
                        request = request.newBuilder()
                            .header("Cache-Control", "public, only-if-cached, max-stale=604800") // 7 days
                            .build()
                    }
                    chain.proceed(request)
                }
                .build()

            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }

    fun clearInstance() {
        retrofit = null // Call this when toggle changes
    }
}
