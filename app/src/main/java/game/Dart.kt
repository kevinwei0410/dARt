package game

import android.content.Context
import android.opengl.GLES30
import com.google.ar.core.Pose
import com.google.ar.core.examples.java.augmentedimage.rendering.DartRenderer
import com.google.ar.core.examples.java.augmentedimage.rendering.ObjectRenderer


class Dart(private val pose: Pose = Pose(DART_POSITION, DART_ROTATION)) {
    companion object {
        val DART_POSITION = floatArrayOf(0.022f, -0.018f, -0.15f)
        val DART_ROTATION = floatArrayOf(0f, 0f, 0f, 1f)
    }

    val direction
        get() = floatArrayOf(-pose.zAxis[0], -pose.zAxis[1], -pose.zAxis[2])


    private val renderer = DartRenderer()

    fun updateModelMatrix(modelMatrix: FloatArray) = renderer.updateModelMatrix(modelMatrix)
    fun createOnGlThread(context: Context) = renderer.createOnGlThread(context)
    fun draw(
            viewMatrix: FloatArray,
            projectionMatrix: FloatArray,
            colorCorrectionRgba: FloatArray
    ) = renderer.draw(viewMatrix, projectionMatrix, colorCorrectionRgba)
}