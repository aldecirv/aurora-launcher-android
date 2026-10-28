package org.aurora.launcher

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

/**
 * Aplica o tema salvo antes de qualquer Activity ser criada.
 */
class AuroraApp : Application() {

    override fun onCreate() {
        super.onCreate()
        applyTheme()
    }

    fun applyTheme() {
        AppCompatDelegate.setDefaultNightMode(
            when (Prefs.theme(this)) {
                Prefs.THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                Prefs.THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )
    }
}
