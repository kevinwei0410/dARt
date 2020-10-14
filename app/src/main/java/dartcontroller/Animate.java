package dartcontroller;

import android.util.Log;

import com.google.ar.core.Pose;

public class Animate {

    private float speed;

    // direction is a three float vector
    private float[] direction;
    // gravity in local coordinate
    private float[] gravity;
    // max frame set by 60 frame
    final private int maxFrame = 1000;

    // Number of milliseconds in one second.
    private static final float MILLISECONDS_PER_SECOND = 1000f;

    // time the animate starts
    private long startAnimateTime;
    // Counter for Frame
    private int frameCounter = 0;
    // Boolean checking animate end or not
    private boolean endAnimate;
    // Pose for present Pose
    private Pose AnimatePose;
    // Pose for Starting Translation and Rotation
    private Pose StartingPose;
    // String for testing
    private String TestString;


    /**
     * Animate construct
     * init direction is (0, 0, -1) for the -Z Axis
     * init gravity is (0, -0.5, 0) as the -Y Axis
     */
    public Animate(float speed) {
        //construct direction speed starting position
        this.speed = speed;
        this.direction = new float[]{0, 0, -1f * speed * 0.01f};
        this.gravity = new float[]{0, -0.5f, 0};
    }

    /**
     * Animate construct with speed and init Pose
     *
     * @param speed speed of Dart animate
     * @param p     the Starting Pose
     */
    public Animate(float speed, Pose p) {
        //construct direction speed starting position
        this.speed = speed;
        this.direction = new float[]{0, 0, -1f * speed};
        setStartingCameraPose(p);
    }

    // start the animate, set the starting time and previousFrame time
    public void startAnimate() {
        startAnimateTime = System.currentTimeMillis();
        frameCounter = 0;
        endAnimate = true;
    }

    // calculate the new pose with current time

    /**
     * This function is to update the AnimatePose
     * del is the time length
     * setup the endAnimate if the del is bigger than maxFrame
     */
    public void upDatePose() {
        long deltaT = System.currentTimeMillis() - startAnimateTime;
        float del = (float) deltaT;
        del /= 1000f;
        float[] translation = {direction[0] * del + gravity[0] * del * del, direction[1] * del + gravity[1] * del * del, direction[2] * del + gravity[2] * del * del};
        float[] newSpeed = {direction[0] + gravity[0] * del, direction[1] + gravity[1] * del, direction[2] + gravity[2] * del};
        float[] rotation = getRotation(getVector(direction, newSpeed), getTheta(direction, newSpeed));
        //TestString = String.format("%.3f, %.3f, %.3f", f[0], f[1], f[2]);
        AnimatePose = new Pose(translation, rotation);
        if (deltaT >= maxFrame) {
            endAnimate = false;
        } else {
            frameCounter++;

        }
    }

    /**
     * this is the function to calculate the Pose with the time of animate
     *
     * @param del the delta time of the animate (unit: second)
     * @return the Pose
     */
    public Pose calculatePose(float del) {
        float[] translation = {direction[0] * del + gravity[0] * del * del, direction[1] * del + gravity[1] * del * del, direction[2] * del + gravity[2] * del * del};
        float[] newSpeed = {direction[0] + gravity[0] * del, direction[1] + gravity[1] * del, direction[2] + gravity[2] * del};
        float[] rotation = getRotation(getVector(direction, newSpeed), getTheta(direction, newSpeed));
        AnimatePose = new Pose(translation, rotation);
        return StartingPose.compose(AnimatePose);
    }

    /**
     * this function calculate the degree of two vector
     *
     * @param v1 the first vector.
     * @param v2 the second vetcor.
     * @return the theta of two vector
     */
    public float getTheta(float[] v1, float[] v2) {

        float inner = v1[0] * v2[0] + v1[1] * v2[1] + v1[2] * v2[2];
        float d1 = (float) Math.sqrt(v1[0] * v1[0] + v1[1] * v1[1] + v1[2] * v1[2]);
        float d2 = (float) Math.sqrt(v2[0] * v2[0] + v2[1] * v2[1] + v2[2] * v2[2]);
        return (float) Math.acos(inner / d1 / d2);
    }

    /**
     * this fuction make the rotation part of a Pose with rotation Axis and rotate degree(radian)
     *
     * @param k     the rotation Axis
     * @param theta degree (radian)
     * @return float array with x, y, z, w. the elements of rotation Pose.
     */
    public float[] getRotation(float[] k, float theta) {
        float x, y, z, w;
        x = k[0] * (float) Math.sin(theta / 2);
        y = k[1] * (float) Math.sin(theta / 2);
        z = k[2] * (float) Math.sin(theta / 2);
        w = (float) Math.cos(theta / 2);
        return new float[]{x, y, z, w};
    }

    /**
     * this function setup the init Pose to start the animation.
     *
     * @param p init Pose
     */
    public void setStartingCameraPose(Pose p) {
        StartingPose = p;
        Log.d("CameraPose", StartingPose.toString());
        setGravity(StartingPose.extractRotation().inverse().rotateVector(new float[]{0f, -4.9f, 0f}));
        //setDirection(StartingPose.extractRotation().inverse().rotateVector(new float[]{0f,0,speed*-0.01f}));
    }

    public Pose getStartingCameraPose() {
        return StartingPose;
    }

    /**
     * calculate the cross product of two vector in ul
     *
     * @param v1 the first vector in cross product
     * @param v2 the second vector in cross product
     * @return the unit vector of cross product
     */
    private float[] getVector(float[] v1, float[] v2) {

        float[] v = new float[]{
                v1[1] * v2[2] - v1[2] * v2[1],
                v1[2] * v2[0] - v1[0] * v2[2],
                v1[0] * v2[1] - v1[1] * v2[0]
        };
        float sum = v[0] * v[0] + v[1] * v[1] + v[2] * v[2];
        float norm = (float) Math.sqrt(sum);

        return new float[]{v[0] / norm, v[1] / norm, v[2] / norm};
    }


    public Pose getAnimatePose() {
        return AnimatePose;
    }

    public int getFrameCounter() {
        return frameCounter;
    }

    public int getMaxFrame() {
        return maxFrame;
    }

    public boolean getEndAnimate() {
        return endAnimate;
    }

    public String getTestString() {
        return TestString;
    }

    public float[] getGravity() {
        return gravity;
    }

    public void setSpeed(float v) {
        this.speed = v;
        setDirection(new float[]{0, 0, -1f * speed * 0.01f});
    }


    private void setDirection(float[] direction) {
        this.direction = direction;
    }


    private void setGravity(float[] f) {
        gravity = new float[f.length];
        System.arraycopy(f, 0, this.gravity, 0, f.length);
    }
}
