package com.google.ar.core.examples.java.augmentedimage.rendering

import android.content.Context
import com.google.ar.core.Pose
import java.lang.Exception
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.schedule

class DartboardRenderer {
    companion object {
        internal val TAG = DartboardRenderer::class.simpleName!!
    }

    private val dartboardRenderer = ObjectRenderer()
    private val dartsOnBoardRenderer = DartsOnBoardRenderer()

    fun updateModelMatrix(modelMatrix: FloatArray) {
        dartboardRenderer.updateModelMatrix(modelMatrix, 0.01f)
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
        dartboardRenderer.draw(viewMatrix, projectionMatrix, colorCorrectionRgba)
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