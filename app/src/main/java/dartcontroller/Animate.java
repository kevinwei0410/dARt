package dartcontroller;

import android.util.Log;

import com.google.ar.core.Pose;

public class Animate {

    private float speed;
    // direction is a three float vector
    private float[] direction;

    private float[] gravity;

    private float[] rotationVector;

    // max frame set by 60 frame
    final private int maxFrame = 20000;

    // Number of milliseconds in one second.
    private static final float MILLISECONDS_PER_SECOND = 1000f;

    // Rate by which smoothed frame rate should approach momentary frame rate.
    private static final float SMOOTHING_FACTOR = .03f;

    // System time of last frame, or zero if no time has been recorded.
    private long previousFrameTime;

    // Smoothed frame time, or zero if frame time has not yet been recorded.
    private float smoothedFrameTime;

    // time the animate starts
    private long startAnimateTime;

    private int frameCounter = 0;

    private boolean endAnimate ;


    public Animate(float s, float[] d, float[] g){
        //construct direction speed starting position
        this.direction = d;
        this.speed = s;
        this.gravity = g;
        setQVector();
    }

    // start the animate, set the starting time and previousFrame time
    public void startAnimate(){
        startAnimateTime = System.currentTimeMillis();
        frameCounter = 0;
        endAnimate = true;
    }

    // calculate the new pose with current time
    //
    public Pose getPose(){
        long deltaT = System.currentTimeMillis() - startAnimateTime;
        float del = (float)deltaT;
        del /= 1000f;
        float[] translation = {direction[0]*speed*del - gravity[0]*del*del, direction[1]*speed*del - gravity[1]*del*del, direction[2]*speed*del - gravity[2]*del*del};
        float[] newSpeed = {direction[0] - gravity[0]*del, direction[1] - gravity[1]*del, direction[2] - gravity[2]*del};
        float[] rotation =getRotation(rotationVector, getTheta(direction, newSpeed ) );
        Log.d("getPose", String.format("%f %f %f %f",translation[0], translation[1], translation[2], del));
        Log.d("getRotate", String.format("%f %f %f %f",rotation[0], rotation[1], rotation[2], del));
        if(deltaT >= maxFrame )
        {
            endAnimate = false;
        }else {
            frameCounter++;
        }
        return new Pose(translation, rotation);
    }

    private float getTheta(float[] v1, float[] v2){

        float inner =  v1[0]*v2[0] + v1[1]*v2[1] +v1[2]*v2[2];
        float d1 =(float) Math.sqrt(v1[0]*v1[0] + v1[1]*v1[1] +v1[2]*v1[2]);
        float d2 =(float) Math.sqrt(v2[0]*v2[0] + v2[1]*v2[1] +v2[2]*v2[2]);
        float theta = (float)Math.acos(inner/d1/d2 );
        if(theta > 180) {
            return theta -180;
        }
        return theta;
    }

    private void setQVector(){

        float[] v = new float[]{
                direction[1]*gravity[2] - direction[2]* gravity[1],
                direction[2]*gravity[0] - direction[0]* gravity[2],
                direction[0]*gravity[1] - direction[1]* gravity[0]
        };
        float sum = v[0]*v[0] + v[1]*v[1] +v[2]*v[2];
        float norm = (float)Math.sqrt(sum);
        rotationVector = new float[]{v[0]/norm, v[1]/norm, v[2]/norm};
    }

    private float[] getRotation(float[] k, float theta){
        float x =0f , y=0f,z=0f, w=1f;
        x = k[0]*(float)Math.sin(theta/2);
        y = k[1]*(float)Math.sin(theta/2);
        z = k[2]*(float)Math.sin(theta/2);
        w = (float)Math.cos(theta/2);
        return new float[]{x, y, z, w};
    }


    public Pose getFirstRotate(){
        float[] g = {0, 0, -1};
        float[] v = new float[]{
                direction[1]*g[2] - direction[2]* g[1],
                direction[2]*g[0] - direction[0]* g[2],
                direction[0]*g[1] - direction[1]* g[0]
        };
        float sum = v[0]*v[0] + v[1]*v[1] +v[2]*v[2];
        float norm = (float)Math.sqrt(sum);
        float[] rotation =getRotation(new float[]{v[0]/norm, v[1]/norm, v[2]/norm}, getTheta(direction, g ) );
        return new Pose(new float[]{0,0,0},rotation);
    }

    public int getFrameCounter(){return frameCounter;}
    public int getMaxFrame(){return maxFrame;}
    public boolean getEndAnimate(){return endAnimate;}
}
