package com.example.cinemap

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.cinemap.utils.TranslationManager
import kotlinx.coroutines.launch

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        applyTranslation()
    }

    private fun applyTranslation() {
        val root = findViewById<View>(android.R.id.content) as ViewGroup
        lifecycleScope.launch {
            translateViews(root)
        }
    }

    private suspend fun translateViews(viewGroup: ViewGroup) {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            if (child is TextView) {
                // Don't translate movie titles from TMDB (they have tag "no_translate")
                if (child.tag != "no_translate" && child.text.isNotBlank()) {
                    val original = child.text.toString()
                    // save original in tag to avoid double translation
                    if (child.getTag(R.id.original_text) == null) {
                        child.setTag(R.id.original_text, original)
                    }
                    val translated = TranslationManager.translate(this@BaseActivity, child.getTag(R.id.original_text) as String)
                    child.text = translated
                }
            } else if (child is ViewGroup) {
                translateViews(child)
            }
        }
    }
}