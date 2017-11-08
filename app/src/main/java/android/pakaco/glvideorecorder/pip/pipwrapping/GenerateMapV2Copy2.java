

package android.pakaco.glvideorecorder.pip.pipwrapping;

import android.content.Context;
import android.content.res.Resources;
import android.opengl.GLES20;
import android.util.Log;
//import com.bql.log.Logger;
//import com.bql.misc.DebugStatistic;
//import com.bql.misc.Utils;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class GenerateMapV2Copy2
{
    /*public static class Log__
    {

        public static void d(String s, String s1)
        {
            Logger.d(s, s1);
        }

        public static void e(String s, String s1)
        {
            Logger.e(s, s1);
        }

        public static void e(String s, String s1, Throwable throwable)
        {
            Logger.e(s, s1);
        }

        public static void i(String s, String s1)
        {
            Logger.i(s, s1);
        }

        public static void i(String s, String s1, Throwable throwable)
        {
            Logger.i(s, s1);
        }

        public static void v(String s, String s1)
        {
            Logger.v(s, s1);
        }

        public static void w(String s, String s1)
        {
            Logger.w(s, s1);
        }

        public static void w(String s, String s1, Throwable throwable)
        {
            Logger.w(s, s1);
        }

        public Log__()
        {
        }
    }*/


    private GenerateMapV2Copy2(Context context)
    {
        COORD_TEX_HEIGHT = 0;
        COORD_TEX_WIDTH = 0;
        Log.d("GenerateMapV2Copy2", "GenerateMapV2Copy2 new! _CONFIG_LUT_PREVIEW_CAPTURE_USE_DIFFERENT=true");
        long l = System.currentTimeMillis();
        if(!generateFromFile(2))
        {
            if(!generateFromFile(0))
            {
                generateFromResourceRaw(context);
                DebugStatistic.vr_pip_capture_lut_path = "raw_lut_loaded";
            } else
            {
                DebugStatistic.vr_pip_capture_lut_path = "old_lut_loaded";
            }
        } else
        {
            DebugStatistic.vr_pip_capture_lut_path = "capture_lut_loaded";
        }
        DebugStatistic.vr_pip_capture_lut_size_width = COORD_TEX_WIDTH;
        DebugStatistic.vr_pip_capture_lut_size_height = COORD_TEX_HEIGHT;
        Log.d("GenerateMapV2Copy2", (new StringBuilder()).append("GenerateMapV2Copy2 new time=").append(System.currentTimeMillis() - l).toString());
    }

    private boolean generateFromFile(int i)
    {
        boolean flag = false;
        String s = null;
        Log.d("GenerateMapV2Copy2", "generateFromFile start");
        long l = System.currentTimeMillis();
        if(Utils.test_camera_res_read_confirm_lut_file(_RES_ROOT_DEBUG_PATH).result == 0)
            s = _RES_ROOT_DEBUG_PATH;
        if(s == null)
            if(Utils.test_camera_res_read(_RES_ROOT_PATH1).result == 0)
                s = _RES_ROOT_PATH1;
            else
            if(Utils.test_camera_res_read(_RES_ROOT_PATH2).result == 0)
                s = _RES_ROOT_PATH2;
        DebugStatistic.vr_pip_capture_lut_use_path = s;
        if(s == null)
        {
            Log.v("GenerateMapV2Copy2", "Error find res directory!");
        } else
        {
            Log.v("GenerateMapV2Copy2", (new StringBuilder()).append("find res dir =(").append(s).toString());
            mapHandle = new int[2];
            alphaHandle = new int[1];
            GLES20.glGenTextures(2, mapHandle, 0);
            GLES20.glGenTextures(1, alphaHandle, 0);
            if(mapHandle[0] == 0 || mapHandle[1] == 0 || alphaHandle[0] == 0)
            {
                Log.v("GenerateMapV2Copy2", "Error generating mapping texture.");
                release_all_texture();
            } else
            {
                String as[] = __RES_LUT_OLD;
                if(1 == i)
                {
                    as = __RES_LUT_PREVIEW;
                    Log.v("GenerateMapV2Copy2", "try load preview lut!");
                } else
                if(2 == i)
                {
                    as = __RES_LUT_CAPTURE;
                    Log.v("GenerateMapV2Copy2", "try load capture lut!");
                } else
                {
                    Log.v("GenerateMapV2Copy2", "try load old lut!");
                }
                if(!generateTexture_((new StringBuilder()).append(s).append("/").append(as[0]).toString(), getAlphaHandler(), true))
                {
                    release_all_texture();
                    Log.v("GenerateMapV2Copy2", (new StringBuilder()).append("res_path:").append(s).append("/").append(as[0]).append(" failed!").toString());
                } else
                if(!generateTexture_((new StringBuilder()).append(s).append("/").append(as[1]).toString(), getLeftMapHandler(), false))
                {
                    release_all_texture();
                    Log.v("GenerateMapV2Copy2", (new StringBuilder()).append("res_path:").append(s).append("/").append(as[1]).append(" failed!").toString());
                } else
                if(!generateTexture_((new StringBuilder()).append(s).append("/").append(as[2]).toString(), getRightMapHandler(), false))
                {
                    release_all_texture();
                    Log.v("GenerateMapV2Copy2", (new StringBuilder()).append("res_path:").append(s).append("/").append(as[2]).append(" failed!").toString());
                } else
                {
                    Log.d("GenerateMapV2Copy2", (new StringBuilder()).append("generateFromFile done Time:").append(System.currentTimeMillis() - l).toString());
                    flag = true;
                }
            }
        }
        return flag;
    }

    private boolean generateFromFile_with_matching(int i)
    {
        boolean flag;
        String s;
        flag = false;
        s = null;
        if(i < 1 || i > 2) 
            return false;
        //if(i >= 1 && i <= 2) goto _L2; else goto _L1
//_L1:
//        return flag;
//_L2:
        long l;
        Log.d("GenerateMapV2Copy2", "generateFromFile start");
        l = System.currentTimeMillis();
        if(Utils.test_camera_res_read(_RES_ROOT_PATH1).result == 0) 
            s = _RES_ROOT_PATH1;
        else if(Utils.test_camera_res_read(_RES_ROOT_PATH2).result == 0)
            s = _RES_ROOT_PATH2;
            
        //if(Utils.test_camera_res_read("/custom/lib/camera_res").result != 0) goto _L4; else goto _L3
//_L3:
//        s = "/custom/lib/camera_res";
//_L5:
        if(Utils.test_camera_res_read_confirm_lut_file(_RES_ROOT_DEBUG_PATH).result == 0)
            s = _RES_ROOT_DEBUG_PATH;
        DebugStatistic.vr_pip_preview_record_lut_use_path = s;
        if(s == null)
        {
            Log.v("GenerateMapV2Copy2", "Error find res directory!");
        } else
        {
            Log.v("GenerateMapV2Copy2", (new StringBuilder()).append("find res dir =(").append(s).toString());
            ArrayList arraylist = Utils.search_lut_under_path(s, false);
            if(arraylist == null || arraylist.size() <= 0)
            {
                Log.v("GenerateMapV2Copy2", "Error find lutArray!");
            } else
            {
                Utils.LutUnderPathInfo lutunderpathinfo;
                if(i == 1)
                {
                    if(arraylist.size() >= 2)
                        lutunderpathinfo = (Utils.LutUnderPathInfo)arraylist.get(-2 + arraylist.size());
                    else
                        lutunderpathinfo = (Utils.LutUnderPathInfo)arraylist.get(-1 + arraylist.size());
                } else
                {
                    lutunderpathinfo = (Utils.LutUnderPathInfo)arraylist.get(-1 + arraylist.size());
                }
                mapHandle = new int[2];
                alphaHandle = new int[1];
                GLES20.glGenTextures(2, mapHandle, 0);
                GLES20.glGenTextures(1, alphaHandle, 0);
                if(mapHandle[0] == 0 || mapHandle[1] == 0 || alphaHandle[0] == 0)
                {
                    Log.v("GenerateMapV2Copy2", "Error generating mapping texture.");
                    release_all_texture();
                } else
                if(!generateTexture_((new StringBuilder()).append(s).append("/").append(lutunderpathinfo.filenames[0]).toString(), getAlphaHandler(), true))
                {
                    release_all_texture();
                    Log.v("GenerateMapV2Copy2", (new StringBuilder()).append("res_path:").append(s).append("/").append(lutunderpathinfo.filenames[0]).append(" failed!").toString());
                } else
                if(!generateTexture_((new StringBuilder()).append(s).append("/").append(lutunderpathinfo.filenames[1]).toString(), getLeftMapHandler(), false))
                {
                    release_all_texture();
                    Log.v("GenerateMapV2Copy2", (new StringBuilder()).append("res_path:").append(s).append("/").append(lutunderpathinfo.filenames[1]).append(" failed!").toString());
                } else
                if(!generateTexture_((new StringBuilder()).append(s).append("/").append(lutunderpathinfo.filenames[2]).toString(), getRightMapHandler(), false))
                {
                    release_all_texture();
                    Log.v("GenerateMapV2Copy2", (new StringBuilder()).append("res_path:").append(s).append("/").append(lutunderpathinfo.filenames[2]).append(" failed!").toString());
                } else
                {
                    Log.d("GenerateMapV2Copy2", (new StringBuilder()).append("generateFromFile done Time:").append(System.currentTimeMillis() - l).toString());
                    flag = true;
                }
            }
        }
        return flag;
//        if(true) goto _L1; else goto _L4
//_L4:
//        if(Utils.test_camera_res_read("/persist/lib/camera_res").result == 0)
//            s = "/persist/lib/camera_res";
 //         goto _L5
    }

    private void generateFromResourceRaw(Context context)
    {
        Log.d("GenerateMapV2Copy2", "generateFromResourceRaw start");
        mapHandle = new int[2];
        alphaHandle = new int[1];
        GLES20.glGenTextures(2, mapHandle, 0);
        GLES20.glGenTextures(1, alphaHandle, 0);
        if(mapHandle[0] == 0 || mapHandle[1] == 0 || alphaHandle[0] == 0)
            throw new RuntimeException("Error generating mapping texture.");
        long l = System.currentTimeMillis();
        if(!uploadTexture_(context, 0x7f070000, getAlphaHandler(), true))
            throw new RuntimeException("Error loading alphamerged texture.");
        if(!uploadTexture_(context, 0x7f070006, getLeftMapHandler(), false))
            throw new RuntimeException("Error loading leftlut texture.");
        if(!uploadTexture_(context, 0x7f070010, getRightMapHandler(), false))
        {
            throw new RuntimeException("Error loading rightlut texture.");
        } else
        {
            Log.d("GenerateMapV2Copy2", (new StringBuilder()).append("generateFromResourceRaw Time:").append(System.currentTimeMillis() - l).toString());
            return;
        }
    }

    private boolean generateTexture_(String s, int i, boolean flag)
    {
        boolean flag1 = true;
        Log.v("GenerateMapV2Copy2", (new StringBuilder()).append("res_path:").append(s).append(" texObj:").append(i).append(" is_alpha:").append(flag).toString());
        File file = new File(s);
        if(file == null || !file.exists())
        {
            Log.v("GenerateMapV2Copy2", (new StringBuilder()).append("generateTexture_ : res_path=").append(s).append("File is not find").toString());
            flag1 = false;
        } else
        {
            try
            {
                FileInputStream fileinputstream = new FileInputStream(file);
                if(flag)
                {
                    byte abyte1[] = new byte[4];
                    fileinputstream.read(abyte1, 0, 4);
                    COORD_TEX_HEIGHT = (0xff & abyte1[0]) << 8 | 0xff & abyte1[1];
                    COORD_TEX_WIDTH = (0xff & abyte1[2]) << 8 | 0xff & abyte1[3];
                    Log.i("GenerateMapV2Copy2", (new StringBuilder()).append("------- size width*height : ").append(COORD_TEX_WIDTH).append("x").append(COORD_TEX_HEIGHT).toString());
                }
                byte abyte0[] = new byte[4 * (COORD_TEX_WIDTH * COORD_TEX_HEIGHT)];
                fileinputstream.read(abyte0);
                fileinputstream.close();
                ByteBuffer bytebuffer = ByteBuffer.allocateDirect(abyte0.length);
                bytebuffer.order(ByteOrder.nativeOrder());
                bytebuffer.put(abyte0);
                bytebuffer.position(0);
                loadTexture(i, bytebuffer);
            }
            catch(IOException ioexception)
            {
                ioexception.printStackTrace();
                flag1 = false;
            }
        }
        return flag1;
    }

    public static GenerateMapV2Copy2 getInstance(Context context)
    {
        StringBuilder stringbuilder = (new StringBuilder()).append("getInstance gMap:");
        Object obj;
        if(gMap != null)
            obj = gMap;
        else
            obj = "null";
        Log.v("GenerateMapV2Copy2", stringbuilder.append(obj).toString());
        if(gMap == null)
            gMap = new GenerateMapV2Copy2(context);
        return gMap;
    }

    private void loadTexture(int i, ByteBuffer bytebuffer)
    {
        GLES20.glBindTexture(3553, i);
        GLES20.glTexParameteri(3553, 10241, 9728);
        GLES20.glTexParameteri(3553, 10240, 9728);
        GLES20.glTexParameterf(3553, 10242, 33071F);
        GLES20.glTexParameterf(3553, 10243, 33071F);
        GLES20.glTexImage2D(3553, 0, 6408, COORD_TEX_WIDTH, COORD_TEX_HEIGHT, 0, 6408, 5121, bytebuffer);
    }

    private void release_all_texture()
    {
        release_texture_obj(getLeftMapHandler());
        release_texture_obj(getRightMapHandler());
        release_texture_obj(getAlphaHandler());
    }

    private void release_texture_obj(int i)
    {
        if(i > 0)
        {
            int ai[] = new int[1];
            ai[0] = i;
            GLES20.glDeleteTextures(1, ai, 0);
        }
    }

    private boolean uploadTexture_(Context context, int i, int j, boolean flag)
    {
        return false;
        /*boolean flag1;
        InputStream inputstream;
        flag1 = false;
        inputstream = context.getResources().openRawResource(i);
        if(!flag)
            break MISSING_BLOCK_LABEL_123;
        byte abyte1[] = new byte[4];
        inputstream.read(abyte1, 0, 4);
        COORD_TEX_HEIGHT = (0xff & abyte1[0]) << 8 | 0xff & abyte1[1];
        COORD_TEX_WIDTH = (0xff & abyte1[2]) << 8 | 0xff & abyte1[3];
        Log.i("GenerateMapV2Copy2", (new StringBuilder()).append("    LUT size width*height=").append(COORD_TEX_WIDTH).append("*").append(COORD_TEX_HEIGHT).toString());
        byte abyte0[] = new byte[4 * (COORD_TEX_WIDTH * COORD_TEX_HEIGHT)];
        inputstream.read(abyte0);
        inputstream.close();
        ByteBuffer bytebuffer = ByteBuffer.allocateDirect(abyte0.length);
        bytebuffer.order(ByteOrder.nativeOrder());
        bytebuffer.put(abyte0);
        bytebuffer.position(0);
        loadTexture(j, bytebuffer);
        flag1 = true;
_L2:
        return flag1;
        IOException ioexception;
        ioexception;
        ioexception.printStackTrace();
        Log.v("GenerateMapV2Copy2", (new StringBuilder()).append("generateTexture_ failed excep:").append(ioexception).toString());
        if(true) goto _L2; else goto _L1
_L1:*/
    }

    public int getAlphaHandler()
    {
        return alphaHandle[0];
    }

    public int getLeftMapHandler()
    {
        return mapHandle[0];
    }

    public int getRightMapHandler()
    {
        return mapHandle[1];
    }

    public void release()
    {
        release_all_texture();
        gMap = null;
    }

    public void useLeft(int i, int j)
    {
        int k = getLeftMapHandler();
        int l = getAlphaHandler();
        GLES20.glActiveTexture(i);
        GLES20.glBindTexture(3553, k);
        GLES20.glActiveTexture(j);
        GLES20.glBindTexture(3553, l);
    }

    public void useRight(int i, int j)
    {
        int k = getRightMapHandler();
        int l = getAlphaHandler();
        GLES20.glActiveTexture(i);
        GLES20.glBindTexture(3553, k);
        GLES20.glActiveTexture(j);
        GLES20.glBindTexture(3553, l);
    }

    static final String TAG = "GenerateMapV2Copy2";
    private static final int _IDX_ALPHA = 0;
    private static final int _IDX_LEFTLUT = 1;
    private static final int _IDX_RIGHTLUT = 2;
    private static final String _RES_ROOT_DEBUG_PATH = "/sdcard/vrcamera_lut__";
    //private static final String _RES_ROOT_PATH1 = "/custom/lib/camera_res";    
    private static final String _RES_ROOT_PATH1 = "/storage/emulated/0/camera_res";
    private static final String _RES_ROOT_PATH2 = "/persist/lib/camera_res";
    private static final String __RES_LUT_CAPTURE[];
    private static final String __RES_LUT_OLD[];
    private static final String __RES_LUT_PREVIEW[];
    private static GenerateMapV2Copy2 gMap = null;
    int COORD_TEX_HEIGHT;
    int COORD_TEX_WIDTH;
    private int alphaHandle[];
    private int mapHandle[];

    static 
    {
        String as[] = new String[3];
        as[0] = "alphamerged.dat";
        as[1] = "leftlut.dat";
        as[2] = "rightlut.dat";
        __RES_LUT_OLD = as;
        String as1[] = new String[3];
        as1[0] = "alphamerged_v.dat";
        as1[1] = "leftlut_v.dat";
        as1[2] = "rightlut_v.dat";
        __RES_LUT_PREVIEW = as1;
        String as2[] = new String[3];
        as2[0] = "alphamerged_c.dat";
        as2[1] = "leftlut_c.dat";
        as2[2] = "rightlut_c.dat";
        __RES_LUT_CAPTURE = as2;
    }
}
