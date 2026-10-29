package com.example.cinemap.data

import retrofit2.http.GET
import retrofit2.http.Query

interface TranslationApi {
    @GET("get")
    suspend fun translate(
        @Query("q") text: String,
        @Query("langpair") langPair: String // e.g. "en|af"
    ): MyMemoryResponse
}

data class MyMemoryResponse(val responseData: ResponseData)
data class ResponseData(val translatedText: String)

object TranslationClient {
    val api: TranslationApi by lazy {
        retrofit2.Retrofit.Builder()
            .baseUrl("https://api.mymemory.translated.net/")
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
            .create(TranslationApi::class.java)
    }
}