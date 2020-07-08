package dartcontroller

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.google.ar.core.examples.java.augmentedimage.R
import kotlinx.android.synthetic.main.seekbars_translation.view.*

class SeekBarsTranslation : FrameLayout {
    private val seekBars: Array<SeekBarShowValue>

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        View.inflate(context, R.layout.seekbars_translation, this)
        seekBars = arrayOf(seekBarX, seekBarY, seekBarZ)
    }

    fun getTranslation() = seekBars.map {
        it.progress
    }.toFloatArray()
}