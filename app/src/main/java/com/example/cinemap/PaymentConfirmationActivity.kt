package com.example.cinemap

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.cinemap.databinding.ActivityPaymentConfirmationBinding
import com.example.cinemap.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlin.random.Random

class PaymentConfirmationActivity : BaseActivity() {

    private lateinit var binding: ActivityPaymentConfirmationBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("theme", MODE_PRIVATE)
        val isDark = prefs.getBoolean("isDark", true)
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentConfirmationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userId = auth.currentUser?.uid ?: return

        // data from payment and booking pages
        val movieTitle = intent.getStringExtra("movie_title") ?: "The Last Cartographer"
        val seatsRaw = intent.getStringExtra("seats") ?: "G12, G13"
        val totalPaid = intent.getIntExtra("total_paid", 450)
        val date = intent.getStringExtra("selected_date") ?: "Today, 7:30 PM"
        val cinema = intent.getStringExtra("cinema") ?: "Starlight Multiplex"

        val seatsList = seatsRaw.split(",").map { it.trim() }
        val rows = seatsList.map { it.filter { c -> c.isLetter() } }.distinct()
        val seatNumbersOnly = seatsList.map { it.filter { c -> c.isDigit() } }

        val rowDisplay = if (rows.size == 1) rows[0] else rows.joinToString(", ")
        val seatsDisplay = seatNumbersOnly.joinToString(", ")
        val ticketCount = seatsList.size

        // UI
        binding.tvMovieTitle.text = movieTitle
        binding.tvDateTime.text = date
        binding.tvTicketCount.text = "$ticketCount ${if (ticketCount == 1) "Ticket" else "Tickets"}"
        binding.tvCinema.text = cinema
        binding.tvScreen.text = "Screen 4"
        binding.tvRow.text = rowDisplay
        binding.tvSeats.text = seatsDisplay
        binding.tvTotalPaid.text = "R$totalPaid"

        val bookingId = "CM-${Random.nextInt(10000, 99999)}"
        binding.tvBookingId.text = "BOOKING ID: #$bookingId"

        // --- THIS IS THE REAL LOGIC ---
        processBookingAndReward(userId, movieTitle, seatsList, totalPaid, bookingId, cinema, date)

        binding.btnBackToHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun processBookingAndReward(
        userId: String,
        movieTitle: String,
        seatsList: List<String>,
        totalPaid: Int,
        bookingId: String,
        cinema: String,
        date: String
    ) {
        // 1. Get current bookings count to calculate reward
        db.collection("users").document(userId).collection("bookings").get()
            .addOnSuccessListener { snap ->
                val totalBookingsBefore = snap.size()
                val totalBookingsNow = totalBookingsBefore + 1

                // 2. Decide reward based on YOUR rules
                var xp = 50
                var title = "First Movie! +50 XP"
                var msg = "Free Popcorn unlocked!"
                var badgeId = "badge_first"

                when {
                    totalBookingsNow == 1 -> {
                        xp = 50
                        title = "First Movie! +50 XP"
                        msg = "Free Popcorn | Badge: First Movie"
                        badgeId = "badge_first"
                    }
                    seatsList.size == 2 -> {
                        xp = 100
                        title = "Movie Monster! +100 XP"
                        msg = "x2 Popcorn + Drink + 25% OFF next | Badge: Movie Monster"
                        badgeId = "badge_monster"
                    }
                    totalBookingsNow > 2 -> {
                        xp = 50
                        title = "Movie Guru! +50 XP"
                        msg = "Snack Basket: Popcorn+Drink+Chocolate+Astros + 50% OFF | Badge: Movie Guru"
                        badgeId = "badge_guru"
                    }
                }

                // 3. Save booking
                val bookingData = hashMapOf(
                    "bookingId" to bookingId,
                    "movieTitle" to movieTitle,
                    "seats" to seatsList,
                    "cinema" to cinema,
                    "date" to date,
                    "totalPaid" to totalPaid,
                    "timestamp" to FieldValue.serverTimestamp()
                )
                db.collection("users").document(userId).collection("bookings").document(bookingId).set(bookingData)

                // 4. Update points (create user doc if not exists)
                db.collection("users").document(userId).set(
                    hashMapOf("points" to xp), SetOptions.merge()
                )
                db.collection("users").document(userId).update("points", FieldValue.increment(xp.toLong()))
                    .addOnFailureListener {
                        // if update fails, first time user
                        db.collection("users").document(userId).set(hashMapOf("points" to xp), SetOptions.merge())
                    }

                // 5. Create 2 notifications: one for booking, one for reward (this is what your NotificationsActivity reads)
                val bookingNotif = hashMapOf(
                    "title" to "Booking Confirmed - $movieTitle",
                    "message" to "$bookingId | $cinema | Seats ${seatsList.joinToString(",")} | $date",
                    "xp" to 0,
                    "badgeId" to "ticket",
                    "timestamp" to FieldValue.serverTimestamp(),
                    "isRead" to false
                )
                db.collection("users").document(userId).collection("notifications").add(bookingNotif)

                val rewardNotif = hashMapOf(
                    "title" to title,
                    "message" to msg,
                    "xp" to xp,
                    "badgeId" to badgeId,
                    "timestamp" to FieldValue.serverTimestamp(),
                    "isRead" to false
                )
                db.collection("users").document(userId).collection("notifications").add(rewardNotif)

                // 6. Show system push notification (real time)
                NotificationHelper.showRewardNotification(this, title, "$movieTitle booked - $msg")
            }
    }
}