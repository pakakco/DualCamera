
package android.pakaco.glvideorecorder.pip.pipwrapping;


import java.lang.reflect.Array;
import java.nio.*;

public class SphereV2
{

    public SphereV2()
    {
        int ai[] = new int[2];
        ai[0] = 0x23280;
        ai[1] = 3;
        triangleCoords = (float[][])Array.newInstance(Float.TYPE, ai);
        int ai1[] = new int[2];
        ai1[0] = 0x23280;
        ai1[1] = 2;
        textureCoords = (float[][])Array.newInstance(Float.TYPE, ai1);
        vertexStride = 4 * COORDS_PER_VERTEX;
        textureStride = 4 * COORDS_PER_TEXTURE;
        step = 6F;
        bb = ByteBuffer.allocateDirect(4 * (triangleCoords.length * triangleCoords[0].length));
        aa = ByteBuffer.allocateDirect(4 * (textureCoords.length * textureCoords[0].length));
    }

    public int init()
    {
        bb.order(ByteOrder.nativeOrder());
        aa.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        textureBuffer = aa.asFloatBuffer();
        int i = 0;
        pai = -90F;
        do
        {
            if(pai >= 90F)
                break;
            float f = 360 / (int)step;
            float f1 = 180 / (int)step;
            r1 = (float)Math.cos((3.1415926535897931D * (double)pai) / 180D);
            r2 = (float)Math.cos((3.1415926535897931D * (double)(pai + step)) / 180D);
            h1 = (float)Math.sin((3.1415926535897931D * (double)pai) / 180D);
            h2 = (float)Math.sin((3.1415926535897931D * (double)(pai + step)) / 180D);
            float f2 = (float)(90D + (double)pai) / step;
            theta = 0.0F;
            while(theta <= 360F) 
            {
                if(theta == 0.0F)
                {
                    co = (float)Math.cos((3.1415926535897931D * (double)theta) / 180D);
                    si = -(float)Math.sin((3.1415926535897931D * (double)theta) / 180D);
                    float f5 = (int)theta / (int)step;
                    triangleCoords[i][0] = r2 * co;
                    triangleCoords[i][1] = h2;
                    triangleCoords[i][2] = r2 * si;
                    textureCoords[i][0] = f5 / f;
                    textureCoords[i][1] = 1.0F - (1.0F + f2) / f1;
                    triangleCoords[i + 1][0] = r1 * co;
                    triangleCoords[i + 1][1] = h1;
                    triangleCoords[i + 1][2] = r1 * si;
                    textureCoords[i + 1][0] = f5 / f;
                    textureCoords[i + 1][1] = 1.0F - f2 / f1;
                    vertexBuffer.put(triangleCoords[i]);
                    vertexBuffer.put(triangleCoords[i + 1]);
                    textureBuffer.put(textureCoords[i]);
                    textureBuffer.put(textureCoords[i + 1]);
                    i += 2;
                } else if(theta != 0.0F && theta != 360F)
                {
                    co = (float)Math.cos((3.1415926535897931D * (double)theta) / 180D);
                    si = -(float)Math.sin((3.1415926535897931D * (double)theta) / 180D);
                    float f4 = (int)theta / (int)step;
                    triangleCoords[i][0] = r2 * co;
                    triangleCoords[i][1] = h2;
                    triangleCoords[i][2] = r2 * si;
                    textureCoords[i][0] = f4 / f;
                    textureCoords[i][1] = 1.0F - (1.0F + f2) / f1;
                    vertexBuffer.put(triangleCoords[i]);
                    textureBuffer.put(textureCoords[i]);
                    triangleCoords[i + 1][0] = triangleCoords[i][0];
                    triangleCoords[i + 1][1] = triangleCoords[i][1];
                    triangleCoords[i + 1][2] = triangleCoords[i][2];
                    textureCoords[i + 1][0] = textureCoords[i][0];
                    textureCoords[i + 1][1] = textureCoords[i][1];
                    vertexBuffer.put(triangleCoords[i + 1]);
                    textureBuffer.put(textureCoords[i + 1]);
                    int k = i + 2;
                    triangleCoords[k][0] = triangleCoords[k - 3][0];
                    triangleCoords[k][1] = triangleCoords[k - 3][1];
                    triangleCoords[k][2] = triangleCoords[k - 3][2];
                    textureCoords[k][0] = textureCoords[k - 3][0];
                    textureCoords[k][1] = textureCoords[k - 3][1];
                    triangleCoords[k + 1][0] = r1 * co;
                    triangleCoords[k + 1][1] = h1;
                    triangleCoords[k + 1][2] = r1 * si;
                    textureCoords[k + 1][0] = f4 / f;
                    textureCoords[k + 1][1] = 1.0F - f2 / f1;
                    vertexBuffer.put(triangleCoords[k]);
                    vertexBuffer.put(triangleCoords[k + 1]);
                    textureBuffer.put(textureCoords[k]);
                    textureBuffer.put(textureCoords[k + 1]);
                    int l = k + 2;
                    triangleCoords[l][0] = r2 * co;
                    triangleCoords[l][1] = h2;
                    triangleCoords[l][2] = r2 * si;
                    textureCoords[l][0] = f4 / f;
                    textureCoords[l][1] = 1.0F - (1.0F + f2) / f1;
                    triangleCoords[l + 1][0] = r1 * co;
                    triangleCoords[l + 1][1] = h1;
                    triangleCoords[l + 1][2] = r1 * si;
                    textureCoords[l + 1][0] = f4 / f;
                    textureCoords[l + 1][1] = 1.0F - f2 / f1;
                    vertexBuffer.put(triangleCoords[l]);
                    vertexBuffer.put(triangleCoords[l + 1]);
                    textureBuffer.put(textureCoords[l]);
                    textureBuffer.put(textureCoords[l + 1]);
                    i = l + 2;
                } else if(theta == 360F)
                {
                    co = (float)Math.cos((3.1415926535897931D * (double)theta) / 180D);
                    si = -(float)Math.sin((3.1415926535897931D * (double)theta) / 180D);
                    float f3 = (int)theta / (int)step;
                    triangleCoords[i][0] = r2 * co;
                    triangleCoords[i][1] = h2;
                    triangleCoords[i][2] = r2 * si;
                    textureCoords[i][0] = f3 / f;
                    textureCoords[i][1] = 1.0F - (1.0F + f2) / f1;
                    vertexBuffer.put(triangleCoords[i]);
                    textureBuffer.put(textureCoords[i]);
                    triangleCoords[i + 1][0] = triangleCoords[i][0];
                    triangleCoords[i + 1][1] = triangleCoords[i][1];
                    triangleCoords[i + 1][2] = triangleCoords[i][2];
                    textureCoords[i + 1][0] = textureCoords[i][0];
                    textureCoords[i + 1][1] = textureCoords[i][1];
                    vertexBuffer.put(triangleCoords[i + 1]);
                    textureBuffer.put(textureCoords[i + 1]);
                    int j = i + 2;
                    triangleCoords[j][0] = triangleCoords[j - 3][0];
                    triangleCoords[j][1] = triangleCoords[j - 3][1];
                    triangleCoords[j][2] = triangleCoords[j - 3][2];
                    textureCoords[j][0] = textureCoords[j - 3][0];
                    textureCoords[j][1] = textureCoords[j - 3][1];
                    triangleCoords[j + 1][0] = r1 * co;
                    triangleCoords[j + 1][1] = h1;
                    triangleCoords[j + 1][2] = r1 * si;
                    textureCoords[j + 1][0] = f3 / f;
                    textureCoords[j + 1][1] = 1.0F - f2 / f1;
                    vertexBuffer.put(triangleCoords[j]);
                    vertexBuffer.put(triangleCoords[j + 1]);
                    textureBuffer.put(textureCoords[j]);
                    textureBuffer.put(textureCoords[j + 1]);
                    i = j + 2;
                }
                theta = theta + step;
            }
            pai = pai + step;
        } while(true);
        return i;
    }

    static int COORDS_PER_TEXTURE = 2;
    static int COORDS_PER_VERTEX = 3;
    ByteBuffer aa;
    ByteBuffer bb;
    private float co;
    private float h1;
    private float h2;
    private float pai;
    private float r1;
    private float r2;
    private float si;
    private float step;
    public FloatBuffer textureBuffer;
    float textureCoords[][];
    private int textureStride;
    private float theta;
    float triangleCoords[][];
    public FloatBuffer vertexBuffer;
    private int vertexStride;

}
