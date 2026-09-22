package com.example.cinemap

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.cinemap.databinding.ActivityRewardsBinding

class RewardsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRewardsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("theme", MODE_PRIVATE)
        val isDark = prefs.getBoolean("isDark", true) // true = dark default
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
        super.onCreate(savedInstanceState)
        binding = ActivityRewardsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // demo data
        val points = 1250
        val goldNeeded = 1500
        val left = goldNeeded - points // 250 pts left

        binding.progressGold.max = goldNeeded
        binding.progressGold.progress = points

        binding.cardPopcorn.setOnClickListener {
            Toast.makeText(this, "Free Popcorn - Small combo applied!", Toast.LENGTH_SHORT).show()
        }

        binding.cardDiscount.setOnClickListener {
            Toast.makeText(this, "20% Off your next ticket!", Toast.LENGTH_SHORT).show()
        }

        binding.btnViewAll.setOnClickListener {
            Toast.makeText(this, "$points pts - $left pts to Gold", Toast.LENGTH_SHORT).show()
        }

        binding.btnHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
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
}