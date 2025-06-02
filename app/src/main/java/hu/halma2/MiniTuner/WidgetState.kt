package hu.halma2.MiniTuner

import android.content.Context

object WidgetState {
    private const val PREFS_NAME = "WidgetPrefs"
    private const val KEY_IS_RECORDING = "isRecording"

    fun isRecording(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_IS_RECORDING, false)
    }

    fun setRecording(context: Context, isRecording: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_IS_RECORDING, isRecording).apply()
    }
}
