package com.google.ar.core.examples.java.augmentedimage.rendering

import android.content.Context
import android.opengl.GLES30
import android.util.Log
import com.google.ar.core.Pose
import game.DartOnDartBoard
import java.util.*

class DartboardRenderer(private val modelScaleRate: Float) {
    companion object {
        internal val TAG = DartboardRenderer::class.simpleName!!
    }

    private val dartboardRenderer = ObjectRenderer()
    val dartsOnBoardRenderer = DartsOnBoardRenderer()

    fun updateModelMatrix(modelMatrix: FloatArray) {
        dartboardRenderer.updateModelMatrix(modelMatrix, modelScaleRate)
    }

    fun createOnGlThread(context: Context) {
        dartsOnBoardRenderer.createOnGlThread(context)
        dartboardRenderer.createOnGlThread(context, "models/11721_dartboard_V4_L3.obj", "models/dartboard.jpg")
        dartboardRenderer.setBlendMode(ObjectRenderer.BlendMode.SourceAlpha)
    }

    fun draw(
            viewMatrix: FloatArray,
            projectionMatrix: FloatArray,
            colorCorrectionRgba: FloatArray,
            dartboardPose: Pose
    ) {
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        dartboardRenderer.draw(viewMatrix, projectionMatrix, colorCorrectionRgba)
        dartsOnBoardRenderer.draw(viewMatrix, projectionMatrix, colorCorrectionRgba, dartboardPose)
    }
}

class DartsOnBoardRenderer() {
    companion object {
        private val TAG = DartsOnBoardRenderer::class::simpleName.get()
    }

    private val dartsOnDartboard = LinkedList<DartOnDartBoard>()

    private val dartRenderer = DartRenderer()
    private val enemyDartRenderer = EnemyDartRenderer()
    private val modelMatrix = FloatArray(16)

    internal fun createOnGlThread(context: Context) {
        dartRenderer.createOnGlThread(context)
    }

    fun addDart(pose: DartOnDartBoard) {
        Log.i(TAG, "New darts hits board")
        dartsOnDartboard.add(pose)
    }


    fun draw(
            viewMatrix: FloatArray,
            projectionMatrix: FloatArray,
            colorCorrectionRgba: FloatArray,
            dartboardPose: Pose
    ) {
        val iterator = dartsOnDartboard.iterator()
        while (iterator.hasNext()) {
            val dartOnDartBoard = iterator.next()
            if (System.currentTimeMillis() > dartOnDartBoard.cleanTime) {
                iterator.remove()
                continue
            }
            (dartboardPose.compose(dartOnDartBoard.poseInDartboard)).toMatrix(modelMatrix, 0)

            if (dartOnDartBoard.isEnemyDart) {
                enemyDartRenderer.updateModelMatrix(modelMatrix)
                enemyDartRenderer.draw(viewMatrix, projectionMatrix, colorCorrectionRgba)
            } else {
                dartRenderer.updateModelMatrix(modelMatrix)
                dartRenderer.draw(viewMatrix, projectionMatrix, colorCorrectionRgba)
            }
        }
    }
}