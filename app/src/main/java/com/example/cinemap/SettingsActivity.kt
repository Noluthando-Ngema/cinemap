package com.example.cinemap

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class SettingsActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val PICK_IMAGE = 100
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefer = getSharedPreferences("theme", MODE_PRIVATE)
        val isDark = prefer.getBoolean("isDark", true) // true = dark default
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val imgProfile = findViewById<ImageView>(R.id.imgProfile)
        val txtName = findViewById<TextView>(R.id.txtName)
        val txtEmail = findViewById<TextView>(R.id.txtEmail)
        val switchTheme = findViewById<Switch>(R.id.switchTheme)

        val userId = auth.currentUser?.uid

        // load name/email from signup (Firebase)
        if (userId != null) {
            db.collection("users").document(userId).get().addOnSuccessListener { doc ->
                txtName.text = doc.getString("name") ?: "User"
                txtEmail.text = doc.getString("email") ?: auth.currentUser?.email
                val pic = doc.getString("profilePic")
                if (!pic.isNullOrEmpty()) Glide.with(this).load(pic).into(imgProfile)
            }
        }

        // pen botton goes to gallery
        findViewById<ImageView>(R.id.btnEditPic).setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE)
        }

        // goes topersonal information page
        findViewById<LinearLayout>(R.id.btnPersonalInfo).setOnClickListener {
            startActivity(Intent(this, PersonalInfoActivity::class.java))
        }

        // dark mode to light mode toggle
        val switchThemes = findViewById<Switch>(R.id.switchTheme)
        val prefs = getSharedPreferences("theme", MODE_PRIVATE)
        switchThemes.isChecked = prefs.getBoolean("isDark", true)

        switchThemes.setOnCheckedChangeListener { _, isDark ->
            prefs.edit().putBoolean("isDark", isDark).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
            recreate() // instantly changes whole app
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            imageUri = data?.data
            val userId = auth.currentUser?.uid ?: return
            val ref = storage.reference.child("profile_pics/$userId.jpg")
            ref.putFile(imageUri!!).addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { url ->
                    db.collection("users").document(userId).update("profilePic", url.toString())
                    Glide.with(this).load(url).into(findViewById<ImageView>(R.id.imgProfile))
                    Toast.makeText(this, "Profile photo updated", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}