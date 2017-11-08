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
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.view.Surface;

import java.nio.*;



/**
 * Render preview buffer to preview surface
 */
public class ScreenRendererV3 extends Renderer implements Runnable {
    private static final String TAG = "ScreenRenderer";
    private FloatBuffer         mVtxBuf; // vertex coordinates
    private FloatBuffer         mTexCoordBuf; // texture coordinates
    private FloatBuffer         mTopGraphicPositionBuf = null;
    private int                 mRenderTexWidth = -1;
    private int                 mRenderTexHeight = -1;
    private int                 mTextureRotation = 0;

    private float[]             mPosMtx = GLUtil.createIdentityMtx();
    private float[]             mPMtx   = GLUtil.createIdentityMtx(); // project
    private float[]             mVMtx   = GLUtil.createIdentityMtx(); // view
    private float[]             mMMtx   = GLUtil.createIdentityMtx(); // mode
    private float[]             mTexRotateMtx = GLUtil.createIdentityMtx(); // rotate texture

    private ResourceRenderer    mEditTexRenderer;
    private ResourceRenderer    mPressedTexRenderer; //highlight press effects

    // A surface is used to receive preview buffer, will create EGLSurface by it
    private Surface             mPreviewSurface = null;
    private WindowSurface       mPreviewEGLSurface;
    private EglCore             mEglCore;
    private int                 mEditTexSize = 0;
    private int                 mProgram         = -1; // program id
    private int                 maPositionHandle = -1; // vertex position handle
    private int                 muTexRotateMtxHandle = -1;
    private int                 maTexCoordHandle = -1; // texture position handle
    private int                 muPosMtxHandle   = -1;
    private int                 muSamplerHandle  = -1; // sampler handle

    private ScreenHandler       mScreenHandler;
    private Object              mReadyFence = new Object(); // guards ready/running
    private boolean             mReady;
    private boolean             mRunning;
    private EGLContext          mSharedEGLContext = null;
    private boolean             mIsEGLSurfaceReady = false;
    private ConditionVariable   mUpdateEGLSurfaceSync = new ConditionVariable();
    private ConditionVariable   mReleaseScreenSurfaceSync = new ConditionVariable();
    private ConditionVariable   mDrawLockableConditionVariable = new ConditionVariable();

    private PIPOperator.Listener PIPOperatorListener = null;
    private float fov = 90f;
    private GenerateMapV2 gMap = null;
    //public PipFrameAliveDetector mAliveDetector;
    public volatile float mAngleX = -90f;
    public volatile float mAngleY;

    int mFishIMageHeight = 960;
    int mFishImageWidth = 1280;
    private boolean mIsPIPOrginal = false;

    private float mMMtx_o[] = GLUtil.createIdentityMtx();
    private float mPMtx_o[] = GLUtil.createIdentityMtx();
    float mPickInfo[] = new float[3];
    private float mPosMtx_o[] = GLUtil.createIdentityMtx();
    private int mProgram_o = -1;
    public volatile float mRate = 1.3f;
    private SphereV2 mSphere;
    private SphereUVShader mSphereUVShader;
    private FloatBuffer mTexCoordBuf_o;
    private float mTexRotateMtx_o[] = GLUtil.createIdentityMtx();
    private float mVMtx_o[] = GLUtil.createIdentityMtx();
    private FloatBuffer mVtxBuf_o;
    private int maPositionHandle_o = -1;
    private int maTexCoordHandle_o = -1;
    private int muPosMtxHandle_o = -1;
    private int muSamplerHandle_o = -1;
    private int muTexRotateMtxHandle_o = -1;
    private int numVertices;
    int point_count = 0;
    private float ratio = 1.4f;
    private String screenSnapFilePrefix = null;
    private int screenSnapshotCount = 0;
    private boolean screenSnapshotRequest = false;
    

    // This can be called from non-gl thread
    public ScreenRendererV3(Activity activity) {
        super(activity);
        mEditTexRenderer = new ResourceRenderer(activity);
        mPressedTexRenderer = new ResourceRenderer(activity);
        //mTexCoordBuf = createFloatBuffer(mTexCoordBuf, GLUtil.createTexCoord());
        mTexCoordBuf_o = createFloatBuffer(mTexCoordBuf_o, GLUtil.createTexCoord());
        // new a thread to share EGLContext with pip wrapping GL Thread
        new Thread(this, "PIP-ScreenRenderer").start();
    }

    public ScreenRendererV3(Service s) {
        super(s);
        mEditTexRenderer = new ResourceRenderer(s);
        mPressedTexRenderer = new ResourceRenderer(s);
        //mTexCoordBuf = createFloatBuffer(mTexCoordBuf, GLUtil.createTexCoord());
        // new a thread to share EGLContext with pip wrapping GL Thread
        mTexCoordBuf_o = createFloatBuffer(mTexCoordBuf_o, GLUtil.createTexCoord());
        new Thread(this, "PIP-ScreenRenderer").start();
    }

    /**
     * Initialize screen renderer program and shader,
     * get shared egl context and init sub render
     * <p>
     * Note: this should be called in GL Thread
     * //@param  edit button texture resource id
     */
    public void init() {
        Log.i(TAG, "init: " + this);
        initPIPOriginalGL();
        // initialize program and shader
        initGL();
        // get shared egl context
        mSharedEGLContext = EGL14.eglGetCurrentContext();
        // initialize edit texture renderer
        mEditTexRenderer.init();
        mPressedTexRenderer.init();
    }

    public void updateScreenEffectTemplate(int hightLightId, int editButtonId) {
        if (hightLightId > 0) {
            mPressedTexRenderer.updateTemplate(hightLightId);
        }
        if (editButtonId > 0) {
            mEditTexRenderer.updateTemplate(editButtonId);
        }
    }

    public void setCurrTouchTapPostion(int i, int j)
    {
        synchronized (mReadyFence) {
            mScreenHandler.obtainMessage(ScreenHandler.MSG_SET_TOUCHED_TAP_POSTION, i, j).sendToTarget();
        }
    }

    public void setListener(PIPOperator.Listener listener)
    {
        PIPOperatorListener = listener;
    }

    /**
     * set preview surface' width and height.
     * reset transform matrix and vertex coordinates.
     * set render size to sub renderer
     * <p>
     * Note: this should be called in GL Thread
     */
    @Override
    public void setRendererSize(int width, int height) {
        Log.i(TAG, "setRendererSize width = " + width + " height = " + height);
        mFishImageWidth = width;
        mFishIMageHeight = height;
        if (!isMatchingSurfaceSize(width, height)) {
            mIsEGLSurfaceReady = false;
        }
        super.setRendererSize(width, height);
        ratio = (float)width / (float)height;
    }

    @Override
    protected void setSurface(Surface surface, boolean scaled, boolean rotated) {
        Log.i(TAG, "setSurface scaled = " + scaled + " rotated = " + rotated +
                ", mPreviewSurface = " + mPreviewSurface);
        if (skipUpdateSurface(surface)) {
            Log.i(TAG, "the same surface skip update mPreviewSurface = " + mPreviewSurface);
            mIsEGLSurfaceReady = true;
            return;
        }
        mIsEGLSurfaceReady = false;
        super.setSurface(surface, scaled, rotated);
        if (surface == null) {
            throw new RuntimeException("ScreenRenderer setSurface to null!!!!!");
        }
        mPreviewSurface = surface;
        waitRendererThreadActive();
        updateEGLSurface();
        //mRenderTexWidth = mPreviewEGLSurface.getWidth();
        //mRenderTexHeight = mPreviewEGLSurface.getHeight();
        //mTextureRotation = getDisplayRotation(getActivity());
        
            if(scaled || rotated)
                mRenderTexWidth = mPreviewEGLSurface.getWidth();
            else
                mRenderTexWidth = getRendererWidth();
            
            if(scaled || rotated)
                mRenderTexHeight = mPreviewEGLSurface.getHeight();
            else
                mRenderTexHeight = getRendererHeight();
            
            if(rotated)
                mTextureRotation = getDisplayRotation(getActivity());
            else
                mTextureRotation = 0;
        
        updateRendererSize(mRenderTexWidth, mRenderTexHeight);
        mIsEGLSurfaceReady = true;
    }

    private boolean isMatchingSurfaceSize(int width, int height) {
        Log.i(TAG, "isMatchingSurfaceSize mRenderTexWidth:" + mRenderTexWidth
                + " mRenderTexHeight:" + mRenderTexHeight);
        int max = Math.max(width, height);
        int min = Math.min(width, height);
        int surfaceMax = Math.max(mRenderTexWidth, mRenderTexHeight);
        int surfaceMin = Math.min(mRenderTexWidth, mRenderTexHeight);
        return Math.abs(((float) max / min) - ((float) surfaceMax / surfaceMin)) <= 0.02;
    }

    private boolean skipUpdateSurface(Surface surface) {
        int disPlayRotation = getDisplayRotation(getActivity());
        boolean rotationIsLandscape = (disPlayRotation == 90 || disPlayRotation == 270);
        boolean renderSizeIsLandscape = mRenderTexWidth > mRenderTexHeight;
        boolean skipRotation = (renderSizeIsLandscape == rotationIsLandscape);
        boolean skipSurface = surface.equals(mPreviewSurface);
        boolean skipMinimalSize = (mRenderTexWidth > 2) && (mRenderTexHeight > 2);
        boolean skipSurfaceSize = (mRenderTexWidth == getRendererWidth()
                && mRenderTexHeight == getRendererHeight());
        boolean skipUnValidSurface = !(surface.isValid());
        return (skipSurface && skipMinimalSize && skipRotation && skipSurfaceSize)
                || skipUnValidSurface;
    }

    private void waitRendererThreadActive() {
        synchronized (mReadyFence) {
            if (mRunning) {
                Log.i(TAG, "screen renderer already running!");
                return;
            }
            mRunning = true;
            while (!mReady) {
                try {
                    Log.i(TAG, "wait for screen renderer thread ready, current mReady = " + mReady);
                    mReadyFence.wait();
                } catch (InterruptedException ie) {
                    // ignore
                }
            }
            // when screen renderer thread started, be sure not block first frame
            mDrawLockableConditionVariable.open();
        }
    }

    @Override
    public void release() {
        Log.i(TAG, "release: " + this);
        synchronized (mReadyFence) {
            if (mScreenHandler != null) {
                mScreenHandler.removeCallbacksAndMessages(null);
                mReleaseScreenSurfaceSync.close();
                mScreenHandler.obtainMessage(ScreenHandler.MSG_RELEASE_SURFACE).sendToTarget();
                mReleaseScreenSurfaceSync.block(2000);
            }
        }
        super.setRendererSize(-1, -1);
    }

    public ResourceRenderer getResourceRenderer() {
        return mEditTexRenderer;
     }

    public void draw(AnimationRect topGraphicRect, int texId, boolean highlight) {
        synchronized (mReadyFence) {
            if (mScreenHandler != null && mIsEGLSurfaceReady) {
                mDrawLockableConditionVariable.close();
                mScreenHandler.removeMessages(ScreenHandler.MSG_FRAME_AVAILABLE);
                mScreenHandler.obtainMessage(ScreenHandler.MSG_FRAME_AVAILABLE, texId,
                        highlight ? 1 : 0, topGraphicRect).sendToTarget();
                mDrawLockableConditionVariable.block(100);
            }
        }
    }

    public void draw(int texId, boolean highlight) {
        synchronized (mReadyFence) {
            if (mScreenHandler != null && mIsEGLSurfaceReady) {
                mDrawLockableConditionVariable.close();
                mScreenHandler.removeMessages(ScreenHandler.MSG_FRAME_AVAILABLE);
                mScreenHandler.obtainMessage(ScreenHandler.MSG_FRAME_AVAILABLE, texId,
                        highlight ? 1 : 0).sendToTarget();
                mDrawLockableConditionVariable.block(100);
            }
        }
    }    

    public void notifySurfaceStatus(Surface surface) {
        synchronized (mReadyFence) {
            if (mScreenHandler != null && mPreviewEGLSurface != null
                    && surface == mPreviewEGLSurface.getSurface()) {
                mScreenHandler.removeCallbacksAndMessages(null);
                mReleaseScreenSurfaceSync.close();
                mScreenHandler.obtainMessage(ScreenHandler.MSG_SURFACE_DESTROYED).sendToTarget();
                mReleaseScreenSurfaceSync.block(2000);
            }
        }
    }

    @Override
    public void run() {
        Looper.prepare();
        Process.setThreadPriority(-17);
        synchronized (mReadyFence) {
            Log.i(TAG, "Screen renderer thread started!");
            mScreenHandler = new ScreenHandler();
            mReady = true;
            mReadyFence.notify();
        }
        Looper.loop();
        Log.i(TAG, "Screen renderer thread exiting!");
        synchronized (mReadyFence) {
            mReady = mRunning = false;
            mScreenHandler = null;
        }
    }

    private void updateRendererSize(int width, int height) {
        Log.i(TAG, "updateRendererSize width = " + width + " height = " + height);
        //resetMatrix();
        //Matrix.orthoM(mPMtx, 0, 0, width, 0, height, -1, 1);
        //initVertexData(width, height);
        
        mPosMtx = GLUtil.createIdentityMtx();
        mPMtx = GLUtil.createIdentityMtx();
        mVMtx = GLUtil.createIdentityMtx();
        mMMtx = GLUtil.createIdentityMtx();
        ratio = (float)width/ (float)height;
        mTexRotateMtx_o = GLUtil.createIdentityMtx();
        Matrix.translateM(mTexRotateMtx_o, 0, mTexRotateMtx_o, 0, 0.5F, 0.5F, 0.0F);
        Matrix.rotateM(mTexRotateMtx_o, 0, -mTextureRotation, 0.0F, 0.0F, 1.0F);
        Matrix.translateM(mTexRotateMtx_o, 0, -0.5F, -0.5F, 0.0F);
        mPosMtx_o = GLUtil.createIdentityMtx();
        mPMtx_o = GLUtil.createIdentityMtx();
        mVMtx_o = GLUtil.createIdentityMtx();
        mMMtx_o = GLUtil.createIdentityMtx();
        Matrix.orthoM(mPMtx_o, 0, 0.0F, width, 0.0F, height, -1F, 1.0F);
        Matrix.multiplyMM(mPosMtx_o, 0, mMMtx_o, 0, mVMtx_o, 0);
        Matrix.multiplyMM(mPosMtx_o, 0, mPMtx_o, 0, mPosMtx_o, 0);
        mVtxBuf_o = createFloatBuffer(mVtxBuf_o, GLUtil.createFullSquareVtx(width, height));
        
        mEditTexSize = Math.min(width, height)
                / PIPCustomization.TOP_GRAPHIC_EDIT_BUTTON_RELATIVE_VALUE;
        mEditTexRenderer.setRendererSize(width, height);
        mPressedTexRenderer.setRendererSize(width, height);
    }

    private int getDisplayRotation(Activity activity) {
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
        }
        return 0;
    }

    private class ScreenHandler extends Handler {
        public static final int         MSG_SETUP_SURFACE = 0;
        public static final int         MSG_RELEASE_SURFACE = 1;
        public static final int         MSG_UPDATE_EGL_SURFACE = 2;
        public static final int         MSG_FRAME_AVAILABLE = 3;
        public static final int         MSG_SURFACE_DESTROYED = 4;
        public static final int         MSG_SWITCH_RENDERER = 5;
        public static final int         MSG_SET_TOUCHED_TAP_POSTION = 6;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_SETUP_SURFACE:
                doSetupEGLSurface();
                break;
            case MSG_RELEASE_SURFACE:
                doReleaseSurface();
                mReleaseScreenSurfaceSync.open();
                break;
            case MSG_SURFACE_DESTROYED:
                releaseEglSurface();
                mReleaseScreenSurfaceSync.open();
                break;
            case MSG_UPDATE_EGL_SURFACE:
                doUpdateEGLSurface();
                mUpdateEGLSurfaceSync.open();
                break;
            case MSG_FRAME_AVAILABLE:
                boolean highlight = msg.arg2 > 0;
                //doDraw((AnimationRect) msg.obj, msg.arg1, highlight);
                doDraw(msg.arg1, highlight);
                break;

            case MSG_SET_TOUCHED_TAP_POSTION:
                mSphereUVShader.initFbo(mRenderTexWidth, mRenderTexHeight);
                mSphereUVShader.getCoord(msg.arg1, msg.arg2, mPosMtx, mRenderTexWidth, mRenderTexHeight);
                boolean flag1;
                float f;
                float f1;
                if(mPickInfo[2] > 0.0F)
                    flag1 = true;
                else
                    flag1 = false;
                f = mPickInfo[0] * (float)mFishImageWidth;
                f1 = (float)mFishIMageHeight - mPickInfo[1] * (float)mFishIMageHeight;
                Log.i("TouchedTabPostion", "x="+f1+";  y="+f+";  bTop="+flag1+"; PIPOperatorListener="+PIPOperatorListener);
                if(PIPOperatorListener != null)
                    PIPOperatorListener.updateTouchedTabPostion((int)f1, (int)f, flag1);
                break;
            }
        }

        private void doSetupEGLSurface() {
            Log.i(TAG, "handleSetupSurface  mEglCore = " + mEglCore +
                    " mPreviewEGLSurface = " + mPreviewEGLSurface
                    + " mPreviewSurface = " + mPreviewSurface);
            if (mEglCore == null) {
                mEglCore = new EglCore(mSharedEGLContext,
                        EglCore.FLAG_TRY_GLES3 | EglCore.FLAG_RECORDABLE,
                        new int[]{PixelFormat.RGBA_8888, ImageFormat.YV12});
            }
            if (mPreviewEGLSurface == null) {
                mPreviewEGLSurface = new WindowSurface(mEglCore, mPreviewSurface);
                mPreviewEGLSurface.makeCurrent();
            }
        }

        private void doUpdateEGLSurface() {
            Log.i(TAG, "updateEGLSurface mPreviewEGLSurface = " + mPreviewEGLSurface);
            if (mPreviewEGLSurface != null) {
                // release old egl surface
                mPreviewEGLSurface.makeNothingCurrent();
                mPreviewEGLSurface.releaseEglSurface();
                // create new egl surface
                mPreviewEGLSurface = new WindowSurface(mEglCore, mPreviewSurface);
                mPreviewEGLSurface.makeCurrent();
            } else {
                doSetupEGLSurface();
            }
        }

        private void doReleaseSurface() {
            releaseEglSurface();
            if (mEglCore != null) {
                mEglCore.release();
                mEglCore = null;
            }
            mEditTexRenderer.releaseResource();
            mPressedTexRenderer.releaseResource();
            mIsEGLSurfaceReady = false;
            mPreviewSurface = null;
            mProgram = -1;
            mProgram_o = -1;
            Looper looper = Looper.myLooper();
            if (looper != null) {
                looper.quit();
            }
        }

        private void releaseEglSurface() {
            Log.i(TAG, "releaseEglSurface");
            if (mPreviewEGLSurface != null) {
                mPreviewEGLSurface.makeNothingCurrent();
                mPreviewEGLSurface.releaseEglSurface();
                mPreviewEGLSurface = null;
            }
        }

        private void updateRenderParameter()
        {

            //float af1[] = MainActivity.getRenderParameter();
            //mAngleX = af1[0];
            //mAngleY = af1[1];
            // mRate = af1[2];
           // Log.d("updateRenderParameter","mAngleX:"+mAngleX+" mAngleY:"+mAngleY+" mRate:"+mRate);
            //TODO
            /*if(getActivity() != null)
            {
                float af1[] = ((CameraActivity)getActivity()).getPreviewSurfaceView().getRenderParameter();
                mAngleX = af1[0];
                mAngleY = af1[1];
                mRate = af1[2];
            } else
            {
                float af[] = ((BqlVRCameraService)getService()).getRenderParameter();
                mAngleX = af[0];
                mAngleY = af[1];
                mRate = af[2];
            }*/
        }

        private void doDraw(int i, boolean flag)
        {
            //System.currentTimeMillis();
            if(getRendererWidth() > 0 && getRendererHeight() > 0 && mPreviewEGLSurface != null)
            {
                updateRenderParameter();
                GLUtil.checkGlError("ScreenDraw_Start");
                GLES20.glViewport(0, 0, mRenderTexWidth, mRenderTexHeight);
                GLES20.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
                GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
                GLES20.glUseProgram(mProgram);
                mSphere.vertexBuffer.position(0);
                GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false, 12, mSphere.vertexBuffer);
                mSphere.textureBuffer.position(0);
                GLES20.glVertexAttribPointer(maTexCoordHandle, 2, GLES20.GL_FLOAT, false, 8, mSphere.textureBuffer);
                GLES20.glEnableVertexAttribArray(maPositionHandle);
                GLES20.glEnableVertexAttribArray(maTexCoordHandle);
                fov = 90F + 15F * mRate;
                Matrix.perspectiveM(mPMtx, 0, (float)Math.min(90D, fov), ratio, 0.01F, 10F);
                Matrix.setLookAtM(mVMtx, 0, 0.0F, 0.0F, (float)Math.max(0.0D, (double)fov - 90D) / 10F, 0.0F, 0.0F, -1F, 0.0F, -1F, 0.0F);
                Matrix.setIdentityM(mMMtx, 0);
                Matrix.rotateM(mMMtx, 0, mAngleY, 1.0F, 0.0F, 0.0F);
                Matrix.rotateM(mMMtx, 0, mAngleX, 0.0F, 1.0F, 0.0F);
                Matrix.multiplyMM(mPosMtx, 0, mVMtx, 0, mMMtx, 0);
                Matrix.multiplyMM(mPosMtx, 0, mPMtx, 0, mPosMtx, 0);
                GLES20.glUniformMatrix4fv(muPosMtxHandle, 1, false, mPosMtx, 0);
                GLES20.glUniform1i(muSamplerHandle, 0);
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, i);
                GLES20.glEnable(GLES20.GL_CULL_FACE);
                GLES20.glCullFace(GLES20.GL_FRONT);
                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, numVertices);
                mPreviewEGLSurface.swapBuffers();
                mDrawLockableConditionVariable.open();
                debugFrameRate("ScreenRendererV3");
                //screenSnapShotCheck();
                GLUtil.checkGlError("ScreenDraw_End");
            }
        }

        private void doDraw_old(AnimationRect topGraphicRect, int texId, boolean highlight) {
            if (getRendererWidth() <= 0 || getRendererHeight() <= 0 || mPreviewEGLSurface == null) {
                return;
            }
            GLUtil.checkGlError("ScreenDraw_Start");
            GLES20.glViewport(0, 0, mRenderTexWidth, mRenderTexHeight);
            GLES20.glClearColor(0f, 0f, 0f, 1f);
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
            //use program
            GLES20.glUseProgram(mProgram);
            //vertex
            mVtxBuf.position(0);
            GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false, 4 * 3,
                    mVtxBuf);
            mTexCoordBuf.position(0);
            GLES20.glVertexAttribPointer(maTexCoordHandle, 2, GLES20.GL_FLOAT, false, 4 * 2,
                    mTexCoordBuf);
            GLES20.glEnableVertexAttribArray(maPositionHandle);
            GLES20.glEnableVertexAttribArray(maTexCoordHandle);
            //matrix
            GLES20.glUniformMatrix4fv(muPosMtxHandle, 1, false, mPosMtx, 0);
            GLES20.glUniformMatrix4fv(muTexRotateMtxHandle, 1, false, mTexRotateMtx, 0);
            //sampler
            GLES20.glUniform1i(muSamplerHandle, 0);
            //texture
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId);
            // draw
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 6);
            // draw edit texture
            if (topGraphicRect != null) {
                topGraphicRect.changeCooridnateSystem(mRenderTexWidth, mRenderTexHeight,
                        mTextureRotation);
                mEditTexRenderer.draw(topGraphicRect.getRightBottom()[0],
                        topGraphicRect.getRightBottom()[1], mEditTexSize, null, null);
            }
            // draw highlight texture
            if (topGraphicRect != null && highlight) {
                mTopGraphicPositionBuf = createFloatBuffer(mTopGraphicPositionBuf,
                        GLUtil.createTopRightRect(topGraphicRect));
                mTopGraphicPositionBuf.position(0);
                mPressedTexRenderer.draw(0, 0, 0, mTopGraphicPositionBuf, null);
            }
            //swap buffer
            mPreviewEGLSurface.swapBuffers();
            mDrawLockableConditionVariable.open();
            debugFrameRate(TAG);
//            mPreviewEGLSurface.saveFrame(new File("/storage/sdcard0/piptest.jpg"));
            GLUtil.checkGlError("ScreenDraw_End");
        }
    }

    private void initGL() {
        if (mProgram != -1) {
            return;
        }
        GLUtil.checkGlError("initGL_Start");
        final String vertexShader =
              /*  "attribute vec4 aPosition;\n"
              + "attribute vec4 aTexCoord;\n"
              + "uniform   mat4 uPosMtx;\n"
              + "uniform   mat4 uTexRotateMtx;\n"
              + "varying   vec2 vTexCoord;\n"
              + "void main() {\n"
              + "  gl_Position = uPosMtx * aPosition;\n"
              + "  vTexCoord   = (uTexRotateMtx * aTexCoord).xy;\n"
              + "}\n";*/

                "attribute vec4 aPosition;\n"
              + "attribute vec4 aTexCoord;\n"
              + "uniform   mat4 uPosMtx;\n"
              + "varying   vec2 vTexCoord;\n"
              + "void main() {\n" 
              + "gl_Position = uPosMtx * aPosition;\n"  
              + "vTexCoord   = vec2 (aTexCoord.x,aTexCoord.y);\n"
              + "}\n";
        final String fragmentShader =
                "precision mediump float;\n"
              + "uniform sampler2D uSampler;\n"
              + "varying vec2      vTexCoord;\n"
              + "void main() {\n"
              + "  gl_FragColor = texture2D(uSampler, vTexCoord);\n"
              + "}\n";

        mSphere = new SphereV2();
        numVertices = mSphere.init();
        mProgram         = GLUtil.createProgram(vertexShader, fragmentShader);
        
        //mProgram = GLUtil.createProgram("attribute vec4 aPosition;\nattribute vec4 aTexCoord;\nuniform   mat4 uPosMtx;\nvarying   vec2 vTexCoord;\nvoid main() {\n  gl_Position = uPosMtx * aPosition;\n  vTexCoord   = vec2 (aTexCoord.x,aTexCoord.y);\n}\n", "precision mediump float;\nuniform sampler2D uSampler;\nvarying vec2      vTexCoord;\nvoid main() {\n  gl_FragColor = texture2D(uSampler, vTexCoord);\n}\n");
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        maTexCoordHandle = GLES20.glGetAttribLocation(mProgram, "aTexCoord");
        //matrix
        muPosMtxHandle   = GLES20.glGetUniformLocation(mProgram, "uPosMtx");
        //muTexRotateMtxHandle = GLES20.glGetUniformLocation(mProgram, "uTexRotateMtx");
        //sampler
        muSamplerHandle  = GLES20.glGetUniformLocation(mProgram, "uSampler");

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glDisable(GLES20.GL_CULL_FACE);
        GLES20.glDisable(GLES20.GL_BLEND);
        GLUtil.checkGlError("initGL_E");
        mSphereUVShader = new SphereUVShader();
    }

    private void initPIPOriginalGL()
    {
        if(mProgram_o == -1)
        {
            GLUtil.checkGlError("initGL_Start");
            mProgram_o = GLUtil.createProgram("attribute vec4 aPosition;\nattribute vec4 aTexCoord;\nuniform   mat4 uPosMtx;\nuniform   mat4 uTexRotateMtx;\nvarying   vec2 vTexCoord;\nvoid main() {\n  gl_Position = uPosMtx * aPosition;\n  vTexCoord   = (uTexRotateMtx * aTexCoord).xy;\n}\n", "precision mediump float;\nuniform sampler2D uSampler;\nvarying vec2      vTexCoord;\nvoid main() {\n  gl_FragColor = texture2D(uSampler, vTexCoord);\n}\n");
            maPositionHandle_o = GLES20.glGetAttribLocation(mProgram_o, "aPosition");
            maTexCoordHandle_o = GLES20.glGetAttribLocation(mProgram_o, "aTexCoord");
            muPosMtxHandle_o = GLES20.glGetUniformLocation(mProgram_o, "uPosMtx");
            muTexRotateMtxHandle_o = GLES20.glGetUniformLocation(mProgram_o, "uTexRotateMtx");
            muSamplerHandle_o = GLES20.glGetUniformLocation(mProgram_o, "uSampler");
            GLES20.glDisable(GLES20.GL_DEPTH_TEST);
            GLES20.glDisable(GLES20.GL_CULL_FACE);
            GLES20.glDisable(GLES20.GL_BLEND);
            GLUtil.checkGlError("initGL_E");
        }
    }

    private void resetMatrix() {
        mPosMtx = GLUtil.createIdentityMtx();
        mPMtx   = GLUtil.createIdentityMtx();
        mVMtx   = GLUtil.createIdentityMtx();
        mMMtx   = GLUtil.createIdentityMtx();
        mTexRotateMtx = GLUtil.createIdentityMtx();
    }

    private void updateEGLSurface() {
        synchronized (mReadyFence) {
            mUpdateEGLSurfaceSync.close();
            if (mScreenHandler != null) {
                mScreenHandler.removeMessages(ScreenHandler.MSG_FRAME_AVAILABLE);
                mScreenHandler.obtainMessage(ScreenHandler.MSG_UPDATE_EGL_SURFACE).sendToTarget();
            }
            mUpdateEGLSurfaceSync.block();
        }
    }

    private void initVertexData(float width, float height) {
        Matrix.translateM(mTexRotateMtx, 0,
                mTexRotateMtx, 0, .5f, .5f, 0);
        Matrix.rotateM(mTexRotateMtx, 0,
                -mTextureRotation, 0, 0, 1);
        Matrix.translateM(mTexRotateMtx, 0, -.5f, -.5f, 0);
        mVtxBuf = createFloatBuffer(mVtxBuf, GLUtil.createFullSquareVtx(width, height));
        Matrix.multiplyMM(mPosMtx, 0, mMMtx, 0, mVMtx, 0);
        Matrix.multiplyMM(mPosMtx, 0, mPMtx, 0, mPosMtx, 0);
    }

    private class SphereUVShader
    {
        private int mAlphaMapHandle;
        private int mBotMapHandle;
        FrameBuffer mFBO;
        private int mPosMtxHandle1;
        private int mPosMtxHandle2;
        private int mPositionHandle1;
        private int mPositionHandle2;
        private int mProgram1;
        private int mProgram2;
        private int mTexCoordHandle1;
        private int mTexCoordHandle2;
        private int mTopMapHandle;
        float squareTextureCoords[];
        float squareVertexCoords[];
        //final ScreenRendererV3 this$0;

        public SphereUVShader()
        {
            //this$0 = ScreenRendererV3.this;
            //super();
            mProgram1 = -1;
            mTexCoordHandle1 = -1;
            mPositionHandle1 = -1;
            mPosMtxHandle1 = -1;
            mProgram2 = -1;
            mTexCoordHandle2 = -1;
            mPositionHandle2 = -1;
            mPosMtxHandle2 = -1;
            mTopMapHandle = -1;
            mBotMapHandle = -1;
            mAlphaMapHandle = -1;
            mFBO = null;
            float af[] = new float[18];
            af[0] = -1F;
            af[1] = -1F;
            af[2] = 0.0F;
            af[3] = 1.0F;
            af[4] = -1F;
            af[5] = 0.0F;
            af[6] = 1.0F;
            af[7] = 1.0F;
            af[8] = 0.0F;
            af[9] = -1F;
            af[10] = -1F;
            af[11] = 0.0F;
            af[12] = 1.0F;
            af[13] = 1.0F;
            af[14] = 0.0F;
            af[15] = -1F;
            af[16] = 1.0F;
            af[17] = 0.0F;
            squareVertexCoords = af;
            float af1[] = new float[12];
            af1[0] = 0.0F;
            af1[1] = 0.0F;
            af1[2] = 1.0F;
            af1[3] = 0.0F;
            af1[4] = 1.0F;
            af1[5] = 1.0F;
            af1[6] = 0.0F;
            af1[7] = 0.0F;
            af1[8] = 1.0F;
            af1[9] = 1.0F;
            af1[10] = 0.0F;
            af1[11] = 1.0F;
            squareTextureCoords = af1;
            creatShader();
        }

        void creatShader()
        {
            mProgram1 = GLUtil.createProgram("attribute vec4 aPosition;\nattribute vec4 aTexCoord;\nuniform   mat4 uPosMtx;\nvarying   vec2 vTexCoord;\nvoid main() {\n  gl_Position = uPosMtx * aPosition;\n  vTexCoord   = vec2 (aTexCoord.x,aTexCoord.y);\n}\n", "precision mediump float;\nvarying vec2      vTexCoord;\nvoid main() {\n  gl_FragColor = vec4(vTexCoord.x, vTexCoord.y, 0.66, 1.0);\n}\n");
            mPositionHandle1 = GLES20.glGetAttribLocation(mProgram1, "aPosition");
            mTexCoordHandle1 = GLES20.glGetAttribLocation(mProgram1, "aTexCoord");
            mPosMtxHandle1 = GLES20.glGetUniformLocation(mProgram1, "uPosMtx");
            mProgram2 = GLUtil.createProgram("attribute vec3 aPosition;\nattribute vec2 aTexCoord;\nvarying   vec2 vTexCoord;\nvoid main() {\n  gl_Position = vec4(aPosition, 1.0);\n  vTexCoord   = vec2(aTexCoord.x,1.0-aTexCoord.y);\n}\n", "precision mediump float;\nuniform sampler2D gTopMap;\nuniform sampler2D gBotMap;\nuniform sampler2D gAlphaMap;\nvarying vec2 vTexCoord;\nvoid main() {\n     vec2 uv;\n    float alpha = texture2D(gAlphaMap, vTexCoord).x;\n    vec4 color;\n    if (alpha>0.5) {\n        color = texture2D(gTopMap, vTexCoord).xyzw;\n        float y = color.x + color.y/255.0;\n        float x = color.z + color.w/255.0;\n        uv = vec2(y, x);}\n    else {\n        color = texture2D(gBotMap, vTexCoord).xyzw;\n        float y = color.x + color.y/255.0;\n        float x = color.z + color.w/255.0;\n        uv = vec2(y, x);}\n    gl_FragColor = vec4(uv.x, uv.y, alpha, 1.0);\n}\n");
            mPositionHandle2 = GLES20.glGetAttribLocation(mProgram2, "aPosition");
            mTexCoordHandle2 = GLES20.glGetAttribLocation(mProgram2, "aTexCoord");
            mTopMapHandle = GLES20.glGetUniformLocation(mProgram2, "gTopMap");
            mBotMapHandle = GLES20.glGetUniformLocation(mProgram2, "gBotMap");
            mAlphaMapHandle = GLES20.glGetUniformLocation(mProgram2, "gAlphaMap");
            GLUtil.checkGlError("shader create");
        }

        void getCoord(float f, float f1, float af[], int i, int j)
        {
            if(0.0F < f && 0.0F < f1 && mFBO != null)
            {
                if(gMap == null)
                    gMap = GenerateMapV2.getInstance(getActivity());
                int k = (int)f;
                int l = (int)((float)j - f1);
                Log.i(TAG, "wh="+i+","+j+";xy="+k+","+l);
                GLUtil.checkGlError("getCoord start");
                GLES20.glUseProgram(mProgram1);
                mFBO.setupFrameBufferGraphics(i, j);
                GLES20.glViewport(0, 0, i, j);
                mSphere.vertexBuffer.position(0);
                mSphere.textureBuffer.position(0);
                GLES20.glVertexAttribPointer(mPositionHandle1, 3, GLES20.GL_FLOAT, false, 12, mSphere.vertexBuffer);
                GLES20.glVertexAttribPointer(mTexCoordHandle1, 2, GLES20.GL_FLOAT, false, 8, mSphere.textureBuffer);
                GLES20.glEnableVertexAttribArray(mPositionHandle1);
                GLES20.glEnableVertexAttribArray(mTexCoordHandle1);
                GLES20.glUniformMatrix4fv(mPosMtxHandle1, 1, false, af, 0);
                GLES20.glEnable(GLES20.GL_CULL_FACE);
                GLES20.glCullFace(GLES20.GL_FRONT);
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
                GLES20.glDrawArrays(4, 0, numVertices);
                ByteBuffer bytebuffer = ByteBuffer.allocateDirect(4);
                bytebuffer.order(ByteOrder.LITTLE_ENDIAN);
                GLES20.glReadPixels(k, l, 1, 1, 6408, 5121, bytebuffer);
                GLUtil.checkGlError("glReadPixels");
                bytebuffer.rewind();
                byte abyte0[] = new byte[4];
                bytebuffer.asReadOnlyBuffer().get(abyte0);
                float f2 = (float)(0xff & abyte0[0]) / 255F;
                float f3 = 1.0F - (float)(0xff & abyte0[1]) / 255F;
                Log.i("ScreenRendererV3", (new StringBuilder()).append("uv=").append(f2).append(",").append(f3).toString());
                ByteBuffer bytebuffer1 = ByteBuffer.allocateDirect(4 * squareVertexCoords.length);
                bytebuffer1.order(ByteOrder.nativeOrder());
                FloatBuffer floatbuffer = bytebuffer1.asFloatBuffer();
                floatbuffer.put(squareVertexCoords);
                floatbuffer.position(0);
                ByteBuffer bytebuffer2 = ByteBuffer.allocateDirect(4 * squareTextureCoords.length);
                bytebuffer2.order(ByteOrder.nativeOrder());
                FloatBuffer floatbuffer1 = bytebuffer2.asFloatBuffer();
                floatbuffer1.put(squareTextureCoords);
                floatbuffer1.position(0);
                GLES20.glUseProgram(mProgram2);
                GLES20.glVertexAttribPointer(mPositionHandle2, 3, GLES20.GL_FLOAT, false, 12, floatbuffer);
                GLES20.glVertexAttribPointer(mTexCoordHandle2, 2, GLES20.GL_FLOAT, false, 8, floatbuffer1);
                GLES20.glEnableVertexAttribArray(mPositionHandle2);
                GLES20.glEnableVertexAttribArray(mTexCoordHandle2);
                GLES20.glUniform1i(mTopMapHandle, 0);
                GLES20.glUniform1i(mBotMapHandle, 1);
                GLES20.glUniform1i(mAlphaMapHandle, 2);
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, gMap.getLeftMapHandler());
                GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, gMap.getRightMapHandler());
                GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, gMap.getAlphaHandler());
                GLES20.glDisable(GLES20.GL_CULL_FACE);
                GLES20.glDisable(GLES20.GL_DEPTH_TEST);
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
                GLES20.glDrawArrays(4, 0, 6);
                float f4 = f2 * (float)i;
                float f5 = f3 * (float)j;
                ByteBuffer bytebuffer3 = ByteBuffer.allocateDirect(4);
                bytebuffer3.order(ByteOrder.LITTLE_ENDIAN);
                bytebuffer3.position(0);
                GLES20.glReadPixels((int)f4, (int)f5, 1, 1, 6408, 5121, bytebuffer3);
                GLUtil.checkGlError("glReadPixels");
                bytebuffer3.rewind();
                bytebuffer3.asReadOnlyBuffer().get(abyte0);
                int i1 = 0xff & abyte0[0];
                int j1 = 0xff & abyte0[1];
                int k1 = 0xff & abyte0[2];
                float f6 = (float)i1 / 255F;
                float f7 = (float)j1 / 255F;
                boolean flag;
                float af1[];
                float f8;
                if(k1 > 127)
                    flag = true;
                else
                    flag = false;
                mPickInfo[0] = f6;
                mPickInfo[1] = f7;
                af1 = mPickInfo;
                if(flag)
                    f8 = 1.0F;
                else
                    f8 = 0.0F;
                af1[2] = f8;
                Log.i("ScreenRendererV3", (new StringBuilder()).append("mPickInfo=").append(mPickInfo[0]).append(", ").append(mPickInfo[1]).append(",").append(mPickInfo[2]).toString());
                GLES20.glUseProgram(0);
                mFBO.setScreenBufferGraphics();
            }
        }

        void initFbo(int i, int j)
        {
            if(mFBO == null)
            {
                mFBO = new FrameBuffer();
                mFBO.setRendererSize(i, j);
                mFBO.setScreenBufferGraphics();
            }
        }

        void releaseFbo()
        {
            if(mFBO != null)
                mFBO.release();
        }
    }

    public void updateScreenRenderParameter(float anglex,float angley,float rate){
        mAngleX = anglex;
        mAngleY = angley;
        mRate = rate;

        mAngleX =mAngleX%360;
        mAngleY =mAngleY%360;
        if(mRate < 0.5F)mRate = 0.5F;

        Log.d("ScreenRenderParameter","mAngleX:"+mAngleX+" mAngleY:"+mAngleY+" mRate:"+mRate);
    }
}
