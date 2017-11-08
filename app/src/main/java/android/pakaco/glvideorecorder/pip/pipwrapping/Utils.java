
package android.pakaco.glvideorecorder.pip.pipwrapping;


import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import android.util.Log;


public class Utils
{
    static final String TAG = "VR_Utils";
    public static class LutUnderPathInfo
    {

        public String filenames[];
        public int resolution;

        public LutUnderPathInfo(int i, String as[])
        {
            resolution = i;
            filenames = as;
        }
    }

    private static class LutInfo1
    {

        public ArrayList map0;
        public ArrayList map1;
        public ArrayList map2;

        private LutInfo1()
        {
            map0 = new ArrayList();
            map1 = new ArrayList();
            map2 = new ArrayList();
        }

    }

    private static class LutFilenameInfo
    {

        public String toString()
        {
            return (new StringBuilder()).append("[").append(type).append(" resolution:").append(resolu).toString();
        }

        String filename;
        int resolu;
        int type;

        public LutFilenameInfo(int i, int j, String s)
        {
            type = i;
            resolu = j;
            filename = s;
        }
    }

    public static class TestCameraResResult
    {

        public String info;
        public int result;

        public TestCameraResResult(int i, String s)
        {
            result = i;
            info = s;
        }
    }

    public static class SomeDelays
    {

        public void delay()
        {
            for(; !isDelayedEnough(); SystemClock.sleep(1L));
        }

        public boolean isDelayedEnough()
        {
            return isDelayedMillis(delays);
        }

        public boolean isDelayedMillis(long l)
        {
            boolean flag = false;
            if(__last_millis__ != -1L) {
                if(System.currentTimeMillis() >= l + __last_millis__)
                {
                    __last_millis__ = System.currentTimeMillis();
                    flag = true;
                }
            } else {
                __last_millis__ = System.currentTimeMillis();
            }

            //if(__last_millis__ != -1L) goto _L2; else goto _L1
//_L1:
//            __last_millis__ = System.currentTimeMillis();
//_L4:
            return flag;
//_L2:
//            if(System.currentTimeMillis() >= l + __last_millis__)
//            {
//                __last_millis__ = System.currentTimeMillis();
//                flag = true;
 //           }
 //           if(true) goto _L4; else goto _L3
//_L3:
        }

        public long __last_millis__;
        public long delays;

        public SomeDelays(long l)
        {
            __last_millis__ = -1L;
            delays = 0L;
            delays = l;
        }
    }


    public Utils()
    {
    }

    private static String _getSizeReadable(long l)
    {
        String s;
        if(l < 1024L)
            s = (new StringBuilder()).append(" ").append(l).append(" B").toString();
        else
        if(l < 0x100000L)
            s = (new StringBuilder()).append(" ").append(l / 1024L).append(" KB").toString();
        else
        if(l < 0x40000000L)
            s = (new StringBuilder()).append(" ").append(l / 0x100000L).append(" MB").toString();
        else
            s = (new StringBuilder()).append(" ").append(l / 0x40000000L).append(" GB").toString();
        return s;
    }

    private static int _readFileFirstByte(String s)
    {
        File file = new File(s);
        int i;
        if(file.exists())
            try
            {
                FileInputStream fileinputstream = new FileInputStream(file);
                i = fileinputstream.read();
                fileinputstream.close();
            }
            catch(FileNotFoundException filenotfoundexception)
            {
                i = -1000;
            }
            catch(IOException ioexception)
            {
                i = -1000;
            }
        else
            i = -1000;
        return i;
    }

    private static String _readFileFirstByteStr(String s)
    {
        File file = new File(s);
        String s1;
        if(!file.exists())
            s1 = " /none";
        else {            
            try
            {
                FileInputStream fileinputstream = new FileInputStream(file);
                int i = fileinputstream.read();
                fileinputstream.close();
                s1 = (new StringBuilder()).append(" /").append(i).toString();
            }
            catch(FileNotFoundException filenotfoundexception)
            {
                s1 = " /nfe";
                //continue; /* Loop/switch isn't completed */
            }
            catch(IOException ioexception)
            {
                s1 = " /ioe";
                //continue; /* Loop/switch isn't completed */
            }            
        }
        return s1;
       
        /*if(!file.exists()) goto _L2; else goto _L1
_L1:
        String s1;
        String s2;
        try
        {
            FileInputStream fileinputstream = new FileInputStream(file);
            int i = fileinputstream.read();
            fileinputstream.close();
            s2 = (new StringBuilder()).append(" /").append(i).toString();
        }
        catch(FileNotFoundException filenotfoundexception)
        {
            s1 = " /nfe";
            continue; 
        }
        catch(IOException ioexception)
        {
            s1 = " /ioe";
            continue; 
        }
        s1 = s2;
_L4:
        return s1;
_L2:
        s1 = " /none";
        if(true) goto _L4; else goto _L3
_L3:*/
    }

    public static String getIntentAction(Intent intent)
    {
        String s;
        if(intent != null)
            s = intent.getAction();
        else
            s = null;
        return s;
    }

    public static final float getIntentExtraFloat(Intent intent, String s, float f)
    {
        if(intent != null)
        {
            Bundle bundle = intent.getExtras();
            if(bundle != null)
                f = bundle.getFloat(s, f);
        }
        return f;
    }

    public static int getIntentExtraInt(Intent intent, String s, int i)
    {
        if(intent != null && intent.getExtras() != null)
        {
            Bundle bundle = intent.getExtras();
            if(bundle != null)
                i = bundle.getInt(s, i);
        }
        return i;
    }

    public static final long getIntentExtraLong(Intent intent, String s, long l)
    {
        if(intent != null)
        {
            Bundle bundle = intent.getExtras();
            if(bundle != null)
                l = bundle.getLong(s, l);
        }
        return l;
    }

    public static LutFilenameInfo get_lut_type_from_filename(String s)
    {
        LutFilenameInfo lutfilenameinfo = null;
        if(!s.endsWith("M.dat") && !s.endsWith("m.dat") && !s.endsWith("M.DAT") && !s.endsWith("m.DAT"))
            lutfilenameinfo = null;
        try{
            if(s.startsWith("alphamerged_")) {
                lutfilenameinfo = new LutFilenameInfo(0, Integer.valueOf(s.substring("alphamerged_".length(), -5 + s.length())).intValue(), s);
            } else if (s.startsWith("leftlut_")) {            
                lutfilenameinfo = new LutFilenameInfo(1, Integer.valueOf(s.substring("leftlut_".length(), -5 + s.length())).intValue(), s);
            } else if (s.startsWith("rightlut_")) {
                lutfilenameinfo = new LutFilenameInfo(2, Integer.valueOf(s.substring("rightlut_".length(), -5 + s.length())).intValue(), s);
            }
        }catch(NumberFormatException e) {
            lutfilenameinfo = null;
        }catch(IndexOutOfBoundsException e1) {
            lutfilenameinfo = null;
        }
        return lutfilenameinfo;
      
        /*if(!s.endsWith("M.dat") && !s.endsWith("m.dat") && !s.endsWith("M.DAT") && !s.endsWith("m.DAT")) goto _L2; else goto _L1
_L1:
        if(!s.startsWith("alphamerged_")) goto _L4; else goto _L3
_L3:
        LutFilenameInfo lutfilenameinfo = new LutFilenameInfo(0, Integer.valueOf(s.substring("alphamerged_".length(), -5 + s.length())).intValue(), s);
_L5:
        return lutfilenameinfo;
        NumberFormatException numberformatexception2;
        numberformatexception2;
_L4:
        if(!s.startsWith("leftlut_"))
            break MISSING_BLOCK_LABEL_127;
        lutfilenameinfo = new LutFilenameInfo(1, Integer.valueOf(s.substring("leftlut_".length(), -5 + s.length())).intValue(), s);
          goto _L5
        IndexOutOfBoundsException indexoutofboundsexception1;
        indexoutofboundsexception1;
_L7:
        if(!s.startsWith("rightlut_")) goto _L2; else goto _L6
_L6:
        lutfilenameinfo = new LutFilenameInfo(2, Integer.valueOf(s.substring("rightlut_".length(), -5 + s.length())).intValue(), s);
          goto _L5
        IndexOutOfBoundsException indexoutofboundsexception;
        indexoutofboundsexception;
_L2:
        lutfilenameinfo = null;
          goto _L5
        NumberFormatException numberformatexception;
        numberformatexception;
          goto _L2
        NumberFormatException numberformatexception1;
        numberformatexception1;
          goto _L7
        IndexOutOfBoundsException indexoutofboundsexception2;
        indexoutofboundsexception2;
          goto _L4*/
    }

    private static boolean isFileExistByPrefix(String as[], String s)
    {
        boolean flag = false;
        if(as != null && as.length > 0)
        {
            int i = as.length;
            for(int j = 0; j < i; j++)
                if(as[j].startsWith(s))
                    flag = true;

        }
        return flag;
    }

    public static ArrayList search_lut_under_path(String s, boolean flag)
    {
        ArrayList arraylist = new ArrayList();
        File file = null;
        //arraylist = new ArrayList();
        //file = null;
        String as[] = null;
        if(flag) {
            as = lut_dir_test_files;         
        } else {
            file = new File(s);
            if(file.exists()) {
                if(!file.isDirectory()) {
                    arraylist = null;
                } else {
                    as = file.list();
                }
            } else {
                arraylist = null;
            }
        }
        if(as != null && as.length > 0) {
            HashMap hashmap = new HashMap(LUT_MAX_RESOLUTIONS__);
            //String as1[];
            File file1;
            for(int j = 0 ; j < as.length; j ++) {
               file1 = new File((new StringBuilder()).append(file.getAbsolutePath()).append("/").append(as[j]).toString());
               if(file1.exists() && file1.isFile()) {
                    LutFilenameInfo lutfilenameinfo = get_lut_type_from_filename(as[j]);
                    if(lutfilenameinfo != null)
                    {
                        LutInfo1 lutinfo1_1 = (LutInfo1)hashmap.get(Integer.valueOf(lutfilenameinfo.resolu));
                        if(lutinfo1_1 == null)
                        {
                            lutinfo1_1 = new LutInfo1();
                            hashmap.put(Integer.valueOf(lutfilenameinfo.resolu), lutinfo1_1);
                        }
                        if(lutfilenameinfo.type == 0)
                            lutinfo1_1.map0.add(lutfilenameinfo);
                        else if(lutfilenameinfo.type == 1)
                            lutinfo1_1.map1.add(lutfilenameinfo);
                        else if(lutfilenameinfo.type == 2)
                            lutinfo1_1.map2.add(lutfilenameinfo);
                    }
               }
            }

            int k = 0;
            while(k < 60) 
            {
                LutInfo1 lutinfo1 = (LutInfo1)hashmap.get(Integer.valueOf(k));
                if(lutinfo1 != null && lutinfo1.map0.size() == 1 && lutinfo1.map1.size() == 1 && lutinfo1.map2.size() == 1)
                {
                    String as2[] = new String[3];
                    as2[0] = ((LutFilenameInfo)lutinfo1.map0.get(0)).filename;
                    as2[1] = ((LutFilenameInfo)lutinfo1.map1.get(0)).filename;
                    as2[2] = ((LutFilenameInfo)lutinfo1.map2.get(0)).filename;
                    arraylist.add(new LutUnderPathInfo(k, as2));
                }
                k++;
            }

        }
        return arraylist;
        
        /*if(!flag) goto _L2; else goto _L1
_L1:
        String as[] = lut_dir_test_files;
_L11:
        if(as == null || as.length <= 0) goto _L4; else goto _L3
_L3:
        HashMap hashmap;
        String as1[];
        int i;
        int j;
        hashmap = new HashMap(60);
        as1 = as;
        i = as1.length;
        j = 0;
_L7:
        String s1;
        File file1;
        if(j >= i)
            break MISSING_BLOCK_LABEL_299;
        s1 = as1[j];
        if(flag)
            break; 
        file1 = new File((new StringBuilder()).append(file.getAbsolutePath()).append("/").append(s1).toString());
          goto _L5
_L12:
        j++;
        if(true) goto _L7; else goto _L6
_L2:
        file = new File(s);
        if(file.exists()) goto _L9; else goto _L8
_L8:
        arraylist = null;
_L4:
        return arraylist;
_L9:
        if(file.isDirectory())
            break; 
        arraylist = null;
        if(true) goto _L4; else goto _L10
_L10:
        as = file.list();
          goto _L11
_L5:
        if(!file1.exists() || !file1.isFile()) goto _L12; else goto _L6
_L6:
        LutFilenameInfo lutfilenameinfo = get_lut_type_from_filename(s1);
        if(lutfilenameinfo != null)
        {
            LutInfo1 lutinfo1_1 = (LutInfo1)hashmap.get(Integer.valueOf(lutfilenameinfo.resolu));
            if(lutinfo1_1 == null)
            {
                lutinfo1_1 = new LutInfo1();
                hashmap.put(Integer.valueOf(lutfilenameinfo.resolu), lutinfo1_1);
            }
            if(lutfilenameinfo.type == 0)
                lutinfo1_1.map0.add(lutfilenameinfo);
            else
            if(lutfilenameinfo.type == 1)
                lutinfo1_1.map1.add(lutfilenameinfo);
            else
            if(lutfilenameinfo.type == 2)
                lutinfo1_1.map2.add(lutfilenameinfo);
        }
          goto _L12
        int k = 0;
        while(k < 60) 
        {
            LutInfo1 lutinfo1 = (LutInfo1)hashmap.get(Integer.valueOf(k));
            if(lutinfo1 != null && lutinfo1.map0.size() == 1 && lutinfo1.map1.size() == 1 && lutinfo1.map2.size() == 1)
            {
                String as2[] = new String[3];
                as2[0] = ((LutFilenameInfo)lutinfo1.map0.get(0)).filename;
                as2[1] = ((LutFilenameInfo)lutinfo1.map1.get(0)).filename;
                as2[2] = ((LutFilenameInfo)lutinfo1.map2.get(0)).filename;
                arraylist.add(new LutUnderPathInfo(k, as2));
            }
            k++;
        }
          goto _L4*/
    }

    public static TestCameraResResult test_camera_res_read(String s)
    {
        StringBuilder stringbuilder = new StringBuilder("");
        File file = new File(s);
        Log.d(TAG, "test_camera_res_read start: " + s);
        TestCameraResResult testcameraresresult = null;
        if(!file.exists()) {
            Log.d(TAG, "not exist : " + s);
            testcameraresresult = new TestCameraResResult(-1, (new StringBuilder()).append(s).append(" not exist!!!\n").toString());
        }
        else if(!file.isDirectory())
        {
            Log.d(TAG, "is not directory : " + s);
            testcameraresresult = new TestCameraResResult(-2, "is not directory!\n");
        } else  {
            String as[] = file.list();
            boolean flag = false;
            stringbuilder.append((new StringBuilder()).append(s).append(" list:\n").toString());
            if(as != null && as.length > 0)
            {
                int j = as.length;
                for(int k = 0; k < j; k++)
                {
                    String s1 = as[k];
                    String s2 = (new StringBuilder()).append(file.getAbsolutePath()).append("/").append(s1).toString();
                    
                    File file1 = new File(s2);
                    stringbuilder.append((new StringBuilder()).append(" ").append(s1).append(" size:").append(_getSizeReadable(file1.length())).append(_readFileFirstByteStr(s2)).append("\n").toString());
                    Log.d(TAG, "File : " + s2);
                    if(file1.length() > 0L && _readFileFirstByte(s2) != -1000)
                        flag = true;
                }

            }
            int i;
            if(flag)
                i = 0;
            else
                i = -3;
            Log.d(TAG, "flag : " + flag + stringbuilder.toString());
            testcameraresresult = new TestCameraResResult(i, stringbuilder.toString());
        }
        return testcameraresresult;
    }

    public static TestCameraResResult test_camera_res_read_confirm_lut_file(String s)
    {
        StringBuilder stringbuilder = new StringBuilder("");
        File file = new File(s);
        TestCameraResResult testcameraresresult;
        if(!file.exists())
            testcameraresresult = new TestCameraResResult(-1, (new StringBuilder()).append(s).append(" not exist!!!\n").toString());
        else
        if(!file.isDirectory())
        {
            testcameraresresult = new TestCameraResResult(-2, "is not directory!\n");
        } else
        {
            String as[] = file.list();
            boolean flag = false;
            stringbuilder.append((new StringBuilder()).append(s).append(" list:\n").toString());
            if(as != null && as.length > 0)
            {
                int k = as.length;
                for(int l = 0; l < k; l++)
                {
                    String s1 = as[l];
                    String s2 = (new StringBuilder()).append(file.getAbsolutePath()).append("/").append(s1).toString();
                    File file1 = new File(s2);
                    stringbuilder.append((new StringBuilder()).append(" ").append(s1).append(" size:").append(_getSizeReadable(file1.length())).append(_readFileFirstByteStr(s2)).append("\n").toString());
                    if(file1.length() > 0L && _readFileFirstByte(s2) != -1000)
                        flag = true;
                }

            }
            if(!flag)
            {
                testcameraresresult = new TestCameraResResult(-3, stringbuilder.toString());
            } else
            {
                int i = 0;
                if(isFileExistByPrefix(as, "alphamerged"))
                    i = 0 + 1;
                if(isFileExistByPrefix(as, "leftlut"))
                    i++;
                if(isFileExistByPrefix(as, "rightlut"))
                    i++;
                int j;
                if(i >= 3)
                    j = 0;
                else
                    j = -4;
                testcameraresresult = new TestCameraResResult(j, stringbuilder.toString());
            }
        }
        return testcameraresresult;
    }

    public static TestCameraResResult test_camera_res_write(String s)
    {
        StringBuilder stringbuilder;
        File file;
        stringbuilder = new StringBuilder("");
        TestCameraResResult testcameraresresult;
        file = new File(s);
        if(!file.exists()) {
            testcameraresresult = new TestCameraResResult(-1, (new StringBuilder()).append(s).append(" not exist!!!\n").toString());
            return testcameraresresult;
        }
        //if(file.exists()) goto _L2; else goto _L1
//_L1:
//        TestCameraResResult testcameraresresult = new TestCameraResResult(-1, (new StringBuilder()).append(s).append(" not exist!!!\n").toString());
//_L4:
//        return testcameraresresult;
//_L2:
        if(!file.isDirectory())
        {
            testcameraresresult = new TestCameraResResult(-2, "is not directory!\n");
            return testcameraresresult;
            //continue; /* Loop/switch isn't completed */
        }
        File file1 = new File((new StringBuilder()).append(file.getAbsolutePath()).append("/test.dat").toString());
        if(file1.exists())
            file1.delete();
        FileOutputStream fileoutputstream;
        if(!file1.exists())
            try
            {
                file1.createNewFile();
                stringbuilder.append("create ok!\n");
            }
            catch(IOException ioexception1)
            {
                stringbuilder.append("create file failed!!! \n");
                testcameraresresult = new TestCameraResResult(-3, stringbuilder.toString());
                //continue; /* Loop/switch isn't completed */
            }

        try {
            fileoutputstream = new FileOutputStream(file1);
            fileoutputstream.write(1);
            fileoutputstream.flush();
            fileoutputstream.close();
            stringbuilder.append("write ok!\n");
            if(file1.exists())
            {
                file1.delete();
                stringbuilder.append("delete ok!\n");
            }
            testcameraresresult = new TestCameraResResult(0, stringbuilder.toString());
        }
        catch(FileNotFoundException filenotfoundexception)
            {
                stringbuilder.append(" not found exp! \n");
                testcameraresresult = new TestCameraResResult(-4, stringbuilder.toString());
            }
        catch(IOException ioexception1)
            {
                stringbuilder.append(" io exp! \n");
                testcameraresresult = new TestCameraResResult(-5, stringbuilder.toString());
            }
        /*testcameraresresult = new TestCameraResResult(0, stringbuilder.toString());
        continue; 
        FileNotFoundException filenotfoundexception;
        filenotfoundexception;
        stringbuilder.append(" not found exp! \n");
        testcameraresresult = new TestCameraResResult(-4, stringbuilder.toString());
        continue; 
        IOException ioexception;
        ioexception;
        stringbuilder.append(" io exp! \n");
        testcameraresresult = new TestCameraResResult(-5, stringbuilder.toString());*/
        return testcameraresresult;
        //if(true) goto _L4; else goto _L3
//_L3:
    }

    static final int LUT_MAX_RESOLUTIONS__ = 60;
    static final String __LUT_ALPHA_PREFIX = "alphamerged";
    static final String __LUT_LEFTLUT_PREFIX = "leftlut";
    static final String __LUT_RIGHTLUT_PREFIX = "rightlut";
    static final String lut_dir_test_files[];

    static 
    {
        String as[] = new String[23];
        as[0] = "alphamerged_49M.dat";
        as[1] = "leftlut_49m.dat";
        as[2] = "rightlut_41M.dat";
        as[3] = "alphamerged_40M.dat";
        as[4] = "leftlut_40m.dat";
        as[5] = "rightlut_40M.dat";
        as[6] = "alphamerged_5m.dat";
        as[7] = "leftlut_5M.dat";
        as[8] = "rightlut_5m.dat";
        as[9] = "alphamerged_13M.dat";
        as[10] = "leftlut_13m.dat";
        as[11] = "rightlut_13M.dat";
        as[12] = "alphamerged_8M.dat";
        as[13] = "leftlut_8M.dat";
        as[14] = "rightlut_8M.dat";
        as[15] = "alphamerged_2M.dat";
        as[16] = "leftlut_2m.dat";
        as[17] = "rightlut_1M.dat";
        as[18] = "alphamerged_3M.dat";
        as[19] = "leftlut_4M.dat";
        as[20] = "rightlut_1M.dat";
        as[21] = "aa.dat";
        as[22] = "bb.dat";
        lut_dir_test_files = as;
    }
}
