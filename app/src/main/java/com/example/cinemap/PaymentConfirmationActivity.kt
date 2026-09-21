package com.example.cinemap

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.cinemap.databinding.ActivityPaymentConfirmationBinding
import kotlin.random.Random

class PaymentConfirmationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentConfirmationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentConfirmationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // data from payment and booking pages
        val movieTitle = intent.getStringExtra("movie_title")?: "The Last Cartographer"
        val seatsRaw = intent.getStringExtra("seats")?: "G12, G13" // e.g. "G12, G13" or "A1, B2"
        val totalPaid = intent.getIntExtra("total_paid", 450)
        val date = intent.getStringExtra("selected_date")?: "Today, 7:30 PM"
        val cinema = intent.getStringExtra("cinema")?: "Starlight Multiplex"

        // parse seats...row and seats numbers, e.g "A1, A2" -> Row = A, Seats = 1, 2
        val seatsList = seatsRaw.split(",").map { it.trim() }
        val rows = seatsList.map { it.filter { c -> c.isLetter() } }.distinct()
        val seatNumbers = seatsList.map { it.filter { c -> c.isDigit() } }

        val rowDisplay = if (rows.size == 1) rows[0] else rows.joinToString(", ")
        val seatsDisplay = seatNumbers.joinToString(", ")
        val ticketCount = seatsList.size

        // setting ui elements
        binding.tvMovieTitle.text = movieTitle
        binding.tvDateTime.text = date
        binding.tvTicketCount.text = "$ticketCount ${if (ticketCount == 1) "Ticket" else "Tickets"}"
        binding.tvCinema.text = cinema
        binding.tvScreen.text = "Screen 4" // you can make dynamic if you want
        binding.tvRow.text = rowDisplay
        binding.tvSeats.text = seatsDisplay
        binding.tvTotalPaid.text = "R$totalPaid"

        // random booking ID
        val bookingId = "CM-${Random.nextInt(10000, 99999)}"
        binding.tvBookingId.text = "BOOKING ID: #$bookingId"

        binding.btnBackToHome.setOnClickListener {
            // goes back to landing page and clear all previous pages
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}