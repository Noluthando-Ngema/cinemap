package com.example.cinemap

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.cinemap.databinding.ActivityPaymentBinding

class PaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding
    private var selectedMethod = "VISA"
    private var finalTotal = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("theme", MODE_PRIVATE)
        val isDark = prefs.getBoolean("isDark", true) // true = dark default
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // gets data from booking page
        val movieTitle = intent.getStringExtra("movie_title") ?: "The Last Cartographer"
        val format = intent.getStringExtra("format") ?: "Standard"
        val seats = intent.getStringExtra("seats") ?: "A1"
        val rows = intent.getStringExtra("rows") ?: "A"
        val ticketCount = intent.getIntExtra("ticket_count", 1)
        val subtotal = intent.getIntExtra("subtotal", 0)

        val serviceFee = 50
        finalTotal = subtotal + serviceFee

        // setting ui elements
        binding.tvMovieTitle.text = movieTitle
        binding.tvFormat.text = "• $format"
        binding.tvSeatsInfo.text = "${ticketCount}x Adult Tickets - Row $rows - Seats $seats"
        binding.tvTicketsLabel.text = "Tickets ($ticketCount)"
        binding.tvSubtotal.text = "R$subtotal"
        binding.tvTotal.text = "R$finalTotal"
        binding.btnConfirmPay.text = "🔒 Confirm & Pay R$finalTotal"

        binding.btnBack.setOnClickListener { finish() }

        // payment methods
        fun selectPayment(method: String) {
            selectedMethod = method
            // Reset all
            binding.cardVisa.background = getDrawable(R.drawable.bg_payment_unselected)
            binding.cardApple.background = getDrawable(R.drawable.bg_payment_unselected)
            binding.cardGoogle.background = getDrawable(R.drawable.bg_payment_unselected)
            binding.checkVisa.text = "○"
            binding.checkApple.text = "○"
            binding.checkGoogle.text = "○"
            binding.checkVisa.setTextColor(getColor(android.R.color.darker_gray))
            binding.checkApple.setTextColor(getColor(android.R.color.darker_gray))
            binding.checkGoogle.setTextColor(getColor(android.R.color.darker_gray))

            when (method) {
                "VISA" -> {
                    binding.cardVisa.background = getDrawable(R.drawable.bg_payment_selected)
                    binding.checkVisa.text = "●"
                    binding.checkVisa.setTextColor(getColor(R.color.purple_light))
                }
                "APPLE" -> {
                    binding.cardApple.background = getDrawable(R.drawable.bg_payment_selected)
                    binding.checkApple.text = "●"
                    binding.checkApple.setTextColor(getColor(R.color.purple_light))
                }
                "GOOGLE" -> {
                    binding.cardGoogle.background = getDrawable(R.drawable.bg_payment_selected)
                    binding.checkGoogle.text = "●"
                    binding.checkGoogle.setTextColor(getColor(R.color.purple_light))
                }
            }
        }

        // default selected
        selectPayment("VISA")

        binding.cardVisa.setOnClickListener { selectPayment("VISA") }
        binding.cardApple.setOnClickListener { selectPayment("APPLE") }
        binding.cardGoogle.setOnClickListener { selectPayment("GOOGLE") }

        binding.btnAddCard.setOnClickListener {
            Toast.makeText(this, "Add new card - Coming soon", Toast.LENGTH_SHORT).show()
        }

        // confirm and pay
        binding.btnConfirmPay.setOnClickListener {
            val intent = Intent(this, PaymentConfirmationActivity::class.java)
            intent.putExtra("movie_title", movieTitle)
            intent.putExtra("seats", seats) // e.g. "G12, G13"
            intent.putExtra("total_paid", finalTotal)
            intent.putExtra("payment_method", selectedMethod)
            intent.putExtra("selected_date", "Today, 7:30 PM") // you can pass actual selected date from booking
            intent.putExtra("cinema", "Starlight Multiplex")
            startActivity(intent)
        }
    }
}