package com.example.cinemap

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cinemap.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private var selectedLanguage = "English"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // language dropdown: English, Sotho, Zulu
        val languages = arrayOf("English", "Sotho", "Zulu")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerLanguage.adapter = adapter

        binding.spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                selectedLanguage = languages[pos]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // theme switch
        binding.switchTheme.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, if(isChecked) "Dark Theme ON" else "Light Theme ON", Toast.LENGTH_SHORT).show()
        }

        // offline switch
        binding.switchOffline.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, if(isChecked) "Offline Mode Enabled" else "Offline Mode Disabled", Toast.LENGTH_SHORT).show()
        }

        binding.btnSaveSettings.setOnClickListener {
            val prefs = getSharedPreferences("CineMap", MODE_PRIVATE)
            prefs.edit()
                .putString("language", selectedLanguage)
                .putBoolean("darkTheme", binding.switchTheme.isChecked)
                .putBoolean("offlineMode", binding.switchOffline.isChecked)
                .apply()

            Toast.makeText(this, "Saved: $selectedLanguage", Toast.LENGTH_LONG).show()
        }

        binding.btnLogout.setOnClickListener {
            finish() // Go back to MainActivity
        }
        // bottom navigation
        binding.btnNavHome.setOnClickListener {
           startActivity(Intent(this, MainActivity::class.java))
        }
        binding.btnNavSearch.setOnClickListener {
           //already here
        }
        binding.btnNavRewards.setOnClickListener {
            startActivity(Intent(this, RewardsActivity::class.java))
        }
        binding.btnNavSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
}