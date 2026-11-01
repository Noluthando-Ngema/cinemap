package com.example.cinemap

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.HideReturnsTransformationMethod
import android.text.method.LinkMovementMethod
import android.text.method.PasswordTransformationMethod
import android.text.style.ClickableSpan
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

        // makes sign up link clickable
        val fullText = "Don't have an account? Sign up"
        val spannable = SpannableString(fullText)
        val start = fullText.indexOf("Sign up")
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@LoginActivity, SignupActivity::class.java))
            }
            override fun updateDrawState(ds: TextPaint) {
                ds.color = getColor(R.color.purple_light)
                ds.isUnderlineText = false
                ds.isFakeBoldText = true
            }
        }
        spannable.setSpan(clickableSpan, start, start + 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.tvGoToSignup.text = spannable
        binding.tvGoToSignup.movementMethod = LinkMovementMethod.getInstance()
        binding.tvGoToSignup.highlightColor = Color.TRANSPARENT

        // toggle password visibility feature
        var isPasswordVisible = false
        binding.btnTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                binding.etPasswordLogin.transformationMethod = HideReturnsTransformationMethod.getInstance()
            } else {
                binding.etPasswordLogin.transformationMethod = PasswordTransformationMethod.getInstance()
            }
            binding.etPasswordLogin.setSelection(binding.etPasswordLogin.text.length)
        }

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

        // checks email format before hitting firebase
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
                    // success, email & password matched in firebase database
                    val user = auth.currentUser
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    // failed - email or password incorrect
                    binding.btnLogin.isEnabled = true
                    binding.btnLogin.text = "Login"

                    // error message
                    binding.tvGeneralError.text = "email/ password incorrect, please try again, and if you haven't signed up, please signup"
                    binding.tvGeneralError.visibility = View.VISIBLE
                }
            }
    }
}
