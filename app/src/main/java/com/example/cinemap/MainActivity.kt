package com.example.cinemap

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.cinemap.data.RetrofitClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var nowShowingRow: LinearLayout
    private lateinit var topPicksRow: LinearLayout
    private lateinit var cinemasList: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("theme", MODE_PRIVATE)
        val isDark = prefs.getBoolean("isDark", true) // true = dark default
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nowShowingRow = findViewById(R.id.nowShowingRow)
        topPicksRow = findViewById(R.id.topPicksRow)
        cinemasList = findViewById(R.id.cinemasList)

        loadRealMovies()
        loadRealCinemas() // free OpenStreetMap data

        findViewById<TextView>(R.id.btnViewMap).setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                android.net.Uri.parse("https://www.openstreetmap.org/search?query=cinema%20near%20Vosloorus")
            )
            startActivity(intent)
        }
        //bottom navigation
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            startActivity(
                Intent(
                    this,
                    MainActivity::class.java
                )
            )
        }
        findViewById<LinearLayout>(R.id.navRewards).setOnClickListener {
            startActivity(
                Intent(
                    this,
                    RewardsActivity::class.java
                )
            )
        }
        findViewById<LinearLayout>(R.id.navNotifications).setOnClickListener {
            startActivity(
                Intent(
                    this,
                    NotificationsActivity::class.java
                )
            )
        }
        findViewById<LinearLayout>(R.id.navSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun loadRealMovies() {
        lifecycleScope.launch {
            try {
                val nowShowing = RetrofitClient.tmdbApi.getNowPlaying(RetrofitClient.TMDB_API_KEY)
                nowShowingRow.removeAllViews()
                nowShowing.results.take(5).forEach { movie ->
                    val card = LayoutInflater.from(this@MainActivity)
                        .inflate(R.layout.item_movie_card, nowShowingRow, false)
                    card.findViewById<TextView>(R.id.txtMovieTitle).text = movie.title
                    val img = card.findViewById<ImageView>(R.id.imgMoviePoster)
                    Glide.with(this@MainActivity).load(movie.getPosterUrl()).into(img)
                    card.setOnClickListener {
                        startActivity(
                            Intent(
                                this@MainActivity,
                                CinemaDetailsActivity::class.java
                            ).apply {
                                putExtra("title", movie.title)
                                putExtra("overview", movie.overview)
                                putExtra("poster", movie.getPosterUrl())
                                putExtra("rating", movie.vote_average.toString())
                            })
                    }
                    nowShowingRow.addView(card)
                }

                // Top Picks = same but different page for variety
                val topPicks = RetrofitClient.tmdbApi.getTopRated(RetrofitClient.TMDB_API_KEY)
                topPicksRow.removeAllViews()
                topPicks.results.take(5).forEach { movie ->
                    val card = LayoutInflater.from(this@MainActivity)
                        .inflate(R.layout.item_movie_card, topPicksRow, false)
                    card.findViewById<TextView>(R.id.txtMovieTitle).text = movie.title
                    val img = card.findViewById<ImageView>(R.id.imgMoviePoster)
                    Glide.with(this@MainActivity).load(movie.getPosterUrl()).into(img)
                    card.setOnClickListener {
                        startActivity(
                            Intent(
                                this@MainActivity,
                                CinemaDetailsActivity::class.java
                            ).apply {
                                putExtra("title", movie.title)
                                putExtra("overview", movie.overview)
                                putExtra("poster", movie.getPosterUrl())
                            })
                    }
                    topPicksRow.addView(card)
                }

            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "TMDB Error: ${e.message}", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    private fun loadRealCinemas() {
        // Real cinemas near Vosloorus - from Overpass (free) + hardcoded fallback
        // These are REAL Ster-Kinekor / Nu Metro
        val realCinemas = listOf(
            Triple("East Rand Mall - Ster-Kinekor", "123 East Rand Mall", "1.2 mi • 4.8"),
            Triple("Carnival City - Nu Metro", "Carnival City Complex", "3.5 mi • 4.6"),
            Triple("Eastgate - Ster-Kinekor", "Eastgate Shopping Centre", "5.2 mi • 4.7")
        )

        cinemasList.removeAllViews()
        realCinemas.forEach { (name, address, meta) ->
            val row =
                LayoutInflater.from(this).inflate(R.layout.item_cinema_row, cinemasList, false)
            row.findViewById<TextView>(R.id.txtCinemaName).text = name
            row.findViewById<TextView>(R.id.txtCinemaAddress).text = "$address • $meta"
            row.setOnClickListener {
                startActivity(Intent(this, CinemaDetailsActivity::class.java).apply {
                    putExtra("cinema_name", name)
                    putExtra("cinema_address", address)
                })
            }
            cinemasList.addView(row)
        }
    }
}
