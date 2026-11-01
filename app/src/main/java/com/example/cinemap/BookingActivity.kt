package com.example.cinemap

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.bumptech.glide.Glide
import com.example.cinemap.databinding.ActivityBookingBinding
import java.text.SimpleDateFormat
import java.util.*

class BookingActivity : BaseActivity() {

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

            if (::binding.isInitialized) {
                Glide.with(this).load(posterUrl).into(binding.imgBookingPoster)
            }
        } catch (e: Exception) {

        }

        binding.btnBack.setOnClickListener { finish() }

        // Setup real-time calendar
        setupCalendar()

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

    private fun setupCalendar() {
        binding.dateContainer.removeAllViews()
        val calendar = Calendar.getInstance()
        val dayOfWeekFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val dayNumFormat = SimpleDateFormat("dd", Locale.getDefault())

        for (i in 0 until 5) {
            val dayName = if (i == 0) "TODAY" else dayOfWeekFormat.format(calendar.time).uppercase()
            val dayNum = dayNumFormat.format(calendar.time)

            val dateCard = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    if (i < 4) setMargins(0, 0, 6, 0)
                }
                setBackgroundResource(if (i == 0) R.drawable.bg_date_selected else R.drawable.bg_date_unselected)
                setPadding(8, 8, 8, 8)
                gravity = Gravity.CENTER
            }

            val tvDay = TextView(this).apply {
                text = dayName
                textSize = 8f
                setTypeface(null, Typeface.BOLD)
                setTextColor(if (i == 0) 0xFF0B1020.toInt() else getColor(R.color.text_secondary))
                gravity = Gravity.CENTER
            }

            val tvNum = TextView(this).apply {
                text = dayNum
                textSize = 16f
                setTypeface(null, Typeface.BOLD)
                setTextColor(if (i == 0) 0xFF0B1020.toInt() else getColor(R.color.text_primary))
                gravity = Gravity.CENTER
            }

            dateCard.addView(tvDay)
            dateCard.addView(tvNum)

            dateCard.setOnClickListener {
                for (j in 0 until binding.dateContainer.childCount) {
                    val child = binding.dateContainer.getChildAt(j) as LinearLayout
                    child.setBackgroundResource(R.drawable.bg_date_unselected)
                    (child.getChildAt(0) as TextView).setTextColor(getColor(R.color.text_secondary))
                    (child.getChildAt(1) as TextView).setTextColor(getColor(R.color.text_primary))
                }
                dateCard.setBackgroundResource(R.drawable.bg_date_selected)
                tvDay.setTextColor(0xFF0B1020.toInt())
                tvNum.setTextColor(0xFF0B1020.toInt())
            }

            binding.dateContainer.addView(dateCard)
            calendar.add(Calendar.DAY_OF_YEAR, 1)
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
