package dartcontroller

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.FrameLayout
import android.widget.SeekBar
import com.google.ar.core.examples.java.augmentedimage.R
import kotlinx.android.synthetic.main.seekbar_show_value.view.*


class SeekBarShowValue : FrameLayout {

    internal val seekBar: SeekBar

    val precision: Float
    val valueFormat: String

    var progress: Float
        get() = seekBar.progress * precision
        set(value) {
            seekBar.progress = (value / precision).toInt()
        }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        inflate(context, R.layout.seekbar_show_value, this)

        this.seekBar = findViewById(R.id.seekBar)

        with(context.obtainStyledAttributes(attrs, R.styleable.SeekBarShowValue)) {

            // Default precision is 10E-3,
            this@SeekBarShowValue.precision =
                    getFloat(R.styleable.SeekBarShowValue_precision, 0.001f)

            if (this@SeekBarShowValue.precision <= .0f)
                throw IllegalArgumentException("Precision can't be zero or negative")

            val attrMax = getInt(R.styleable.SeekBarShowValue_max, 1)
            val attrMin = getInt(R.styleable.SeekBarShowValue_min, -1)
            val initProgress = getInt(R.styleable.SeekBarShowValue_progress, 0)
            if (attrMax < attrMin)
                throw IllegalArgumentException("Max value can't be smaller than min value")

            seekBar.apply {
                max = attrMax
                min = attrMin
                progress = initProgress
            }

            this@SeekBarShowValue.valueFormat = getString(R.styleable.SeekBarShowValue_format)
                    ?: "%.3f"

            seekBarName.text = getString(R.styleable.SeekBarShowValue_android_text)
            seekBarValue.text = valueFormat.format(progress)
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, value: Int, fromUser: Boolean) {
                seekBarValue.text = valueFormat.format(value * precision)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {
                Log.d(TAG, "${seekBarName.text} set to ${seekBar.progress}, computed to $progress")
            }
        })
    }


    companion object {
        val TAG by lazy {
            SeekBarShowValue::class::simpleName.get()!!
        }
    }
}