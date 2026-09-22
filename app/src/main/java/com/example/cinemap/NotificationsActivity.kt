package com.example.cinemap

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.cinemap.databinding.ActivityNotificationsBinding

class NotificationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("theme", MODE_PRIVATE)
        val isDark = prefs.getBoolean("isDark", true) // true = dark default
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.chipAll.setOnClickListener {
            Toast.makeText(this, "Showing All", Toast.LENGTH_SHORT).show()
        }
        binding.chipTickets.setOnClickListener {
            Toast.makeText(this, "Filtering Tickets", Toast.LENGTH_SHORT).show()
        }
        binding.chipRewards.setOnClickListener {
            Toast.makeText(this, "Filtering Rewards", Toast.LENGTH_SHORT).show()
        }

        binding.btnHome.setOnClickListener {
            finish()
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