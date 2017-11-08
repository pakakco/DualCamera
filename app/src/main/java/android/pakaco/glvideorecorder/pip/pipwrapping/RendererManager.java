/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 *
 * MediaTek Inc. (C) 2014. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */
package android.pakaco.glvideorecorder.pip.pipwrapping;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.util.Size;
import android.view.Surface;


import java.util.HashMap;
import java.util.Map;

/**
 * Pip renderer manager.
 */
public class RendererManager {
    private static final String TAG = RendererManager.class.getSimpleName();
    private static final String BOTTOM = "pip_bottom";
    private static final String TOP = "pip_top";
    private final Activity mActivity;

    private int mCurrentOrientation;
    private int mPreviewTexWidth = -1;
    private int mPreviewTexHeight = -1;
    private boolean mIsBottomHasHighFrameRate = true;
    private boolean mPIPSwitched = false;
    private AnimationRect mPreviewTopGraphicRect = null;

    private HandlerThread mPreviewEglThread;
    private PreviewRendererHandler mPreviewEglHandler;

    private HandlerThread mCaptureEglThread;
    private CaptureRendererHandler mCaptureEglHandler;

    private int mBackResId = 0;
    private int mFrontResId = 0;
    private int mHighlightResId = 0;
    private int mEditBtnResId = 0;

    private RecorderRenderer mRecorderRenderer;

    private static final int _PROFILE_FRAME_CNT = 30;
    private static final boolean __debug_draw_bottom = false;
    private static final boolean __debug_draw_top = true;
    private static final boolean __normal_draw = true;
    private static final boolean __timestamp_sync_enable = true;
    private PIPOperator.Listener PIPOperatorListener;
    private int _profile_draw_fbo_cnts;
    private long _profile_draw_fbo_time_accu;
    //public PipFrameAliveDetector aliveDetector;
    private boolean mIsPIPOrginal = false;
    private int mLOGOSelected;
    private Service mService;

    
    public RendererManager(Activity activity) {
        mActivity = activity;
		mService = null;
    }

    public RendererManager(Service s) {
        mService = s;
        mActivity = null;
    }

    public void init() {
        Log.i(TAG, "[init]+");
        if (mPreviewEglHandler == null) {
            createPreviewGLThread();
        }
        initScreenRenderer();
        Log.i(TAG, "[init]-");
    }

    public void unInit() {
        Log.i(TAG, "[unInit]+");
        if (mPreviewEglHandler != null) {
            doReleaseAndQuitThread(mPreviewEglHandler, mPreviewEglThread);
            mPreviewEglHandler = null;
            mPreviewEglThread = null;
            mPreviewTexWidth = -1;
            mPreviewTexHeight = -1;
        }
        if (mCaptureEglHandler != null) {
            doReleaseAndQuitThread(mCaptureEglHandler, mCaptureEglThread);
            mCaptureEglHandler = null;
            mCaptureEglThread = null;
        }
        Log.i(TAG, "[unInit]-");
    }

    /**
     * update pip template resource.
     * Note: if resource id is the same with previous, call this function has no use.
     * @param backResId bottom graphic template
     * @param frontResId top graphic template
     * @param highlightResId top graphic highlight template
     * @param editBtnResId top graphic edit template
     */
    public void updateEffectTemplates(int backResId, int frontResId,
            int highlightResId, int editBtnResId) {
        if ((mBackResId) == backResId && (mFrontResId == frontResId)
                && (mHighlightResId == highlightResId)) {
            return;
        }
        if (mPreviewEglHandler != null) {
            mBackResId = backResId;
            mFrontResId = frontResId;
            mHighlightResId = highlightResId;
            mEditBtnResId = editBtnResId;
            mPreviewEglHandler.removeMessages(PipRendererHandler.MSG_UPDATE_TEMPLATE);
            mPreviewEglHandler.obtainMessage(
                    PipRendererHandler.MSG_UPDATE_TEMPLATE).sendToTarget();
        }
    }

    /**
     * Set pip preview texture's size.
     * <p>
     * Note: pip bottom and top texture's size must be the same for switch pip.
     * @param width bottom/top texture's width
     * @param height bottom/top texture's height
     */
    public void setPreviewSize(int width, int height) {
        Log.i(TAG, "[setPreviewTextureSize]+ width = " + width
                + " height = " + height);
        setPreviewSizeForSwitch(width,height);
        if (mPreviewTexWidth == width && mPreviewTexHeight == height) {
            Log.i(TAG, "setPreviewTextureSize same size set, ignore!");
            return;
        }
        if (mPreviewEglHandler != null) {
            mPreviewEglHandler.obtainMessage(
                    PipRendererHandler.MSG_UPDATE_RENDERER_SIZE,
                        width, height, null).sendToTarget();
            waitDone(mPreviewEglHandler);
        }
        Log.i(TAG, "[setPreviewTextureSize]-");
    }

    /**
     * create two surface textures, switch pip by needSwitchPIP
     * <p>
     * By default, bottom surface texture is drawn in bottom graphic.
     * top surface texture is drawn in top graphic.
     */
    public void setUpSurfaceTextures() {
        Log.i(TAG, "[setUpSurfaceTextures]-");
        boolean needUpdate = false;
        // press home key exit pip and resume again, template update action will not happen
        // here call update template for this case.
        // update template should not block ui thread
        if (mPreviewEglHandler != null && !mPreviewEglHandler.hasMessages(
                PipRendererHandler.MSG_UPDATE_TEMPLATE)) {
            needUpdate = true;
        }
        setupPIPTextures();
        if (needUpdate && mPreviewEglHandler != null) {
            mPreviewEglHandler.obtainMessage(
                    PipRendererHandler.MSG_UPDATE_TEMPLATE).sendToTarget();
        }
        Log.i(TAG, "[setUpSurfaceTextures]-");
    }

    /**
     * Set preview surface to receive pip preview buffer.
     * <p>
     * Note: this must be called after setPreviewTextureSize
     * @param surface used to receive preview buffer
     */
    public void setPreviewSurfaceSync(Surface surface) {
        Log.i(TAG, "setPreviewSurfaceSync");
        if (mPreviewEglHandler != null && surface != null) {
            mPreviewEglHandler.obtainMessage(
                    PipRendererHandler.MSG_SET_PREVIEW_SURFACE, surface).sendToTarget();
            waitDone(mPreviewEglHandler);
        }
    }

    /**
     * Set preview surface to receive pip preview buffer.
     * <p>
     * Note: this must be called after setPreviewTextureSize
     * @param surface the surface used for preview
     */
    public void notifySurfaceViewDestroyed(Surface surface) {
        if (mPreviewEglHandler != null) {
            mPreviewEglHandler.obtainMessage(
                    PipRendererHandler.MSG_PREVIEW_SURFACE_DESTORY, surface).sendToTarget();
        }
    }

    /**
     * Get bottom surface texture.
     * @return bottom graphic surface texture
     */
    public SurfaceTexture getBottomPvSt() {
        Log.i(TAG, "getBottomSurfaceTexture mIsPIPSwitched = " + mPIPSwitched);
        return mPIPSwitched ? getTopSurfaceTexture() : getBottomSurfaceTexture();
    }

    /**
     * Get top surface texture.
     * @return top graphic surface texture
     */
    public SurfaceTexture getTopPvSt() {
        Log.i(TAG, "getTopSurfaceTexture mIsPIPSwitched = " + mPIPSwitched);
        return mPIPSwitched ? getBottomSurfaceTexture() : getTopSurfaceTexture();
    }

    /**
     * update top graphic's position.
     * @param topGraphic the top grapihc's position
     */
    public void updateTopGraphic(AnimationRect topGraphic) {
        Log.i(TAG, "updateTopGraphic");
        mPreviewTopGraphicRect = topGraphic;
    }

    /**
     * when G-sensor's orientation changed, should update it to PIPOperator.
     * @param newOrientation G-sensor's new orientation
     */
    public void updateGSensorOrientation(int newOrientation) {
        Log.i(TAG, "updateOrientation newOrientation = " + newOrientation);
        mCurrentOrientation = newOrientation;
    }

    public int getPreviewTextureWidth() {
        return mPreviewTexWidth;
    }

    public int getPreviewTextureHeight() {
        return mPreviewTexHeight;
    }

    /**
     * Prepare recording renderer.
     */
    public void prepareRecordSync() {
        if (mRecorderRenderer == null) {
            mRecorderRenderer = new RecorderRenderer(mActivity);
        }
        if (mPreviewEglHandler != null) {
            mPreviewEglHandler.removeMessages(PipRendererHandler.MSG_SETUP_VIDEO_RENDER);
            mPreviewEglHandler.obtainMessage(
                    PipRendererHandler.MSG_SETUP_VIDEO_RENDER).sendToTarget();
            waitDone(mPreviewEglHandler);
        }
    }
    /**
     * Set recording surface to receive pip buffer.
     * Note: this must be called after setPreviewTextureSize
     * @param surface used for receiving video buffer
     */
    public void setRecordSurfaceSync(Surface surface) {
        if (mPreviewEglHandler != null) {
            mPreviewEglHandler.obtainMessage(
                    PipRendererHandler.MSG_SET_RECORDING_SURFACE, surface).sendToTarget();
            waitDone(mPreviewEglHandler);
        }
    }

    /**
     * Start push video buffer to encoder's surface.
     */
    public void startRecordSync() {
        if (mRecorderRenderer != null) {
            mRecorderRenderer.startRecording();
        }
    }

    /**
     * Stop push video buffer to encoder's surface.
     */
    public void stopRecordSync() {
        if (mRecorderRenderer != null) {
            mRecorderRenderer.stopRecording();
            mRecorderRenderer = null;
        }
    }

    public void switchPipSync() {
        Log.i(TAG, "switchPIP");
        if (mPreviewEglHandler != null) {
            mPreviewEglHandler.obtainMessage(PipRendererHandler.MSG_SWITCH_PIP).sendToTarget();
            waitDone(mPreviewEglHandler);
        }
    }

    /**
     * Take a video snap shot by orientation.
     * @param orientation video snap shot orientation
     * @param vssSurface the surface used to receive
     */
    public void takeVideoSnapShot(int orientation, Surface vssSurface) {
        Log.i(TAG, "takeVideoSnapShot orientation = " + orientation);
        if (mPreviewEglHandler != null) {
            mPreviewEglHandler.removeMessages(PipRendererHandler.MSG_TAKE_VSS);
            mPreviewEglHandler.obtainMessage(
                    PipRendererHandler.MSG_TAKE_VSS, orientation, 0, vssSurface).sendToTarget();
            waitDone(mPreviewEglHandler);
        }
    }

    public SurfaceTexture getBottomCapSt() {
        return mCaptureEglHandler == null ? null : mCaptureEglHandler.getBottomSt();
    }

    public SurfaceTexture getTopCapSt() {
        return mCaptureEglHandler == null ? null : mCaptureEglHandler.getTopSt();
    }

    /**
     *
     * @return pixel format that this egl can output.
     */
    public int initCapture(int[] inputFormats) {
        checkAndCreateCaptureGLThread(inputFormats);
        return mCaptureEglHandler.getPixelFormat();
    }

    /**
     * @param surface the surface used for taking picture.
     */
    public void setCaptureSurface(Surface surface) {
        if (mCaptureEglHandler != null) {
            mCaptureEglHandler.obtainMessage(
                    CaptureRendererHandler.MSG_SETUP_CAPTURE_SURFACE, surface).sendToTarget();
            waitDone(mCaptureEglHandler);
        }
    }

    /**
    *
    * @param bottomCaptureSize bottom picture size
    * @param topCaptureSize top picture size
    */
    public void setCaptureSize(Size bottomCaptureSize, Size topCaptureSize) {
        if (mCaptureEglHandler != null) {
            Map<String, Size> pictureSizeMap = new HashMap<String, Size>();
            pictureSizeMap.put(BOTTOM, bottomCaptureSize);
            pictureSizeMap.put(TOP, topCaptureSize);
            mCaptureEglHandler.obtainMessage(
                   CaptureRendererHandler.MSG_SETUP_PICTURE_TEXTURES,
                       pictureSizeMap).sendToTarget();
            waitDone(mCaptureEglHandler);
        }
    }

    /**
     * Set the jpeg's rotation received from Capture SurfaceTexture.
     * @param isBottomCam is bottom jpeg's rotation.
     * @param rotation received from surface texture's jpeg rotation.
     */
    public void setJpegRotation(boolean isBottomCam, int rotation) {
        if (mCaptureEglHandler != null) {
            mCaptureEglHandler.setJpegRotation(isBottomCam, rotation);
        }
    }

    public void unInitCapture() {
        if (mCaptureEglHandler != null) {
            doReleaseAndQuitThread(mCaptureEglHandler, mCaptureEglThread);
            mCaptureEglHandler = null;
            mCaptureEglThread = null;
        }
    }

    public void setRenderDrawPause(boolean flag)
    {
        if(mPreviewEglHandler != null)
            mPreviewEglHandler.obtainMessage(PipRendererHandler.MSG_SET_DRAW_PAUSE, (Boolean)flag).sendToTarget();
    }

    public void setCurrTouchTapPostion(int i, int j)
    {
        if(mPreviewEglHandler != null)
            mPreviewEglHandler.obtainMessage(PipRendererHandler.MSG_GET_REAL_TOUCH_TAP_POSITION, i, j, null).sendToTarget();
        else
            Log.d(TAG, (new StringBuilder()).append("setCurrTouchTapPostion error: mPreviewEglHandler=").append(mPreviewEglHandler).toString());
    }

    public void setListener(PIPOperator.Listener listener)
    {
        PIPOperatorListener = listener;
        if(mPreviewEglHandler != null)
            mPreviewEglHandler.setListener(listener);
    }

    private void initScreenRenderer() {
        if (mPreviewEglHandler != null) {
            mPreviewEglHandler.obtainMessage(
                    PipRendererHandler.MSG_INIT_SCREEN_RENDERER).sendToTarget();
            waitDone(mPreviewEglHandler);
        }
    }

    private void setupPIPTextures() {
        Log.i(TAG, "setupPIPTextures");
        if (mPreviewEglHandler != null) {
            // here should not remove frame message, must consume all frames
            // otherwise frame will not come to ap if previous frames are not consumed.
            mPreviewEglHandler.obtainMessage(
                    PipRendererHandler.MSG_SETUP_PIP_TEXTURES).sendToTarget();
            waitDone(mPreviewEglHandler);
        }
    }

    private SurfaceTexture getBottomSurfaceTexture() {
        return mPreviewEglHandler == null ? null : mPreviewEglHandler.getBottomSt();
    }

    private SurfaceTexture getTopSurfaceTexture() {
        return mPreviewEglHandler == null ? null : mPreviewEglHandler.getTopSt();
    }

    private void createPreviewGLThread() {
        mPreviewEglThread = new HandlerThread("Pip-PreviewGLThread");
        mPreviewEglThread.start();
        Looper looper = mPreviewEglThread.getLooper();
        if (looper == null) {
            throw new RuntimeException("why looper is null?");
        }
        mPreviewEglHandler = new PreviewRendererHandler(looper);
        mPreviewEglHandler.obtainMessage(PipRendererHandler.MSG_INIT).sendToTarget();
        waitDone(mPreviewEglHandler);
    }

    private void checkAndCreateCaptureGLThread(int[] formats) {
        if (mCaptureEglHandler == null) {
            mCaptureEglThread = new HandlerThread("Pip-CaptureGLThread");
            mCaptureEglThread.start();
            Looper looper = mCaptureEglThread.getLooper();
            if (looper == null) {
                throw new RuntimeException("why looper is null?");
            }
            mCaptureEglHandler = new CaptureRendererHandler(looper);
            mCaptureEglHandler.obtainMessage(PipRendererHandler.MSG_INIT, formats).sendToTarget();
            waitDone(mCaptureEglHandler);
        }
    }

    private void doReleaseAndQuitThread(Handler handler, HandlerThread thread) {
        handler.removeCallbacksAndMessages(null);
        handler.obtainMessage(PipRendererHandler.MSG_RELEASE).sendToTarget();
        waitDone(handler);
        Looper looper = thread.getLooper();
        if (looper != null) {
            looper.quit();
        }
    }

    //TODO must move handler to independent file
    /**
     * An abstract handler for reusing preview and capture handler's common code.
     */
    private abstract class PipRendererHandler extends Handler {
        public static final int MSG_INIT = 0;
        public static final int MSG_RELEASE = 1;
        public static final int MSG_UPDATE_TEMPLATE = 2;
        public static final int MSG_UPDATE_RENDERER_SIZE = 3;
        public static final int MSG_SETUP_VIDEO_RENDER = 4;
        public static final int MSG_SWITCH_PIP = 6;
        public static final int MSG_SETUP_PIP_TEXTURES = 7;
        public static final int MSG_NEW_BOTTOM_FRAME_ARRIVED = 8;
        public static final int MSG_NEW_TOP_FRAME_ARRIVED = 9;
        public static final int MSG_PREVIEW_SURFACE_DESTORY = 10;
        public static final int MSG_TAKE_VSS = 11;
        public static final int MSG_INIT_SCREEN_RENDERER = 13;
        public static final int MSG_SET_PREVIEW_SURFACE = 14;
        public static final int MSG_SET_RECORDING_SURFACE = 15;
        public static final int MSG_GET_REAL_TOUCH_TAP_POSITION = 16;
        public static final int MSG_SET_DRAW_PAUSE = 17;
        public static final int MSG_SET_LOGO = 18;
        public static final int MSG_COUNT = 19;
        public static final int MSG_SET_SCREEN_RENDER_PARAMETERS = MSG_COUNT+101;

        private static final int UNVALID_TEXTURE_ID = -12345;

        private final HandlerThread mFrameListener = new HandlerThread("PIP-STFrameListener");
        private Handler mSurfaceTextureHandler;

        private BottomGraphicRenderer mPreviewBottomGraphicRenderer;
        private TopGraphicRenderer mPreviewTopGraphicRenderer;
        private SurfaceTextureWrapper mBottomPrvSt;
        private SurfaceTextureWrapper mTopPrvSt;
        private int mPreviewFboTexId = UNVALID_TEXTURE_ID;

        private ScreenRendererV3 mScreenRenderer;
        private FrameBuffer mPreviewFrameBuffer;

        private CaptureRenderer mVssRenderer;

        private float[] mBottomTransformMatrix = new float[16];
        private float[] mTopCamTransformMatrix = new float[16];
        private long mLatestBottomCamTimeStamp = 0L;
        private long mLatestTopCamTimeStamp = 0L;
        private long mBottomCamTimeStamp = 0L;
        private long mTopCamTimeStamp = 0L;

        private long mLastFrameDrawTimeStamp_ = 0L;
        private int mLogoSelected = 0;
        private long mOnceLastFrameDrawTimeStamp_ = 0L;
        private boolean mPause = false;
        private static final long LIMIT_FPS_DELAY_MILLIS = 34L;
        private long LIMIT_ONCE_DELAY_MILLIS = 500L;

        private SurfaceTexture.OnFrameAvailableListener
            mBottomCamFrameAvailableListener = new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                Log.d(TAG,"SurfaceTexture.OnFrameAvailableListener bottom");
                obtainMessage(
                        PipRendererHandler.MSG_NEW_BOTTOM_FRAME_ARRIVED,
                            mBottomPrvSt).sendToTarget();
            }
        };

        private SurfaceTexture.OnFrameAvailableListener
            mTopCamFrameAvailableListener = new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                Log.d(TAG,"SurfaceTexture.OnFrameAvailableListener top");
                obtainMessage(
                        PipRendererHandler.MSG_NEW_TOP_FRAME_ARRIVED, mTopPrvSt).sendToTarget();
            }
        };

        protected EglCore mEglCore;
        protected EGLSurface mOffScreenSurface = null;
        private PipRendererHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Log.d("PipRendererHandler","handleMessage:"+getMSG_str(msg.what));
            switch (msg.what) {
            case MSG_UPDATE_TEMPLATE:
                doUpdateTemplate();
                break;
            case MSG_UPDATE_RENDERER_SIZE:
                mPreviewTexWidth = (Integer) msg.arg1;
                mPreviewTexHeight = (Integer) msg.arg2;
                doUpdateRenderSize();
                break;
            case MSG_SETUP_VIDEO_RENDER:
                setUpRenderForRecord();
                break;
            case MSG_SWITCH_PIP:
                doSwitchPIP();
                break;
            case MSG_INIT_SCREEN_RENDERER:
                doInitScreenRenderer();
                break;
            case MSG_SETUP_PIP_TEXTURES:
                doSetupPIPTextures();
                break;
            case MSG_SET_PREVIEW_SURFACE:
                doUpdatePreviewSurface((Surface) msg.obj);
                break;
            case MSG_SET_RECORDING_SURFACE:
                doUpdateRecordingSurface((Surface) msg.obj);
                break;
            case MSG_NEW_BOTTOM_FRAME_ARRIVED:
                doProcessPreviewFrame((SurfaceTextureWrapper) msg.obj, true);
                break;
            case MSG_NEW_TOP_FRAME_ARRIVED:
                doProcessPreviewFrame((SurfaceTextureWrapper) msg.obj, false);
                break;
            case MSG_PREVIEW_SURFACE_DESTORY:
                if (mScreenRenderer != null) {
                    mScreenRenderer.notifySurfaceStatus((Surface) msg.obj);
                }
                break;
            case MSG_TAKE_VSS:
                doVideoSnapShot((Integer) msg.arg1, (Surface) msg.obj);
                break;
            case MSG_RELEASE:
                doRelease();
                break;
            case MSG_GET_REAL_TOUCH_TAP_POSITION:
                doUpdateTouchTapPostion((Integer) msg.arg1, (Integer) msg.arg2);
                break;
            case MSG_SET_DRAW_PAUSE:
                setDrawPause((Boolean) msg.obj);
                break;
            case MSG_SET_SCREEN_RENDER_PARAMETERS:
                if(mScreenRenderer!=null) {
                    ScreenRenderParameter spr = (ScreenRenderParameter) msg.obj;
                    mScreenRenderer.updateScreenRenderParameter(spr.getAngleX(),
                            spr.getAngleY(),spr.getRate());
                }
                break;
            default:
                break;
            }
        }

        public SurfaceTexture getBottomSt() {
            return mBottomPrvSt.getSurfaceTexture();
        }

        public SurfaceTexture getTopSt() {
            return mTopPrvSt.getSurfaceTexture();
        }

        public void setDrawPause(boolean flag)
        {
            if(mScreenRenderer != null)
                mPause = flag;
        }

        public void setListener(PIPOperator.Listener listener)
        {
            if(mScreenRenderer != null)
                mScreenRenderer.setListener(listener);
        }

        protected void initEglCore() {
            mEglCore = new EglCore(null, EglCore.FLAG_TRY_GLES3);
        }

        protected void unInitEglCore() {
            Log.i(TAG, "[release]+");
            if (mEglCore != null) {
                mEglCore.release();
                mEglCore = null;
            }
            Log.i(TAG, "[release]-");
        }

        private void doUpdateTemplate() {
            Log.i(TAG, "doUpdateTemplate backResourceId = " + mBackResId
                    + " frontResourceId = " + mFrontResId + " fronthighlight = "
                    + mHighlightResId);
            if(mIsPIPOrginal)
            {
                if (mPreviewTopGraphicRenderer != null) {
                    mPreviewTopGraphicRenderer.initTemplateTexture(
                            mBackResId, mFrontResId);
                }
                if (mScreenRenderer != null) {
                    mScreenRenderer.updateScreenEffectTemplate(
                            mHighlightResId, mEditBtnResId);
                }
            }
            Log.i(TAG, "doUpdateTemplate end");
        }

        private void doUpdateRenderSize() {
            Log.i(TAG, "doUpdateRenderSize mPreviewTexWidth = " +
                    mPreviewTexWidth + " mPreviewTexHeight = " + mPreviewTexHeight);
            // start frame available thread
            if (!mFrameListener.isAlive()) {
                mFrameListener.start();
                mSurfaceTextureHandler = new Handler(mFrameListener.getLooper());
            }

            if (mBottomPrvSt == null) {
                // initialize bottom surface texture wrapper
                mBottomPrvSt = new SurfaceTextureWrapper();
                mBottomPrvSt.setDefaultBufferSize(mPreviewTexWidth, mPreviewTexHeight);
                mBottomPrvSt.setOnFrameAvailableListener(mBottomCamFrameAvailableListener,
                        mSurfaceTextureHandler);
            }
            mBottomPrvSt.setDefaultBufferSize(mPreviewTexWidth, mPreviewTexHeight);

            if (mTopPrvSt == null) {
                // initialize top surface texture
                mTopPrvSt = new SurfaceTextureWrapper();
                mTopPrvSt.setDefaultBufferSize(mPreviewTexWidth, mPreviewTexHeight);
                mTopPrvSt.setOnFrameAvailableListener(mTopCamFrameAvailableListener,
                        mSurfaceTextureHandler);
            }
            mTopPrvSt.setDefaultBufferSize(mPreviewTexWidth, mPreviewTexHeight);

            if (mPreviewFrameBuffer != null) {
                mPreviewFrameBuffer.setRendererSize(mPreviewTexWidth, mPreviewTexHeight);
            }
            if (mPreviewBottomGraphicRenderer != null) {
                mPreviewBottomGraphicRenderer.setRendererSize(mPreviewTexWidth, mPreviewTexHeight, true);
            }
            if (mPreviewTopGraphicRenderer != null) {
                mPreviewTopGraphicRenderer.setRendererSize(mPreviewTexWidth, mPreviewTexHeight,false);
            }
            if (mScreenRenderer != null) {
                mScreenRenderer.setRendererSize(mPreviewTexWidth, mPreviewTexHeight);
            }
        }

        private void setUpRenderForRecord() {
            if (mRecorderRenderer != null) {
                mRecorderRenderer.init();
            }
        }

        private void doSwitchPIP() {
            doSwitchTextures();
            mPIPSwitched = !mPIPSwitched;
        }

        private void doSwitchTextures() {
            // switch matrix
            float[] matrix = mTopCamTransformMatrix;
            mTopCamTransformMatrix = mBottomTransformMatrix;
            mBottomTransformMatrix = matrix;
        }

        private void doInitScreenRenderer() {
            //mScreenRenderer = new ScreenRenderer(mActivity);
            if(mActivity != null)
                mScreenRenderer = new ScreenRendererV3(mActivity);
            else if(mService != null)
                mScreenRenderer = new ScreenRendererV3(mService);
            mScreenRenderer.init();
        }

        private void doSetupPIPTextures() {
            Log.i(TAG, "doInitiSurfaceTextures mPreviewFrameBuffer = " + mPreviewFrameBuffer);
            resetTimeStamp();
            if (mPreviewFrameBuffer == null) {
                // initialize preview frame buffer
                mPreviewFrameBuffer = new FrameBuffer();
                mPreviewFboTexId = mPreviewFrameBuffer.getFboTexId();
                // initialize bottom graphic renderer
                //mPreviewBottomGraphicRenderer = new BottomGraphicRenderer(mActivity);
                // initialize top graphic renderer
                //mPreviewTopGraphicRenderer = new TopGraphicRenderer(mActivity);

                if(mActivity != null) {
                    mPreviewBottomGraphicRenderer = new BottomGraphicRenderer(mActivity, mIsPIPOrginal);
                    mPreviewTopGraphicRenderer = new TopGraphicRenderer(mActivity, mIsPIPOrginal);
                } else if(mService != null) {
                    mPreviewBottomGraphicRenderer = new BottomGraphicRenderer(mService, mIsPIPOrginal);
                    mPreviewTopGraphicRenderer = new TopGraphicRenderer(mService, mIsPIPOrginal);
                }
                // in pip mode press home key to exit camera, and enter
                // again should restore pip state
                if (mPIPSwitched) {
                    doSwitchTextures();
                }
            }
            //mIsBottomHasHighFrameRate =  Util.isBottomHasHighFrameRate(mActivity);
            /*if(mActivity != null)
                mIsBottomHasHighFrameRate = RendererManager.isBottomHasHighFrameRate_(mActivity);
            else if(mService != null)
                mIsBottomHasHighFrameRate = RendererManager.isBottomHasHighFrameRate_(mService);*/
        }

        private void doUpdatePreviewSurface(Surface surface) {
            if (mScreenRenderer != null) {
                mScreenRenderer.setSurface(surface, true, true);
            }
        }

        private void doReleasePIPTexturesAndRenderers() {
            Log.i(TAG, "doReleasePIPSurfaceTextures");
            doReleasePIPTextures();
            releasePIPRenderers();
        }

        private void doUpdateRecordingSurface(Surface surface) {
            if (mRecorderRenderer != null) {
                mRecorderRenderer.setRendererSize(mPreviewTexWidth, mPreviewTexHeight);
                mRecorderRenderer.setRecrodingSurface(surface);
            }
        }

        private void releasePIPRenderers() {
            Log.i(TAG, "releasePIPRenderers");
            mPreviewBottomGraphicRenderer = null;
            if (mPreviewTopGraphicRenderer != null) {
                mPreviewTopGraphicRenderer.release();
                mPreviewTopGraphicRenderer = null;
            }
            if (mRecorderRenderer != null) {
                mRecorderRenderer.releaseSurface();
                mRecorderRenderer = null;
            }
            if (mScreenRenderer != null) {
                mScreenRenderer.release();
                mScreenRenderer = null;
            }
        }

        private void doVideoSnapShot(int orientation, Surface vssSurface) {
            if(mActivity != null)
                mVssRenderer = new CaptureRenderer(mActivity);
            else if(mService != null)
                mVssRenderer = new CaptureRenderer(mService);
            mVssRenderer.init();
            mVssRenderer.setCaptureSize(mPreviewTexWidth, mPreviewTexHeight, orientation);
            mVssRenderer.setCaptureSurface(vssSurface);
        }

        private void doRelease() {
            Log.i(TAG, "doRelease");
            if (mTopPrvSt != null) {
                mTopPrvSt.setOnFrameAvailableListener(null, null);
                mTopPrvSt.release();
                mTopPrvSt = null;
            }
            if (mBottomPrvSt != null) {
                mBottomPrvSt.setOnFrameAvailableListener(null, null);
                mBottomPrvSt.release();
                mBottomPrvSt = null;
            }
            doReleasePIPTexturesAndRenderers();
            if(mSurfaceTextureHandler != null)
            {
                mFrameListener.quitSafely();
                mSurfaceTextureHandler = null;
            }
        }

        private void doReleasePIPTextures() {
            Log.i(TAG, "_doReleasePIPTextures");
            if (mPreviewFrameBuffer != null) {
                mPreviewFrameBuffer.release();
                mPreviewFrameBuffer = null;
                mPreviewFboTexId = UNVALID_TEXTURE_ID;
            }
        }

        private void doProcessPreviewFrame(SurfaceTextureWrapper stWrapper, boolean isBottom) {
            Log.i(TAG, "doProcessPreviewFrame start isBottom" + isBottom);
            stWrapper.updateTexImage();
            if (isBottom) {
                doUpdateBottomCamTimeStamp();
            } else {
                doUpdateTopCamTimeStamp();
            }
            draw();
            Log.i(TAG, "doProcessPreviewFrame end");
        }

        private void doUpdateTopCamTimeStamp() {
            if (mTopPrvSt == null) {
                return;
            }
            mLatestTopCamTimeStamp = mTopPrvSt.getBufferTimeStamp();
            if (mPIPSwitched) {
                mBottomTransformMatrix = mTopPrvSt.getBufferTransformMatrix();
            } else {
                mTopCamTransformMatrix = mTopPrvSt.getBufferTransformMatrix();
            }
        }

        private void doUpdateBottomCamTimeStamp() {
            if (mBottomPrvSt == null) {
                return;
            }
            mLatestBottomCamTimeStamp = mBottomPrvSt.getBufferTimeStamp();
            if (mPIPSwitched) {
                mTopCamTransformMatrix = mBottomPrvSt.getBufferTransformMatrix();
            } else {
                mBottomTransformMatrix = mBottomPrvSt.getBufferTransformMatrix();
            }
        }

        private void resetTimeStamp() {
            mBottomCamTimeStamp = 0L;
            mLatestBottomCamTimeStamp = 0L;
            mTopCamTimeStamp = 0L;
            mLatestTopCamTimeStamp = 0;
            mLastFrameDrawTimeStamp_ = 0L;
            mDrawDrawFrameCount = 0;
            mDrawDrawStartTime = 0L;
            mPause = false;
        }

        private boolean doTimestampSync() {
            // Step1: waiting two camera's buffer has arrived.
            Log.i(TAG, "[doTimestampSync] mLatestBottomCamTimeStamp="+mLatestBottomCamTimeStamp+
            " mLatestTopCamTimeStamp="+mLatestTopCamTimeStamp);
            if (mLatestBottomCamTimeStamp == 0L || mLatestTopCamTimeStamp == 0L) {
                return false;
            }
            // Step2: update according to bottom's fps, because it has high fps.
            if (mIsBottomHasHighFrameRate && (mBottomCamTimeStamp != mLatestBottomCamTimeStamp)) {
                mBottomCamTimeStamp = mLatestBottomCamTimeStamp;
                mTopCamTimeStamp = mLatestTopCamTimeStamp;
                return true;
            }
            //Step3: update according to top's fps, because it has high fps.
            if (!mIsBottomHasHighFrameRate && (mTopCamTimeStamp != mLatestTopCamTimeStamp)) {
                mBottomCamTimeStamp = mLatestBottomCamTimeStamp;
                mTopCamTimeStamp = mLatestTopCamTimeStamp;
                return true;
            }
            return false;
        }

        private void draw() {
            if (doTimestampSync() && mPreviewFrameBuffer != null) {
                drawToFbo();

                if (mRecorderRenderer != null) {
                    mRecorderRenderer.draw(
                            mPreviewFboTexId,
                            mBottomPrvSt.getBufferTimeStamp());
                }
                if(mIsPIPOrginal)
                {
                    mScreenRenderer.draw(
                        mPreviewTopGraphicRenderer == null ? null : mPreviewTopGraphicRect.copy(),
                        mPreviewFboTexId,
                        mPreviewTopGraphicRect.getHighLightStatus());
                } else {
                    mScreenRenderer.draw(mPreviewFboTexId, false);
                }
                if (mVssRenderer != null) {
                    mVssRenderer.draw(mPreviewFboTexId);
                    mVssRenderer.release();
                    mVssRenderer = null;
                    mEglCore.makeCurrent(mOffScreenSurface);
                }

                // Debug for printing swap fps
                updateFrameCounter();
            }
        }

        private void drawToFbo() {
            mPreviewFrameBuffer.setupFrameBufferGraphics(mPreviewTexWidth,
                    mPreviewTexHeight);
            /*mPreviewBottomGraphicRenderer.draw(
                    mPIPSwitched ? mTopPrvSt.getTextureId() : mBottomPrvSt.getTextureId(),
                    mBottomTransformMatrix,
                    GLUtil.createIdentityMtx(),
                    false);
            mPreviewTopGraphicRenderer.draw(
                    mPIPSwitched ? mBottomPrvSt.getTextureId() : mTopPrvSt.getTextureId(),
                    mTopCamTransformMatrix,
                    GLUtil.createIdentityMtx(),
                    mPreviewTopGraphicRect.copy(),
                    mCurrentOrientation,
                    false);*/
            mPreviewBottomGraphicRenderer.setPickInfo(mScreenRenderer.mPickInfo);
            mPreviewBottomGraphicRenderer.drawPrev(mTopPrvSt.getTextureId(), mBottomPrvSt.getTextureId());
            mPreviewFrameBuffer.setScreenBufferGraphics();
        }

        private void doUpdateTouchTapPostion(int x, int y)
        {
            if(mScreenRenderer != null)
                mScreenRenderer.setCurrTouchTapPostion(x, y);
        }
    }

    /**
     * Handler used for doing preview and recoding.
     */
    private class PreviewRendererHandler extends PipRendererHandler {

        public PreviewRendererHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
			Log.d("PreviewRendererHandler","handleMessage:"+getMSG_str(msg.what));
            switch (msg.what) {
            case MSG_INIT:
                initEglCore();
                mOffScreenSurface = mEglCore.createOffscreenSurface(1, 1);
                mEglCore.makeCurrent(mOffScreenSurface);
                Process.setThreadPriority(-18);
                break;
            case MSG_RELEASE:
                unInitEglCore();
                break;
            default:
                break;
            }
        }
    }

    /**
     * Handler used for taking picture.
     *
     */
    private class CaptureRendererHandler extends PipRendererHandler {
        private static final int MSG_SETUP_PICTURE_TEXTURES = MSG_COUNT;
        private static final int MSG_SETUP_CAPTURE_SURFACE = MSG_COUNT + 1;
        private static final int MSG_CAPTURE_FRAME_AVAILABLE = MSG_COUNT + 2;

        private final HandlerThread mFrameListener = new HandlerThread("PIP-STFrameListener");
        private Handler mSurfaceTextureHandler;

        private SurfaceTextureWrapper mBottomCapSt = null;
        private SurfaceTextureWrapper mTopCapSt = null;
        private BottomGraphicRenderer mBottomRenderer;
        private TopGraphicRenderer mTopRenderer;

        private WindowSurface mCapEglSurface = null;
        private int mBottomJpegRotation = 0;
        private int mTopJpegRotation = 0;
        private boolean __CONFIG_CAP_USE_ORIGIN = false;

        public CaptureRendererHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "handleMessage:" + msg.what);
            switch (msg.what) {
            case MSG_SETUP_PICTURE_TEXTURES:
                @SuppressWarnings("unchecked")
                Map<String, Size> pictureSizeMap = (HashMap<String, Size>) msg.obj;
                setUpTexturesForCapture(pictureSizeMap.get(BOTTOM), pictureSizeMap.get(TOP));
                break;
            case MSG_SETUP_CAPTURE_SURFACE:
                if (mEglCore != null) {
                    Surface captureSurface = (Surface) msg.obj;
                    mCapEglSurface = new WindowSurface(mEglCore, captureSurface);
                    mCapEglSurface.makeCurrent();
                }
                mBottomJpegRotation = 0;
                mTopJpegRotation = 0;
                break;
            case MSG_CAPTURE_FRAME_AVAILABLE:
                SurfaceTextureWrapper stPicWrapper = (SurfaceTextureWrapper) msg.obj;
                stPicWrapper.updateTexImage();
                tryTakePicutre();
                break;
            case MSG_INIT:
                int[] formats = (int[]) msg.obj;
                mEglCore = new EglCore(
                        null,
                        EglCore.FLAG_TRY_GLES3 | EglCore.FLAG_RECORDABLE,
                        formats);
                break;
            case MSG_RELEASE:
                releaseRenderer();
                unInitEglCore();
                break;
            default:
                break;
            }
        }

        public int getPixelFormat() {
            return mEglCore.getPixelFormat();
        }

        public SurfaceTexture getBottomSt() {
            return mBottomCapSt.getSurfaceTexture();
        }

        public SurfaceTexture getTopSt() {
            return mTopCapSt.getSurfaceTexture();
        }

        public void setJpegRotation(boolean isBottomCam, int rotation) {
            if (isBottomCam) {
                mBottomJpegRotation = rotation;
                return;
            }
            mTopJpegRotation = rotation;
        }

        private void setUpTexturesForCapture(Size bPictureSize, Size tPictureSize) {
            Log.i(TAG, "[setUpTexturesForCapture]+");
            if (!mFrameListener.isAlive()) {
                mFrameListener.start();
                mSurfaceTextureHandler = new Handler(mFrameListener.getLooper());
            }

            if (mBottomCapSt == null) {
                mBottomCapSt = new SurfaceTextureWrapper();
                mBottomCapSt.setDefaultBufferSize(
                        bPictureSize.getWidth(), bPictureSize.getHeight());
                mBottomCapSt.setOnFrameAvailableListener(
                        mBottomCamFrameAvailableListener, mSurfaceTextureHandler);
            }
            mBottomCapSt.setDefaultBufferSize(bPictureSize.getWidth(), bPictureSize.getHeight());
            if (mBottomRenderer == null) {
                if(mActivity != null)
                    mBottomRenderer = new BottomGraphicRenderer(mActivity,__CONFIG_CAP_USE_ORIGIN, true);
                else if(mService != null)
                    mBottomRenderer = new BottomGraphicRenderer(mService, __CONFIG_CAP_USE_ORIGIN, true);
            }

            if (mTopCapSt == null) {
                mTopCapSt = new SurfaceTextureWrapper();
                mTopCapSt.setDefaultBufferSize(tPictureSize.getWidth(), tPictureSize.getHeight());
                mTopCapSt.setOnFrameAvailableListener(
                        mTopCamFrameAvailableListener, mSurfaceTextureHandler);
            }
            mTopCapSt.setDefaultBufferSize(tPictureSize.getWidth(), tPictureSize.getHeight());
            if (mTopRenderer == null) {
                if(mActivity != null)
                    mTopRenderer = new TopGraphicRenderer(mActivity,__CONFIG_CAP_USE_ORIGIN, true);
                else if(mService != null)
                    mTopRenderer = new TopGraphicRenderer(mService, __CONFIG_CAP_USE_ORIGIN, true);
            }

            Log.i(TAG, "[setUpTexturesForCapture]-");
        }

        private void tryTakePicutre() {
            if (mBottomCapSt != null && mBottomCapSt.getBufferTimeStamp() > 0
                    && mTopCapSt != null && mTopCapSt.getBufferTimeStamp() > 0) {
                Log.i(TAG, "[tryTakePicutre]+");
                Object obj;
                boolean flag;
                mBottomRenderer.setRendererSize(
                        mBottomCapSt.getWidth(), mBottomCapSt.getHeight(), true);
                if(__CONFIG_CAP_USE_ORIGIN) {
                    if(mTopRenderer != null) {
                        mTopRenderer.initTemplateTexture(mBackResId, mFrontResId);
                        mTopRenderer.setRendererSize(mBottomCapSt.getWidth(), mBottomCapSt.getHeight());
                    }
                } else {
                    if(mTopRenderer != null)
                        mTopRenderer.setRendererSize(mBottomCapSt.getWidth(), mBottomCapSt.getHeight(), false);                   
                }

                if(__CONFIG_CAP_USE_ORIGIN) {
                    mPreviewTopGraphicRect.copy().changeCooridnateSystem(
                        mBottomCapSt.getWidth(),
                        mBottomCapSt.getHeight(), 360 - mCurrentOrientation);
                }
                boolean bottomIsMainCamera = true;
                boolean bottomNeedMirror = (!bottomIsMainCamera)
                        && PIPCustomization.SUB_CAMERA_NEED_HORIZONTAL_FLIP;
                boolean topNeedMirror = bottomIsMainCamera
                        && PIPCustomization.SUB_CAMERA_NEED_HORIZONTAL_FLIP;

                // enable blend, in order to get a transparent background
                GLES20.glViewport(0, 0, mBottomCapSt.getWidth(), mBottomCapSt.getHeight());
                GLES20.glEnable(GLES20.GL_BLEND);
                GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
                /*mBottomRenderer.draw(
                        mBottomCapSt.getTextureId(),
                        GLUtil.createIdentityMtx(), // OES Texture
                        getTexMatrixByRotation(mBottomJpegRotation), // texture rotate
                        bottomNeedMirror); //need flip
                mTopRenderer.draw(
                        mTopCapSt.getTextureId(),
                        GLUtil.createIdentityMtx(), // OES Texture
                        getTexMatrixByRotation(mTopJpegRotation), // texture rotate
                        pictureTopGraphicRect.copy(),
                        -1,
                        topNeedMirror); //need flip*/
                mBottomRenderer.drawPic(mTopCapSt.getTextureId(), mBottomCapSt.getTextureId());
                mCapEglSurface.swapBuffers();
                GLES20.glFinish();
                // Be careful, Surface Texture's release should always happen
                // before make nothing current.
                doReleaseCaptureSt();
                mCapEglSurface.makeNothingCurrent();
                mCapEglSurface.releaseEglSurface();
                mCapEglSurface = null;
                Log.i(TAG, "[tryTakePicutre]-");
            }
        }

        private float[] getTexMatrixByRotation(int rotation) {
            float[] texRotateMtxByOrientation = GLUtil.createIdentityMtx();
            android.opengl.Matrix.translateM(texRotateMtxByOrientation, 0,
                    texRotateMtxByOrientation, 0, .5f, .5f, 0);
            android.opengl.Matrix.rotateM(texRotateMtxByOrientation, 0,
                    -rotation, 0, 0, 1);
            android.opengl.Matrix.translateM(texRotateMtxByOrientation, 0, -.5f, -.5f, 0);
            return texRotateMtxByOrientation;
        }

        private SurfaceTexture.OnFrameAvailableListener
            mBottomCamFrameAvailableListener = new SurfaceTexture.OnFrameAvailableListener() {
                @Override
                public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                    obtainMessage(MSG_CAPTURE_FRAME_AVAILABLE, mBottomCapSt).sendToTarget();
                }
        };

        private SurfaceTexture.OnFrameAvailableListener
            mTopCamFrameAvailableListener = new SurfaceTexture.OnFrameAvailableListener() {
                @Override
                public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                    obtainMessage(MSG_CAPTURE_FRAME_AVAILABLE, mTopCapSt).sendToTarget();
                }
        };

        private void releaseRenderer() {
            if (mBottomRenderer != null) {
                mBottomRenderer.release();
                mBottomRenderer = null;
            }
            if (mTopRenderer != null) {
                mTopRenderer.release();
                mTopRenderer = null;
            }
            doReleaseCaptureSt();
            if (mSurfaceTextureHandler != null) {
                mFrameListener.quitSafely();
                mSurfaceTextureHandler = null;
            }
        }

        private void doReleaseCaptureSt() {
            if (mBottomCapSt != null) {
                mBottomCapSt.release();
                mBottomCapSt = null;
            }

            if (mTopCapSt != null) {
                mTopCapSt.release();
                mTopCapSt = null;
            }
        }
    }

    private boolean waitDone(Handler handler) {
        final Object waitDoneLock = new Object();
        final Runnable unlockRunnable = new Runnable() {
            @Override
            public void run() {
                synchronized (waitDoneLock) {
                    waitDoneLock.notifyAll();
                }
            }
        };
        synchronized (waitDoneLock) {
            handler.post(unlockRunnable);
            try {
                waitDoneLock.wait();
            } catch (InterruptedException ex) {
                Log.i(TAG, "waitDone interrupted");
                return false;
            }
        }
        return true;
    }

    private static final int INTERVALS = 60;//300;
    private int mDrawDrawFrameCount = 0;
    private long mDrawDrawStartTime = 0;
    private void updateFrameCounter() {
        mDrawDrawFrameCount++;
        if (mDrawDrawFrameCount % INTERVALS == 0) {
            long currentTime = System.currentTimeMillis();
            int intervals = (int) (currentTime - mDrawDrawStartTime);
            Log.i(TAG, "[AP-->Wrapping][Preview] Drawing frame, fps = "
                + (mDrawDrawFrameCount * 1000.0f) / intervals + " in last " + intervals
                + " millisecond.");
            mDrawDrawStartTime = currentTime;
            mDrawDrawFrameCount = 0;
        }
    }

    private final String getMSG_str(int i)
        {
            String s;
            if(i == 0)
                s = "MSG_INIT";
            else if(i == 1)
                s = "MSG_RELEASE";
            else if(i == 2)
                s = "MSG_UPDATE_TEMPLATE";
            else if(i == 3)
                s = "MSG_UPDATE_RENDERER_SIZE";
            else if(i == 4)
                s = "MSG_SETUP_VIDEO_RENDER";
            else if(i == 6)
                s = "MSG_SWITCH_PIP";
            else if(i == 7)
                s = "MSG_SETUP_PIP_TEXTURES";
            else if(i == 8)
                s = "MSG_NEW_BOTTOM_FRAME_ARRIVED";
            else if(i == 9)
                s = "MSG_NEW_TOP_FRAME_ARRIVED";
            else if(i == 10)
                s = "MSG_PREVIEW_SURFACE_DESTORY";
            else if(i == 11)
                s = "MSG_TAKE_VSS";
            else if(i == 13)
                s = "MSG_INIT_SCREEN_RENDERER";
            else if(i == 14)
                s = "MSG_SET_PREVIEW_SURFACE";
            else if(i == 15)
                s = "MSG_SET_RECORDING_SURFACE";
            else if(i == 19)
                s = "MSG_SETUP_PICTURE_TEXTURES";
            else if(i == 20)
                s = "MSG_SETUP_CAPTURE_SURFACE";
            else if(i == 21)
                s = "MSG_CAPTURE_FRAME_AVAILABLE";
            else
                s = "**UNKNOWN**";
            return s;
        }

    private void setPreviewSizeForSwitch(int i, int j)
    {
        if(mPreviewTexWidth != j || mPreviewTexHeight != i) 
        {
            if(mPreviewEglHandler != null)
            {
                mPreviewEglHandler.obtainMessage(PipRendererHandler.MSG_UPDATE_RENDERER_SIZE, j, i, null).sendToTarget();
                waitDone(mPreviewEglHandler);
            }
        }
        else{
                Log.i(TAG, "setPreviewTextureSize same size set, ignore!");
        }

        return;

    }

    public  void updateScreenRenderParameter(float anglex,float angley,float rate){
            if (mPreviewEglHandler != null) {
                ScreenRenderParameter srp = new ScreenRenderParameter(anglex,angley,rate);
                mPreviewEglHandler.obtainMessage(
                        PipRendererHandler.MSG_SET_SCREEN_RENDER_PARAMETERS,srp).sendToTarget();
            }
    }

    private class ScreenRenderParameter{
        private float mAnglex;
        private float mAngley;
        private float mRate;

        public ScreenRenderParameter(float mAnglex, float mAngley, float mRate) {
            this.mAnglex = mAnglex;
            this.mAngley = mAngley;
            this.mRate = mRate;
        }

        public float getAngleX() {
            return mAnglex;
        }

        public float getAngleY() {
            return mAngley;
        }

        public float getRate() {
            return mRate;
        }
    }
}
