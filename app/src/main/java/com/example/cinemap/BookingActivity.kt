package com.example.cinemap

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.bumptech.glide.Glide
import com.example.cinemap.databinding.ActivityBookingBinding

class BookingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookingBinding

    private val rowPrices = mapOf("A" to 450, "B" to 400, "C" to 350, "D" to 300)
    private val selectedSeatsMap = mutableMapOf<String, String>()

    // This will hold the real movie title from TMDB
    private var currentMovieTitle = "Movie"

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("theme", MODE_PRIVATE)
        val isDark = prefs.getBoolean("isDark", true)
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
        super.onCreate(savedInstanceState)
        binding = ActivityBookingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // get data from MainActivity (TMDB movie clicked)
        currentMovieTitle = intent.getStringExtra("title")?: "Movie Title"
        val posterUrl = intent.getStringExtra("poster") // this is already full URL: https://image.tmdb.org/t/p/w500...
        val cinemaName = intent.getStringExtra("cinema_name")?: "East Rand Mall - Ster-Kinekor"
        val rating = intent.getStringExtra("rating")?: "8.0"
        val showTimes = intent.getStringExtra("showTimes")?: "18:00, 20:30"

        // setting views
        try {
            binding.txtBookingMovieTitle.text = currentMovieTitle
            binding.txtBookingCinemaName.text = "Showing at: $cinemaName"
            binding.txtBookingShowTimes.text = showTimes
            // binding.txtBookingRating.text = "★ $rating"

            // load poster - make sure you have an ImageView in booking layout e.g. imgBookingPoster
            if (::binding.isInitialized) {
                // binding.imgBookingPoster is example
                Glide.with(this).load(posterUrl).into(binding.imgBookingPoster)
            }
        } catch (e: Exception) {

        }

        binding.btnBack.setOnClickListener { finish() }

        // build seats
        val rows = listOf("A", "B", "C", "D")
        val cols = 6
        // add rows of seats
        for (row in rows) {
            val label = TextView(this)
            label.text = row
            label.setTextColor(getColor(android.R.color.white))
            label.textSize = 12f
            label.gravity = Gravity.CENTER
            label.width = 60
            label.height = 80
            binding.seatGrid.addView(label)
            // add seats in row
            for (col in 1..cols) {
                val seatId = "$row$col"
                val seat = TextView(this)
                seat.width = 80
                seat.height = 80
                seat.gravity = Gravity.CENTER
                seat.textSize = 8f
                seat.text = seatId
                seat.setTextColor(getColor(android.R.color.white))
                seat.background = getDrawable(R.drawable.bg_seat_available)
                seat.tag = "available"
            // FIXED: seat taken
                if ((row == "B" && col == 3) || (row == "C" && col == 5)) {
                    seat.background = getDrawable(R.drawable.bg_seat_taken)
                    seat.tag = "taken"
                    seat.text = ""
                } else {
                    seat.setOnClickListener {
                        if (seat.tag == "taken") return@setOnClickListener
                        if (selectedSeatsMap.containsKey(seatId)) {
                            seat.background = getDrawable(R.drawable.bg_seat_available)
                            selectedSeatsMap.remove(seatId)
                        } else {
                            seat.background = getDrawable(R.drawable.bg_seat_selected)
                            seat.setTextColor(getColor(R.color.black))
                            selectedSeatsMap[seatId] = row
                        }
                        updateBottomBar()
                    }
                }
                binding.seatGrid.addView(seat)
            }
        }
        // set grid layout
        binding.seatGrid.columnCount = 7
        updateBottomBar()

        binding.btnContinue.setOnClickListener {
            if (selectedSeatsMap.isEmpty()) {
                Toast.makeText(this, "Select at least 1 seat", Toast.LENGTH_SHORT).show()
            } else {
                val totalTickets = selectedSeatsMap.size
                val subtotal = calculateTotal()
                val seatsList = selectedSeatsMap.keys.joinToString(", ")

                val intent = Intent(this, PaymentActivity::class.java)
                // FIXED: Now uses real TMDB title, not hardcoded
                intent.putExtra("movie_title", currentMovieTitle)
                intent.putExtra("movie_poster", posterUrl)
                intent.putExtra("cinema_name", cinemaName)
                intent.putExtra("format", "IMAX Laser")
                intent.putExtra("seats", seatsList)
                intent.putExtra("ticket_count", totalTickets)
                intent.putExtra("subtotal", subtotal)
                startActivity(intent)
            }
        }
    }
    // calculate total price
    private fun calculateTotal(): Int {
        var total = 0
        for ((_, row) in selectedSeatsMap) {
            total += rowPrices[row]?: 0
        }
        return total
    }
    // update bottom bar
    private fun updateBottomBar() {
        val count = selectedSeatsMap.size
        val total = calculateTotal()
        binding.tvTicketCount.text = if (count == 0) "0 Tickets" else "$count Ticket${if (count > 1) "s" else ""} - ${selectedSeatsMap.keys.joinToString(", ")}"
        binding.tvTotalPrice.text = "R$total.00"
    // disable button if no seats selected
        if (count > 0) {
            binding.btnContinue.text = "Pay R$total"
            binding.btnContinue.backgroundTintList = getColorStateList(R.color.purple_light)
            binding.btnContinue.setTextColor(getColor(R.color.black))
        } else {
            binding.btnContinue.text = "Select Seats to Continue"
            binding.btnContinue.backgroundTintList = getColorStateList(R.color.gray_dark)
            binding.btnContinue.setTextColor(getColor(android.R.color.darker_gray))
        }
    }
}