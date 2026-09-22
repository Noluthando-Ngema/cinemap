package com.example.cinemap

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PersonalInfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("theme", MODE_PRIVATE)
        val isDark = prefs.getBoolean("isDark", true) // true = dark default
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_info)

        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val userId = auth.currentUser?.uid

        val edtName = findViewById<EditText>(R.id.edtName)
        val edtEmail = findViewById<EditText>(R.id.edtEmail)

        // local cache to make it show instantly in settings
        val localPrefs = getSharedPreferences("user_profile", MODE_PRIVATE)

        // load current
        if (userId != null) {
            db.collection("users").document(userId).get().addOnSuccessListener { doc ->
                edtName.setText(doc.getString("name"))
                edtEmail.setText(doc.getString("email"))
                // also cache locally
                localPrefs.edit()
                    .putString("user_name", doc.getString("name"))
                    .putString("user_email", doc.getString("email"))
                    .apply()
            }
        } else {
            // if offline, load from local cache
            edtName.setText(localPrefs.getString("user_name", ""))
            edtEmail.setText(localPrefs.getString("user_email", ""))
        }

        findViewById<Button>(R.id.btnSaveInfo).setOnClickListener {
            val newName = edtName.text.toString().trim()
            val newEmail = edtEmail.text.toString().trim()

            // simple validation
            if (newName.isEmpty()) {
                edtName.error = "name required"
                return@setOnClickListener
            }
            if (newEmail.isEmpty()) {
                edtEmail.error = "email required"
                return@setOnClickListener
            }

            // save locally first so settings shows it instantly even offline
            localPrefs.edit()
                .putString("user_name", newName)
                .putString("user_email", newEmail)
                .apply()

            if (userId != null) {
                db.collection("users").document(userId).update(
                    mapOf(
                        "name" to newName,
                        "email" to newEmail
                    )
                ).addOnSuccessListener {
                    Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
                    finish() // Sends them back to Settings page
                }.addOnFailureListener {
                    // even if firebase fails, we already saved locally
                    Toast.makeText(this, "Saved locally", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } else {
                Toast.makeText(this, "Saved locally", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, SettingsActivity::class.java))
            }
        }
    }
}