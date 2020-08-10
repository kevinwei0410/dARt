package game

import com.google.ar.core.Pose
import org.junit.Test
import kotlin.math.abs


class DartBoardTest {

    private val dartboard = Dartboard()


    // https://www.geogebra.org/3d/a9hby9kc
    @Test
    fun testCalculateHitTime() {
        val vec3StringFormat = "[%1$.2f, %2$.2f, %3$.2f]"
        fun String.format(arr: FloatArray): String {
            return this.format(arr[0], arr[1], arr[2])
        }
        dartboard.pose = Pose(floatArrayOf(-0.011f, -0.072f, -0.311f),
                floatArrayOf(0.65f, -0.09f, 0.08f, 0.75f))

        println("Dartboard")
        println("  position = ${vec3StringFormat.format(dartboard.pose.translation)}")
        // zAxis = [-0.24, 0.14, 0.96]
        println("  z axis (normal) = ${vec3StringFormat.format(dartboard.pose.zAxis)}")
        println("  equation: ${dartboard.pose.zAxis.x}x + ${dartboard.pose.zAxis.y}y + ${dartboard.pose.zAxis.z}z = " +
                "${-(dartboard.pose.zAxis.x * -dartboard.pose.tx() +
                        dartboard.pose.zAxis.y * -dartboard.pose.ty() +
                        dartboard.pose.zAxis.z * -dartboard.pose.tz())}")

        println("Dart")
        val p0 = floatArrayOf(0.73f, 0.81f, 2f)
        val v0 = floatArrayOf(-0.35f, 5f, -2.1f)
        println("  position = ${vec3StringFormat.format(p0)}")
        println("  initial velocity = ${vec3StringFormat.format(v0)}")

        val secondsToBoard = dartboard.calculateHitTime(p0, v0)

        val correctTime = 1.093f
        println("Dart will hit dartboard after $secondsToBoard")

        assert(abs(correctTime - secondsToBoard) < 0.01f)
    }
}