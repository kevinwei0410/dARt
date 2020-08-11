package game

import android.content.Context
import android.util.Log
import com.google.ar.core.Pose
import com.google.ar.core.examples.java.augmentedimage.rendering.DartRenderer
import dartcontroller.Animate
import java.util.*

class Game {
    companion object {
        val TAG = Game::class.simpleName
    }

    val dart = Dart()
    private val flyingDart = FlyingDart()
    val dartboard = Dartboard()


    fun shootDart(cameraPose: Pose) {
        val ETA = dartboard.calculateHitTime(dart.position, dart.direction).run {
            (this * 1000).toLong()
        } // in millis

        if (ETA > 0.0f) {
            Log.i(TAG, "dart shot, ETA: $ETA millis")
            flyingDart.addDart(System.currentTimeMillis(), cameraPose, dart.standbyPose)
        }
    }

    fun updateDartboardPose(pose: Pose) {
        dartboard.pose = pose
    }

    fun createOnGlThread(context: Context) {
        dartboard.createOnGlThread(context)
        flyingDart.createOnGlThread(context)
        dart.createOnGlThread(context)
    }

    fun draw(
            viewMatrix: FloatArray,
            projectionMatrix: FloatArray,
            colorCorrectionRgba: FloatArray
    ) {
        dartboard.draw(viewMatrix, projectionMatrix, colorCorrectionRgba)
        flyingDart.draw(viewMatrix, projectionMatrix, colorCorrectionRgba)
        dart.draw(viewMatrix, projectionMatrix, colorCorrectionRgba)
    }
}

class FlyingDart {
    data class DartInitialState(val t0InMillis: Long,
                                val cameraPose: Pose,
                                val dartPoseInWorld: Pose) {
        val animate = Animate(230.0f, cameraPose.compose(dartPoseInWorld))
    }

    fun addDart(t0InMillis: Long, cameraPose: Pose, dartPoseInWorld: Pose) {
        dartInitialStates.add(DartInitialState(t0InMillis, cameraPose, dartPoseInWorld))
    }

    private val dartInitialStates = LinkedList<DartInitialState>()

    private val renderer = DartRenderer()
    private val modelViewMatrix = FloatArray(16)


    fun createOnGlThread(context: Context) {
        renderer.createOnGlThread(context)
    }

    fun draw(
            viewMatrix: FloatArray,
            projectionMatrix: FloatArray,
            colorCorrectionRgba: FloatArray
    ) {
        val iterator = dartInitialStates.iterator()
        while (iterator.hasNext()) {
            val dartInitialState = iterator.next()
            val deltaT = (System.currentTimeMillis() - dartInitialState.t0InMillis) / 1000.0f // in second

            if (deltaT > 3) {
                iterator.remove()
                continue
            }

            dartInitialState.animate.calculatePose(deltaT).toMatrix(modelViewMatrix, 0)
            renderer.updateModelMatrix(modelViewMatrix)
            renderer.draw(viewMatrix, projectionMatrix, colorCorrectionRgba)
        }
    }

}