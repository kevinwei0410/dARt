package connection;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import game.Game;

public class SocketConnection {
    private HandlerThread mBackgroundThread;
    public Handler mBackgroundHandler;
    public Thread receiveThread;
    public DatagramSocket socket = null;
    private InetAddress serverAddr;
    private Game game;
    public SocketConnection(Game game)
    {
        this.game  = game;
    }
    public void startBackgroundThread(){
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    public void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public class SendImageData implements Runnable {
        private Bitmap mBitMap;
        private Bitmap resizeBitMap;
        private byte[] data = null;

        public SendImageData(Bitmap bmp) {
            mBitMap = bmp;
            resizeBitMap = Bitmap.createScaledBitmap(mBitMap, mBitMap.getWidth() / 10, mBitMap.getHeight() / 10, true);
        }

        @Override
        public void run() {
            try {
                if (serverAddr == null) {
                    serverAddr = InetAddress.getByName("140.121.196.201");
                }
                if (socket == null) {
                    socket = new DatagramSocket();
                }

                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                resizeBitMap.compress(Bitmap.CompressFormat.JPEG, 80, byteStream);
                data = byteStream.toByteArray();
                try {
                    DatagramPacket packet = new DatagramPacket(data, data.length, serverAddr, 5000);
                    Log.i("data length", String.valueOf(data.length));
                    socket.send(packet);
                } catch (Exception e) {
                    System.out.println("Error 1:" + e.toString());
                }
            } catch (Exception e) {
                System.out.println("Error 2:" + e.toString());
                socket.close();
            }
            mBitMap.recycle();
        }
    }

    public class ReceiveThread implements Runnable {
        private byte[] revData = new byte[2048];
        private DatagramPacket revPacket = new DatagramPacket(revData, revData.length);

        @Override
        public void run() {
            if(socket == null) {
                try {
                    socket = new DatagramSocket();
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            }
            while(true)
            {
                // Receive data
                try {
                    socket.receive(revPacket);
                    float velocity = ByteBuffer.wrap(revPacket.getData()).order(ByteOrder.BIG_ENDIAN).getFloat();
                    Log.i("Server Message", String.valueOf(velocity));
                    game.shootDart((5*velocity)+10);


                } catch (Exception e) {
                    Log.i("Receive Error", e.toString());
                }
            }
        }
    }
}
