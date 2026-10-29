package com.example.cinemap

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.cinemap.databinding.ActivityRewardsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RewardsActivity : BaseActivity() {

    private lateinit var binding: ActivityRewardsBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("theme", MODE_PRIVATE)
        val isDark = prefs.getBoolean("isDark", true)
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
        super.onCreate(savedInstanceState)
        binding = ActivityRewardsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadRealRewards()

        // bottom nav
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.navRewards).setOnClickListener { }
        findViewById<LinearLayout>(R.id.navNotifications).setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.navSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        binding.btnHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun loadRealRewards() {
        val uid = auth.currentUser?.uid ?: return

        // 1. Get Points + Bookings Count
        db.collection("users").document(uid).get().addOnSuccessListener { userDoc ->
            val points = userDoc.getLong("points")?.toInt() ?: 0

            db.collection("users").document(uid).collection("bookings").get()
                .addOnSuccessListener { bookingSnap ->
                    val totalBookings = bookingSnap.size()

                    // --- LOGIC FOR LEVELS ---
                    val goldNeeded = 1500
                    val level = when {
                        totalBookings == 0 -> "BRONZE MEMBER"
                        totalBookings == 1 -> "SILVER MEMBER"
                        totalBookings == 2 -> "MOVIE MONSTER"
                        else -> "MOVIE GURU"
                    }

                    // Update UI
                    binding.tvPoints.text = "$points pts"
                    binding.tvLevel.text = level
                    binding.progressGold.max = goldNeeded
                    binding.progressGold.progress = points.coerceAtMost(goldNeeded)
                    binding.tvPointsLeft.text = "${(goldNeeded - points).coerceAtLeast(0)} pts left"

                    // --- REWARDS UNLOCK LOGIC ---
                    // Reward 1: First Booking = Free Popcorn
                    if (totalBookings >= 1) {
                        binding.cardPopcorn.alpha = 1f
                        binding.tvPopcornTitle.text = "Free Popcorn"
                        binding.tvPopcornDesc.text = "Unlocked - Small combo"
                    } else {
                        binding.cardPopcorn.alpha = 0.4f
                        binding.tvPopcornTitle.text = "Locked"
                        binding.tvPopcornDesc.text = "Book 1 movie to unlock"
                    }

                    // Reward 2: 2 Seats in one booking OR 2 total bookings = 25% OFF
                    if (totalBookings >= 2) {
                        binding.cardDiscount.alpha = 1f
                        binding.tvDiscountTitle.text = "25% Off"
                        binding.tvDiscountDesc.text = "Movie Monster Reward"
                    } else {
                        binding.cardDiscount.alpha = 0.4f
                        binding.tvDiscountTitle.text = "20% Off"
                        binding.tvDiscountDesc.text = "Book 2 movies to unlock"
                    }

                    // Reward 3: Guru basket - show only if guru
                    if (totalBookings > 2) {
                        binding.cardGuru.visibility = View.VISIBLE
                    }

                    binding.cardPopcorn.setOnClickListener {
                        if (totalBookings >= 1) Toast.makeText(this, "Reward: Free Popcorn - Show at counter", Toast.LENGTH_SHORT).show()
                        else Toast.makeText(this, "Book your first movie!", Toast.LENGTH_SHORT).show()
                    }

                    binding.cardDiscount.setOnClickListener {
                        if (totalBookings >= 2) Toast.makeText(this, "Reward: 25% OFF applied on next booking!", Toast.LENGTH_SHORT).show()
                        else Toast.makeText(this, "Book 2 movies to unlock", Toast.LENGTH_SHORT).show()
                    }

                    binding.btnViewAll.setOnClickListener {
                        Toast.makeText(this, "$points pts - ${goldNeeded - points} pts to Gold - $totalBookings bookings", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}