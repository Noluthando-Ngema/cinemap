package com.example.cinemap

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.cinemap.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // bottom navigation
        binding.btnNavHome.setOnClickListener {
            // already here
        }
        binding.btnNavNotification.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }
        binding.btnNavRewards.setOnClickListener {
            startActivity(Intent(this, RewardsActivity::class.java))
        }
        binding.btnNavSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

    }
}
