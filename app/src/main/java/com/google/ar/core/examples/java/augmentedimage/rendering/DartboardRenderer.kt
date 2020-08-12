package com.google.ar.core.examples.java.augmentedimage.rendering

import android.content.Context
import android.opengl.GLES30
import android.util.Log
import com.google.ar.core.Pose
import java.lang.Exception
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.schedule

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
            colorCorrectionRgba: FloatArray
    ) {
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        dartboardRenderer.draw(viewMatrix, projectionMatrix, colorCorrectionRgba)
        dartsOnBoardRenderer.draw(viewMatrix, projectionMatrix, colorCorrectionRgba)
    }
}

class DartsOnBoardRenderer() {
    companion object {
        private val TAG = DartsOnBoardRenderer::class::simpleName.get()
    }

    private val poseListLock = ReentrantLock()
    private val poseOfDartsOnBoard = LinkedList<Pose>()

    private val dartRenderer = DartRenderer()
    private val modelMatrix = FloatArray(16)

    private val autoCleanDartTimer = Timer().apply {
        schedule(1000, 1500) {
            popDart()
        }
    }

    internal fun createOnGlThread(context: Context) {
        dartRenderer.createOnGlThread(context)
    }

    fun addDart(pose: Pose) {
        poseListLock.lock()
        try {
            Log.i(TAG, "New darts hits board")
            poseOfDartsOnBoard.add(pose)
        } catch (e: Exception) {

        } finally {
            poseListLock.unlock()
        }
    }

    private fun popDart() {
        poseListLock.lock()
        try {
            poseOfDartsOnBoard.poll()
        } catch (e: Exception) {

        } finally {
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

        } finally {
            poseListLock.unlock()
        }
    }
}