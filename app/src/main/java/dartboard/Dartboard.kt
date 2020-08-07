package dartboard

import android.content.Context
import android.util.Log
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

class Dartboard() {
    companion object {
        val TAG = Dartboard::class.simpleName
        val rotateXAxis90 = Pose(floatArrayOf(0f, 0f, 0f),
                floatArrayOf(sin(Math.toRadians(135.0).toFloat()), 0f, 0f, cos(Math.toRadians(135.0).toFloat())))
        const val GRAVITY = -9.8f
    }

    // Pose of augmented image is z pointed down, x pointed right
    var pose: Pose = Pose(kotlin.floatArrayOf(0f, 0f, 3f), kotlin.floatArrayOf(0f, 0f, 0f, 1f))
        set(value) {
            field = value.compose(rotateXAxis90)
            field.toMatrix(modelMatrix, 0)
        }

    private val renderer = DartboardRenderer()
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
            discriminant < 0 -> -1.0f
            discriminant == 0.0f -> (-b + sqrt(discriminant)) / (2 * a)
            discriminant > 0 -> {
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
     * Dart you add will be draw at the pose you given (world coordinate)
     */
    fun addDart(pose: Pose) = renderer.dartsOnBoardRenderer.addDart(pose)

    val normalVector: FloatArray
        get() = pose.zAxis
    val positionInWorldSpace: FloatArray
        get() = pose.translation

    private fun updateModelMatrix(modelMatrix: FloatArray) = renderer.updateModelMatrix(modelMatrix)
    fun createOnGlThread(context: Context) {
        renderer.createOnGlThread(context)
    }

    fun draw(
            viewMatrix: FloatArray,
            projectionMatrix: FloatArray,
            colorCorrectionRgba: FloatArray
    ) {
        updateModelMatrix(modelMatrix)
        renderer.draw(viewMatrix, projectionMatrix, colorCorrectionRgba)
    }

}
