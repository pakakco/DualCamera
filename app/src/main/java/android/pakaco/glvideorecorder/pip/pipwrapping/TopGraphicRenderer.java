

package android.pakaco.glvideorecorder.pip.pipwrapping;

import android.app.Activity;
import android.app.Service;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.opengl.GLES11Ext;
import android.opengl.Matrix;
import android.util.Log;
//import com.android.camera.Util;
import java.io.*;
import java.nio.*;



public class TopGraphicRenderer extends Renderer
{
    private static final String TAG = "TopGraphicRenderer";
    final String fragmentShader;
    final String fragmentShaderPIPOriginal;
    private GenerateMapV2 gMap;
    private GenerateMapV2Copy2 gMap4cap;
    private int mAlphaHandle;
    private int mBackTempTexId;
    private float mEditMtx[];
    private int mHeight;
    private int mIsCapture;
    private int mIsFrameSaved;
    private boolean mIsPIPOrginal;
    private float mMMtx[];
    private float mMVPMtx[];
    private float mPMtx[];
    private int mPicHeight;
    private int mPicWidth;
    float mPickInfo[];
    private int mPickInfoHandle;
    private float mPosMtx[];
    private int mPositionMapHandle;
    private int mProgram;
    private FloatBuffer mTempTexCoordBuf;
    private FloatBuffer mTexCoordBuf;
    FloatBuffer mTextureBuffer;
    private ResourceRenderer mTopTemplateRenderer;
    private float mVMtx[];
    FloatBuffer mVertexBuffer;
    private FloatBuffer mVtxBuf;
    private int mWidth;
    private int maPositionHandle;
    private int maTempTexCoordHandle;
    private int maTexCoordHandle;
    private int muBackTempSamplerHandle;
    private int muIsPreviewHandle;
    private int muPictureSampleHandle;
    private int muPosMtxHandle;
    private int muPreviewSamplerHandle;
    private int muTexMtxHandle;
    private int muTexRotateMtxHandle;
    final String vertexShader;
    final String vertexShaderPIPOriginal;
    
    public TopGraphicRenderer(Activity activity, boolean flag)
    {
        super(activity);
        mMVPMtx = GLUtil.createIdentityMtx();
        mMMtx = GLUtil.createIdentityMtx();
        mVMtx = GLUtil.createIdentityMtx();
        mPMtx = GLUtil.createIdentityMtx();
        mPosMtx = GLUtil.createIdentityMtx();
        mEditMtx = GLUtil.createIdentityMtx();
        mBackTempTexId = -12345;
        mProgram = -1;
        maPositionHandle = -1;
        maTexCoordHandle = -1;
        maTempTexCoordHandle = -1;
        muPosMtxHandle = -1;
        muTexMtxHandle = -1;
        muTexRotateMtxHandle = -1;
        muIsPreviewHandle = -1;
        muPictureSampleHandle = -1;
        muPreviewSamplerHandle = -1;
        muBackTempSamplerHandle = -1;
        mPositionMapHandle = -1;
        mAlphaHandle = -1;
        mPickInfoHandle = -1;
        mWidth = 1280;
        mHeight = 960;
        mPicWidth = 4224;
        mPicHeight = 3268;
        mIsCapture = 0;
        mIsFrameSaved = 1;
        mPickInfo = new float[3];
        vertexShader = "attribute vec3    aPosition;\nattribute vec2    aTexCoord;\nvarying   vec2    vTexCoord;\nvoid main() {\n    gl_Position   = vec4(aPosition, 1.0);\n    vTexCoord = vec2(1.0-aTexCoord.x, aTexCoord.y);\n}\n";
        fragmentShader = "#extension GL_OES_EGL_image_external : require\nprecision highp float;\nuniform sampler2D uPictureSampler;\nuniform sampler2D gPositionMap;\nuniform sampler2D gAlphaMap;\nuniform samplerExternalOES uPreviewSampler;\nuniform vec4 pickInfo;\nuniform float vfIsPreview;\nvarying vec2 vTexCoord;\nvoid main() {\n    vec2 preTexCoord;\n    float up0 = texture2D(gAlphaMap, vTexCoord).x;\n    vec4 vTexCoord1 = texture2D(gPositionMap, vTexCoord.xy).xyzw;\n    float y = vTexCoord1.x + vTexCoord1.y / 255.0;\n    float x = vTexCoord1.z + vTexCoord1.w / 255.0;\n    if(vfIsPreview > 0.0) {\n        preTexCoord = vec2(1.0-y, x);\n        gl_FragColor = texture2D(uPreviewSampler, preTexCoord) * (up0);/* new lut */\n        float k = abs(pickInfo.x-preTexCoord.x)+abs(pickInfo.y-preTexCoord.y);\n        if (pickInfo.z>0.5 && k<0.02){\n              gl_FragColor = vec4(1,0,0,1);\n        };\n    } else { \n        preTexCoord = vec2(y, x);\n        gl_FragColor = texture2D(uPreviewSampler, preTexCoord) * (up0);/* new lut */\n    }\n}\n";
        mIsPIPOrginal = false;
        vertexShaderPIPOriginal = "attribute vec4   aPosition;\nattribute vec4   aTexCoord;\nattribute vec4   aTempTexCoord;\nuniform   float  uIsPreview;\nuniform   mat4   uPosMtx;\nuniform   mat4   uTexMtx;\nuniform   mat4   uTexRotateMtx;\nvarying   vec2   vTexCoord;\nvarying   vec2   vTempTexCoord;\nvarying   float  vIsPreview;\nvoid main() {\n    gl_Position    = uPosMtx * aPosition;\n    vTexCoord     = (uTexRotateMtx * uTexMtx * aTexCoord).xy;\n    vTempTexCoord  = aTempTexCoord.xy;\n    vIsPreview     = uIsPreview;\n}\n";
        fragmentShaderPIPOriginal = "#extension GL_OES_EGL_image_external : require\nprecision mediump float;\nuniform   samplerExternalOES uPreviewSampler;\nuniform   sampler2D uPictureSampler;\nuniform   sampler2D uBackSampler;\nvarying   vec2               vTexCoord;\nvarying   vec2       vTempTexCoord;\nvarying   float  vIsPreview;\nconst vec3 black = vec3(0, 0, 0);  \nvoid main() {\n    vec3 texture1 = vec3(texture2D(uBackSampler,vTempTexCoord).rgb);\n    if((equal(texture1, black)).r) {\n        gl_FragColor = vec4(0, 0, 0, 0);\n    } else {\n        if (vIsPreview > 0.0) {\n            gl_FragColor = texture2D(uPreviewSampler, vTexCoord);\n        } else { \n            gl_FragColor = texture2D(uPictureSampler, vTexCoord);\n        }\n    }\n}\n";
        Log.i(TAG, "TopGraphicRenderer");
        mIsPIPOrginal = flag;
        initProgram();
        mTexCoordBuf = createFloatBuffer(mTexCoordBuf, GLUtil.createTexCoord());
        mTempTexCoordBuf = createFloatBuffer(mTempTexCoordBuf, GLUtil.createTexCoord());
        mTopTemplateRenderer = new ResourceRenderer(getActivity());
        mTopTemplateRenderer.init();
        gMap = GenerateMapV2.getInstance(activity);
    }

    public TopGraphicRenderer(Activity activity, boolean flag, boolean flag1)
    {
        super(activity);
        mMVPMtx = GLUtil.createIdentityMtx();
        mMMtx = GLUtil.createIdentityMtx();
        mVMtx = GLUtil.createIdentityMtx();
        mPMtx = GLUtil.createIdentityMtx();
        mPosMtx = GLUtil.createIdentityMtx();
        mEditMtx = GLUtil.createIdentityMtx();
        mBackTempTexId = -12345;
        mProgram = -1;
        maPositionHandle = -1;
        maTexCoordHandle = -1;
        maTempTexCoordHandle = -1;
        muPosMtxHandle = -1;
        muTexMtxHandle = -1;
        muTexRotateMtxHandle = -1;
        muIsPreviewHandle = -1;
        muPictureSampleHandle = -1;
        muPreviewSamplerHandle = -1;
        muBackTempSamplerHandle = -1;
        mPositionMapHandle = -1;
        mAlphaHandle = -1;
        mPickInfoHandle = -1;
        mWidth = 1280;
        mHeight = 960;
        mPicWidth = 4224;
        mPicHeight = 3268;
        mIsCapture = 0;
        mIsFrameSaved = 1;
        mPickInfo = new float[3];
        vertexShader = "attribute vec3    aPosition;\nattribute vec2    aTexCoord;\nvarying   vec2    vTexCoord;\nvoid main() {\n    gl_Position   = vec4(aPosition, 1.0);\n    vTexCoord = vec2(1.0-aTexCoord.x, aTexCoord.y);\n}\n";
        fragmentShader = "#extension GL_OES_EGL_image_external : require\nprecision highp float;\nuniform sampler2D uPictureSampler;\nuniform sampler2D gPositionMap;\nuniform sampler2D gAlphaMap;\nuniform samplerExternalOES uPreviewSampler;\nuniform vec4 pickInfo;\nuniform float vfIsPreview;\nvarying vec2 vTexCoord;\nvoid main() {\n    vec2 preTexCoord;\n    float up0 = texture2D(gAlphaMap, vTexCoord).x;\n    vec4 vTexCoord1 = texture2D(gPositionMap, vTexCoord.xy).xyzw;\n    float y = vTexCoord1.x + vTexCoord1.y / 255.0;\n    float x = vTexCoord1.z + vTexCoord1.w / 255.0;\n    if(vfIsPreview > 0.0) {\n        preTexCoord = vec2(1.0-y, x);\n        gl_FragColor = texture2D(uPreviewSampler, preTexCoord) * (up0);/* new lut */\n        float k = abs(pickInfo.x-preTexCoord.x)+abs(pickInfo.y-preTexCoord.y);\n        if (pickInfo.z>0.5 && k<0.02){\n              gl_FragColor = vec4(1,0,0,1);\n        };\n    } else { \n        preTexCoord = vec2(y, x);\n        gl_FragColor = texture2D(uPreviewSampler, preTexCoord) * (up0);/* new lut */\n    }\n}\n";
        mIsPIPOrginal = false;
        vertexShaderPIPOriginal = "attribute vec4   aPosition;\nattribute vec4   aTexCoord;\nattribute vec4   aTempTexCoord;\nuniform   float  uIsPreview;\nuniform   mat4   uPosMtx;\nuniform   mat4   uTexMtx;\nuniform   mat4   uTexRotateMtx;\nvarying   vec2   vTexCoord;\nvarying   vec2   vTempTexCoord;\nvarying   float  vIsPreview;\nvoid main() {\n    gl_Position    = uPosMtx * aPosition;\n    vTexCoord     = (uTexRotateMtx * uTexMtx * aTexCoord).xy;\n    vTempTexCoord  = aTempTexCoord.xy;\n    vIsPreview     = uIsPreview;\n}\n";
        fragmentShaderPIPOriginal = "#extension GL_OES_EGL_image_external : require\nprecision mediump float;\nuniform   samplerExternalOES uPreviewSampler;\nuniform   sampler2D uPictureSampler;\nuniform   sampler2D uBackSampler;\nvarying   vec2               vTexCoord;\nvarying   vec2       vTempTexCoord;\nvarying   float  vIsPreview;\nconst vec3 black = vec3(0, 0, 0);  \nvoid main() {\n    vec3 texture1 = vec3(texture2D(uBackSampler,vTempTexCoord).rgb);\n    if((equal(texture1, black)).r) {\n        gl_FragColor = vec4(0, 0, 0, 0);\n    } else {\n        if (vIsPreview > 0.0) {\n            gl_FragColor = texture2D(uPreviewSampler, vTexCoord);\n        } else { \n            gl_FragColor = texture2D(uPictureSampler, vTexCoord);\n        }\n    }\n}\n";
        Log.i("TopGraphicRenderer", "TopGraphicRenderer");
        mIsPIPOrginal = flag;
        initProgram();
        mTexCoordBuf = createFloatBuffer(mTexCoordBuf, GLUtil.createTexCoord());
        mTempTexCoordBuf = createFloatBuffer(mTempTexCoordBuf, GLUtil.createTexCoord());
        mTopTemplateRenderer = new ResourceRenderer(getActivity());
        mTopTemplateRenderer.init();
        gMap4cap = GenerateMapV2Copy2.getInstance(activity);
    }

    public TopGraphicRenderer(Service service, boolean flag)
    {
        super(service);
        mMVPMtx = GLUtil.createIdentityMtx();
        mMMtx = GLUtil.createIdentityMtx();
        mVMtx = GLUtil.createIdentityMtx();
        mPMtx = GLUtil.createIdentityMtx();
        mPosMtx = GLUtil.createIdentityMtx();
        mEditMtx = GLUtil.createIdentityMtx();
        mBackTempTexId = -12345;
        mProgram = -1;
        maPositionHandle = -1;
        maTexCoordHandle = -1;
        maTempTexCoordHandle = -1;
        muPosMtxHandle = -1;
        muTexMtxHandle = -1;
        muTexRotateMtxHandle = -1;
        muIsPreviewHandle = -1;
        muPictureSampleHandle = -1;
        muPreviewSamplerHandle = -1;
        muBackTempSamplerHandle = -1;
        mPositionMapHandle = -1;
        mAlphaHandle = -1;
        mPickInfoHandle = -1;
        mWidth = 1280;
        mHeight = 960;
        mPicWidth = 4224;
        mPicHeight = 3268;
        mIsCapture = 0;
        mIsFrameSaved = 1;
        mPickInfo = new float[3];
        vertexShader = "attribute vec3    aPosition;\nattribute vec2    aTexCoord;\nvarying   vec2    vTexCoord;\nvoid main() {\n    gl_Position   = vec4(aPosition, 1.0);\n    vTexCoord = vec2(1.0-aTexCoord.x, aTexCoord.y);\n}\n";
        fragmentShader = "#extension GL_OES_EGL_image_external : require\nprecision highp float;\nuniform sampler2D uPictureSampler;\nuniform sampler2D gPositionMap;\nuniform sampler2D gAlphaMap;\nuniform samplerExternalOES uPreviewSampler;\nuniform vec4 pickInfo;\nuniform float vfIsPreview;\nvarying vec2 vTexCoord;\nvoid main() {\n    vec2 preTexCoord;\n    float up0 = texture2D(gAlphaMap, vTexCoord).x;\n    vec4 vTexCoord1 = texture2D(gPositionMap, vTexCoord.xy).xyzw;\n    float y = vTexCoord1.x + vTexCoord1.y / 255.0;\n    float x = vTexCoord1.z + vTexCoord1.w / 255.0;\n    if(vfIsPreview > 0.0) {\n        preTexCoord = vec2(1.0-y, x);\n        gl_FragColor = texture2D(uPreviewSampler, preTexCoord) * (up0);/* new lut */\n        float k = abs(pickInfo.x-preTexCoord.x)+abs(pickInfo.y-preTexCoord.y);\n        if (pickInfo.z>0.5 && k<0.02){\n              gl_FragColor = vec4(1,0,0,1);\n        };\n    } else { \n        preTexCoord = vec2(y, x);\n        gl_FragColor = texture2D(uPreviewSampler, preTexCoord) * (up0);/* new lut */\n    }\n}\n";
        mIsPIPOrginal = false;
        vertexShaderPIPOriginal = "attribute vec4   aPosition;\nattribute vec4   aTexCoord;\nattribute vec4   aTempTexCoord;\nuniform   float  uIsPreview;\nuniform   mat4   uPosMtx;\nuniform   mat4   uTexMtx;\nuniform   mat4   uTexRotateMtx;\nvarying   vec2   vTexCoord;\nvarying   vec2   vTempTexCoord;\nvarying   float  vIsPreview;\nvoid main() {\n    gl_Position    = uPosMtx * aPosition;\n    vTexCoord     = (uTexRotateMtx * uTexMtx * aTexCoord).xy;\n    vTempTexCoord  = aTempTexCoord.xy;\n    vIsPreview     = uIsPreview;\n}\n";
        fragmentShaderPIPOriginal = "#extension GL_OES_EGL_image_external : require\nprecision mediump float;\nuniform   samplerExternalOES uPreviewSampler;\nuniform   sampler2D uPictureSampler;\nuniform   sampler2D uBackSampler;\nvarying   vec2               vTexCoord;\nvarying   vec2       vTempTexCoord;\nvarying   float  vIsPreview;\nconst vec3 black = vec3(0, 0, 0);  \nvoid main() {\n    vec3 texture1 = vec3(texture2D(uBackSampler,vTempTexCoord).rgb);\n    if((equal(texture1, black)).r) {\n        gl_FragColor = vec4(0, 0, 0, 0);\n    } else {\n        if (vIsPreview > 0.0) {\n            gl_FragColor = texture2D(uPreviewSampler, vTexCoord);\n        } else { \n            gl_FragColor = texture2D(uPictureSampler, vTexCoord);\n        }\n    }\n}\n";
        Log.i("TopGraphicRenderer", "TopGraphicRenderer");
        mIsPIPOrginal = flag;
        initProgram();
        mTexCoordBuf = createFloatBuffer(mTexCoordBuf, GLUtil.createTexCoord());
        mTempTexCoordBuf = createFloatBuffer(mTempTexCoordBuf, GLUtil.createTexCoord());
        mTopTemplateRenderer = new ResourceRenderer(getActivity());
        mTopTemplateRenderer.init();
        gMap = GenerateMapV2.getInstance(service);
    }

    public TopGraphicRenderer(Service service, boolean flag, boolean flag1)
    {
        super(service);
        mMVPMtx = GLUtil.createIdentityMtx();
        mMMtx = GLUtil.createIdentityMtx();
        mVMtx = GLUtil.createIdentityMtx();
        mPMtx = GLUtil.createIdentityMtx();
        mPosMtx = GLUtil.createIdentityMtx();
        mEditMtx = GLUtil.createIdentityMtx();
        mBackTempTexId = -12345;
        mProgram = -1;
        maPositionHandle = -1;
        maTexCoordHandle = -1;
        maTempTexCoordHandle = -1;
        muPosMtxHandle = -1;
        muTexMtxHandle = -1;
        muTexRotateMtxHandle = -1;
        muIsPreviewHandle = -1;
        muPictureSampleHandle = -1;
        muPreviewSamplerHandle = -1;
        muBackTempSamplerHandle = -1;
        mPositionMapHandle = -1;
        mAlphaHandle = -1;
        mPickInfoHandle = -1;
        mWidth = 1280;
        mHeight = 960;
        mPicWidth = 4224;
        mPicHeight = 3268;
        mIsCapture = 0;
        mIsFrameSaved = 1;
        mPickInfo = new float[3];
        vertexShader = "attribute vec3    aPosition;\nattribute vec2    aTexCoord;\nvarying   vec2    vTexCoord;\nvoid main() {\n    gl_Position   = vec4(aPosition, 1.0);\n    vTexCoord = vec2(1.0-aTexCoord.x, aTexCoord.y);\n}\n";
        fragmentShader = "#extension GL_OES_EGL_image_external : require\nprecision highp float;\nuniform sampler2D uPictureSampler;\nuniform sampler2D gPositionMap;\nuniform sampler2D gAlphaMap;\nuniform samplerExternalOES uPreviewSampler;\nuniform vec4 pickInfo;\nuniform float vfIsPreview;\nvarying vec2 vTexCoord;\nvoid main() {\n    vec2 preTexCoord;\n    float up0 = texture2D(gAlphaMap, vTexCoord).x;\n    vec4 vTexCoord1 = texture2D(gPositionMap, vTexCoord.xy).xyzw;\n    float y = vTexCoord1.x + vTexCoord1.y / 255.0;\n    float x = vTexCoord1.z + vTexCoord1.w / 255.0;\n    if(vfIsPreview > 0.0) {\n        preTexCoord = vec2(1.0-y, x);\n        gl_FragColor = texture2D(uPreviewSampler, preTexCoord) * (up0);/* new lut */\n        float k = abs(pickInfo.x-preTexCoord.x)+abs(pickInfo.y-preTexCoord.y);\n        if (pickInfo.z>0.5 && k<0.02){\n              gl_FragColor = vec4(1,0,0,1);\n        };\n    } else { \n        preTexCoord = vec2(y, x);\n        gl_FragColor = texture2D(uPreviewSampler, preTexCoord) * (up0);/* new lut */\n    }\n}\n";
        mIsPIPOrginal = false;
        vertexShaderPIPOriginal = "attribute vec4   aPosition;\nattribute vec4   aTexCoord;\nattribute vec4   aTempTexCoord;\nuniform   float  uIsPreview;\nuniform   mat4   uPosMtx;\nuniform   mat4   uTexMtx;\nuniform   mat4   uTexRotateMtx;\nvarying   vec2   vTexCoord;\nvarying   vec2   vTempTexCoord;\nvarying   float  vIsPreview;\nvoid main() {\n    gl_Position    = uPosMtx * aPosition;\n    vTexCoord     = (uTexRotateMtx * uTexMtx * aTexCoord).xy;\n    vTempTexCoord  = aTempTexCoord.xy;\n    vIsPreview     = uIsPreview;\n}\n";
        fragmentShaderPIPOriginal = "#extension GL_OES_EGL_image_external : require\nprecision mediump float;\nuniform   samplerExternalOES uPreviewSampler;\nuniform   sampler2D uPictureSampler;\nuniform   sampler2D uBackSampler;\nvarying   vec2               vTexCoord;\nvarying   vec2       vTempTexCoord;\nvarying   float  vIsPreview;\nconst vec3 black = vec3(0, 0, 0);  \nvoid main() {\n    vec3 texture1 = vec3(texture2D(uBackSampler,vTempTexCoord).rgb);\n    if((equal(texture1, black)).r) {\n        gl_FragColor = vec4(0, 0, 0, 0);\n    } else {\n        if (vIsPreview > 0.0) {\n            gl_FragColor = texture2D(uPreviewSampler, vTexCoord);\n        } else { \n            gl_FragColor = texture2D(uPictureSampler, vTexCoord);\n        }\n    }\n}\n";
        Log.i("TopGraphicRenderer", "TopGraphicRenderer");
        mIsPIPOrginal = flag;
        initProgram();
        mTexCoordBuf = createFloatBuffer(mTexCoordBuf, GLUtil.createTexCoord());
        mTempTexCoordBuf = createFloatBuffer(mTempTexCoordBuf, GLUtil.createTexCoord());
        mTopTemplateRenderer = new ResourceRenderer(getActivity());
        mTopTemplateRenderer.init();
        gMap4cap = GenerateMapV2Copy2.getInstance(service);
    }

    private void initProgram()
    {
        if(mIsPIPOrginal)
        {
            mProgram = createProgram("attribute vec4   aPosition;\nattribute vec4   aTexCoord;\nattribute vec4   aTempTexCoord;\nuniform   float  uIsPreview;\nuniform   mat4   uPosMtx;\nuniform   mat4   uTexMtx;\nuniform   mat4   uTexRotateMtx;\nvarying   vec2   vTexCoord;\nvarying   vec2   vTempTexCoord;\nvarying   float  vIsPreview;\nvoid main() {\n    gl_Position    = uPosMtx * aPosition;\n    vTexCoord     = (uTexRotateMtx * uTexMtx * aTexCoord).xy;\n    vTempTexCoord  = aTempTexCoord.xy;\n    vIsPreview     = uIsPreview;\n}\n", "#extension GL_OES_EGL_image_external : require\nprecision mediump float;\nuniform   samplerExternalOES uPreviewSampler;\nuniform   sampler2D uPictureSampler;\nuniform   sampler2D uBackSampler;\nvarying   vec2               vTexCoord;\nvarying   vec2       vTempTexCoord;\nvarying   float  vIsPreview;\nconst vec3 black = vec3(0, 0, 0);  \nvoid main() {\n    vec3 texture1 = vec3(texture2D(uBackSampler,vTempTexCoord).rgb);\n    if((equal(texture1, black)).r) {\n        gl_FragColor = vec4(0, 0, 0, 0);\n    } else {\n        if (vIsPreview > 0.0) {\n            gl_FragColor = texture2D(uPreviewSampler, vTexCoord);\n        } else { \n            gl_FragColor = texture2D(uPictureSampler, vTexCoord);\n        }\n    }\n}\n");
            maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
            maTexCoordHandle = GLES20.glGetAttribLocation(mProgram, "aTexCoord");
            maTempTexCoordHandle = GLES20.glGetAttribLocation(mProgram, "aTempTexCoord");
            muPosMtxHandle = GLES20.glGetUniformLocation(mProgram, "uPosMtx");
            muTexMtxHandle = GLES20.glGetUniformLocation(mProgram, "uTexMtx");
            muTexRotateMtxHandle = GLES20.glGetUniformLocation(mProgram, "uTexRotateMtx");
            muIsPreviewHandle = GLES20.glGetUniformLocation(mProgram, "uIsPreview");
            muPreviewSamplerHandle = GLES20.glGetUniformLocation(mProgram, "uPreviewSampler");
            muBackTempSamplerHandle = GLES20.glGetUniformLocation(mProgram, "uBackSampler");
            muPictureSampleHandle = GLES20.glGetUniformLocation(mProgram, "uPictureSampler");
        } else
        {
            mProgram = createProgram("attribute vec3    aPosition;\nattribute vec2    aTexCoord;\nvarying   vec2    vTexCoord;\nvoid main() {\n    gl_Position   = vec4(aPosition, 1.0);\n    vTexCoord = vec2(1.0-aTexCoord.x, aTexCoord.y);\n}\n", "#extension GL_OES_EGL_image_external : require\nprecision highp float;\nuniform sampler2D uPictureSampler;\nuniform sampler2D gPositionMap;\nuniform sampler2D gAlphaMap;\nuniform samplerExternalOES uPreviewSampler;\nuniform vec4 pickInfo;\nuniform float vfIsPreview;\nvarying vec2 vTexCoord;\nvoid main() {\n    vec2 preTexCoord;\n    float up0 = texture2D(gAlphaMap, vTexCoord).x;\n    vec4 vTexCoord1 = texture2D(gPositionMap, vTexCoord.xy).xyzw;\n    float y = vTexCoord1.x + vTexCoord1.y / 255.0;\n    float x = vTexCoord1.z + vTexCoord1.w / 255.0;\n    if(vfIsPreview > 0.0) {\n        preTexCoord = vec2(1.0-y, x);\n        gl_FragColor = texture2D(uPreviewSampler, preTexCoord) * (up0);/* new lut */\n        float k = abs(pickInfo.x-preTexCoord.x)+abs(pickInfo.y-preTexCoord.y);\n        if (pickInfo.z>0.5 && k<0.02){\n              gl_FragColor = vec4(1,0,0,1);\n        };\n    } else { \n        preTexCoord = vec2(y, x);\n        gl_FragColor = texture2D(uPreviewSampler, preTexCoord) * (up0);/* new lut */\n    }\n}\n");
            GLUtil.checkGlError("TopGraphicRenderer after mProgram");
            maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
            maTexCoordHandle = GLES20.glGetAttribLocation(mProgram, "aTexCoord");
            muIsPreviewHandle = GLES20.glGetUniformLocation(mProgram, "vfIsPreview");
            mPickInfoHandle = GLES20.glGetUniformLocation(mProgram, "pickInfo");
            muPreviewSamplerHandle = GLES20.glGetUniformLocation(mProgram, "uPreviewSampler");
            muPictureSampleHandle = GLES20.glGetUniformLocation(mProgram, "uPictureSampler");
            mPositionMapHandle = GLES20.glGetUniformLocation(mProgram, "gPositionMap");
            mAlphaHandle = GLES20.glGetUniformLocation(mProgram, "gAlphaMap");
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
            ByteBuffer bytebuffer = ByteBuffer.allocateDirect(4 * af.length);
            bytebuffer.order(ByteOrder.nativeOrder());
            mVertexBuffer = bytebuffer.asFloatBuffer();
            mVertexBuffer.put(af);
            mVertexBuffer.position(0);
            ByteBuffer bytebuffer1 = ByteBuffer.allocateDirect(4 * af1.length);
            bytebuffer1.order(ByteOrder.nativeOrder());
            mTextureBuffer = bytebuffer1.asFloatBuffer();
            mTextureBuffer.put(af1);
            mTextureBuffer.position(0);
            GLUtil.checkGlError("TopGraphicRenderer initProgram");
        }
    }

    private void resetMatrix()
    {
        mMVPMtx = GLUtil.createIdentityMtx();
        mPMtx = GLUtil.createIdentityMtx();
        mVMtx = GLUtil.createIdentityMtx();
        mMMtx = GLUtil.createIdentityMtx();
        if(!mIsPIPOrginal)
        {
            mPosMtx = GLUtil.createIdentityMtx();
            mEditMtx = GLUtil.createIdentityMtx();
        }
    }

    public void draw(int preTex, final float[] preTexMtx, final float[] texReverseRotateMtx,
            final AnimationRect topRect, int rotation, boolean needFlip) {
        if (preTex <= 0 || topRect == null) {
            return;
        }
        // copy AnimationRect
        AnimationRect animationRect = new AnimationRect();
        animationRect.setRendererSize(topRect.getPreviewWidth(), topRect.getPreviewHeight());
        animationRect.setCurrentScaleValue(topRect.getCurrentScaleValue());
        animationRect.setOriginalDistance(topRect.getOriginalDistance());
        animationRect.initialize(topRect.getRectF().left, topRect.getRectF().top,
                topRect.getRectF().right, topRect.getRectF().bottom);
        animationRect.rotate(topRect.getCurrrentRotationValue());
        // keep original centerX and centerY
        float centerX = animationRect.getRectF().centerX();
        float centerY = animationRect.getRectF().centerY();
        // translate big box to match small box's center point
        CropBox cropBox = getCropBox();
        animationRect.translate(cropBox.getTranslateXRatio() * animationRect.getRectF().width(),
                cropBox.getTranslateYRatio() * animationRect.getRectF().height(), false);
        animationRect.rotate(animationRect.getCurrrentRotationValue());
        // scale by the same ratio to find the smallest box to wrap the small
        // box
        animationRect.scale(cropBox.getScaleRatio(), false);
        animationRect.rotate(animationRect.getCurrrentRotationValue(), centerX, centerY);
        GLUtil.checkGlError("TopGraphicRenderer draw start");
        // compute crop area
        boolean isLandScape = getRendererWidth() > getRendererHeight();
        int longer = Math.max(getRendererWidth(), getRendererHeight());
        int shorter = Math.min(getRendererWidth(), getRendererHeight());
        float lowHeight = .0f;
        float highHeight = .0f;
        float lowWidth = .0f;
        float highWidth = .0f;
        float topGraphicCropValue = PIPCustomization.TOP_GRAPHIC_CROP_RELATIVE_POSITION_VALUE;
        if (rotation < 0) {
            // take picture, preview is not reverse
            lowHeight = isLandScape ? 0 : (longer - shorter) * topGraphicCropValue / longer;
            highHeight = isLandScape ? 1 : ((longer - shorter) * topGraphicCropValue + shorter)
                    / longer;
            // that is top is sub camera
            boolean bottomIsMainCamera =true;
            lowWidth = isLandScape ? (longer - shorter)
                    * (bottomIsMainCamera ? (1f - topGraphicCropValue) :
                        topGraphicCropValue) / longer
                    : 0;
            highWidth = isLandScape ? ((longer - shorter)
                    * (bottomIsMainCamera ? (1f - topGraphicCropValue) :
                        topGraphicCropValue) + shorter) / longer
                    : 1;
            mTexCoordBuf = createFloatBuffer(mTexCoordBuf,
                    GLUtil.createTexCoord(lowWidth, highWidth, lowHeight, highHeight, needFlip));
        } else {
            lowHeight = isLandScape ? 0 : (longer - shorter) * (1f - topGraphicCropValue) / longer;
            highHeight = isLandScape ? 1
                    : ((longer - shorter) * (1f - topGraphicCropValue) + shorter) / longer;
            lowWidth = isLandScape ? (longer - shorter) * (1f - topGraphicCropValue) / longer : 0;
            highWidth = isLandScape ? ((longer - shorter) * (1f - topGraphicCropValue) + shorter)
                    / longer : 1;
            switch (rotation) {
            case 0:
                mTexCoordBuf = createFloatBuffer(mTexCoordBuf,
                        GLUtil.createReverseStandTexCoord(
                                lowWidth, highWidth, lowHeight, highHeight));
                break;
            case 90:
                mTexCoordBuf = createFloatBuffer(mTexCoordBuf,
                        GLUtil.createRightTexCoord(
                                lowWidth, highWidth, lowHeight, highHeight));
                break;
            case 180:
                mTexCoordBuf = createFloatBuffer(mTexCoordBuf,
                        GLUtil.createStandTexCoord(
                                lowWidth, highWidth, lowHeight, highHeight));
                break;
            case 270:
                mTexCoordBuf = createFloatBuffer(mTexCoordBuf,
                        GLUtil.createLeftTexCoord(
                                lowWidth, highWidth, lowHeight, highHeight));
                break;
            }
        }
        GLES20.glUseProgram(mProgram);
        // position
        mVtxBuf = createFloatBuffer(mVtxBuf, GLUtil.createTopRightRect(animationRect));
        mVtxBuf.position(0);
        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false, 4 * 3, mVtxBuf);
        mTexCoordBuf.position(0);
        GLES20.glVertexAttribPointer(maTexCoordHandle, 2, GLES20.GL_FLOAT, false, 4 * 2,
                mTexCoordBuf);
        mTempTexCoordBuf.position(0);
        GLES20.glVertexAttribPointer(maTempTexCoordHandle, 2, GLES20.GL_FLOAT, false, 4 * 2,
                mTempTexCoordBuf);
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        GLES20.glEnableVertexAttribArray(maTexCoordHandle);
        GLES20.glEnableVertexAttribArray(maTempTexCoordHandle);
        // draw
        // matrix
        GLES20.glUniformMatrix4fv(muPosMtxHandle, 1, false, mMVPMtx, 0);
        GLES20.glUniformMatrix4fv(muTexMtxHandle, 1, false,
                (preTexMtx == null) ? GLUtil.createIdentityMtx() : preTexMtx, 0);
        GLES20.glUniformMatrix4fv(muTexRotateMtxHandle, 1, false, texReverseRotateMtx, 0);
        GLES20.glUniform1f(muIsPreviewHandle, (preTexMtx == null) ? 0.0f : 1.0f);
        // sampler
        GLES20.glUniform1i((preTexMtx == null) ? muPictureSampleHandle : muPreviewSamplerHandle, 0);
        GLES20.glUniform1i(muBackTempSamplerHandle, 1);
        // texture
        GLUtil.checkGlError("TopGraphicRenderer draw end");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture((preTexMtx == null) ? GLES20.GL_TEXTURE_2D
                : GLES11Ext.GL_TEXTURE_EXTERNAL_OES, preTex);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLUtil.checkGlError("TopGraphicRenderer draw end");
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mBackTempTexId);
        // draw
        GLUtil.checkGlError("TopGraphicRenderer draw end");
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 6);
        // set mVtxBuf back to big box
        GLUtil.checkGlError("TopGraphicRenderer glDrawArrays");
        mVtxBuf = createFloatBuffer(mVtxBuf, GLUtil.createTopRightRect(topRect));
        mTopTemplateRenderer.draw(0, 0, 0, getVtxFloatBuffer(), null);
        GLUtil.checkGlError("TopGraphicRenderer draw end");
    }    

    public void draw_4cap(int i, float af[], float af1[], boolean flag)
    {
        GLUtil.checkGlError("TopGraphicRenderer draw start");
        GLES20.glDisable(2929);
        GLES20.glEnable(3042);
        GLES20.glBlendFunc(1, 1);
        GLUtil.checkGlError("glBlendFunc");
        GLES20.glUseProgram(mProgram);
        GLES20.glVertexAttribPointer(maPositionHandle, 3, 5126, false, 12, mVertexBuffer);
        GLES20.glVertexAttribPointer(maTexCoordHandle, 2, 5126, false, 8, mTextureBuffer);
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        GLES20.glEnableVertexAttribArray(maTexCoordHandle);
        GLES20.glUniform1f(muIsPreviewHandle, 0.0F);
        GLES20.glUniform1i(muPictureSampleHandle, 0);
        GLES20.glUniform1i(mPositionMapHandle, 1);
        GLES20.glUniform1i(mAlphaHandle, 2);
        GLUtil.checkGlError("glUniform1i");
        GLES20.glActiveTexture(33984);
        GLES20.glBindTexture(36197, i);
        GLUtil.checkGlError("getLeftMapHandler");
        GLES20.glActiveTexture(33985);
        GLES20.glBindTexture(3553, gMap4cap.getLeftMapHandler());
        GLUtil.checkGlError("getLeftMapHandler");
        GLES20.glActiveTexture(33986);
        GLES20.glBindTexture(3553, gMap4cap.getAlphaHandler());
        GLUtil.checkGlError("getAlphaHandler");
        GLES20.glDrawArrays(4, 0, 6);
        GLUtil.checkGlError("TopGraphicRenderer draw end");
        mIsCapture = 0;
        try
        {
            saveFrame(mPicWidth, mPicHeight, new File("/storage/sdcard0/picTop.jpg"));
        }
        catch(IOException ioexception)
        {
            Log.e("TopGraphicRenderer", "[saveImageToSDCard]Failed to write image,ex:", ioexception);
        }
        mIsCapture = 0;
        GLUtil.checkGlError("TopGraphicRenderer draw end");
    }

    public void draw__(int i, float af[], float af1[], boolean flag)
    {
        GLUtil.checkGlError("TopGraphicRenderer draw start");
        GLES20.glDisable(2929);
        GLES20.glEnable(3042);
        GLES20.glBlendFunc(1, 1);
        GLES20.glUseProgram(mProgram);
        GLES20.glVertexAttribPointer(maPositionHandle, 3, 5126, false, 12, mVertexBuffer);
        GLES20.glVertexAttribPointer(maTexCoordHandle, 2, 5126, false, 8, mTextureBuffer);
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        GLES20.glEnableVertexAttribArray(maTexCoordHandle);
        GLES20.glUniformMatrix4fv(muPosMtxHandle, 1, false, mPosMtx, 0);
        GLES20.glUniform1f(muIsPreviewHandle, 1.0F);
        GLES20.glUniform4f(mPickInfoHandle, mPickInfo[0], mPickInfo[1], mPickInfo[2], 0.0F);
        GLES20.glUniform1i(muPreviewSamplerHandle, 0);
        GLES20.glActiveTexture(33984);
        GLES20.glBindTexture(36197, i);
        if(!mIsPIPOrginal)
        {
            GLES20.glUniform1i(mPositionMapHandle, 1);
            GLES20.glUniform1i(mAlphaHandle, 2);
            GLES20.glActiveTexture(33985);
            GLES20.glBindTexture(3553, gMap.getLeftMapHandler());
            GLES20.glActiveTexture(33986);
            GLES20.glBindTexture(3553, gMap.getAlphaHandler());
        }
        GLES20.glDrawArrays(4, 0, 6);
        try
        {
            saveFrame(mWidth, mHeight, new File("/storage/sdcard0/prevTop.jpg"));
        }
        catch(IOException ioexception)
        {
            Log.e("TopGraphicRenderer", "[saveImageToSDCard]Failed to write image,ex:", ioexception);
        }
        GLUtil.checkGlError("TopGraphicRenderer draw end");
    }

    public FloatBuffer getVtxFloatBuffer()
    {
        return mVtxBuf;
    }

    public void initTemplateTexture(int i, int j)
    {
        Log.i("TopGraphicRenderer", "initTemplateTexture");
        if(mBackTempTexId > 0)
        {
            releaseBitmapTexture(mBackTempTexId);
            mBackTempTexId = -12345;
        }
        if(i > 0)
            try
            {
                mBackTempTexId = initBitmapTexture(i, true);
            }
            catch(IOException ioexception)
            {
                Log.e("TopGraphicRenderer", (new StringBuilder()).append("initBitmapTexture faile + ").append(ioexception).toString());
            }
        if(mTopTemplateRenderer != null && j > 0)
            mTopTemplateRenderer.updateTemplate(j);
    }

    public void release()
    {
        if(mBackTempTexId > 0)
        {
            releaseBitmapTexture(mBackTempTexId);
            mBackTempTexId = -12345;
        }
        if(mTopTemplateRenderer != null)
        {
            mTopTemplateRenderer.releaseResource();
            mTopTemplateRenderer = null;
        }
        if(gMap != null)
            gMap.release();
        if(gMap4cap != null)
        {
            gMap4cap.release();
            gMap4cap = null;
        }
    }

    public void saveFrame(int i, int j, File file)
        throws IOException
    {
        String s;
        int ai[];
        BufferedOutputStream bufferedoutputstream;
        if(mIsFrameSaved != 0 && mIsCapture != 1)
            return;
        mIsFrameSaved = 1;
        s = file.toString();
        ByteBuffer bytebuffer = ByteBuffer.allocateDirect(4 * (i * j));
        bytebuffer.order(ByteOrder.LITTLE_ENDIAN);
        GLES20.glReadPixels(0, 0, i, j, 6408, 5121, bytebuffer);
        bytebuffer.rewind();
        int k = i * j;
        ai = new int[k];
        bytebuffer.asIntBuffer().get(ai);
        for(int l = 0; l < k; l++)
        {
            int i1 = ai[l];
            ai[l] = 0xff00ff00 & i1 | (0xff0000 & i1) >> 16 | (i1 & 0xff) << 16;
        }

        bufferedoutputstream = null;
        try{
            bufferedoutputstream  = new BufferedOutputStream(new FileOutputStream(s));
            Bitmap bitmap = Bitmap.createBitmap(ai, i, j, Bitmap.Config.ARGB_8888);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, bufferedoutputstream);
            bitmap.recycle();
        } finally {
            if(bufferedoutputstream != null)
                bufferedoutputstream.close();
        }
        Log.d(TAG, "Saved " +i +"x" +j + " frame as '" +s + "'");        
    }

    public void setPickInfo(float af[])
    {
        mPickInfo[0] = af[0];
        mPickInfo[1] = af[1];
        mPickInfo[2] = af[2];
        Log.d(TAG, "mPickInfo: " + mPickInfo[0]+","+mPickInfo[1]+" ," + mPickInfo[2]);
    }

    public void setRendererSize(int i, int j)
    {
        Log.i("TopGraphicRenderer", (new StringBuilder()).append("setRendererSize width = ").append(i).append(" height = ").append(j).toString());
        if(i != getRendererWidth() || j != getRendererHeight())
        {
            mWidth = i;
            mHeight = j;
            resetMatrix();
            Matrix.orthoM(mPMtx, 0, 0.0F, i, 0.0F, j, -1F, 1.0F);
            Matrix.translateM(mMMtx, 0, 0.0F, j, 0.0F);
            Matrix.scaleM(mMMtx, 0, mMMtx, 0, 1.0F, -1F, 1.0F);
            Matrix.multiplyMM(mMVPMtx, 0, mMMtx, 0, mMVPMtx, 0);
            Matrix.multiplyMM(mMVPMtx, 0, mVMtx, 0, mMVPMtx, 0);
            Matrix.multiplyMM(mMVPMtx, 0, mPMtx, 0, mMVPMtx, 0);
            super.setRendererSize(i, j);
            mTopTemplateRenderer.setRendererSize(i, j);
        }
    }

    public void setRendererSize(int i, int j, boolean flag)
    {
        Log.i("TopGraphicRenderer", (new StringBuilder()).append("setRendererSize width = ").append(i).append(" height = ").append(j).append(" needReverse = ").append(flag).toString());
        resetMatrix();
        super.setRendererSize(i, j);
        Matrix.orthoM(mPMtx, 0, 0.0F, i, 0.0F, j, -1F, 1.0F);
        if(flag)
        {
            Matrix.translateM(mMMtx, 0, 0.0F, j, 0.0F);
            Matrix.scaleM(mMMtx, 0, mMMtx, 0, 1.0F, -1F, 1.0F);
        }
        Matrix.multiplyMM(mPosMtx, 0, mEditMtx, 0, mMMtx, 0);
        Matrix.multiplyMM(mPosMtx, 0, mVMtx, 0, mPosMtx, 0);
        Matrix.multiplyMM(mPosMtx, 0, mPMtx, 0, mPosMtx, 0);
        mVtxBuf = createFloatBuffer(mVtxBuf, GLUtil.createFullSquareVtx(i, j));
    }

    
}
