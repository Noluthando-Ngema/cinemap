package com.example.cinemap

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.cinemap.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("theme", MODE_PRIVATE)
        val isDark = prefs.getBoolean("isDark", true) // true = dark default
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // makes login link clickable ---
        val fullText = "Already have an account? Login"
        val spannable = android.text.SpannableString(fullText)
        val start = fullText.indexOf("Login")
        val clickableSpan = object : android.text.style.ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
                finish()
            }
            override fun updateDrawState(ds: android.text.TextPaint) {
                ds.color = getColor(R.color.purple_light)
                ds.isUnderlineText = false
                ds.isFakeBoldText = true
            }
        }
        spannable.setSpan(clickableSpan, start, start+5, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.tvGoToLogin.text = spannable
        binding.tvGoToLogin.movementMethod = android.text.method.LinkMovementMethod.getInstance()
        binding.tvGoToLogin.highlightColor = android.graphics.Color.TRANSPARENT

        binding.btnSignup.setOnClickListener {
            if (validateAndSignup()) {
                // validation passes
            }
        }
    }

    private fun validateAndSignup(): Boolean {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        var isValid = true

        // reset errors
        binding.tvEmailError.visibility = View.GONE
        binding.tvPasswordError.visibility = View.GONE

        // email validation
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tvEmailError.text = "incorrect email format"
            binding.tvEmailError.visibility = View.VISIBLE
            isValid = false
        }

        // password validation
        val passwordError = getPasswordError(password)
        if (passwordError != null) {
            binding.tvPasswordError.text = passwordError
            binding.tvPasswordError.visibility = View.VISIBLE
            isValid = false
        }

        if (!isValid) return false

        // firebase signup, password is hashed & encrypted by Firebase
        binding.btnSignup.isEnabled = false
        binding.btnSignup.text = "Creating account..."

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // password is hashed & encrypted by Firebase, never stored plain
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    binding.tvEmailError.text = task.exception?.message ?: "Signup failed"
                    binding.tvEmailError.visibility = View.VISIBLE
                    binding.btnSignup.isEnabled = true
                    binding.btnSignup.text = "Sign Up"
                }
            }
        return true
    }

    // returns error message in red, null if valid
    private fun getPasswordError(password: String): String? {
        if (password.length < 8) {
            return "Password must be at least 8 characters long"
        }
        if (!password.any { it.isUpperCase() }) {
            return "Password must include a capital letter"
        }
        if (!password.any { it.isDigit() }) {
            return "Password must include a number"
        }
        if (!password.any { !it.isLetterOrDigit() }) {
            return "Password must include a special character e.g. @#\$%"
        }
        return null
    }
}