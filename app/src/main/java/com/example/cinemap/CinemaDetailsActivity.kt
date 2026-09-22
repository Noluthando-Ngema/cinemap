package com.example.cinemap

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cinemap.databinding.ActivityCinemaDetailsBinding
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker

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
            // no API key needed, opens Google Maps app
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
        org.osmdroid.config.Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))

        val map = binding.mapView
        map.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)

        val cinemaLocation = org.osmdroid.util.GeoPoint(-26.3557, 28.2086) // Vosloorus - replace with real lat/lng later
        map.controller.setZoom(15.0)
        map.controller.setCenter(cinemaLocation)

        val marker = org.osmdroid.views.overlay.Marker(map)
        marker.position = cinemaLocation
        marker.title = name ?: "Cinema"
        marker.setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM)
        map.overlays.add(marker)
        map.invalidate()

        //makes directions more reliable
        binding.btnDirections.setOnClickListener {
            // if address is empty, use coordinates
            val geoUri = "geo:${cinemaLocation.latitude},${cinemaLocation.longitude}?q=${cinemaLocation.latitude},${cinemaLocation.longitude}(${name})"
            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(geoUri))
            startActivity(intent)
        }
    }
}