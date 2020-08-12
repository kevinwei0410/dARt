package game

import android.content.Context
import android.util.Log
import com.google.ar.core.Pose
import com.google.ar.core.examples.java.augmentedimage.rendering.DartRenderer
import dartcontroller.Animate
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt

fun FloatArray.distanceTo(other: FloatArray): Float {
    return sqrt((this[0] - other[0]).pow(2) +
            (this[1] - other[1]).pow(2) +
            (this[2] - other[2]).pow(2))
}

class Game {
    companion object {
        val TAG = Game::class.simpleName
    }

    val dart = Dart()
    val dartboard = Dartboard()
    private val flyingDart = FlyingDart(dartboard)


    fun shootDart(cameraPose: Pose, speed: Float = 2.3f) {
        val dartPoseInWorld = cameraPose.compose(dart.standbyPose)

        val p0 = dartPoseInWorld.translation
        val v0 = dartPoseInWorld.zAxis.map { -it * speed }.toFloatArray()

        // ETA to dartboard *plane*, not dartboard per se
        val ETA = dartboard.calculateHitTime(p0, v0)
        val distanceToDartboardCenter = dartboard.pose.translation.distanceTo(
                Animate(speed, cameraPose.compose(dart.standbyPose)).calculatePose(ETA).translation)

        Log.i(TAG, "dart shot, ETA: $ETA seconds")
        Log.i(TAG, "Distance to dartboard center: $distanceToDartboardCenter")

        if (ETA > 0.0f) {
            val isHit = distanceToDartboardCenter < Dartboard.STANDARD_RADIUS
            flyingDart.addDart(System.currentTimeMillis(), cameraPose, dart.standbyPose, speed, ETA, isHit)
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

class FlyingDart(private val dartboard: Dartboard) {
    data class DartInitialState(val t0InMillis: Long,
                                val cameraPose: Pose,
                                val dartPoseInCamera: Pose,
                                val speed: Float,
                                val ETA: Float,
                                val isHit: Boolean) {
        val animate = Animate(speed, cameraPose.compose(dartPoseInCamera))
    }

    fun addDart(t0InMillis: Long, cameraPose: Pose, dartPoseInWorld: Pose, speed: Float, ETA: Float, isHit: Boolean) {
        dartInitialStates.add(DartInitialState(t0InMillis, cameraPose, dartPoseInWorld, speed, ETA, isHit))
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

            if (deltaT >= dartInitialState.ETA) {
                if (dartInitialState.isHit) {
                    with(dartInitialState) {
                        val poseOnBoard = animate.calculatePose(ETA)
                        dartboard.addDart(poseOnBoard)
                    }
                }
                iterator.remove()
                continue
            } else if (deltaT > 10) {
                iterator.remove()
                continue
            }

            dartInitialState.animate.calculatePose(deltaT).toMatrix(modelViewMatrix, 0)
            renderer.updateModelMatrix(modelViewMatrix)
            renderer.draw(viewMatrix, projectionMatrix, colorCorrectionRgba)
        }
    }

}