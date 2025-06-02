import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.io.android.AudioDispatcherFactory
import be.tarsos.dsp.pitch.PitchProcessor
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm

class PitchDetector {
    private var dispatcher: AudioDispatcher? = null
    private var isRunning = false

    // Start pitch detection with a callback for pitch updates
    fun startPitchDetection(callback: (Float) -> Unit) {
        if (isRunning) return // Prevent multiple instances
        isRunning = true

        // Create an AudioDispatcher for real-time audio processing
        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(44100, 7168, 0)

        // Set up the PitchProcessor
        val pitchProcessor = PitchProcessor(
            PitchEstimationAlgorithm.YIN,
            44100F,
            1024
        ) { result, _ ->
            val pitchInHz = result.pitch
            if (pitchInHz > 0) {
                callback(pitchInHz) // Send pitch data to the UI or other components
            }
        }        // Add the PitchProcessor to the dispatcher
        dispatcher?.addAudioProcessor(pitchProcessor)

        // Run the dispatcher in a separate thread
        Thread {
            dispatcher?.run()
        }.start()
    }

    // Stop the pitch detection
    fun stopPitchDetection() {
        isRunning = false
        dispatcher?.stop()
    }
}
