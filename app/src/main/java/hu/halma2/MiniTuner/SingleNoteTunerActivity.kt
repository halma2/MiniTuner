package hu.halma2.MiniTuner

import PitchDetector
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

class SingleNoteTunerActivity : AppCompatActivity() {
    private val pitchDetector = PitchDetector()
    private var currentReferencePitch = 440.0f
    private val targetFrequency = 932.0f // A# target frequency
    private val toleranceRange = 10.0f // Tolerance range in Hz

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_note_tuner)



        findViewById<Button>(R.id.backToMainButton).setOnClickListener {
            finish()
        }
        setupReferencePitchAdjuster()

        startPitchDetection()
    }

    private fun setupReferencePitchAdjuster() {
        val seekBar = findViewById<SeekBar>(R.id.singleNoteReferencePitchSeekBar)
        val referencePitchTextView = findViewById<TextView>(R.id.referencePitchSingleNoteTextView)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentReferencePitch = 440.0f + (progress - 10) // Adjust range as needed
                referencePitchTextView.text = "Reference Pitch: ${String.format("%.2f", currentReferencePitch)} Hz"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun startPitchDetection() {
        val layout = findViewById<ConstraintLayout>(R.id.rootLayout) // Ensure the root layout ID is correct

        pitchDetector.startPitchDetection { pitchInHz ->
            val isWithinRange = pitchInHz in (targetFrequency - toleranceRange)..(targetFrequency + toleranceRange)
            val centsOffset = calculateCentsOffset(pitchInHz, targetFrequency)

            Log.d("SingleNoteTuner", "Pitch: $pitchInHz Hz, Offset: $centsOffset cents")

            runOnUiThread {
                findViewById<TextView>(R.id.offsetTextView).text = "Offset: $centsOffset cents"

                if (isWithinRange) {
                    layout.setBackgroundColor(Color.GREEN) // Change background to green
                } else {
                    layout.setBackgroundColor(Color.WHITE) // Reset background to default
                }
            }
        }
    }

    private fun calculateCentsOffset(frequency: Float, targetFrequency: Float): Int {
        val semitonesFromTarget = 12 * Math.log(frequency / targetFrequency.toDouble()) / Math.log(2.0)
        val closestSemitone = Math.round(semitonesFromTarget).toDouble()
        return ((semitonesFromTarget - closestSemitone) * 100).toInt()
    }

    override fun onDestroy() {
        super.onDestroy()
        pitchDetector.stopPitchDetection()
    }
}
