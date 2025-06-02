package hu.halma2.MiniTuner

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews

class SingleNoteTunerWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_TOGGLE_RECORDING) {
            val isRecording = WidgetState.isRecording(context)
            WidgetState.setRecording(context, !isRecording)

            // Update the widget UI
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val views = RemoteViews(context.packageName, R.layout.single_note_tuner_widget)

            if (!isRecording) {
                // Start recording
                views.setTextViewText(R.id.widgetRecordButton, "Stop")
                Log.d("TunerWidget", "Recording started")
                startPitchDetectionService(context)
            } else {
                // Stop recording
                views.setTextViewText(R.id.widgetRecordButton, "Start")
                Log.d("TunerWidget", "Recording stopped")
                stopPitchDetectionService(context)
            }

            // Update all widgets
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, SingleNoteTunerWidget::class.java)
            )
            for (appWidgetId in appWidgetIds) {
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }

    private fun startPitchDetectionService(context: Context) {
        val intent = Intent(context, PitchDetectionService::class.java)
        context.startService(intent)
    }

    private fun stopPitchDetectionService(context: Context) {
        val intent = Intent(context, PitchDetectionService::class.java)
        context.stopService(intent)
    }

    companion object {
        private const val ACTION_TOGGLE_RECORDING = "hu.halma2.MiniTuner.TOGGLE_RECORDING"

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.single_note_tuner_widget)

            // Set button label based on state
            val isRecording = WidgetState.isRecording(context)
            views.setTextViewText(R.id.widgetRecordButton, if (isRecording) "Stop" else "Start")

            // Set up PendingIntent for button click
            val intent = Intent(context, SingleNoteTunerWidget::class.java).apply {
                action = ACTION_TOGGLE_RECORDING
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widgetRecordButton, pendingIntent)

            // Update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
