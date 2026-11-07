package com.example.data.local.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.domain.model.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("pak_signal_prefs", Context.MODE_PRIVATE)

    private val _languageFlow = MutableStateFlow(loadLanguage())
    val languageFlow: StateFlow<AppLanguage> = _languageFlow.asStateFlow()

    private fun loadLanguage(): AppLanguage {
        val code = prefs.getString(KEY_LANGUAGE, AppLanguage.ENGLISH.code)
        return AppLanguage.values().firstOrNull { it.code == code } ?: AppLanguage.ENGLISH
    }

    fun setLanguage(language: AppLanguage) {
        prefs.edit().putString(KEY_LANGUAGE, language.code).apply()
        _languageFlow.value = language
    }

    companion object {
        private const val KEY_LANGUAGE = "app_language"
    }
}
