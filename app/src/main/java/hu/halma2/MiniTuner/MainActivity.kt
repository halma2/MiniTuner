package hu.halma2.MiniTuner

import PitchDetector
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private val pitchDetector = PitchDetector()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.openSingleNoteTunerButton).setOnClickListener {
            val intent = Intent(this, SingleNoteTunerActivity::class.java)
            startActivity(intent)
        }

        setupReferencePitchAdjuster()
        // Request permissions and start pitch detection
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 1)
        } else {
            startPitchDetection()
        }
    }

    private fun startPitchDetection() {
        pitchDetector.startPitchDetection { pitchInHz ->
            // Log.d("MainActivity", "Pitch detected: $pitchInHz Hz")
            val noteName = frequencyToNoteName(pitchInHz, currentReferencePitch)
            val centsOffset = calculateCentsOffset(pitchInHz, currentReferencePitch)
            runOnUiThread {
                findViewById<TextView>(R.id.frequencyTextView).text = "Pitch: ${String.format("%.2f", pitchInHz)} Hz"
                findViewById<TextView>(R.id.noteTextView).text = "Note: $noteName"
                findViewById<TextView>(R.id.noteTextView).text =
                    "Pitch: ${String.format("%.2f", pitchInHz)} Hz\nNote: $noteName (${centsOffset} cents)"
            }

        }
    }

    fun frequencyToNoteName(frequency: Float, referencePitch: Float = 440.0f): String {
        if (frequency <= 0) return "N/A"
        val noteNames = arrayOf("C", "C#", "D", "Eb", "E", "F", "F#", "G", "G#", "A", "Bb", "B")
        val semitonesFromA4 = (12 * Math.log(frequency / referencePitch.toDouble()) / Math.log(2.0)).toInt()
        val noteIndex = (69 + semitonesFromA4) % 12
        return noteNames[noteIndex]
    }

    private var currentReferencePitch = 440.0f

    private fun setupReferencePitchAdjuster() {
        val seekBar = findViewById<SeekBar>(R.id.referencePitchSeekBar)
        val referencePitchTextView = findViewById<TextView>(R.id.referencePitchTextView)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentReferencePitch = 440.0f + (progress - 10) // Adjust range as needed
                referencePitchTextView.text = "Reference Pitch: ${String.format("%.2f", currentReferencePitch)} Hz"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    fun calculateCentsOffset(frequency: Float, referencePitch: Float): Int {
        val semitonesFromA4 = 12 * Math.log(frequency / referencePitch.toDouble()) / Math.log(2.0)
        val closestSemitone = Math.round(semitonesFromA4).toDouble()
        return ((semitonesFromA4 - closestSemitone) * 100).toInt()
    }


    override fun onDestroy() {
        super.onDestroy()
        // Log.d("MainActivity", "onDestroy called")
        // Stop pitch detection
        pitchDetector.stopPitchDetection()
    }
}
