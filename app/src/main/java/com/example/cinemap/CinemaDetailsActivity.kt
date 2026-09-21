package com.example.cinemap

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cinemap.databinding.ActivityCinemaDetailsBinding

class CinemaDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCinemaDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCinemaDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // get data from previous page
        val name = intent.getStringExtra("cinema_name") ?: "Starlight Multiplex"
        val address = intent.getStringExtra("cinema_address") ?: "123 Nebula Way, Cosmopolis"

        binding.tvCinemaName.text = name
        binding.tvAddress.text = address

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnDirections.setOnClickListener {
            // opens Google Maps
            val uri = Uri.parse("geo:0,0?q=$address")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.google.android.apps.maps")
            startActivity(intent)
        }

        binding.btnCall.setOnClickListener {
            val callIntent = Intent(Intent.ACTION_DIAL)
            callIntent.data = Uri.parse("tel:0111234567")
            startActivity(callIntent)
        }
    }
}