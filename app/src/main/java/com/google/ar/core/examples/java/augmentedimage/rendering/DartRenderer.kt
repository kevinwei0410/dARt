package com.google.ar.core.examples.java.augmentedimage.rendering

import android.content.Context

open class DartRenderer {
    companion object {
        internal val TAG = DartRenderer::class.simpleName!!
    }

    protected val dartRenderer = ObjectRenderer()

    fun updateModelMatrix(modelMatrix: FloatArray) {
        dartRenderer.updateModelMatrix(modelMatrix, 0.01f)
    }

    open fun createOnGlThread(context: Context) {
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

class EnemyDartRenderer: DartRenderer() {
    override fun createOnGlThread(context: Context) {
        dartRenderer.createOnGlThread(context, "models/11750_throwing_dart_v1_L3.obj", "models/throwing_dart_diffuse_enemy.jpg")
        dartRenderer.setBlendMode(ObjectRenderer.BlendMode.SourceAlpha)
    }
}