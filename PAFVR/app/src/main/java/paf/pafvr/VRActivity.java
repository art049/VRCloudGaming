package paf.pafvr;

import android.content.Intent;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;

import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Scanner;

import javax.microedition.khronos.egl.EGLConfig;

import io.vov.vitamio.MediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * Created by Aloïs on 16/06/2016.
 */
public class VRActivity extends GvrActivity implements GvrView.StereoRenderer, SurfaceTexture.OnFrameAvailableListener, MediaPlayer.OnPreparedListener, IjkMediaPlayer.OnPreparedListener{

    private float[] mMVPMatrix = new float[16];
    private float[] mSTMatrix = new float[16];

    private int mProgram;
    private int mTextureIDLeft;
    private int mTextureIDRight;
    private int mTextureIDBoth;
    private int muMVPMatrixHandle;
    private int muSTMatrixHandle;
    private int maPositionHandle;
    private int maTextureHandle;

    private SurfaceTexture mSurfaceLeft;
    private SurfaceTexture mSurfaceRight;
    private SurfaceTexture mSurfaceBoth;

    private boolean updateSurfaceLeft = false;
    private boolean updateSurfaceRight = false;
    private boolean updateSurfaceBoth=false;

    private static int GL_TEXTURE_EXTERNAL_OES = 0x8D65;

    private MediaPlayer mMediaPlayerLeft;
    private MediaPlayer mMediaPlayerRight;
    private MediaPlayer mMediaPlayerBoth;
    private IjkMediaPlayer mIjkMediaPlayerBoth;

    private Uri myUri;

    Point size;

    private static String TAG = "VRActivityRender";

    private static final int FLOAT_SIZE_BYTES = 4;
    private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
    private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
    private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;
    private final float[] mTriangleVerticesData = {
            // X, Y, Z, U, V
            -1.0f, -1.0f, 0, 0.f, 0.f,
            1.0f, -1.0f, 0, 1.f, 0.f,
            -1.0f,  1.0f, 0, 0.f, 1.f,
            1.0f,  1.0f, 0, 1.f, 1.f,
    };

    private FloatBuffer mTriangleVertices;

    private final String mVertexShader =
            "uniform mat4 uMVPMatrix;\n" +
                    "uniform mat4 uSTMatrix;\n" +
                    "attribute vec4 aPosition;\n" +
                    "attribute vec4 aTextureCoord;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "void main() {\n" +
                    "  gl_Position = uMVPMatrix * aPosition;\n" +
                    "  vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n" +
                    "}\n";

    private final String mFragmentShader =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "void main() {\n" +
                    "  gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                    "}\n";

    private Socket mSocket;
    private static final int SERVER_PORT = 5000;
    private static final String SERVER_IP = "192.168.42.220";
    boolean listening = true;

    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);


        Intent tetherSettings = new Intent();
        tetherSettings.setClassName("com.android.settings", "com.android.settings.TetherSettings");
        startActivity(tetherSettings);


        if (!io.vov.vitamio.LibsChecker.checkVitamioLibs(this))
            return;
        setContentView(R.layout.vr_layout);
        GvrView gvrView = (GvrView) findViewById(R.id.gvr_view);
        gvrView.setRenderer(this);
        setGvrView(gvrView);
        mTriangleVertices = ByteBuffer.allocateDirect(
                mTriangleVerticesData.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTriangleVertices.put(mTriangleVerticesData).position(0);
        Matrix.setIdentityM(mSTMatrix, 0);
        GLES20.glClearColor(1f, 1f, 1f, 1f);

        //new Thread(new ServerThread()).start();
    }


    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d(TAG, "onPrepared called");
        mp.start();
    }

    @Override
    public void onPrepared(IMediaPlayer mp) {

        Log.d(TAG, "onPrepared called");
        /*mIjkMediaPlayerBoth.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0);
        mIjkMediaPlayerBoth.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 0);
        mIjkMediaPlayerBoth.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);*/
        /* mIjkMediaPlayerBoth.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, );
        mMediaPlayerBoth.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzemaxduration", 100);


        mMediaPlayerBoth.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);
        mMediaPlayerBoth.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1);

        mMediaPlayerBoth.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_frame", 8);
        mMediaPlayerBoth.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);*/
        mp.start();
    }

    public void restartActivity(View v) {

        mMediaPlayerBoth.stop();
        mMediaPlayerBoth.release();
        /*mIjkMediaPlayerBoth.stop();
        mIjkMediaPlayerBoth.release();*/
        recreate();
    }

    class ServerThread implements Runnable {
        private ServerSocket server;
        private boolean clientConnected = false;

        @Override
        public void run() {
            try {
                server = new ServerSocket(SERVER_PORT);
                Log.d("Server", "Start the server at port " + SERVER_PORT
                        + " and waiting for clients...");
                while (!clientConnected) {
                    Socket socket = server.accept();
                    Log.d("Server",
                            "Accept socket connection: "
                                    + socket.getLocalAddress());
                    clientConnected = true;
                    new Thread(new ClientHandler(socket)).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    class ClientHandler implements Runnable {

        private Socket clientSocket;
        private PrintWriter out;
        private Scanner in;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream());
                in = new Scanner(clientSocket.getInputStream());
                String line;
                Log.d("ClientHandlerThread", "Start communication with : "
                        + clientSocket.getLocalAddress());
                out.println("Hello client");
                out.flush();
                while ((line = in.nextLine()) != null) {
                    Log.d("ClientHandlerThread", "Client says: " + line);
                    if (line.equals("Reply")){
                        out.print("Server replies");
                        out.flush();
                    }
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

    }

    /*
    class Client implements Runnable {
        private Socket client;
        private PrintWriter out;
        private Scanner in;

        @Override
        public void run() {
            try {
                client = new Socket("192.168.42.220", SERVER_PORT);
                Log.d("Client", "Connected to server at port " + SERVER_PORT);
                out = new PrintWriter(client.getOutputStream());
                in = new Scanner(client.getInputStream());
                String line;

                while ((line = in.nextLine()) != null) {
                    Log.d("Client", "Server says: " + line);
                    if (line.equals("Hello client")) {
                        out.println("Reply");
                        out.flush();
                    }
                }

            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }*/


    /*class ClientThread implements Runnable {

        boolean connectionStarted = false;
        byte[] buffer = new byte[32 * 1024];
        String string = new String();

        @Override
        public void run() {

            while (!connectionStarted) {
                try {
                    InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                    mSocket = new Socket(serverAddr, SERVERPORT);
                    connectionStarted = true;

                } catch (UnknownHostException e1) {
                    //e1.printStackTrace();
                    Log.e("connection", "connection failed");
                } catch (IOException e1) {
                    //e1.printStackTrace();
                    Log.e("connection", "connection failed");
                }

            }
            while (mSocket == null) ;
            Log.d("serveur", "Socket created");

            try {
                BufferedInputStream in = new BufferedInputStream(mSocket.getInputStream());
                while (listening) {
                    try {
                        int d = in.read();
                        if(d==-1) Log.d("connection", "eof");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }*/

    @Override
    public void onNewFrame(HeadTransform headTransform){

        float[] angles = new float[3];
        headTransform.getEulerAngles(angles, 0);
        String messageStr = angles[0] +" "+ angles[1];
        //Log.d(TAG, messageStr);
        new GyroTask().execute(messageStr);
        //Log.d("tag", "lol");
    }

    @Override
    public void onDrawEye(Eye eye) {

        /* Pour deux images
        switch(eye.getType()){
            case(Eye.Type.LEFT):
                synchronized (this) {
                    if (updateSurfaceLeft) {
                        mSurfaceLeft.updateTexImage();
                        mSurfaceLeft.getTransformMatrix(mSTMatrix);
                        updateSurfaceLeft = false;
                    }
                }
                break;
            case(Eye.Type.RIGHT):
                synchronized (this) {
                    if (updateSurfaceRight) {
                        mSurfaceRight.updateTexImage();
                        mSurfaceRight.getTransformMatrix(mSTMatrix);
                        updateSurfaceRight = false;
                    }
                }
                break;
        }*/

        /* Pour une seule image coupée en deux*/
        synchronized(this) {
            mSurfaceBoth.updateTexImage();
            mSurfaceBoth.getTransformMatrix(mSTMatrix);
            switch(eye.getType()){
                case(Eye.Type.LEFT):
                    for(int i=0; i<4; i++){
                        mSTMatrix[i]=mSTMatrix[i]/2;
                    }
                    updateSurfaceBoth = false;
                    break;
                case(Eye.Type.RIGHT):
                    for(int i=0; i<4; i++){
                        mSTMatrix[i]=mSTMatrix[i]/2;
                    }
                    for(int i=12; i<16; i++){
                        mSTMatrix[i]=mSTMatrix[i]+mSTMatrix[i-12];
                    }
                    updateSurfaceBoth = false;
                    break;
            }

        }

        GLES20.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(mProgram);
        checkGlError("glUseProgram");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        /* Pour deux images
        switch(eye.getType()){
            case(Eye.Type.LEFT): {
                GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureIDLeft);
                break;
            }
            case(Eye.Type.RIGHT): {
                GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureIDRight);
                break;
            }
        }*/

        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
        checkGlError("glVertexAttribPointer maPosition");
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        checkGlError("glEnableVertexAttribArray maPositionHandle");

        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
        GLES20.glVertexAttribPointer(maTextureHandle, 3, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
        checkGlError("glVertexAttribPointer maTextureHandle");
        GLES20.glEnableVertexAttribArray(maTextureHandle);
        checkGlError("glEnableVertexAttribArray maTextureHandle");

        Matrix.setIdentityM(mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(muSTMatrixHandle, 1, false, mSTMatrix, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        checkGlError("glDrawArrays");
        GLES20.glFinish();

    }

    @Override
    public void onFinishFrame(Viewport viewport) {

    }

    @Override
    public void onSurfaceChanged(int i, int i1) {

    }

    @Override
    public void onSurfaceCreated(EGLConfig config) {
        mProgram = createProgram(mVertexShader, mFragmentShader);
        if (mProgram == 0) {
            return;
        }

        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        checkGlError("glGetAttribLocation aPosition");
        if (maPositionHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aPosition");
        }
        maTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
        checkGlError("glGetAttribLocation aTextureCoord");
        if (maTextureHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aTextureCoord");
        }
        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        checkGlError("glGetUniformLocation uMVPMatrix");
        if (muMVPMatrixHandle == -1) {
            throw new RuntimeException("Could not get attrib location for uMVPMatrix");
        }
        muSTMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uSTMatrix");
        checkGlError("glGetUniformLocation uSTMatrix");
        if (muSTMatrixHandle == -1) {
            throw new RuntimeException("Could not get attrib location for uSTMatrix");
        }

        int[] textures = new int[3];
        GLES20.glGenTextures(3, textures, 0);

        mTextureIDLeft = textures[0];
        mTextureIDRight = textures[1];
        mTextureIDBoth = textures[2];

        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureIDBoth);
        checkGlError("glBindTexture mTextureID");

        GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);

            /*
             * Create the SurfaceTexture that will feed this textureID,
             * and pass it to the MediaPlayer
             */

        /* Pour deux images
        mSurfaceLeft = new SurfaceTexture(mTextureIDLeft);
        mSurfaceLeft.setDefaultBufferSize(size.x, size.y);
        mSurfaceLeft.setOnFrameAvailableListener((SurfaceTexture.OnFrameAvailableListener) this);


        Surface surfaceLeft = new Surface(mSurfaceLeft);
        mMediaPlayerLeft = MediaPlayer.create(this, R.raw.cat);
        mMediaPlayerLeft.setSurface(surfaceLeft);
        surfaceLeft.release();

        mSurfaceRight = new SurfaceTexture(mTextureIDRight);
        mSurfaceRight.setDefaultBufferSize(size.x, size.y);
        mSurfaceRight.setOnFrameAvailableListener((SurfaceTexture.OnFrameAvailableListener) this);

        Surface surfaceRight = new Surface(mSurfaceRight);
        mMediaPlayerRight = MediaPlayer.create(this, R.raw.dog);
        mMediaPlayerRight.setSurface(surfaceRight);
        surfaceRight.release();

        synchronized(this) {
            updateSurfaceLeft = false;
        }

        synchronized(this) {
            updateSurfaceRight = false;
        }

        mMediaPlayerRight.setLooping(true);
        mMediaPlayerLeft.setLooping(true);
        mMediaPlayerLeft.start();
        mMediaPlayerRight.start();
        */

        /*Pour une seule image*/
        mSurfaceBoth = new SurfaceTexture(mTextureIDBoth);
        mSurfaceBoth.setOnFrameAvailableListener((SurfaceTexture.OnFrameAvailableListener) this);
        Surface surfaceBoth = new Surface(mSurfaceBoth);
        myUri = Uri.parse("udp://localhost:5454?Listen");
        //String path = "raw/cat.mp4";

        // Avec le MediaPlayer de Vitamio
        mMediaPlayerBoth = new MediaPlayer(this);
        try {
            mMediaPlayerBoth.setDataSource(this, myUri);
            mMediaPlayerBoth.setAdaptiveStream(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayerBoth.setSurface(surfaceBoth);
        mMediaPlayerBoth.setBufferSize(0);
        mMediaPlayerBoth.prepareAsync();
        mMediaPlayerBoth.setOnPreparedListener(this);


        /* Avec ijk
        mIjkMediaPlayerBoth = new IjkMediaPlayer();
        try {
            mIjkMediaPlayerBoth.setDataSource(this, myUri);
            mIjkMediaPlayerBoth.setSurface(surfaceBoth);
            mIjkMediaPlayerBoth.prepareAsync();
            mIjkMediaPlayerBoth.setOnPreparedListener(this);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        synchronized (this) {
            updateSurfaceBoth = false;
        }
    }

    @Override
    public void onRendererShutdown() {

    }

    synchronized public void onFrameAvailable(SurfaceTexture surface) {
        updateSurfaceLeft = true;
        updateSurfaceRight = true;
        updateSurfaceBoth = true;
    }

    private int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                Log.e(TAG, "Could not compile shader " + shaderType + ":");
                Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

    private int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }
        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (pixelShader == 0) {
            return 0;
        }

        int program = GLES20.glCreateProgram();
        if (program != 0) {
            GLES20.glAttachShader(program, vertexShader);
            checkGlError("glAttachShader");
            GLES20.glAttachShader(program, pixelShader);
            checkGlError("glAttachShader");
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.e(TAG, "Could not link program: ");
                Log.e(TAG, GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }
        return program;
    }

    private void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }

}

