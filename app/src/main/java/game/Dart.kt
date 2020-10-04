package game

import android.content.Context
import com.google.ar.core.Pose
import com.google.ar.core.examples.java.augmentedimage.rendering.DartRenderer


class Dart(val standbyPose: Pose = Pose(DART_POSITION, DART_ROTATION)) {
    companion object {
        // relative to camera
        val DART_POSITION = floatArrayOf(0.022f, -0.018f, -0.15f)
        val DART_ROTATION = floatArrayOf(0f, 0f, 0f, 1f)
    }

    val position: FloatArray
        get() = standbyPose.translation
    val direction: FloatArray
        get() = floatArrayOf(-standbyPose.zAxis[0], -standbyPose.zAxis[1], -standbyPose.zAxis[2])



    private val renderer = DartRenderer()

    fun updateModelMatrix(modelMatrix: FloatArray) = renderer.updateModelMatrix(modelMatrix)
    fun createOnGlThread(context: Context) = renderer.createOnGlThread(context)
    fun draw(
            viewMatrix: FloatArray,
            projectionMatrix: FloatArray,
            colorCorrectionRgba: FloatArray
    ) = renderer.draw(viewMatrix, projectionMatrix, colorCorrectionRgba)
}