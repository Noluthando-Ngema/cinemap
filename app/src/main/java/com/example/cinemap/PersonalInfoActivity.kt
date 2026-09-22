package com.example.cinemap

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

        // load current
        if (userId != null) {
            db.collection("users").document(userId).get().addOnSuccessListener { doc ->
                edtName.setText(doc.getString("name"))
                edtEmail.setText(doc.getString("email"))
            }
        }

        findViewById<Button>(R.id.btnSaveInfo).setOnClickListener {
            if (userId != null) {
                db.collection("users").document(userId).update(
                    mapOf(
                        "name" to edtName.text.toString(),
                        "email" to edtEmail.text.toString()
                    )
                ).addOnSuccessListener {
                    Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
                    finish() // Sends them back to Settings page
                }
            }
        }
    }
}