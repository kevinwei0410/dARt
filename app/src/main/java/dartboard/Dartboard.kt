package dartboard

import android.content.Context
import com.google.ar.core.Pose
import com.google.ar.core.examples.java.augmentedimage.rendering.DartboardRenderer
import kotlin.math.sqrt

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


class Dartboard(private val pose: Pose) {
    companion object {
        const val GRAVITY = -9.8f
    }

    private val renderer = DartboardRenderer()
    private val modelMatrix = FloatArray(16).also {
        pose.toMatrix(it, 0)
    }

    /**
     * Test if a dart can hit dartboard with given position & initial velocity
     * Calculated in world coordinate
     *
     * Dartboard: db
     * Normal of dartboard: N
     * g: [0, -9.8, 0]
     *
     * Equation: (P + V * t + (1/2) * g * t^2 - db) dot N = 0
     *
     * X: (p_x0 + v_x0 * t             - db_x) dot (N_x)
     * Y: (p_y0 + v_y0 * t + (g/2)*t^2 - db_y) dot (N_y)
     * Z: (p_z0 + v_z0 * t             - db_z) dot (N_z)
     * Unit: meter
     * @param p0 position in world coordinate at time zero
     * @param v0 velocity (vector) at time zero
     * @return The time when dart hits dartboard
     */
    fun hitTest(p0: FloatArray, v0: FloatArray): Float {

        // dartboard's position & normal vector
        val db = pose.translation
        val N = pose.zAxis

        // polynomial in descending order, a * t^2 + b * t + c = 0
        val a = 0.5f * GRAVITY * N.y
        val b = v0.dot(N)
        val c = (p0.minus(db)).dot(N)

        // b^2 - 4ac
        val discriminant = b * b - 4 * a * c

        if (discriminant < 0)
            return -1.0f
        else return sqrt(discriminant).also { sqrtD ->
            val r1 = (-b + sqrtD) / (2 * a)
            val r2 = (-b - sqrtD) / (2 * a)

            return when {
                r1 >= 0 -> r1
                r2 >= 0 -> r2
                else -> -1.0f
            }
        }
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
