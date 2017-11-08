package android.pakaco.glvideorecorder;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;

import android.media.MediaRecorder;
import android.os.Environment;
import android.pakaco.glvideorecorder.pip.pipwrapping.AnimationRect;
import android.pakaco.glvideorecorder.pip.pipwrapping.PIPOperator;
import android.pakaco.glvideorecorder.pip.recorder.MediaRecorderWrapper;
//import android.support.v7.app.AppCompatActivity;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.IOException;

import java.lang.reflect.Method;



public class MainActivity extends Activity implements View.OnClickListener,
        MediaRecorderWrapper.OnInfoListener,TextureView.SurfaceTextureListener,
        View.OnTouchListener,ScaleGestureDetector.OnScaleGestureListener,
        GestureDetector.OnGestureListener{
    private static final String TAG = "Main:mediaRecorder";
    private TextureView mTextureView;
    private Button mStartBtn;
    private Button mStopBtn;
    private MediaRecorderWrapper mMediaRecorder;
    private PIPOperator mPipOperator;
    private Camera mMainCamera = null;
    private Camera mSlaveCamera = null;
    private Camera mCamera = null;
    private Parameters mParameters;
    public static float mRenderParameter[] ={-90F,0.0F,1.38F};
    float mAngleX = -90F;
    float mAngleY = 0.0F;
    float mRate = 1.38F;
    private ScaleGestureDetector mScaleGestureDetector;
    private GestureDetector detector;

    private float mPreviousX;
    private float mPreviousY;
    private float distance = 0;
    private final float TOUCH_SCALE_FACTOR = 0.2f;//180.0f /720;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextureView = (TextureView)findViewById(R.id.preview_texview);
        mTextureView.setSurfaceTextureListener(this);
        mTextureView.setOnTouchListener(this);
        mStartBtn = (Button)findViewById(R.id.start_recording);
        //mStartBtn.setOnClickListener(this);
        mStopBtn = (Button)findViewById(R.id.stop_recording);
        //mStopBtn.setOnClickListener(this);

        mScaleGestureDetector = new ScaleGestureDetector(this, this);
        //detector = new GestureDetector(this, this);

        //detector.setIsLongpressEnabled(true);
        //detector.setOnDoubleTapListener(new MyDoubleTapListener());
        //testFile();
        if (mPipOperator == null) {
            mPipOperator = new PIPOperator(this, new PIPOperator.Listener(){
                @Override
                public void onPIPPictureTaken(byte[] jpegData) {

                }

                @Override
                public void unlockNextCapture() {

                }

                @Override
                public AnimationRect getPreviewAnimationRect() {
                    return null;
                }

                @Override
                public void updateTouchedTabPostion(int x, int y, boolean flag) {
                    
                }
            });
            mPipOperator.initPIPRenderer();
        }
        initializePipRecorder();

        //mCamera = Camera.open(0);
        //mParameters = mCamera.getParameters();
        //mParameters.set("fisheye-switch", 1);        
        //mCamera.setParameters(mParameters);
        //mCamera.release();

        try {
            Method openMethod = Class.forName("android.hardware.Camera").getMethod(
                    "openLegacy", int.class, int.class);
            mMainCamera = (android.hardware.Camera)openMethod.invoke(null, 0, 0x100);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //mMainCamera = Camera.open(2);
        try {
            Method openMethod = Class.forName("android.hardware.Camera").getMethod(
                    "openLegacy", int.class, int.class);
            mSlaveCamera = (android.hardware.Camera)openMethod.invoke(null, 2, 0x100);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //mSlaveCamera = Camera.open(3);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.start_recording:
                Log.d(TAG,"onClick startRecording");
                mPipOperator.startPushVideoBuffer();
                mMediaRecorder.start();
                break;
            case R.id.stop_recording:
                Log.d(TAG,"onClick stopRecording");
                mPipOperator.stopPushVideoBuffer();
                mMediaRecorder.stop();
                mMediaRecorder.release();
                break;
            case R.id.preview_texview:
                break;
        }

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(v.getId()==R.id.preview_texview){
            Log.d(TAG,"preview_texview onTouch");
        }
        float x = event.getX();
        float y = event.getY();
        float newDist = 0;
        Log.d(TAG,"event.getAction()="+event.getAction());
        switch (event.getAction())
        {
            case MotionEvent.ACTION_MOVE:
                // rotate
                if (event.getPointerCount() == 1)
                {
                    float dx = x - mPreviousX;
                    float dy = y - mPreviousY;
                    float dr = (float) Math.sqrt(dx*dx+dy*dy);
                    mPreviousX =x;
                    mPreviousY =y;
                    Log.d(TAG, "wz delta: " + dx+"  dy:"+dy+"  dr:"+dr);

                    if(mPreviousX>0&&mPreviousY>0&&dr<30) {
                        mAngleX = mAngleX + dx * TOUCH_SCALE_FACTOR;
                        mAngleX = mAngleX % 360;


                        mAngleY = -dy * TOUCH_SCALE_FACTOR + mAngleY;
                        mAngleY = mAngleY % 360;
                    }

                    //mAngleX = (mAngleX+360)%360;

                }

                // pinch to zoom
                if (event.getPointerCount() == 2)
                {
                    if (distance == 0)
                    {
                        distance = fingerDist(event);
                    }
                    newDist = fingerDist(event);
                    float d = distance / newDist;


                    distance = newDist;
                }

        }
        updateRenderParameter();
        //detector.onTouchEvent(event);
        mScaleGestureDetector.onTouchEvent(event);
        return true;
    }

    @Override
    public void onInfo(MediaRecorderWrapper mr, int what, int extra) {
        Log.i(TAG, "[onInfo] what = " + what + "  extra = " + extra);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.i(TAG, "onSurfaceTextureAvailable width=" + width + "  height = " + height);
        mPipOperator.updateEffectTemplates(R.drawable.rear_01, R.drawable.rear_01, R.drawable.rear_01, R.drawable.plus);
        mPipOperator.setUpSurfaceTextures();
        mPipOperator.setPreviewTextureSize(width, height);
        AnimationRect top = new AnimationRect();
        top.initialize(100, 200, 480, 640);

        //mPipOperator.setPreviewSurface(new Surface(mTextureView.getSurfaceTexture()));
        mPipOperator.updateTopGraphic(top);

        if(mMainCamera != null){
            try {
                Parameters parameters  = mMainCamera.getParameters();
                parameters.setPreviewSize(1440,1080);
                mMainCamera.setParameters(parameters);
                mMainCamera.setPreviewTexture(mPipOperator.getBottomSurfaceTexture());
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        if(mSlaveCamera != null) {
            try {
                Parameters parameters  = mSlaveCamera.getParameters();
                parameters.setPreviewSize(1440,1080);
                mSlaveCamera.setParameters(parameters);
                mSlaveCamera.setPreviewTexture(mPipOperator.getTopSurfaceTexture());
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        mPipOperator.setPreviewSurface(new Surface(surface));
        if(mMainCamera != null)
            mMainCamera.startPreview();
        if(mSlaveCamera != null)
            mSlaveCamera.startPreview();

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mMainCamera != null) {
            mMainCamera.stopPreview();        
            mMainCamera.release();
        }
        if(mSlaveCamera != null) {
            mSlaveCamera.stopPreview();
            mSlaveCamera.release();
        }
    }


    private void initializePipRecorder() {
        Log.d(TAG, "[initializePipRecorder]...");
        mMediaRecorder = new MediaRecorderWrapper();

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

        mMediaRecorder.setOutputFile(getVideoFilePath());
        mMediaRecorder.setVideoEncodingBitRate(10000000);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoSize(1280, 720);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        //mMediaRecorder.setPreviewDisplay();

        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mPipOperator.prepareRecording();
        mPipOperator.setRecordingSurface(mMediaRecorder.getSurface());

    }




    private String getVideoFilePath() {

        String path= null;
        // video size
        final int width = 1280;
        final int height = 720;
        File file = new File(Environment.getExternalStorageDirectory()+"/storage",
                "record-" + width + "x" + height + "-" + System.currentTimeMillis() + ".mp4");
        path = file.getAbsolutePath();
        return  path;
    }
    private final void updateRenderParameter()
    {
        if(mRate > 3F)
            mRate = 3F;

        if(mAngleX!=mRenderParameter[0]|| mAngleY!=mRenderParameter[1]||mRate!=mRenderParameter[2])
            mPipOperator.updateScreenRenderParameter(mAngleX,mAngleY,mRate);

        mRenderParameter[0] = mAngleX;
        mRenderParameter[1] = mAngleY;
        mRenderParameter[2] = mRate;

        Log.d(TAG,"mAngleX="+mAngleX+" mAngleY="+mAngleY+" mRate="+mRate);
        return;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        Log.d(TAG,"onScale");
        mRate = mRate - 2.0F * (detector.getScaleFactor() - 1.0F);
        if(mRate < 0.5F)
            mRate = 0.5F;
           updateRenderParameter();
        Log.d(TAG, "onScale scaleFactor:"+detector.getScaleFactor()+"mRate: "+mRate);
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        //setRenderParameterForCenterPreview((float)(e.getX()),(float)( e.getY()));
        float af[] = getPointMapCompensation(e.getX(), e.getY());
        setRenderParameterForCenterPreview(Math.round(af[0]), Math.round(af[1]));
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    private float[] getPointMapCompensation(float f, float f1)
    {
        Matrix matrix = new Matrix();
        float af[] = new float[2];
        af[0] = f;
        af[1] = f1;
        matrix.mapPoints(af);
        return af;
    }

    private void setRenderParameterForCenterPreview(int i, int j)
    {
        int k = (int)mAngleX % 360;
        float f = 255F;
        if(k < 0)
            k += 360;
        float f2;
        float f1;
        float af[];
        if(f == 255F)
        {
            int l = k + (int)((180F / (float)mTextureView.getWidth()) * (float)(i - mTextureView.getWidth() / 2));

            if(l > 90 && l < 270)
                f = 180 - k;
            else
                f = 1 - k;
            if(Math.abs(f) >= 180F)
                if(f > 0.0F)
                    f = 360F - f;
                else
                    f += 360F;
        }
        f1 = mAngleY % 360F;
        if(f1 < 0.0F)
            f1 += 360F;
        f2 = 0.0F - f1;
        if(Math.abs(f2) >= 180F)
            if(f2 > 0.0F)
                f2 = 360F - f2;
            else
                f2 += 360F;
        if(f != 255F || f2 != 255F)
        {
            if(f == 255F)
                f = 0.0F;
            if(f2 == 255F)
                f2 = 0.0F;
            if(Math.abs(f) > 5F || Math.abs(f2) > 5F)
            {
                af = new float[3];
                af[0] = f;
                af[1] = f2;
                af[2] = 0.0F;
                //StartParameterAnimation(80 + 10 * Math.abs((int)f), af, mBounceInterpolator, false, false);
            } else
            {
                mAngleX = f + mAngleX;
                mAngleY = f2 + mAngleY;
                updateRenderParameter();
            }
        }
    }


    public static float [] getRenderParameter(){
        return  mRenderParameter;
    }

    protected final float fingerDist(MotionEvent event)
    {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    public void testFile(){
         String path="/custom/lib/camera_res";
        File file = new File(path);
        boolean fileExist = file.exists();
        boolean fileDerectory = file.isDirectory();
        file.isFile();

        Log.d(TAG,"fileExist:"+fileExist+" fileDerectory:"+fileDerectory);
        String listfile[] = file.list();
        Log.d(TAG,"listfile:"+listfile.length);
    }
}
