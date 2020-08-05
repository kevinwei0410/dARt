package com.google.ar.core.examples.java.augmentedimage.rendering

import android.content.Context
import android.opengl.GLES30
import com.google.ar.core.Anchor
import com.google.ar.core.Pose
import com.google.ar.core.examples.java.augmentedimage.AugmentedImageActivity
import kotlinx.android.synthetic.main.activity_main.*

class DartRenderer {
    companion object {
        internal val TAG = DartRenderer::class.simpleName!!
    }

    private val dartRenderer = ObjectRenderer()

    fun updateModelMatrix(modelMatrix: FloatArray) {
        dartRenderer.updateModelMatrix(modelMatrix, 0.01f)
    }

    fun createOnGlThread(context: Context) {
        dartRenderer.createOnGlThread(context, "models/11750_throwing_dart_v1_L3.obj", "models/throwing_dart_diffuse.jpg")
        dartRenderer.setBlendMode(ObjectRenderer.BlendMode.SourceAlpha)
    }

    fun draw(
            viewMatrix: FloatArray,
            projectionMatrix: FloatArray,
            colorCorrectionRgba: FloatArray
    ) {
        dartRenderer.draw(viewMatrix, projectionMatrix, colorCorrectionRgba)
    }
}