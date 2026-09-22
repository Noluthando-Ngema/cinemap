package com.example.cinemap

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.cinemap.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("theme", MODE_PRIVATE)
        val isDark = prefs.getBoolean("isDark", true) // true = dark default
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // makes Sign up link clickable
        val fullText = "Don't have an account? Sign up"
        val spannable = android.text.SpannableString(fullText)
        val start = fullText.indexOf("Sign up")
        val clickableSpan = object : android.text.style.ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@LoginActivity, SignupActivity::class.java))
            }
            override fun updateDrawState(ds: android.text.TextPaint) {
                ds.color = getColor(R.color.purple_light)
                ds.isUnderlineText = false
                ds.isFakeBoldText = true
            }
        }
        spannable.setSpan(clickableSpan, start, start + 7, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.tvGoToSignup.text = spannable
        binding.tvGoToSignup.movementMethod = android.text.method.LinkMovementMethod.getInstance()
        binding.tvGoToSignup.highlightColor = android.graphics.Color.TRANSPARENT

        binding.btnLogin.setOnClickListener {
            loginWithFirebase()
        }
    }

    private fun loginWithFirebase() {
        val email = binding.etEmailLogin.text.toString().trim()
        val password = binding.etPasswordLogin.text.toString().trim()

        // hide errors first
        binding.tvEmailError.visibility = View.GONE
        binding.tvPasswordError.visibility = View.GONE
        binding.tvGeneralError.visibility = View.GONE

        var isValid = true

        // checks email format before hitting Firebase
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tvEmailError.text = "incorrect email format"
            binding.tvEmailError.visibility = View.VISIBLE
            isValid = false
        }

        if (password.isEmpty()) {
            binding.tvPasswordError.text = "Password is required"
            binding.tvPasswordError.visibility = View.VISIBLE
            isValid = false
        }

        if (!isValid) return

        // checks firebase database for credentials
        binding.btnLogin.isEnabled = false
        binding.btnLogin.text = "Checking credentials..."

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // SUCCESS, Email & Password matched in Firebase Database
                    val user = auth.currentUser
                    // user is now logged in, password was verified with Firebase's hashed version
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    // FAILED - Email or password incorrect
                    binding.btnLogin.isEnabled = true
                    binding.btnLogin.text = "Login"

                    //  error message
                    binding.tvGeneralError.text = "email/ password incorrect, please try again, and if you haven't signed up, please signup"
                    binding.tvGeneralError.visibility = View.VISIBLE
                }
            }
    }
}