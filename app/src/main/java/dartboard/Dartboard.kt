package dartboard

import android.content.Context
import com.google.ar.core.Pose
import com.google.ar.core.examples.java.augmentedimage.rendering.DartRenderer
import com.google.ar.core.examples.java.augmentedimage.rendering.DartboardRenderer
import java.lang.Exception
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.schedule

class Dartboard(private val pose: Pose) {
    private val renderer = DartboardRenderer()
    private val modelMatrix = FloatArray(16).also {
        pose.toMatrix(it, 0)
    }


    val normalVector: FloatArray
        get() = pose.zAxis
    val positionInWorldSpace: FloatArray
        get() = pose.translation


    fun updateModelMatrix(modelMatrix: FloatArray) = renderer.updateModelMatrix(modelMatrix)
    fun createOnGlThread(context: Context) {
        renderer.createOnGlThread(context)
    }

    fun draw(
            viewMatrix: FloatArray,
            projectionMatrix: FloatArray,
            colorCorrectionRgba: FloatArray
    ) {
        renderer.draw(viewMatrix, projectionMatrix, colorCorrectionRgba)
    }

}

private class DartsOnBoardRenderer() {
    private val poseListLock = ReentrantLock()
    private val poseOfDartsOnBoard = LinkedList<Pose>()

    private val dartRenderer = DartRenderer()
    private val modelMatrix = FloatArray(16)

    private val autoCleanDartTimer = Timer().apply {
        schedule(1000, 1000) {
            popDart()
        }
    }

    internal fun createOnGlThread(context: Context) {
        dartRenderer.createOnGlThread(context)
    }

    fun addDart(pose: Pose) {
        poseListLock.lock()
        try {
            poseOfDartsOnBoard.add(pose)
        } catch (e: Exception) {
            poseListLock.unlock()
        }
    }

    private fun popDart() {
        poseListLock.lock()
        try {
            poseOfDartsOnBoard.pop()
        } catch (e: Exception) {
            poseListLock.unlock()
        }
    }

    fun draw(
            viewMatrix: FloatArray,
            projectionMatrix: FloatArray,
            colorCorrectionRgba: FloatArray
    ) {
        poseListLock.lock()
        try {
            for (pose in poseOfDartsOnBoard) {
                pose.toMatrix(modelMatrix, 0)
                dartRenderer.updateModelMatrix(modelMatrix)
                dartRenderer.draw(viewMatrix, projectionMatrix, colorCorrectionRgba)
            }
        } catch (e: Exception) {
            poseListLock.unlock()
        }
    }
}