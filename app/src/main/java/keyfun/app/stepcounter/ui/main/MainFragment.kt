package keyfun.app.stepcounter.ui.main

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import keyfun.app.stepcounter.R
import kotlinx.android.synthetic.main.main_fragment.*


class MainFragment : Fragment(), SensorEventListener {

    private var running = false
    private var sensorManager: SensorManager? = null
    private var stepCounter = 0
    private var counterSteps = 0
    private var stepDetector = 0

    companion object {
        fun newInstance() = MainFragment()
        const val TAG = "MainFragment"
    }

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        // TODO: Use the ViewModel

        sensorManager = context?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    override fun onResume() {
        super.onResume()
        running = true
        val stepsSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepsSensor == null) {
            Toast.makeText(context, "No Step Counter Sensor !", Toast.LENGTH_SHORT).show()
        } else {
            sensorManager?.registerListener(this, stepsSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        running = false
        sensorManager?.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(TAG, "accuracy = $accuracy")
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_STEP_DETECTOR -> {
                stepDetector++
            }
            Sensor.TYPE_STEP_COUNTER -> {
                if (counterSteps < 1) {
                    counterSteps = event.values[0].toInt()
                }
                stepCounter = event.values[0].toInt() - counterSteps
            }
        }
        stepsValue.text = "$stepCounter"
    }
}
