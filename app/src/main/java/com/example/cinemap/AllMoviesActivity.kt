package com.example.cinemap

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.cinemap.data.RetrofitClient
import kotlinx.coroutines.launch

class AllMoviesActivity : BaseActivity() {

    private lateinit var allMoviesContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("theme", MODE_PRIVATE)
        val isDark = prefs.getBoolean("isDark", true)
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_movies)

        allMoviesContainer = findViewById(R.id.allMoviesContainer)
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        loadAllMovies()
    }

    private fun loadAllMovies() {
        lifecycleScope.launch {
            try {
                val nowShowing = RetrofitClient.tmdbApi.getNowPlaying(RetrofitClient.TMDB_API_KEY)
                allMoviesContainer.removeAllViews()
                nowShowing.results.forEach { movie ->
                    val card = LayoutInflater.from(this@AllMoviesActivity)
                        .inflate(R.layout.item_movie_card, allMoviesContainer, false)
                    card.findViewById<TextView>(R.id.txtMovieTitle).text = movie.title
                    val img = card.findViewById<ImageView>(R.id.imgMoviePoster)
                    Glide.with(this@AllMoviesActivity).load(movie.getPosterUrl()).into(img)

                    card.setOnClickListener {
                        val intent = Intent(this@AllMoviesActivity, BookingActivity::class.java).apply {
                            putExtra("title", movie.title)
                            putExtra("overview", movie.overview)
                            putExtra("poster", movie.getPosterUrl())
                            putExtra("rating", movie.vote_average.toString())
                            putExtra("movieId", movie.id)
                            putExtra("cinema_name", "East Rand Mall - Ster-Kinekor")
                            putExtra("ticketPrice", "R 120")
                            putExtra("showTimes", "13:00, 16:30, 19:00, 21:30")
                        }
                        startActivity(intent)
                    }
                    allMoviesContainer.addView(card)
                }
            } catch (e: Exception) {
                Toast.makeText(this@AllMoviesActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
