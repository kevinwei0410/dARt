package game

import android.content.Context
import com.google.ar.core.Pose
import com.google.ar.core.examples.java.augmentedimage.rendering.DartboardRenderer
import kotlin.math.*

val FloatArray.x
    get() = this[0]
val FloatArray.y
    get() = this[1]
val FloatArray.z
    get() = this[2]

fun FloatArray.dot(other: FloatArray) = this[0] * other[0] +
        this[1] * other[1] +
        this[2] * other[2]

fun FloatArray.minus(other: FloatArray): FloatArray {
    this[0] -= other[0]
    this[1] -= other[1]
    this[2] -= other[2]
    return this
}

/**
 * This class is responsible for drawing dartboard & calculate hit time
 *
 * @property normalVector Normal vector of dartboard's plane (frontend)
 * @property positionInWorldSpace Dartboard's position in world space
 *
 * @property addDart Give this method the pose when dart hit dartboard, it'll stick on dartboard
 * @property calculateHitTime Calculate the delta time (second) for a given pair of
 *   dart's initial pose & initial velocity
 */

class Dartboard {
    companion object {
        val TAG = Dartboard::class.simpleName
        val rotateXAxis90 = Pose(floatArrayOf(0f, 0f, 0f),
                floatArrayOf(sin(Math.toRadians(135.0).toFloat()), 0f, 0f, cos(Math.toRadians(135.0).toFloat())))
        const val GRAVITY = -9.8f
        private const val modelRadius = 26.7f // model unit
        const val STANDARD_RADIUS = 0.2265f // meter
        const val scaleRate = STANDARD_RADIUS / modelRadius
    }

    // Pose of augmented image is z pointed down, x pointed right
    var pose: Pose = Pose(kotlin.floatArrayOf(0f, 0f, 3f), kotlin.floatArrayOf(0f, 0f, 0f, 1f))
        set(value) {
            field = value.compose(rotateXAxis90)
            field.toMatrix(modelMatrix, 0)
            updateModelMatrix(modelMatrix)
        }

    private val renderer = DartboardRenderer(scaleRate)
    private val modelMatrix = FloatArray(16)

    /**
     * Test if a dart can hit dartboard with given position & initial velocity
     * Calculated in world coordinate
     *
     * Dartboard's position: db
     * Normal of dartboard: N
     * g: [0, -9.8, 0]
     *
     * Equation: (P + V * t + (1/2) * g * t^2 - db) dot N = 0
     *    (x - db) dot N = 0 point-normal equation
     *
     * X: (p_x0 + v_x0 * t             - db_x) dot (N_x)
     * Y: (p_y0 + v_y0 * t + (g/2)*t^2 - db_y) dot (N_y)
     * Z: (p_z0 + v_z0 * t             - db_z) dot (N_z)
     * Unit: meter
     * @param p0 position in world coordinate at time zero
     * @param v0 velocity (vector) at time zero
     * @return The time (second) when dart hits dartboard, negative value if can't hit dartboard
     */
    fun calculateHitTime(p0: FloatArray, v0: FloatArray): Float {

        // dartboard's position & normal vector
        val positionOfDartboard = pose.translation
        val normalOfDartboard = pose.zAxis

        // polynomial in descending order, a * t^2 + b * t + c = 0
        val a = 0.5f * GRAVITY * normalOfDartboard.y
        val b = v0.dot(normalOfDartboard)
        val c = (p0.minus(positionOfDartboard)).dot(normalOfDartboard)

        // b^2 - 4ac
        val discriminant = b * b - 4 * a * c

        return when {
            discriminant < 0.0f -> -1.0f
            discriminant == 0.0f -> (-b + sqrt(discriminant)) / (2 * a)
            discriminant > 0.0f -> {
                val sqrtD = sqrt(discriminant)
                val t1 = (-b + sqrtD) / (2 * a)
                val t2 = (-b - sqrtD) / (2 * a)

                if (t1 > 0 && t2 > 0)
                    min(t1, t2)
                else if (t1 < 0 && t2 < 0)
                    -1.0f
                else
                    if (t1 > t2) t1 else t2
            }
            else -> -1.0f
        }
    }

    /**
     * Dart you add will be draw at the pose you given (relative to dartboard)
     */
    fun addDart(pose: DartOnDartBoard) = renderer.dartsOnBoardRenderer.addDart(pose)

    private fun updateModelMatrix(modelMatrix: FloatArray) = renderer.updateModelMatrix(modelMatrix)
    fun createOnGlThread(context: Context) {
        renderer.createOnGlThread(context)
    }

    fun draw(
            viewMatrix: FloatArray,
            projectionMatrix: FloatArray,
            colorCorrectionRgba: FloatArray
    ) {
        renderer.draw(viewMatrix, projectionMatrix, colorCorrectionRgba, pose)
    }

    fun calculateScore(X: Float, Y: Float) = ScoreCalculator.getScore(X, Y)

}

data class DartOnDartBoard(val poseInDartboard: Pose,
                           val cleanTime: Long,
                           val isEnemyDart: Boolean = false)

private object ScoreCalculator {

    private val point = arrayOf(
            intArrayOf(6, 13, 13, 4, 4, 18, 18, 1, 1, 20, 20),
            intArrayOf(6, 10, 10, 15, 15, 2, 2, 17, 17, 3, 3),
            intArrayOf(11, 8, 8, 16, 16, 7, 7, 19, 19, 3, 3),
            intArrayOf(11, 14, 14, 9, 9, 12, 12, 5, 5, 20, 20))

    //r1 bull  r2~r3 trible r4~r5 double
    // origin 0.1070f, 0.1150f, 0.1620f, 0.1700f
    // new value 0.1040f, 0.1150f, 0.1610f, 0.1710f
    private val Raid = floatArrayOf(0.0445f, 0.1040f, 0.1150f, 0.1610f, 0.1710f)

    /**
     * getScore
     * a function to get the single dart score with position of the dart
     * @param x the x value of dart position (meter)
     * @param y the y value of dart postion (meter)
     * @return int score of the dart
     */
    fun getScore(x: Float, y: Float): Int {
        var times = 1
        val r = Math.sqrt(x * x + y * y.toDouble()).toFloat()
        if (r > Raid[4]) {
            return 0
        } else if (r > Raid[3] && r <= Raid[4]) {
            times = 2
        } else if (r > Raid[1] && r <= Raid[2]) {
            times = 3
        } else if (r <= Raid[0]) {
            return 50
        }
        val theta = Math.abs(Math.toDegrees(Math.atan(y / x.toDouble())).toInt())
        if (x >= 0 && y >= 0) {
            return point[0][theta / 9] * times
        } else if (x >= 0 && y < 0) {
            return point[1][theta / 9] * times
        } else if (x < 0 && y < 0) {
            return point[2][theta / 9] * times
        } else if (x < 0 && y >= 0) {
            return point[3][theta / 9] * times
        }
        return -1
    }
}
