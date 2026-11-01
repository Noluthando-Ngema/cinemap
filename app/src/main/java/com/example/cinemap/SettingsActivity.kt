package com.example.cinemap

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import com.bumptech.glide.Glide
import com.example.cinemap.data.RetrofitClient
import com.example.cinemap.utils.TranslationManager
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileOutputStream
import java.util.*

class SettingsActivity : BaseActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val PICK_IMAGE = 100
    private var imageUri: Uri? = null

    private lateinit var localPrefs: android.content.SharedPreferences
    private lateinit var switchOffline: SwitchMaterial
    private lateinit var tvStatus: TextView
    private val prefs by lazy { getSharedPreferences("CineMapPrefs", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Theme
        val themePref = getSharedPreferences("theme", MODE_PRIVATE)
        val isDark = themePref.getBoolean("isDark", true)
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Init prefs
        localPrefs = getSharedPreferences("user_profile", MODE_PRIVATE)

        // Views
        val imgProfile = findViewById<ImageView>(R.id.imgProfile)
        val txtName = findViewById<TextView>(R.id.txtName)
        val txtEmail = findViewById<TextView>(R.id.txtEmail)
        switchOffline = findViewById(R.id.switchOfflineMode)
        tvStatus = findViewById(R.id.tvOfflineStatus)
        val switchTheme = findViewById<Switch>(R.id.switchTheme)
        val spinnerLang = findViewById<Spinner>(R.id.spinnerLanguage)

        // ================= 1. OFFLINE TOGGLE =================
        val isOffline = prefs.getBoolean("offline_mode", false)
        switchOffline.isChecked = isOffline
        tvStatus.text = if (isOffline) "Offline - cached movies (7 days)" else "Online - fresh from TMDB"

        switchOffline.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("offline_mode", isChecked).apply()
            RetrofitClient.clearInstance() // force rebuild with new cache mode
            Toast.makeText(
                this,
                if (isChecked) "Offline ON - using cached movies (7 days)" else "Online - fresh from TMDB",
                Toast.LENGTH_LONG
            ).show()
            tvStatus.text = if (isChecked) "Offline - cached (7 days)" else "Online"
        }

        // ================= 2. LANGUAGE WITH REAL API TRANSLATION =================
        val languages = arrayOf("English", "Afrikaans")
        val langAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, languages)
        spinnerLang.adapter = langAdapter

        val savedLang = prefs.getString("app_language", "English") ?: "English"
        spinnerLang.setSelection(languages.indexOf(savedLang))

        var firstLoad = true
        spinnerLang.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                if (firstLoad) { firstLoad = false; return }
                val selected = languages[pos]
                if (selected == savedLang) return

                Toast.makeText(this@SettingsActivity, "Downloading $selected model...", Toast.LENGTH_SHORT).show()

                TranslationManager.downloadModelIfNeeded(selected) {
                    prefs.edit().putString("app_language", selected).apply()
                    Toast.makeText(this@SettingsActivity, "Language: $selected applied", Toast.LENGTH_SHORT).show()
                    recreate() // restart to translate whole app
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // ================= 3. PROFILE LOADING (Firebase + Local) =================
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId).get().addOnSuccessListener { doc ->
                val name = doc.getString("name")
                val email = doc.getString("email")
                txtName.text = name ?: "User"
                txtEmail.text = email ?: auth.currentUser?.email
                localPrefs.edit().putString("user_name", txtName.text.toString()).apply()
                localPrefs.edit().putString("user_email", txtEmail.text.toString()).apply()

                val pic = doc.getString("profilePic")
                if (!pic.isNullOrEmpty()) {
                    Glide.with(this).load(pic).circleCrop().into(imgProfile)
                    localPrefs.edit().putString("firebase_pic_url", pic).apply()
                } else {
                    loadLocalImage(imgProfile)
                }
            }.addOnFailureListener {
                loadLocalImage(imgProfile)
                txtName.text = localPrefs.getString("user_name", "User")
                txtEmail.text = localPrefs.getString("user_email", auth.currentUser?.email)
            }
        } else {
            loadLocalImage(imgProfile)
        }

        findViewById<ImageView>(R.id.btnEditPic).setOnClickListener { openGallery() }
        imgProfile.setOnClickListener { openGallery() }

        // Personal Info
        findViewById<LinearLayout>(R.id.btnPersonalInfo).setOnClickListener {
            startActivity(Intent(this, PersonalInfoActivity::class.java))
        }

        // ================= 4. DARK THEME TOGGLE =================
        switchTheme.isChecked = isDark
        switchTheme.setOnCheckedChangeListener { _, isDarkMode ->
            themePref.edit().putBoolean("isDark", isDarkMode).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
            recreate()
        }

        // ================= 5. BOTTOM NAV =================
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
            // already here
        }

        // Sign Out
        findViewById<Button>(R.id.btnSignOut).setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun setLocale(langName: String) {
        val code = when (langName) {
            "Afrikaans" -> "af"
            "isiZulu" -> "zu"
            else -> "en"
        }
        val locale = Locale(code)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE)
    }

    override fun onResume() {
        super.onResume()
        val txtName = findViewById<TextView>(R.id.txtName)
        val txtEmail = findViewById<TextView>(R.id.txtEmail)
        val imgProfile = findViewById<ImageView>(R.id.imgProfile)

        txtName.text = localPrefs.getString("user_name", txtName.text.toString())
        txtEmail.text = localPrefs.getString("user_email", txtEmail.text.toString())
        loadLocalImage(imgProfile)

        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId).get().addOnSuccessListener { doc ->
                doc.getString("name")?.let { txtName.text = it }
                doc.getString("email")?.let { txtEmail.text = it }
            }
        }
    }

    private fun loadLocalImage(imgView: ImageView) {
        val localPath = localPrefs.getString("profile_image_path", null)
        if (localPath != null) {
            val file = File(localPath)
            if (file.exists()) {
                Glide.with(this).load(file).circleCrop().into(imgView)
                return
            }
        }
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

            Glide.with(this).load(imageUri).circleCrop()
                .into(findViewById<ImageView>(R.id.imgProfile))

            try {
                val inputStream = contentResolver.openInputStream(imageUri!!)
                val file = File(filesDir, "profile_image.jpg")
                val outputStream = FileOutputStream(file)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()
                localPrefs.edit().putString("profile_image_path", file.absolutePath).apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val userId = auth.currentUser?.uid ?: return
            val ref = storage.reference.child("profile_pics/$userId.jpg")
            ref.putFile(imageUri!!).addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { url ->
                    db.collection("users").document(userId).update("profilePic", url.toString())
                    localPrefs.edit().putString("firebase_pic_url", url.toString()).apply()
                    Glide.with(this).load(url).circleCrop()
                        .into(findViewById<ImageView>(R.id.imgProfile))
                    Toast.makeText(this, "Profile photo updated", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}