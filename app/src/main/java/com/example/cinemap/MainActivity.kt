package com.example.cinemap

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.cinemap.data.RetrofitClient
import com.example.cinemap.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Your horizontal layouts
    private lateinit var nowShowingContainer: LinearLayout
    private lateinit var topPicksContainer: LinearLayout
    private lateinit var cinemasContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nowShowingContainer = findViewById(R.id.nowShowingRow) // the LinearLayout inside your HorizontalScrollView
        topPicksContainer = findViewById(R.id.topPicksRow)
        cinemasContainer = findViewById(R.id.cinemasList) // vertical LinearLayout

        loadMoviesFromTMDB()
        loadCinemasNearYou() // free Ster-Kinekor / Nu Metro using Google Places
    }

    private fun loadMoviesFromTMDB() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getNowPlaying(RetrofitClient.RetrofitClient.TMDB_API_KEY)

                // 1. NOW SHOWING - first 5 movies
                response.results.take(5).forEach { movie ->
                    addMovieCard(nowShowingContainer, movie, isLarge = true)
                }

                // 2. TOP PICKS - next 5 movies
                response.results.takeLast(5).forEach { movie ->
                    addMovieCard(topPicksContainer, movie, isLarge = false)
                }

            } catch (e: Exception) {
                Toast.makeText(this@MainActivityActivity, "API Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun addMovieCard(container: LinearLayout, movie: Movie, isLarge: Boolean) {
        val cardView = layoutInflater.inflate(R.layout.item_movie_card, container, false)
        cardView.findViewById<TextView>(R.id.txtMovieTitle).text = movie.title

        // Load poster with Glide
        val img = cardView.findViewById<ImageView>(R.id.imgMoviePoster)
        Glide.with(this).load(movie.getPosterUrl()).into(img)

        // On click -> Go to Cinema Details page WITH MAP
        cardView.setOnClickListener {
            val intent = Intent(this, CinemaDetailsActivity::class.java)
            intent.putExtra("title", movie.title)
            intent.putExtra("overview", movie.overview)
            intent.putExtra("poster", movie.getPosterUrl())
            intent.putExtra("rating", movie.vote_average.toString())
            startActivity(intent)
        }
        container.addView(cardView)
    }

    private fun loadCinemasNearYou() {
        // this shows real Ster-Kinekor / Nu Metro near Vosloorus using FREE Google Places
        // for now show placeholder, the map will show real ones in Details page
        val fakeCinemas = listOf("Ster-Kinekor East Rand Mall", "Nu Metro Carnival City")
    }
}
