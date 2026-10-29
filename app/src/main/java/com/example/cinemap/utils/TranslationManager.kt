package com.example.cinemap.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.cinemap.data.TranslationClient
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await

object TranslationManager {

    private fun getLangCode(name: String): String = when(name) {
        "Afrikaans" -> "af"
        else -> "en"
    }

    private fun getMlKitLang(name: String) = when(name) {
        "Afrikaans" -> TranslateLanguage.AFRIKAANS
        else -> TranslateLanguage.ENGLISH
    }

    fun getCurrentLanguage(prefs: SharedPreferences): String {
        return prefs.getString("app_language", "English") ?: "English"
    }

    // main function to translate any string
    suspend fun translate(context: Context, text: String): String {
        val prefs = context.getSharedPreferences("CineMapPrefs", Context.MODE_PRIVATE)
        val langName = getCurrentLanguage(prefs)
        if (langName == "English" || text.isBlank()) return text

        val prefsCache = context.getSharedPreferences("translation_cache", Context.MODE_PRIVATE)
        val cacheKey = "${getLangCode(langName)}_$text"
        prefsCache.getString(cacheKey, null)?.let { return it }

        return try {
            // try catch for ML Kit (offline, works after model downloaded)
            val mlResult = translateWithMlKit(text, langName)
            prefsCache.edit().putString(cacheKey, mlResult).apply()
            mlResult
        } catch (e: Exception) {
            try {
                // fallback to MyMemory API (online)
                val pair = "en|${getLangCode(langName)}"
                val apiResult = TranslationClient.api.translate(text, pair).responseData.translatedText
                prefsCache.edit().putString(cacheKey, apiResult).apply()
                apiResult
            } catch (e2: Exception) {
                text // return original if both fail
            }
        }
    }

    private suspend fun translateWithMlKit(text: String, langName: String): String {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(getMlKitLang(langName))
            .build()
        val translator = Translation.getClient(options)
        translator.downloadModelIfNeeded().await()
        val result = translator.translate(text).await()
        translator.close()
        return result
    }

    fun downloadModelIfNeeded(langName: String, onDone: () -> Unit) {
        if (langName == "English") { onDone(); return }
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(getMlKitLang(langName))
            .build()
        val translator = Translation.getClient(options)
        translator.downloadModelIfNeeded()
            .addOnSuccessListener { onDone() }
            .addOnFailureListener { onDone() }
    }
}
