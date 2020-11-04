package connection;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.google.ar.core.Pose;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import game.Game;
import kotlin.Pair;

public class SocketConnection {
    private HandlerThread mCameraThread;
    private HandlerThread mGameDataThread;
    public Handler mCameraHandler;
    public Handler mGameDataHandler;
    public Thread receiveThread;
    public Thread receiveGameData;
    public DatagramSocket cameraSocket = null;
    public DatagramSocket gameDataSocket = null;
    private InetAddress serverAddress;
    private Game game;
    private kotlin.Pair<Pose, Float> gameData = null;
    private Long send_time;
    private Long recv_time;
    public float[] callBackGameData = new float[7];
    public float[] translationArray = new float[3];
    public float[] roatationArray = new float[4];

    public SocketConnection(Game game)
    {
        this.game  = game;
    }

    public void startCameraThread(){
        mCameraThread = new HandlerThread("CameraThread");
        mCameraThread.start();
        mCameraHandler = new Handler(mCameraThread.getLooper());
    }

    public void stopCameraThread() {
        mCameraThread.quitSafely();
        try {
            mCameraThread.join();
            mCameraThread = null;
            mCameraHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void startGameDataThread()
    {
        mGameDataThread = new HandlerThread("GameDataThread");
        mGameDataThread.start();
        mGameDataHandler = new Handler(mGameDataThread.getLooper());
    }

    public void stopGameDataThread() {
        mGameDataThread.quitSafely();
        try{
            mGameDataThread.join();
            mGameDataThread = null;
            mGameDataHandler = null;
        } catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    public class SendCoordinate implements Runnable{
        private Pair<Pose, Float> poseFloatPair;
        private float gameData[] = new float[7];
        public SendCoordinate(kotlin.Pair<Pose, Float> data){
            poseFloatPair = data;
            poseFloatPair.getFirst().getRotationQuaternion(gameData,0); // rotation: x,y,z,w
            poseFloatPair.getFirst().getTranslation(gameData, 4); //X,Y,Z
            Log.i("Send_GameData", Arrays.toString(gameData));
        }

        public void run() {
            try {
                if (serverAddress == null) {
                    serverAddress = InetAddress.getByName("140.121.196.201");
                }
                if (gameDataSocket == null) {
                    gameDataSocket = new DatagramSocket();
                }
                try {
                    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                    DataOutputStream toServerData = new DataOutputStream(byteStream);
                    for(int data_index = 0; data_index<gameData.length; data_index+=1)
                    {
                        toServerData.writeFloat(gameData[data_index]);
                    }
                    toServerData.flush();
                    byte[] data = byteStream.toByteArray();
                    DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress, 5001);
                    gameDataSocket.send(packet);
                } catch (Exception e) {
                    System.out.println("Error send Coordinate:" + e.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public class SendCameraImage implements Runnable {
        private Bitmap mBitMap;
        private Bitmap resizeBitMap;
        private byte[] data = null;

        public SendCameraImage(Bitmap bmp) {
            mBitMap = bmp;
            resizeBitMap = Bitmap.createScaledBitmap(mBitMap, mBitMap.getWidth() / 10, mBitMap.getHeight() / 10, true);
        }

        @Override
        public void run() {
            try {
                if (serverAddress == null) {
                    serverAddress = InetAddress.getByName("140.121.196.201");
                }
                if (cameraSocket == null) {
                    cameraSocket = new DatagramSocket();
                }

                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                resizeBitMap.compress(Bitmap.CompressFormat.JPEG, 80, byteStream);
                data = byteStream.toByteArray();
                try {
                    DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress, 5000);
                    Log.i("data length", String.valueOf(data.length));
                    send_time = System.currentTimeMillis();
                    cameraSocket.send(packet);
                } catch (Exception e) {
                    System.out.println("Error send Image:" + e.toString());
                }
            } catch (Exception e) {
                System.out.println("Error trying connect to Server:" + e.toString());
                cameraSocket.close();
            }
            mBitMap.recycle();
        }
    }

    public class ReceiveThread implements Runnable {
        private byte[] revData = new byte[2048];
        private DatagramPacket revPacket = new DatagramPacket(revData, revData.length);

        @Override
        public void run() {
            if(cameraSocket == null) {
                try {
                    cameraSocket = new DatagramSocket();
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            }
            while(true)
            {
                // Receive data
                try {
                    cameraSocket.receive(revPacket);
                    recv_time = System.currentTimeMillis();
                    float velocity = ByteBuffer.wrap(revPacket.getData()).order(ByteOrder.BIG_ENDIAN).getFloat();
                    Log.i("Server Message", String.valueOf(velocity));
                    gameData = game.shootDart((10*velocity)+5);
                    mGameDataHandler.post(new SendCoordinate(gameData));
                } catch (Exception e) {
                    Log.i("Receive Error", e.toString());
                }
            }
        }
    }

    public class ReceiveGameData implements Runnable{
        private byte[] revGameData = new byte[2048];
        private DatagramPacket revGamePacket = new DatagramPacket(revGameData, revGameData.length);

        @Override
        public void run() {
            if(gameDataSocket == null) {
                try {
                    gameDataSocket = new DatagramSocket();
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            }
            while(true){
                try{
                    gameDataSocket.receive(revGamePacket);
                    ByteBuffer.wrap(revGamePacket.getData()).order(ByteOrder.BIG_ENDIAN).asFloatBuffer().get(callBackGameData);
                    System.arraycopy(callBackGameData, 0, roatationArray, 0, 4);
                    System.arraycopy(callBackGameData, 4, translationArray, 0, 3);
                    Log.i("Receive_GameData", Arrays.toString(callBackGameData));
                    game.onOtherPlayersDartHitsDartboard(translationArray, roatationArray);
                } catch(Exception e) {
                    Log.i("Receive Game Data Error", e.toString());
                }
            }
        }
    }
}
