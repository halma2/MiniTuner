package hu.halma2.MiniTuner

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.io.android.AudioDispatcherFactory
import be.tarsos.dsp.pitch.PitchProcessor
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm

class PitchDetectionService : Service() {
    private var dispatcher: AudioDispatcher? = null
    private var isRunning = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isRunning) {
            startPitchDetection()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPitchDetection()
    }

    private fun startPitchDetection() {
        isRunning = true

        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(44100, 1024, 0)
        val pitchProcessor = PitchProcessor(
            PitchEstimationAlgorithm.YIN,
            44100F,
            1024
        ) { result, _ ->
            val pitchInHz = result.pitch
            if (pitchInHz > 0) {
                updateWidget(pitchInHz)
            }
        }
        dispatcher?.addAudioProcessor(pitchProcessor)

        Thread { dispatcher?.run() }.start()
    }

    private fun stopPitchDetection() {
        isRunning = false
        dispatcher?.stop()
        dispatcher = null
    }

    private fun updateWidget(pitchInHz: Float) {
        val views = RemoteViews(packageName, R.layout.single_note_tuner_widget)
        views.setTextViewText(
            R.id.widgetPitchTextView,
            "Pitch: ${String.format("%.2f", pitchInHz)} Hz"
        )
        val componentName = ComponentName(this, SingleNoteTunerWidget::class.java)
        AppWidgetManager.getInstance(this).updateAppWidget(componentName, views)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
