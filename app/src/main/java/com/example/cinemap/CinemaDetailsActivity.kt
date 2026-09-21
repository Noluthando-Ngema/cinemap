package com.example.cinemap

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cinemap.databinding.ActivityCinemaDetailsBinding
import com.google.android.libraries.maps.model.Marker
import org.osmdroid.util.GeoPoint

class CinemaDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCinemaDetailsBinding
    private lateinit var map: org.osmdroid.views.MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityCinemaDetailsBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val name = intent.getStringExtra("cinema_name") ?: "Starlight Multiplex"
        val address = intent.getStringExtra("cinema_address") ?: "123 Nebula Way, Cosmopolis"

        // also get movie if coming from landing page
        val movieTitle = intent.getStringExtra("title")
        if(movieTitle != null) {
            binding.tvCinemaName.text = movieTitle
        } else {
            binding.tvCinemaName.text = name
        }
        binding.tvAddress.text = address

        binding.btnBack.setOnClickListener {
            finish()
        }
        // get directions
        binding.btnDirections.setOnClickListener {
            // This still works - no API key needed, opens Google Maps app
            val uri = Uri.parse("geo:0,0?q=$address")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.google.android.apps.maps")
            startActivity(intent)
        }
        // call cinema
        binding.btnCall.setOnClickListener {
            val callIntent = Intent(Intent.ACTION_DIAL)
            callIntent.data = Uri.parse("tel:0111234567")
            startActivity(callIntent)
        }
        // setup map
        org.osmdroid.config.Configuration.getInstance().userAgentValue = packageName

        map = binding.mapView // or findViewById(R.id.mapView) if binding doesn't have it
        map.setMultiTouchControls(true)

        val cinemaLocation = GeoPoint(-26.3557, 28.2086) // Vosloorus
        map.controller.setZoom(13.0)
        map.controller.setCenter(cinemaLocation)

        val marker = Marker(map)
        marker.position = cinemaLocation
        marker.title = name
        map.overlays.add(marker)
        map.invalidate()
    }
}