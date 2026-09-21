package com.example.cinemap.data
import retrofit2.http.GET
import retrofit2.http.Query

interface TmdbApi {
    @GET("movie/now_playing")
    suspend fun getNowPlaying(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1
    ): MovieResponse

    // Free cinema search - this replaces Google Places, no key needed
    @GET("https://overpass-api.de/api/interpreter")
    suspend fun getNearbyCinemas(
        @Query("data") data: String
    ): OverpassResponse
}
// TMDB Movies
data class MovieResponse(val results: List<Movie>)
data class Movie(
    val id: Int,
    val title: String,
    val overview: String,
    val poster_path: String?,
    val vote_average: Double,
    val release_date: String
) {
    fun getPosterUrl() = "https://image.tmdb.org/t/p/w500$poster_path"
}

// overpass free Ster-Kinekor / Nu Metro
data class OverpassResponse(val elements: List<OverpassElement>)
data class OverpassElement(
    val lat: Double,
    val lon: Double,
    val tags: Map<String, String>? = null
)