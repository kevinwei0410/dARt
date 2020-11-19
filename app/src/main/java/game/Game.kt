package game

import android.content.Context
import android.util.Log
import android.widget.TextView
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

class Game() {
    companion object {
        val TAG = Game::class.simpleName
    }

    lateinit var cameraPose: Pose
    lateinit var scoreTextView: TextView

    val dart = Dart()
    val dartboard = Dartboard()
    private val flyingDart = FlyingDart(dartboard)


    /**
     * Return dart pose in dartboard coordinate and ETA in second
     *
     * ETA will be negative if this dart won't hit the dartboard
     */
    fun shootDart(speed: Float = 2.3f): Pair<Pose?, Float> {
        val dartStandbyPoseInWorld = cameraPose.compose(dart.standbyPose)

        val p0 = dartStandbyPoseInWorld.translation
        val v0 = dartStandbyPoseInWorld.zAxis.map { -it * speed }.toFloatArray()

        // ETA to dartboard *plane*, not dartboard per se
        val ETA = dartboard.calculateHitTime(p0, v0)
        val animate = Animate(speed, cameraPose.compose(dart.standbyPose))
        var dartPoseInDartboard: Pose? = null
        var score: Int = 0;
        Log.i(TAG, "dart shot, ETA: $ETA seconds")

        if (ETA > 0.0f) {
            flyingDart.addDart(System.currentTimeMillis(), animate, ETA)
            dartPoseInDartboard = dartboard.pose.inverse().compose(animate.calculatePose(ETA))
            score = dartboard.calculateScore(dartPoseInDartboard.tx(), dartPoseInDartboard.ty())
            scoreTextView.post {
                scoreTextView.text = score.toString()
            }
        }
        Log.i(TAG, "My dart shoots, pose on dartboard $dartPoseInDartboard; score: $score")

        return (dartPoseInDartboard to ETA)
    }

    fun onOtherPlayersDartHitsDartboard(translation: FloatArray, rotation: FloatArray) {
        if (translation.size != 3 || rotation.size != 4)
            throw IllegalArgumentException("Not a valid Pose.")

        val dartPoseInDartboard = Pose(translation, rotation)
        Log.i(TAG, "Enemy dart shoots, pose on dartboard $dartPoseInDartboard")
        val dartOnDartBoard = DartOnDartBoard(dartPoseInDartboard, System.currentTimeMillis() + FlyingDart.CLEAN_TIME_MILLIS, true)
        dartboard.addDart(dartOnDartBoard)
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


/**
 *   Responsible for drawing flying dart, if the dart hit the dartboard
 * it will stick on dartboard for $CLEAN_TIME_MILLIS$ milliseconds
 */
class FlyingDart(private val dartboard: Dartboard) {
    companion object {
        val TAG = FlyingDart::class.simpleName
        const val CLEAN_TIME_MILLIS = 8000
    }

    data class DartInitialState(val t0InMillis: Long,
                                val animate: Animate,
                                val ETA: Float)

    fun addDart(t0InMillis: Long, animate: Animate, ETA: Float) {
        dartInitialStates.add(DartInitialState(t0InMillis, animate, ETA))
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
                val dartPoseInWorld = dartInitialState.animate.calculatePose(dartInitialState.ETA)
                val distanceToDartboardCenter = dartPoseInWorld.translation.distanceTo(dartboard.pose.translation)
                val isHit = distanceToDartboardCenter < Dartboard.STANDARD_RADIUS

                Log.i(TAG, "Distance to dartboard center = $distanceToDartboardCenter, isHit = $isHit")

                if (isHit) {
                    val dartPoseInDartboard = dartboard.pose.inverse().compose(dartPoseInWorld)
                    dartboard.addDart(DartOnDartBoard(dartPoseInDartboard, System.currentTimeMillis() + CLEAN_TIME_MILLIS))
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