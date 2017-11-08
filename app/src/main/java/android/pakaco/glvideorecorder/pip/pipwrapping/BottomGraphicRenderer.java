

package android.pakaco.glvideorecorder.pip.pipwrapping;

import android.app.Activity;
import android.app.Service;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.util.Log;
//import com.bql.camera.GenerateMapV2;
//import com.bql.camera.GenerateMapV2Copy2;
//import com.bql.log.Logger;
import java.io.*;
import java.nio.*;


public class BottomGraphicRenderer extends Renderer
{
    private static int COORDS_PER_TEXTURE = 2;
    private static int COORDS_PER_VERTEX = 3;
    private static final String TAG = "BottomGraphicRenderer";
    final String fragmentShader;// = "#extension GL_OES_EGL_image_external : require\nprecision highp float;\nuniform sampler2D uPictureSampler;\nuniform sampler2D gPositionMap;\nuniform sampler2D gAlphaMap;\nuniform samplerExternalOES uPreviewSampler;\nuniform vec4 pickInfo;\nuniform float vfIsPreview;\nvarying vec2 vTexCoord;\nvoid main() {\n    vec2 preTexCoord;\n    float up0 = texture2D(gAlphaMap, vTexCoord.xy).x;\n    vec4 vTexCoord1 = texture2D(gPositionMap, vTexCoord.xy).xyzw;\n    float y = vTexCoord1.x + vTexCoord1.y / 255.0;\n    float x = vTexCoord1.z + vTexCoord1.w / 255.0;\n    if(vfIsPreview > 0.1) {\n        preTexCoord = vec2(1.0-y, x);\n        gl_FragColor = texture2D(uPreviewSampler, preTexCoord)* (1.0 - up0);/* new lut */\n        float k = abs(pickInfo.x-preTexCoord.x)+abs(pickInfo.y-preTexCoord.y);\n        if (pickInfo.z<0.5 && k<0.02){\n              gl_FragColor = vec4(1,0,0,1);\n        };\n    } else { \n        preTexCoord = vec2(y, x);\n        gl_FragColor = texture2D(uPreviewSampler, preTexCoord)* (1.0 - up0);/* new lut */\n    }\n}\n";
    final String fragmentShaderPic;//="#extension GL_OES_EGL_image_external : require\nprecision highp float;\nuniform samplerExternalOES gPictureTop;\nuniform samplerExternalOES gPictureBot;\nuniform sampler2D gTopMap;\nuniform sampler2D gBotMap;\nuniform sampler2D gAlphaMap;\nvarying vec2 vTexCoord;\nvoid main() {\n\tfloat alpha = texture2D(gAlphaMap, vTexCoord).x;\n\tvec4 vCoord1 = texture2D(gTopMap, vTexCoord).xyzw/vec4(1.0, 255.0, 1.0, 255.0);\n\tvec4 vCoord2 = texture2D(gBotMap, vTexCoord).xyzw/vec4(1.0, 255.0, 1.0, 255.0);\n\tvec2 uv1 = vec2(vCoord1.x+vCoord1.y, (vCoord1.z+vCoord1.w));\n\tvec2 uv2 = vec2(vCoord2.x+vCoord2.y, (vCoord2.z+vCoord2.w));\n    gl_FragColor = texture2D(gPictureTop, uv1)*alpha + texture2D(gPictureBot, uv2)*(1.0-alpha);\n}";
    final String fragmentShaderPreview;//="#extension GL_OES_EGL_image_external : require\nprecision highp float;\nuniform sampler2D gTopMap;\nuniform sampler2D gBotMap;\nuniform sampler2D gAlphaMap;\nuniform samplerExternalOES gPreviewTop;\nuniform samplerExternalOES gPreviewBot;\nuniform vec4 pickInfo;\nvarying vec2 vTexCoord;\nvoid main() {\n\t  float alpha = texture2D(gAlphaMap, vTexCoord).x;\n   vec2 uv1;\n   vec2 uv2;\n   if (alpha<0.05) {\n       vec4 vCoord2 = texture2D(gBotMap, vTexCoord).xyzw/vec4(1.0, 255.0, 1.0, 255.0);\n       uv1 = vec2(vCoord2.x+vCoord2.y, vCoord2.z+vCoord2.w);\n       gl_FragColor = texture2D(gPreviewBot, uv1);\n   } else if (alpha>0.95) { \n       vec4 vCoord2 = texture2D(gTopMap, vTexCoord).xyzw/vec4(1.0, 255.0, 1.0, 255.0);\n       uv2 = vec2(vCoord2.x+vCoord2.y, vCoord2.z+vCoord2.w);\n       gl_FragColor = texture2D(gPreviewTop, uv2);\n   } else {\n\t     vec4 vCoord1 = texture2D(gTopMap, vTexCoord).xyzw/vec4(1.0, 255.0, 1.0, 255.0);\n\t     vec4 vCoord2 = texture2D(gBotMap, vTexCoord).xyzw/vec4(1.0, 255.0, 1.0, 255.0);\n\t     uv1 = vec2(vCoord1.x+vCoord1.y,  (vCoord1.z+vCoord1.w));\n\t     uv2 = vec2(vCoord2.x+vCoord2.y, (vCoord2.z+vCoord2.w));\n      gl_FragColor = texture2D(gPreviewTop, uv1)*alpha + texture2D(gPreviewBot, uv2)*(1.0-alpha);\n   }\n}";
    private GenerateMapV2 gMap;
    private GenerateMapV2Copy2 gMap4cap;
    private int mAlphaHandle;
    private int mAlphaHandle2;
    private int mBotMapHandle;
    private int mBotMapHandle2;
    private float mEditMtx[];
    private int mHeight;
    private int mIsCapture;
    private int mIsFrameSaved;
    private boolean mIsPIPOrginal;
    private float mMMtx[];
    private float mPMtx[];
    private int mPicHeight;
    private int mPicWidth;
    float mPickInfo[];
    private int mPickInfoHandle;
    private int mPictureBotHandle;
    private int mPictureTopHandle;
    private float mPosMtx[];
    private int mPreviewBotHandle;
    private int mPreviewTopHandle;
    private int mProgram;
    private int mProgram2;
    private int mProgram3;
    private FloatBuffer mTextureBuffer;
    private int mTopMapHandle;
    private int mTopMapHandle2;
    private float mVMtx[];
    private FloatBuffer mVertexBuffer;
    private int mWidth;
    private int maPositionHandle;
    private int maPositionHandle2;
    private int maPositionHandle3;
    private int maTexCoordHandle;
    private int maTexCoordHandle2;
    private int maTexCoordHandle3;
    private boolean misPictureMode;
    private int pluginTextureID[];
    private int textureStride;
    final String vertexShaderPic;//="attribute vec3    aPosition;\nattribute vec2    aTexCoord;\nvarying   vec2    vTexCoord;\nvoid main() {\n    gl_Position   = vec4(aPosition, 1.0);\n    vTexCoord     = vec2(aTexCoord.x, aTexCoord.y);\n}\n";
    final String vertexShaderPreview;//="attribute vec3    aPosition;\nattribute vec2    aTexCoord;\nvarying   vec2    vTexCoord;\nvoid main() {\n\t gl_Position   = vec4(aPosition, 1.0);\n\t vTexCoord\t   = vec2(aTexCoord.x, aTexCoord.y);\n}\n";
    private int vertexStride;


    public BottomGraphicRenderer(Activity activity, boolean flag)
    {
        super(activity);
        vertexStride = 4 * COORDS_PER_VERTEX;
        textureStride = 4 * COORDS_PER_TEXTURE;
        mPosMtx = GLUtil.createIdentityMtx();
        mPMtx = GLUtil.createIdentityMtx();
        mVMtx = GLUtil.createIdentityMtx();
        mMMtx = GLUtil.createIdentityMtx();
        mEditMtx = GLUtil.createIdentityMtx();
        mProgram = -1;
        maPositionHandle = -1;
        maTexCoordHandle = -1;
        mTopMapHandle = -1;
        mBotMapHandle = -1;
        mAlphaHandle = -1;
        mProgram2 = -1;
        maPositionHandle2 = -1;
        maTexCoordHandle2 = -1;
        mTopMapHandle2 = -1;
        mBotMapHandle2 = -1;
        mAlphaHandle2 = -1;
        mProgram3 = -1;
        maPositionHandle3 = -1;
        maTexCoordHandle3 = -1;
        pluginTextureID = new int[1];
        mPreviewTopHandle = -1;
        mPreviewBotHandle = -1;
        mPictureTopHandle = -1;
        mPictureBotHandle = -1;
        mPickInfoHandle = -1;
        mWidth = 1280;
        mHeight = 960;
        mPicWidth = 4224;
        mPicHeight = 3268;
        mIsCapture = 0;
        mIsFrameSaved = 1;
        mPickInfo = new float[3];
        misPictureMode = true;
        vertexShaderPic = "attribute vec3    aPosition;\nattribute vec2    aTexCoord;\nvarying   vec2    vTexCoord;\nvoid main() {\n    gl_Position   = vec4(aPosition, 1.0);\n    vTexCoord     = vec2(aTexCoord.x, aTexCoord.y);\n}\n";
        fragmentShaderPic = "#extension GL_OES_EGL_image_external : require\nprecision highp float;\nuniform samplerExternalOES gPictureTop;\nuniform samplerExternalOES gPictureBot;\nuniform sampler2D gTopMap;\nuniform sampler2D gBotMap;\nuniform sampler2D gAlphaMap;\nvarying vec2 vTexCoord;\nvoid main() {\n\tfloat alpha = texture2D(gAlphaMap, vTexCoord).x;\n\tvec4 vCoord1 = texture2D(gTopMap, vTexCoord).xyzw/vec4(1.0, 255.0, 1.0, 255.0);\n\tvec4 vCoord2 = texture2D(gBotMap, vTexCoord).xyzw/vec4(1.0, 255.0, 1.0, 255.0);\n\tvec2 uv1 = vec2(vCoord1.x+vCoord1.y, (vCoord1.z+vCoord1.w));\n\tvec2 uv2 = vec2(vCoord2.x+vCoord2.y, (vCoord2.z+vCoord2.w));\n    gl_FragColor = texture2D(gPictureTop, uv1)*alpha + texture2D(gPictureBot, uv2)*(1.0-alpha);\n}";
        vertexShaderPreview = "attribute vec3    aPosition;\nattribute vec2    aTexCoord;\nvarying   vec2    vTexCoord;\nvoid main() {\n\t gl_Position   = vec4(aPosition, 1.0);\n\t vTexCoord\t   = vec2(aTexCoord.x, aTexCoord.y);\n}\n";
        fragmentShaderPreview = "#extension GL_OES_EGL_image_external : require\nprecision highp float;\nuniform sampler2D gTopMap;\nuniform sampler2D gBotMap;\nuniform sampler2D gAlphaMap;\nuniform samplerExternalOES gPreviewTop;\nuniform samplerExternalOES gPreviewBot;\nuniform vec4 pickInfo;\nvarying vec2 vTexCoord;\nvoid main() {\n\t  float alpha = texture2D(gAlphaMap, vTexCoord).x;\n   vec2 uv1;\n   vec2 uv2;\n   if (alpha<0.05) {\n       vec4 vCoord2 = texture2D(gBotMap, vTexCoord).xyzw/vec4(1.0, 255.0, 1.0, 255.0);\n       uv1 = vec2(vCoord2.x+vCoord2.y, vCoord2.z+vCoord2.w);\n       gl_FragColor = texture2D(gPreviewBot, uv1);\n   } else if (alpha>0.95) { \n       vec4 vCoord2 = texture2D(gTopMap, vTexCoord).xyzw/vec4(1.0, 255.0, 1.0, 255.0);\n       uv2 = vec2(vCoord2.x+vCoord2.y, vCoord2.z+vCoord2.w);\n       gl_FragColor = texture2D(gPreviewTop, uv2);\n   } else {\n\t     vec4 vCoord1 = texture2D(gTopMap, vTexCoord).xyzw/vec4(1.0, 255.0, 1.0, 255.0);\n\t     vec4 vCoord2 = texture2D(gBotMap, vTexCoord).xyzw/vec4(1.0, 255.0, 1.0, 255.0);\n\t     uv1 = vec2(vCoord1.x+vCoord1.y,  (vCoord1.z+vCoord1.w));\n\t     uv2 = vec2(vCoord2.x+vCoord2.y, (vCoord2.z+vCoord2.w));\n      gl_FragColor = texture2D(gPreviewTop, uv1)*alpha + texture2D(gPreviewBot, uv2)*(1.0-alpha);\n   }\n}";
        fragmentShader = "#extension GL_OES_EGL_image_external : require\nprecision highp float;\nuniform sampler2D uPictureSampler;\nuniform sampler2D gPositionMap;\nuniform sampler2D gAlphaMap;\nuniform samplerExternalOES uPreviewSampler;\nuniform vec4 pickInfo;\nuniform float vfIsPreview;\nvarying vec2 vTexCoord;\nvoid main() {\n    vec2 preTexCoord;\n    float up0 = texture2D(gAlphaMap, vTexCoord.xy).x;\n    vec4 vTexCoord1 = texture2D(gPositionMap, vTexCoord.xy).xyzw;\n    float y = vTexCoord1.x + vTexCoord1.y / 255.0;\n    float x = vTexCoord1.z + vTexCoord1.w / 255.0;\n    if(vfIsPreview > 0.1) {\n        preTexCoord = vec2(1.0-y, x);\n        gl_FragColor = texture2D(uPreviewSampler, preTexCoord)* (1.0 - up0);/* new lut */\n        float k = abs(pickInfo.x-preTexCoord.x)+abs(pickInfo.y-preTexCoord.y);\n        if (pickInfo.z<0.5 && k<0.02){\n              gl_FragColor = vec4(1,0,0,1);\n        };\n    } else { \n        preTexCoord = vec2(y, x);\n        gl_FragColor = texture2D(uPreviewSampler, preTexCoord)* (1.0 - up0);/* new lut */\n    }\n}\n";
        mIsPIPOrginal = false;
        mIsPIPOrginal = flag;
        initProgram();
        gMap = GenerateMapV2.getInstance(activity);
    }

    public BottomGraphicRenderer(Activity activity, boolean flag, boolean flag1)
    {
        super(activity);
        vertexStride = 4 * COORDS_PER_VERTEX;
        textureStride = 4 * COORDS_PER_TEXTURE;
        mPosMtx = GLUtil.createIdentityMtx();
        mPMtx = GLUtil.createIdentityMtx();
        mVMtx = GLUtil.createIdentityMtx();
        mMMtx = GLUtil.createIdentityMtx();
        mEditMtx = GLUtil.createIdentityMtx();
        mProgram = -1;
        maPositionHandle = -1;
        maTexCoordHandle = -1;
        mTopMapHandle = -1;
        mBotMapHandle = -1;
        mAlphaHandle = -1;
        mProgram2 = -1;
        maPositionHandle2 = -1;
        maTexCoordHandle2 = -1;
        mTopMapHandle2 = -1;
        mBotMapHandle2 = -1;
        mAlphaHandle2 = -1;
        mProgram3 = -1;
        maPositionHandle3 = -1;
        maTexCoordHandle3 = -1;
        pluginTextureID = new int[1];
        mPreviewTopHandle = -1;
        mPreviewBotHandle = -1;
        mPictureTopHandle = -1;
        mPictureBotHandle = -1;
        mPickInfoHandle = -1;
        mWidth = 1280;
        mHeight = 960;
        mPicWidth = 4224;
        mPicHeight = 3268;
        mIsCapture = 0;
        mIsFrameSaved = 1;
        mPickInfo = new float[3];
        misPictureMode = true;
        vertexShaderPic = "attribute vec3    aPosition;\nattribute vec2    aTexCoord;\nvarying   vec2    vTexCoord;\nvoid main() {\n    gl_Position   = vec4(aPosition, 1.0);\n    vTexCoord     = vec2(aTexCoord.x, aTexCoord.y);\n}\n";
        fragmentShaderPic = "#extension GL_OES_EGL_image_external : require\nprecision highp float;\nuniform samplerExternalOES gPictureTop;\nuniform samplerExternalOES gPictureBot;\nuniform sampler2D gTopMap;\nuniform sampler2D gBotMap;\nuniform sampler2D gAlphaMap;\nvarying vec2 vTexCoord;\nvoid main() {\n\tfloat alpha = texture2D(gAlphaMap, vTexCoord).x;\n\tvec4 vCoord1 = texture2D(gTopMap, vTexCoord).xyzw/vec4(1.0, 255.0, 1.0, 255.0);\n\tvec4 vCoord2 = texture2D(gBotMap, vTexCoord).xyzw/vec4(1.0, 255.0, 1.0, 255.0);\n\tvec2 uv1 = vec2(vCoord1.x+vCoord1.y, (vCoord1.z+vCoord1.w));\n\tvec2 uv2 = vec2(vCoord2.x+vCoord2.y, (vCoord2.z+vCoord2.w));\n    gl_FragColor = texture2D(gPictureTop, uv1)*alpha + texture2D(gPictureBot, uv2)*(1.0-alpha);\n}";
        vertexShaderPreview = "attribute vec3    aPosition;\nattribute vec2    aTexCoord;\nvarying   vec2    vTexCoord;\nvoid main() {\n\t gl_Position   = vec4(aPosition, 1.0);\n\t vTexCoord\t   = vec2(aTexCoord.x, aTexCoord.y);\n}\n";
        fragmentShaderPreview = "#extension GL_OES_EGL_image_external : require\nprecision highp float;\nuniform sampler2D gTopMap;\nuniform sampler2D gBotMap;\nuniform sampler2D gAlphaMap;\nuniform samplerExternalOES gPreviewTop;\nuniform samplerExternalOES gPreviewBot;\nuniform vec4 pickInfo;\nvarying vec2 vTexCoord;\nvoid main() {\n\t  float alpha = texture2D(gAlphaMap, vTexCoord).x;\n   vec2 uv1;\n   vec2 uv2;\n   if (alpha<0.05) {\n       vec4 vCoord2 = texture2D(gBotMap, vTexCoord).xyzw/vec4(1.0, 255.0, 1.0, 255.0);\n       uv1 = vec2(vCoord2.x+vCoord2.y, vCoord2.z+vCoord2.w);\n       gl_FragColor = texture2D(gPreviewBot, uv1);\n   } else if (alpha>0.95) { \n       vec4 vCoord2 = texture2D(gTopMap, vTexCoord).xyzw/vec4(1.0, 255.0, 1.0, 255.0);\n       uv2 = vec2(vCoord2.x+vCoord2.y, vCoord2.z+vCoord2.w);\n       gl_FragColor = texture2D(gPreviewTop, uv2);\n   } else {\n\t     vec4 vCoord1 = texture2D(gTopMap, vTexCoord).xyzw/vec4(1.0, 255.0, 1.0, 255.0);\n\t     vec4 vCoord2 = texture2D(gBotMap, vTexCoord).xyzw/vec4(1.0, 255.0, 1.0, 255.0);\n\t     uv1 = vec2(vCoord1.x+vCoord1.y,  (vCoord1.z+vCoord1.w));\n\t     uv2 = vec2(vCoord2.x+vCoord2.y, (vCoord2.z+vCoord2.w));\n      gl_FragColor = texture2D(gPreviewTop, uv1)*alpha + texture2D(gPreviewBot, uv2)*(1.0-alpha);\n   }\n}";
        fragmentShader = "#extension GL_OES_EGL_image_external : require\nprecision highp float;\nuniform sampler2D uPictureSampler;\nuniform sampler2D gPositionMap;\nuniform sampler2D gAlphaMap;\nuniform samplerExternalOES uPreviewSampler;\nuniform vec4 pickInfo;\nuniform float vfIsPreview;\nvarying vec2 vTexCoord;\nvoid main() {\n    vec2 preTexCoord;\n    float up0 = texture2D(gAlphaMap, vTexCoord.xy).x;\n    vec4 vTexCoord1 = texture2D(gPositionMap, vTexCoord.xy).xyzw;\n    float y = vTexCoord1.x + vTexCoord1.y / 255.0;\n    float x = vTexCoord1.z + vTexCoord1.w / 255.0;\n    if(vfIsPreview > 0.1) {\n        preTexCoord = vec2(1.0-y, x);\n        gl_FragColor = texture2D(uPreviewSampler, preTexCoord)* (1.0 - up0);/* new lut */\n        float k = abs(pickInfo.x-preTexCoord.x)+abs(pickInfo.y-preTexCoord.y);\n        if (pickInfo.z<0.5 && k<0.02){\n              gl_FragColor = vec4(1,0,0,1);\n        };\n    } else { \n        preTexCoord = vec2(y, x);\n        gl_FragColor = texture2D(uPreviewSampler, preTexCoord)* (1.0 - up0);/* new lut */\n    }\n}\n";
        mIsPIPOrginal = false;
        mIsPIPOrginal = flag;
        initProgram();
        gMap4cap = GenerateMapV2Copy2.getInstance(activity);
    }

    public BottomGraphicRenderer(Service service, boolean flag)
    {
        super(service);
        vertexStride = 4 * COORDS_PER_VERTEX;
        textureStride = 4 * COORDS_PER_TEXTURE;
        mPosMtx = GLUtil.createIdentityMtx();
        mPMtx = GLUtil.createIdentityMtx();
        mVMtx = GLUtil.createIdentityMtx();
        mMMtx = GLUtil.createIdentityMtx();
        mEditMtx = GLUtil.createIdentityMtx();
        mProgram = -1;
        maPositionHandle = -1;
        maTexCoordHandle = -1;
        mTopMapHandle = -1;
        mBotMapHandle = -1;
        mAlphaHandle = -1;
        mProgram2 = -1;
        maPositionHandle2 = -1;
        maTexCoordHandle2 = -1;
        mTopMapHandle2 = -1;
        mBotMapHandle2 = -1;
        mAlphaHandle2 = -1;
        mProgram3 = -1;
        maPositionHandle3 = -1;
        maTexCoordHandle3 = -1;
        pluginTextureID = new int[1];
        mPreviewTopHandle = -1;
        mPreviewBotHandle = -1;
        mPictureTopHandle = -1;
        mPictureBotHandle = -1;
        mPickInfoHandle = -1;
        mWidth = 1280;
        mHeight = 960;
        mPicWidth = 4224;
        mPicHeight = 3268;
        mIsCapture = 0;
        mIsFrameSaved = 1;
        mPickInfo = new float[3];
        misPictureMode = true;
        vertexShaderPic = "attribute vec3    aPosition;\nattribute vec2    aTexCoord;\nvarying   vec2    vTexCoord;\nvoid main() {\n    gl_Position   = vec4(aPosition, 1.0);\n    vTexCoord     = vec2(aTexCoord.x, aTexCoord.y);\n}\n";
        fragmentShaderPic = "#extension GL_OES_EGL_image_external : require\nprecision highp float;\nuniform samplerExternalOES gPictureTop;\nuniform samplerExternalOES gPictureBot;\nuniform sampler2D gTopMap;\nuniform sampler2D gBotMap;\nuniform sampler2D gAlphaMap;\nvarying vec2 vTexCoord;\nvoid main() {\n\tfloat alpha = texture2D(gAlphaMap, vTexCoord).x;\n\tvec4 vCoord1 = texture2D(gTopMap, vTexCoord).xyzw/vec4(1.0, 255.0, 1.0, 255.0);\n\tvec4 vCoord2 = texture2D(gBotMap, vTexCoord).xyzw/vec4(1.0, 255.0, 1.0, 255.0);\n\tvec2 uv1 = vec2(vCoord1.x+vCoord1.y, (vCoord1.z+vCoord1.w));\n\tvec2 uv2 = vec2(vCoord2.x+vCoord2.y, (vCoord2.z+vCoord2.w));\n    gl_FragColor = texture2D(gPictureTop, uv1)*alpha + texture2D(gPictureBot, uv2)*(1.0-alpha);\n}";
        vertexShaderPreview = "attribute vec3    aPosition;\nattribute vec2    aTexCoord;\nvarying   vec2    vTexCoord;\nvoid main() {\n\t gl_Position   = vec4(aPosition, 1.0);\n\t vTexCoord\t   = vec2(aTexCoord.x, aTexCoord.y);\n}\n";
        fragmentShaderPreview = "#extension GL_OES_EGL_image_external : require\nprecision highp float;\nuniform sampler2D gTopMap;\nuniform sampler2D gBotMap;\nuniform sampler2D gAlphaMap;\nuniform samplerExternalOES gPreviewTop;\nuniform samplerExternalOES gPreviewBot;\nuniform vec4 pickInfo;\nvarying vec2 vTexCoord;\nvoid main() {\n\t  float alpha = texture2D(gAlphaMap, vTexCoord).x;\n   vec2 uv1;\n   vec2 uv2;\n   if (alpha<0.05) {\n       vec4 vCoord2 = texture2D(gBotMap, vTexCoord).xyzw/vec4(1.0, 255.0, 1.0, 255.0);\n       uv1 = vec2(vCoord2.x+vCoord2.y, vCoord2.z+vCoord2.w);\n       gl_FragColor = texture2D(gPreviewBot, uv1);\n   } else if (alpha>0.95) { \n       vec4 vCoord2 = texture2D(gTopMap, vTexCoord).xyzw/vec4(1.0, 255.0, 1.0, 255.0);\n       uv2 = vec2(vCoord2.x+vCoord2.y, vCoord2.z+vCoord2.w);\n       gl_FragColor = texture2D(gPreviewTop, uv2);\n   } else {\n\t     vec4 vCoord1 = texture2D(gTopMap, vTexCoord).xyzw/vec4(1.0, 255.0, 1.0, 255.0);\n\t     vec4 vCoord2 = texture2D(gBotMap, vTexCoord).xyzw/vec4(1.0, 255.0, 1.0, 255.0);\n\t     uv1 = vec2(vCoord1.x+vCoord1.y,  (vCoord1.z+vCoord1.w));\n\t     uv2 = vec2(vCoord2.x+vCoord2.y, (vCoord2.z+vCoord2.w));\n      gl_FragColor = texture2D(gPreviewTop, uv1)*alpha + texture2D(gPreviewBot, uv2)*(1.0-alpha);\n   }\n}";
        fragmentShader = "#extension GL_OES_EGL_image_external : require\nprecision highp float;\nuniform sampler2D uPictureSampler;\nuniform sampler2D gPositionMap;\nuniform sampler2D gAlphaMap;\nuniform samplerExternalOES uPreviewSampler;\nuniform vec4 pickInfo;\nuniform float vfIsPreview;\nvarying vec2 vTexCoord;\nvoid main() {\n    vec2 preTexCoord;\n    float up0 = texture2D(gAlphaMap, vTexCoord.xy).x;\n    vec4 vTexCoord1 = texture2D(gPositionMap, vTexCoord.xy).xyzw;\n    float y = vTexCoord1.x + vTexCoord1.y / 255.0;\n    float x = vTexCoord1.z + vTexCoord1.w / 255.0;\n    if(vfIsPreview > 0.1) {\n        preTexCoord = vec2(1.0-y, x);\n        gl_FragColor = texture2D(uPreviewSampler, preTexCoord)* (1.0 - up0);/* new lut */\n        float k = abs(pickInfo.x-preTexCoord.x)+abs(pickInfo.y-preTexCoord.y);\n        if (pickInfo.z<0.5 && k<0.02){\n              gl_FragColor = vec4(1,0,0,1);\n        };\n    } else { \n        preTexCoord = vec2(y, x);\n        gl_FragColor = texture2D(uPreviewSampler, preTexCoord)* (1.0 - up0);/* new lut */\n    }\n}\n";
        mIsPIPOrginal = false;
        mIsPIPOrginal = flag;
        initProgram();
        gMap = GenerateMapV2.getInstance(service);
    }

    public BottomGraphicRenderer(Service service, boolean flag, boolean flag1)
    {
        super(service);
        vertexStride = 4 * COORDS_PER_VERTEX;
        textureStride = 4 * COORDS_PER_TEXTURE;
        mPosMtx = GLUtil.createIdentityMtx();
        mPMtx = GLUtil.createIdentityMtx();
        mVMtx = GLUtil.createIdentityMtx();
        mMMtx = GLUtil.createIdentityMtx();
        mEditMtx = GLUtil.createIdentityMtx();
        mProgram = -1;
        maPositionHandle = -1;
        maTexCoordHandle = -1;
        mTopMapHandle = -1;
        mBotMapHandle = -1;
        mAlphaHandle = -1;
        mProgram2 = -1;
        maPositionHandle2 = -1;
        maTexCoordHandle2 = -1;
        mTopMapHandle2 = -1;
        mBotMapHandle2 = -1;
        mAlphaHandle2 = -1;
        mProgram3 = -1;
        maPositionHandle3 = -1;
        maTexCoordHandle3 = -1;
        pluginTextureID = new int[1];
        mPreviewTopHandle = -1;
        mPreviewBotHandle = -1;
        mPictureTopHandle = -1;
        mPictureBotHandle = -1;
        mPickInfoHandle = -1;
        mWidth = 1280;
        mHeight = 960;
        mPicWidth = 4224;
        mPicHeight = 3268;
        mIsCapture = 0;
        mIsFrameSaved = 1;
        mPickInfo = new float[3];
        misPictureMode = true;
        vertexShaderPic = "attribute vec3    aPosition;\nattribute vec2    aTexCoord;\nvarying   vec2    vTexCoord;\nvoid main() {\n    gl_Position   = vec4(aPosition, 1.0);\n    vTexCoord     = vec2(aTexCoord.x, aTexCoord.y);\n}\n";
        fragmentShaderPic = "#extension GL_OES_EGL_image_external : require\nprecision highp float;\nuniform samplerExternalOES gPictureTop;\nuniform samplerExternalOES gPictureBot;\nuniform sampler2D gTopMap;\nuniform sampler2D gBotMap;\nuniform sampler2D gAlphaMap;\nvarying vec2 vTexCoord;\nvoid main() {\n\tfloat alpha = texture2D(gAlphaMap, vTexCoord).x;\n\tvec4 vCoord1 = texture2D(gTopMap, vTexCoord).xyzw/vec4(1.0, 255.0, 1.0, 255.0);\n\tvec4 vCoord2 = texture2D(gBotMap, vTexCoord).xyzw/vec4(1.0, 255.0, 1.0, 255.0);\n\tvec2 uv1 = vec2(vCoord1.x+vCoord1.y, (vCoord1.z+vCoord1.w));\n\tvec2 uv2 = vec2(vCoord2.x+vCoord2.y, (vCoord2.z+vCoord2.w));\n    gl_FragColor = texture2D(gPictureTop, uv1)*alpha + texture2D(gPictureBot, uv2)*(1.0-alpha);\n}";
        vertexShaderPreview = "attribute vec3    aPosition;\nattribute vec2    aTexCoord;\nvarying   vec2    vTexCoord;\nvoid main() {\n\t gl_Position   = vec4(aPosition, 1.0);\n\t vTexCoord\t   = vec2(aTexCoord.x, aTexCoord.y);\n}\n";
        fragmentShaderPreview = "#extension GL_OES_EGL_image_external : require\nprecision highp float;\nuniform sampler2D gTopMap;\nuniform sampler2D gBotMap;\nuniform sampler2D gAlphaMap;\nuniform samplerExternalOES gPreviewTop;\nuniform samplerExternalOES gPreviewBot;\nuniform vec4 pickInfo;\nvarying vec2 vTexCoord;\nvoid main() {\n\t  float alpha = texture2D(gAlphaMap, vTexCoord).x;\n   vec2 uv1;\n   vec2 uv2;\n   if (alpha<0.05) {\n       vec4 vCoord2 = texture2D(gBotMap, vTexCoord).xyzw/vec4(1.0, 255.0, 1.0, 255.0);\n       uv1 = vec2(vCoord2.x+vCoord2.y, vCoord2.z+vCoord2.w);\n       gl_FragColor = texture2D(gPreviewBot, uv1);\n   } else if (alpha>0.95) { \n       vec4 vCoord2 = texture2D(gTopMap, vTexCoord).xyzw/vec4(1.0, 255.0, 1.0, 255.0);\n       uv2 = vec2(vCoord2.x+vCoord2.y, vCoord2.z+vCoord2.w);\n       gl_FragColor = texture2D(gPreviewTop, uv2);\n   } else {\n\t     vec4 vCoord1 = texture2D(gTopMap, vTexCoord).xyzw/vec4(1.0, 255.0, 1.0, 255.0);\n\t     vec4 vCoord2 = texture2D(gBotMap, vTexCoord).xyzw/vec4(1.0, 255.0, 1.0, 255.0);\n\t     uv1 = vec2(vCoord1.x+vCoord1.y,  (vCoord1.z+vCoord1.w));\n\t     uv2 = vec2(vCoord2.x+vCoord2.y, (vCoord2.z+vCoord2.w));\n      gl_FragColor = texture2D(gPreviewTop, uv1)*alpha + texture2D(gPreviewBot, uv2)*(1.0-alpha);\n   }\n}";
        fragmentShader = "#extension GL_OES_EGL_image_external : require\nprecision highp float;\nuniform sampler2D uPictureSampler;\nuniform sampler2D gPositionMap;\nuniform sampler2D gAlphaMap;\nuniform samplerExternalOES uPreviewSampler;\nuniform vec4 pickInfo;\nuniform float vfIsPreview;\nvarying vec2 vTexCoord;\nvoid main() {\n    vec2 preTexCoord;\n    float up0 = texture2D(gAlphaMap, vTexCoord.xy).x;\n    vec4 vTexCoord1 = texture2D(gPositionMap, vTexCoord.xy).xyzw;\n    float y = vTexCoord1.x + vTexCoord1.y / 255.0;\n    float x = vTexCoord1.z + vTexCoord1.w / 255.0;\n    if(vfIsPreview > 0.1) {\n        preTexCoord = vec2(1.0-y, x);\n        gl_FragColor = texture2D(uPreviewSampler, preTexCoord)* (1.0 - up0);/* new lut */\n        float k = abs(pickInfo.x-preTexCoord.x)+abs(pickInfo.y-preTexCoord.y);\n        if (pickInfo.z<0.5 && k<0.02){\n              gl_FragColor = vec4(1,0,0,1);\n        };\n    } else { \n        preTexCoord = vec2(y, x);\n        gl_FragColor = texture2D(uPreviewSampler, preTexCoord)* (1.0 - up0);/* new lut */\n    }\n}\n";
        mIsPIPOrginal = false;
        mIsPIPOrginal = flag;
        initProgram();
        gMap4cap = GenerateMapV2Copy2.getInstance(service);
    }

    private void bindPicTextures(int i, int j)
    {
        GLES20.glVertexAttribPointer(maPositionHandle2, 3, 5126, false, 12, mVertexBuffer);
        GLES20.glVertexAttribPointer(maTexCoordHandle2, 2, 5126, false, 8, mTextureBuffer);
        GLES20.glEnableVertexAttribArray(maPositionHandle2);
        GLES20.glEnableVertexAttribArray(maTexCoordHandle2);
        GLES20.glUniform1i(mPictureTopHandle, 0);
        GLES20.glUniform1i(mPictureBotHandle, 1);
        GLES20.glActiveTexture(33984);
        GLES20.glBindTexture(36197, i);
        GLES20.glActiveTexture(33985);
        GLES20.glBindTexture(36197, j);
        GLES20.glUniform1i(mTopMapHandle2, 2);
        GLES20.glUniform1i(mBotMapHandle2, 3);
        GLES20.glUniform1i(mAlphaHandle2, 4);
        GLES20.glActiveTexture(33986);
        GLES20.glBindTexture(3553, gMap4cap.getLeftMapHandler());
        GLES20.glActiveTexture(33987);
        GLES20.glBindTexture(3553, gMap4cap.getRightMapHandler());
        GLES20.glActiveTexture(33988);
        GLES20.glBindTexture(3553, gMap4cap.getAlphaHandler());
    }

    private void bindPreviewTextures(int i, int j)
    {
        GLES20.glVertexAttribPointer(maPositionHandle, 3, 5126, false, 12, mVertexBuffer);
        GLES20.glVertexAttribPointer(maTexCoordHandle, 2, 5126, false, 8, mTextureBuffer);
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        GLES20.glEnableVertexAttribArray(maTexCoordHandle);
        GLES20.glUniform1i(mPreviewTopHandle, 0);
        GLES20.glUniform1i(mPreviewBotHandle, 1);
        GLES20.glActiveTexture(33984);
        GLES20.glBindTexture(36197, i);
        GLES20.glActiveTexture(33985);
        GLES20.glBindTexture(36197, j);
        GLES20.glUniform1i(mTopMapHandle, 2);
        GLES20.glUniform1i(mBotMapHandle, 3);
        GLES20.glUniform1i(mAlphaHandle, 4);
        GLES20.glActiveTexture(33986);
        GLES20.glBindTexture(3553, gMap.getLeftMapHandler());
        GLES20.glActiveTexture(33987);
        GLES20.glBindTexture(3553, gMap.getRightMapHandler());
        GLES20.glActiveTexture(33988);
        GLES20.glBindTexture(3553, gMap.getAlphaHandler());
    }

    public static void checkGlError(String s)
    {
        int i = GLES20.glGetError();
        if(i != 0)
        {
            Log.i(TAG, (new StringBuilder()).append(s).append(":glGetError:0x").append(Integer.toHexString(i)).toString());
            throw new RuntimeException("glGetError encountered (see log)");
        } else
        {
            return;
        }
    }

    private void initProgram()
    {
        Log.i(TAG, "initProgram");
        mProgram = createProgram("attribute vec3    aPosition;\nattribute vec2    aTexCoord;\nvarying   vec2    vTexCoord;\nvoid main() {\n\t gl_Position   = vec4(aPosition, 1.0);\n\t vTexCoord\t   = vec2(aTexCoord.x, aTexCoord.y);\n}\n", "#extension GL_OES_EGL_image_external : require\nprecision highp float;\nuniform sampler2D gTopMap;\nuniform sampler2D gBotMap;\nuniform sampler2D gAlphaMap;\nuniform samplerExternalOES gPreviewTop;\nuniform samplerExternalOES gPreviewBot;\nuniform vec4 pickInfo;\nvarying vec2 vTexCoord;\nvoid main() {\n\t  float alpha = texture2D(gAlphaMap, vTexCoord).x;\n   vec2 uv1;\n   vec2 uv2;\n   if (alpha<0.05) {\n       vec4 vCoord2 = texture2D(gBotMap, vTexCoord).xyzw/vec4(1.0, 255.0, 1.0, 255.0);\n       uv1 = vec2(vCoord2.x+vCoord2.y, vCoord2.z+vCoord2.w);\n       gl_FragColor = texture2D(gPreviewBot, uv1);\n   } else if (alpha>0.95) { \n       vec4 vCoord2 = texture2D(gTopMap, vTexCoord).xyzw/vec4(1.0, 255.0, 1.0, 255.0);\n       uv2 = vec2(vCoord2.x+vCoord2.y, vCoord2.z+vCoord2.w);\n       gl_FragColor = texture2D(gPreviewTop, uv2);\n   } else {\n\t     vec4 vCoord1 = texture2D(gTopMap, vTexCoord).xyzw/vec4(1.0, 255.0, 1.0, 255.0);\n\t     vec4 vCoord2 = texture2D(gBotMap, vTexCoord).xyzw/vec4(1.0, 255.0, 1.0, 255.0);\n\t     uv1 = vec2(vCoord1.x+vCoord1.y,  (vCoord1.z+vCoord1.w));\n\t     uv2 = vec2(vCoord2.x+vCoord2.y, (vCoord2.z+vCoord2.w));\n      gl_FragColor = texture2D(gPreviewTop, uv1)*alpha + texture2D(gPreviewBot, uv2)*(1.0-alpha);\n   }\n}");
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        maTexCoordHandle = GLES20.glGetAttribLocation(mProgram, "aTexCoord");
        mPreviewTopHandle = GLES20.glGetUniformLocation(mProgram, "gPreviewTop");
        mPreviewBotHandle = GLES20.glGetUniformLocation(mProgram, "gPreviewBot");
        mTopMapHandle = GLES20.glGetUniformLocation(mProgram, "gTopMap");
        mBotMapHandle = GLES20.glGetUniformLocation(mProgram, "gBotMap");
        mAlphaHandle = GLES20.glGetUniformLocation(mProgram, "gAlphaMap");
        mPickInfoHandle = GLES20.glGetUniformLocation(mProgram, "pickInfo");
        GLUtil.checkGlError("BottomGraphicRenderer after mProgram");
        mProgram2 = createProgram("attribute vec3    aPosition;\nattribute vec2    aTexCoord;\nvarying   vec2    vTexCoord;\nvoid main() {\n    gl_Position   = vec4(aPosition, 1.0);\n    vTexCoord     = vec2(aTexCoord.x, aTexCoord.y);\n}\n", "#extension GL_OES_EGL_image_external : require\nprecision highp float;\nuniform samplerExternalOES gPictureTop;\nuniform samplerExternalOES gPictureBot;\nuniform sampler2D gTopMap;\nuniform sampler2D gBotMap;\nuniform sampler2D gAlphaMap;\nvarying vec2 vTexCoord;\nvoid main() {\n\tfloat alpha = texture2D(gAlphaMap, vTexCoord).x;\n\tvec4 vCoord1 = texture2D(gTopMap, vTexCoord).xyzw/vec4(1.0, 255.0, 1.0, 255.0);\n\tvec4 vCoord2 = texture2D(gBotMap, vTexCoord).xyzw/vec4(1.0, 255.0, 1.0, 255.0);\n\tvec2 uv1 = vec2(vCoord1.x+vCoord1.y, (vCoord1.z+vCoord1.w));\n\tvec2 uv2 = vec2(vCoord2.x+vCoord2.y, (vCoord2.z+vCoord2.w));\n    gl_FragColor = texture2D(gPictureTop, uv1)*alpha + texture2D(gPictureBot, uv2)*(1.0-alpha);\n}");
        maPositionHandle2 = GLES20.glGetAttribLocation(mProgram2, "aPosition");
        maTexCoordHandle2 = GLES20.glGetAttribLocation(mProgram2, "aTexCoord");
        mTopMapHandle2 = GLES20.glGetUniformLocation(mProgram2, "gTopMap");
        mBotMapHandle2 = GLES20.glGetUniformLocation(mProgram2, "gBotMap");
        mAlphaHandle2 = GLES20.glGetUniformLocation(mProgram2, "gAlphaMap");
        mPictureTopHandle = GLES20.glGetUniformLocation(mProgram2, "gPictureTop");
        mPictureBotHandle = GLES20.glGetUniformLocation(mProgram2, "gPictureBot");
        GLUtil.checkGlError("BottomGraphicRenderer after mProgram2");
        float af[] = new float[18];
        af[0] = -1F;
        af[1] = 1.0F;
        af[2] = 0.0F;
        af[3] = -1F;
        af[4] = -1F;
        af[5] = 0.0F;
        af[6] = 1.0F;
        af[7] = 1.0F;
        af[8] = 0.0F;
        af[9] = -1F;
        af[10] = -1F;
        af[11] = 0.0F;
        af[12] = 1.0F;
        af[13] = -1F;
        af[14] = 0.0F;
        af[15] = 1.0F;
        af[16] = 1.0F;
        af[17] = 0.0F;
        float af1[] = new float[12];
        af1[0] = 0.0F;
        af1[1] = 1.0F;
        af1[2] = 0.0F;
        af1[3] = 0.0F;
        af1[4] = 1.0F;
        af1[5] = 1.0F;
        af1[6] = 0.0F;
        af1[7] = 0.0F;
        af1[8] = 1.0F;
        af1[9] = 0.0F;
        af1[10] = 1.0F;
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
        GLUtil.checkGlError("BottomGraphicRenderer initProgram");
    }

    private void resetMatrix()
    {
    }

    public void drawPic(int i, int j)
    {
        misPictureMode = true;
        GLES20.glDisable(2929);
        GLES20.glUseProgram(mProgram2);
        bindPicTextures(i, j);
        GLES20.glDrawArrays(4, 0, 6);
    }

    public void drawPrev(int i, int j)
    {
        GLES20.glDisable(2929);
        GLES20.glUseProgram(mProgram);
        GLES20.glUniform4f(mPickInfoHandle, mPickInfo[0], mPickInfo[1], mPickInfo[2], 0.0F);
        if(misPictureMode)
            bindPreviewTextures(i, j);
        GLES20.glDrawArrays(4, 0, 6);
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

    public void setRendererSize(int i, int j, boolean flag)
    {
        Log.i(TAG, "setRendererSize width = "+i+" height = "+j+" needReverse = "+flag);
        mWidth = i;
        mHeight = j;
        resetMatrix();
        super.setRendererSize(i, j);
    }

    

}
