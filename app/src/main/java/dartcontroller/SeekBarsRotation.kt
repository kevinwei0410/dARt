package dartcontroller

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.google.ar.core.examples.java.augmentedimage.R
import kotlinx.android.synthetic.main.seekbars_rotation.view.*

class SeekBarsRotation : FrameLayout {
    private val seekBars: Array<SeekBarShowValue>

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        View.inflate(context, R.layout.seekbars_rotation, this)
        seekBars = arrayOf(seekBarX, seekBarY, seekBarZ, seekBarW)
    }

    fun getQuaternion() = seekBars.map {
        it.progress
    }.toFloatArray()

    fun setQuaternion(quaternion: FloatArray) = seekBars.mapIndexed { index, seekBarShowValue ->
        seekBarShowValue.progress = quaternion[index]
    }
}