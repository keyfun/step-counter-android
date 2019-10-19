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
import keyfun.app.stepcounter.core.SharedPreferencesManager
import keyfun.app.stepcounter.utils.AppUtils
import kotlinx.android.synthetic.main.main_fragment.*
import java.util.*


class MainFragment : Fragment(), SensorEventListener {

    private var isStarted = false
    private var sensorManager: SensorManager? = null
    private var stepCounter = 0
    private var counterSteps = 0
    private var stepDetector = 0
    private var startDate: Date? = null
    private var endDate: Date? = null

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

        // load previous status if any
        startDate = SharedPreferencesManager.instance.loadStartDate()
        counterSteps = SharedPreferencesManager.instance.loadInitialStepCount()

        Log.d(TAG, "startDate = $startDate")
        Log.d(TAG, "counterSteps = $counterSteps")

        if (startDate != null) {
            isStarted = true
        }

        initUI()
        bindUI()
    }

    override fun onResume() {
        super.onResume()
        if (this.isStarted) {
            startService()
        }
    }

    override fun onPause() {
        super.onPause()
        stopService()
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
                    SharedPreferencesManager.instance.saveInitialStepCount(counterSteps)
                }
                stepCounter = event.values[0].toInt() - counterSteps
            }
        }
        tv_step_count.text = "$stepCounter"
    }

    private fun initUI() {
        startDate?.let {
            tv_start_time.text = getString(R.string.start_time, AppUtils.getFormattedDate(it))
        } ?: run {
            tv_start_time.text = getString(R.string.start_time, "-")
        }

        tv_end_time.text = getString(R.string.end_time, "-")
        tv_step_count.text = "$stepCounter"

        if (this.isStarted) {
            btn_start.text = getString(R.string.stop)
        } else {
            btn_start.text = getString(R.string.start)
        }
    }

    private fun bindUI() {
        btn_start.setOnClickListener {
            if (this.isStarted) {
                stopCounter()
                btn_start.text = getString(R.string.start)
            } else {
                startCounter()
                btn_start.text = getString(R.string.stop)
            }
        }
    }

    private fun startService() {
        val stepsSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepsSensor == null) {
            Toast.makeText(context, "No Step Counter Sensor !", Toast.LENGTH_SHORT).show()
        } else {
            sensorManager?.registerListener(this, stepsSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    private fun stopService() {
        sensorManager?.unregisterListener(this)
    }

    private fun startCounter() {
        isStarted = true
        this.stepCounter = 0
        this.stepDetector = 0
        this.counterSteps = 0

        // reset UI
        initUI()

        startDate = Date()
        startDate?.let {
            tv_start_time.text =
                getString(R.string.start_time, AppUtils.getFormattedDate(it))
            SharedPreferencesManager.instance.saveStartDate(it)
        }

        startService()
    }

    private fun stopCounter() {
        Log.d(TAG, "stopCounter")
        isStarted = false
        endDate = Date()
        endDate?.let {
            tv_end_time.text =
                getString(R.string.end_time, AppUtils.getFormattedDate(it))
        }

        stopService()
        SharedPreferencesManager.instance.clear()
    }
}
