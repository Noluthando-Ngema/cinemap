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
import java.io.File
import java.io.FileOutputStream

class SettingsActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val PICK_IMAGE = 100
    private var imageUri: Uri? = null

    // add local prefs for offline cache
    private lateinit var localPrefs: android.content.SharedPreferences

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

        // init local cache prefs
        localPrefs = getSharedPreferences("user_profile", MODE_PRIVATE)

        val userId = auth.currentUser?.uid

        // load name/email from signup (Firebase)
        if (userId != null) {
            db.collection("users").document(userId).get().addOnSuccessListener { doc ->
                txtName.text = doc.getString("name") ?: "User"
                txtEmail.text = doc.getString("email") ?: auth.currentUser?.email
                val pic = doc.getString("profilePic")
                // if firebase has pic, load it
                if (!pic.isNullOrEmpty()) {
                    Glide.with(this).load(pic).circleCrop().into(imgProfile)
                    // also save firebase url locally for offline
                    localPrefs.edit().putString("firebase_pic_url", pic).apply()
                } else {
                    // if no firebase pic, try to load local saved file
                    loadLocalImage(imgProfile)
                }
            }.addOnFailureListener {
                // if offline, load local image and local name/email
                loadLocalImage(imgProfile)
                txtName.text = localPrefs.getString("user_name", "User")
                txtEmail.text = localPrefs.getString("user_email", auth.currentUser?.email)
            }
        } else {
            // no user logged in, load local cache
            loadLocalImage(imgProfile)
        }

        // pen botton goes to gallery
        findViewById<ImageView>(R.id.btnEditPic).setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE)
        }

        // also allow clicking the profile image itself
        imgProfile.setOnClickListener {
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
            startActivity(Intent(this, MainActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.navRewards).setOnClickListener {
            startActivity(Intent(this, RewardsActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.navNotifications).setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.navSettings).setOnClickListener {
            // already here, do nothing or refresh
        }
    }

    // this runs when user comes back from personal info - refresh name/email
    override fun onResume() {
        super.onResume()
        // reload name/email from firebase + local cache
        val txtName = findViewById<TextView>(R.id.txtName)
        val txtEmail = findViewById<TextView>(R.id.txtEmail)
        val imgProfile = findViewById<ImageView>(R.id.imgProfile)

        txtName.text = localPrefs.getString("user_name", txtName.text.toString())
        txtEmail.text = localPrefs.getString("user_email", txtEmail.text.toString())
        loadLocalImage(imgProfile)

        // also try firebase again
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId).get().addOnSuccessListener { doc ->
                val name = doc.getString("name")
                val email = doc.getString("email")
                if (!name.isNullOrEmpty()) txtName.text = name
                if (!email.isNullOrEmpty()) txtEmail.text = email
            }
        }
    }

    // load image saved in internal storage so it stays forever
    private fun loadLocalImage(imgView: ImageView) {
        // first check if we have a local file path
        val localPath = localPrefs.getString("profile_image_path", null)
        if (localPath != null) {
            val file = File(localPath)
            if (file.exists()) {
                Glide.with(this).load(file).circleCrop().into(imgView)
                return
            }
        }
        // fallback to firebase url if no local file
        val firebaseUrl = localPrefs.getString("firebase_pic_url", null)
        if (!firebaseUrl.isNullOrEmpty()) {
            Glide.with(this).load(firebaseUrl).circleCrop().into(imgView)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            imageUri = data?.data
            if (imageUri == null) return

            // show instantly while uploading
            Glide.with(this).load(imageUri).circleCrop().into(findViewById<ImageView>(R.id.imgProfile))

            // save copy locally so it stays after reboot/offline
            try {
                val inputStream = contentResolver.openInputStream(imageUri!!)
                val file = File(filesDir, "profile_image.jpg")
                val outputStream = FileOutputStream(file)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()
                // save local path
                localPrefs.edit().putString("profile_image_path", file.absolutePath).apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // also upload to firebase so it stays on other devices
            val userId = auth.currentUser?.uid ?: return
            val ref = storage.reference.child("profile_pics/$userId.jpg")
            ref.putFile(imageUri!!).addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { url ->
                    db.collection("users").document(userId).update("profilePic", url.toString())
                    // cache url locally too
                    localPrefs.edit().putString("firebase_pic_url", url.toString()).apply()
                    Glide.with(this).load(url).circleCrop().into(findViewById<ImageView>(R.id.imgProfile))
                    Toast.makeText(this, "Profile photo updated", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}