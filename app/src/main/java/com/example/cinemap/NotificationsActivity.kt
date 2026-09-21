package com.example.cinemap

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cinemap.databinding.ActivityNotificationsBinding

class NotificationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
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
    }
}