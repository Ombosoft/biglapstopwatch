package top.ombosoft.biglapstopwatch

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import top.ombosoft.biglapstopwatch.R
import top.ombosoft.biglapstopwatch.databinding.ActivityMainBinding
import java.util.*
import kotlin.concurrent.timer

// TODO test on devices
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var timer: Timer? = null
    private var startTime: Long = 0
    private var lapStart: Long = 0
    private var deltaTotal: Long = 0
    private var deltaLap: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        println("oncreate1")
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.start.setOnClickListener { startStopwatch() }
        binding.lap.setOnClickListener { lapStopwatch() }
        binding.pause.setOnClickListener { stopStopwatch() }
        binding.reset.setOnClickListener { resetStopwatch() }
        initialVisibilitySetup()
        if (savedInstanceState != null) {
            restoreState(savedInstanceState)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        with(outState) {
            putLong("startTime", startTime);
            putLong("lapStart", lapStart);
            putLong("deltaTotal", deltaTotal);
            putLong("deltaLap", deltaLap);
            putBoolean("isStarted", timer != null)
        }
        saveViewVisibility(outState, binding.root)
        println("saved ${outState}")
        println("reset ${binding.reset.visibility}")
    }

    private fun saveViewVisibility(bundle: Bundle, group: ViewGroup) {
        for (v in group.children) {
            if (v is ViewGroup) {
                saveViewVisibility(bundle, v)
            } else {
                println("saving ${v.id} ${v.javaClass} ${if (v is Button) v.text else ""} ${v.visibility}")
                bundle.putInt("visibility_${v.id}", v.visibility)
            }
        }
    }

    private fun loadViewVisibility(bundle: Bundle, group: ViewGroup) {
        for (v in group.children) {
            if (v is ViewGroup) {
                loadViewVisibility(bundle, v)
            } else {
                val vis = bundle.getInt("visibility_${v.id}", -1)
                if (vis >= 0) v.visibility = vis
            }
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        restoreState(savedInstanceState)
    }

    private fun restoreState(savedInstanceState: Bundle) {
        println("loading ${savedInstanceState}")
        with(savedInstanceState) {
            val isStarted = getBoolean("isStarted")
            if (isStarted) {
                startStopwatch()
            }
            startTime = getLong("startTime", 0);
            lapStart = getLong("lapStart", 0);
            deltaTotal = getLong("deltaTotal", 0);
            deltaLap = getLong("deltaLap", 0);
            updateButtonTexts(isStarted)
        }
        loadViewVisibility(savedInstanceState, binding.root)
        renderTime()
        println("reset ${binding.reset.visibility}")
    }

    override fun onPause() {
        super.onPause()
        println("onPause")
    }

    override fun onResume() {
        super.onResume()
        println("onResume")
    }

    private fun initialVisibilitySetup() {
        binding.lapTime.visibility = View.INVISIBLE
        binding.pause.visibility = View.GONE
        binding.lap.visibility = View.INVISIBLE
        binding.reset.visibility = View.INVISIBLE
    }

    private fun startStopwatch() {
        updateButtonTexts(true)
        binding.start.visibility = View.GONE
        binding.pause.visibility = View.VISIBLE
        binding.lap.visibility = View.VISIBLE
        binding.reset.visibility = View.INVISIBLE
        startTime = nowMillis()
        lapStart = startTime
        timer = timer(initialDelay = 1000, period = 250, action = {
            renderTime()
        })
    }

    private fun updateButtonTexts(isStarted: Boolean) {
        binding.start.text =
            getText(if (isStarted || startTime == 0L) R.string.start else R.string.restart)
    }

    private fun renderTime() {
        val isStarted = timer != null;
        val now = nowMillis()
        val elapsed = if (isStarted) now - startTime else 0
        val lapTime = if (isStarted) now - lapStart else 0
        Handler(mainLooper).post {
            binding.totalTime.text = formatTime(elapsed + deltaTotal)
            binding.lapTime.text = formatTime(lapTime + deltaLap)
        }
    }

    private fun stopStopwatch() {
        updateButtonTexts(false)
        binding.start.visibility = View.VISIBLE
        binding.pause.visibility = View.GONE
        binding.lap.visibility = View.INVISIBLE
        binding.reset.visibility = View.VISIBLE
        val now = nowMillis()
        deltaTotal += now - startTime
        deltaLap += now - lapStart
        timer?.cancel()
        timer = null
        binding.lap.visibility = View.INVISIBLE
    }

    private fun lapStopwatch() {
        lapStart = nowMillis()
        deltaLap = 0
        binding.lapTime.text = formatTime(0)
        binding.lapTime.visibility = View.VISIBLE
    }

    private fun resetStopwatch() {
        updateButtonTexts(true)
        startTime = 0
        lapStart = 0
        deltaTotal = 0
        deltaLap = 0
        initialVisibilitySetup()
        binding.totalTime.text = formatTime(0)
    }

    private fun nowMillis() = Calendar.getInstance().timeInMillis

    private fun formatTime(elapsed: Long): String {
        val seconds = elapsed / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val format = "%02d:%02d:%02d".format(hours, minutes % 60, seconds % 60)
        return format
    }
}