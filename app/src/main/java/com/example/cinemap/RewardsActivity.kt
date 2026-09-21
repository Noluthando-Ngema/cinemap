package com.example.cinemap

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cinemap.databinding.ActivityRewardsBinding

class RewardsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRewardsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
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
    }
}