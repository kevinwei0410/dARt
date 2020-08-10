package game

import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import com.google.ar.core.Pose

class Game {
    companion object {
        val TAG = Game::class.simpleName
    }

    private val dart = Dart()
    private val dartboard = Dartboard()
    // TODO: val dartAnimator

    fun shootDart(p0: FloatArray, v0: FloatArray) {
        val ETA = dartboard.calculateHitTime(p0, v0).run {
            (this * 1000).toLong()
        } // in millis

        if (ETA > 0.0f) {
            Log.i(TAG, "dart shot, ETA: $ETA millis")
            object : CountDownTimer(ETA, 300) {
                override fun onFinish() {
                    Log.i(TAG, "onFinish")
                }

                override fun onTick(millisUntilFinished: Long) {
                    val t = ETA - millisUntilFinished
                    Log.i(TAG, "onTick: have fly $t millisecond")
                }
            }.start()
        }
    }

    fun updateDartboardPose(pose: Pose) {
        dartboard.pose = pose
    }

    fun createOnGlThread(context: Context) {
        dartboard.createOnGlThread(context)
        dart.createOnGlThread(context)
    }

    fun draw(
            viewMatrix: FloatArray,
            projectionMatrix: FloatArray,
            colorCorrectionRgba: FloatArray
    ) {
        dartboard.draw(viewMatrix, projectionMatrix, colorCorrectionRgba)
        dart.draw(viewMatrix, projectionMatrix, colorCorrectionRgba)
    }
}