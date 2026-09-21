package com.example.cinemap

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cinemap.databinding.ActivityBookingBinding

class BookingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookingBinding

    // price per row
    private val rowPrices = mapOf(
        "A" to 450,
        "B" to 400,
        "C" to 350,
        "D" to 300
    )

    // store selected seats to row letter
    private val selectedSeatsMap = mutableMapOf<String, String>() // seatId to row

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        // build seats
        val rows = listOf("A", "B", "C", "D")
        val cols = 6

        for (row in rows) {
            // row label
            val label = TextView(this)
            label.text = row
            label.setTextColor(getColor(android.R.color.white))
            label.textSize = 12f
            label.gravity = Gravity.CENTER
            label.width = 60
            label.height = 80
            binding.seatGrid.addView(label)

            for (col in 1..cols) {
                val seatId = "$row$col"
                val seat = TextView(this)
                seat.width = 80
                seat.height = 80
                seat.gravity = Gravity.CENTER
                seat.textSize = 8f
                seat.text = seatId
                seat.setTextColor(getColor(android.R.color.white))

                // default: available
                seat.background = getDrawable(R.drawable.bg_seat_available)
                seat.tag = "available"

                // example taken seats
                if ((row == "B" && col == 3) || (row == "C" && col == 5)) {
                    seat.background = getDrawable(R.drawable.bg_seat_taken)
                    seat.tag = "taken"
                    seat.text = ""
                } else {
                    seat.setOnClickListener {
                        if (seat.tag == "taken") return@setOnClickListener

                        if (selectedSeatsMap.containsKey(seatId)) {
                            // Deselect
                            seat.background = getDrawable(R.drawable.bg_seat_available)
                            selectedSeatsMap.remove(seatId)
                        } else {
                            // select
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

        // making grid have 7 columns, 1 label + 6 seats
        binding.seatGrid.columnCount = 7

        updateBottomBar()

        binding.btnContinue.setOnClickListener {
            if (selectedSeatsMap.isEmpty()) {
                Toast.makeText(this, "Select at least 1 seat", Toast.LENGTH_SHORT).show()
            } else {
                val totalTickets = selectedSeatsMap.size
                val subtotal = calculateTotal()
                val seatsList = selectedSeatsMap.keys.joinToString(", ") // e.g. A1, B2
                val rowsList = selectedSeatsMap.values.distinct().joinToString(", ") // e.g. A, B

                val intent = Intent(this, PaymentActivity::class.java)
                intent.putExtra("movie_title", "The Last Cartographer") // you can make this dynamic
                intent.putExtra("format", "IMAX Laser")
                intent.putExtra("seats", seatsList)
                intent.putExtra("rows", rowsList)
                intent.putExtra("ticket_count", totalTickets)
                intent.putExtra("subtotal", subtotal)
                startActivity(intent)
            }
        }
    }
    // calculate total price based on selected seats
    private fun calculateTotal(): Int {
        var total = 0
        for ((_, row) in selectedSeatsMap) {
            total += rowPrices[row]?: 0
        }
        return total
    }

    private fun updateBottomBar() {
        val count = selectedSeatsMap.size
        val total = calculateTotal()

        binding.tvTicketCount.text = if (count == 0) "0 Tickets" else "$count Ticket${if(count>1) "s" else ""} - ${selectedSeatsMap.keys.joinToString(", ")}"
        binding.tvTotalPrice.text = "R$total.00"

        if (count > 0) {
            binding.btnContinue.text = "Pay R$total"
            binding.btnContinue.backgroundTintList = getColorStateList(R.color.purple_light)
            binding.btnContinue.setTextColor(getColor(R.color.black))
            binding.btnContinue.isEnabled = true
        } else {
            binding.btnContinue.text = "Select Seats to Continue"
            binding.btnContinue.backgroundTintList = getColorStateList(R.color.gray_dark)
            binding.btnContinue.setTextColor(getColor(android.R.color.darker_gray))
            binding.btnContinue.isEnabled = true
        }
    }
}