package com.abdul.hscrollview

import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.abdul.hscrollview.databinding.ActivityMainBinding
import com.cb.horizontalRuler.HorizontalRuler
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.apply {
            xmlRuler.setOnValueChangeListener { value ->
                tvXmlValue.text = "Selected: ${formatValue(value)}"
            }

            codeRuler.setOnValueChangeListener { value ->
                tvCodeValue.text = "Selected: ${formatValue(value)}"
            }

            configureCodeRulerSample(codeRuler)

            btnApplyRuntimeConfig.setOnClickListener {
                configureCodeRulerSample(codeRuler)
            }

            btnAnimateToValue.setOnClickListener {
                codeRuler.scrollToValue(value = 42.5f, animated = true)
            }
        }
    }

    private fun configureCodeRulerSample(ruler: HorizontalRuler) {
        ruler.setRange(min = -50f, max = 250f, initValue = 35f)
        ruler.setStepValue(step = 0.5f)
        ruler.setMinorTicksPerMajor(tickCount = 8)
        ruler.setTickSpacingPx(dpToPx(30f))
        ruler.setSnapAnimationDuration(durationMs = 260)
        ruler.setDrawOptions(drawBaseline = true, drawEdges = true)
        ruler.setFlingEnabled(enabled = true)
        ruler.setColors(
            tickColor = Color.parseColor("#64748B"),
            indicatorColor = Color.parseColor("#0EA5E9"),
            backgroundColor = Color.parseColor("#ECFEFF"),
            edgeColor = Color.parseColor("#14B8A6")
        )
        ruler.setStrokeWidths(
            tickStrokeWidthPx = dpToPx(1f),
            baselineStrokeWidthPx = dpToPx(1f),
            indicatorWidthPx = dpToPx(3f),
            edgeStrokeWidthPx = dpToPx(2f)
        )
        ruler.scrollToValue(value = 35f, animated = false)
    }

    private fun formatValue(value: Float): String = String.format(Locale.US, "%.1f", value)

    private fun dpToPx(dp: Float): Float = dp * resources.displayMetrics.density
}
