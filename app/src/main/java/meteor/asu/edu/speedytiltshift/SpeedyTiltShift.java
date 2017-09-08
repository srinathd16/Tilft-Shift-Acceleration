package meteor.asu.edu.speedytiltshift;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import java.lang.Math.*;


/**
 * Created by roblkw on 7/26/17.
 */

public class SpeedyTiltShift {

    static {
        System.loadLibrary("nativetiltshift-lib");
    }

    public static Bitmap tiltshift_java(Bitmap in, int a0, int a1, int a2, int a3, float s_far, float s_near){
        Bitmap out;
        out=in.copy(in.getConfig(),true);

        int width=in.getWidth();
        int height=in.getHeight();

        Log.d("TILTSHIFT_JAVA","hey:"+width+","+height);
        int[] pixels = new int[width*height];
        int offset=0;
        int stride = width;
        in.getPixels(pixels,offset,stride,0,0,width,height);

        //Kernel Matrix
        double[] g = new double[31*31];

        int conv;
        int p;
        int BB;
        int GG;
        int RR;
        int AA;
        int color;

        Math math = null;
        for (int y=0; y<height; y++){
            for (int x = 0; x<width; x++){

                //Convolution
                p=0;
                BB=0;
                GG=0;
                RR=0;
                AA=0;
                //Log.d("OriginalPixel","value="+pixels[y*width+x]);
                for(int i=0; i<31; i++){
                    for(int j=0; j<31; j++){
                        g[31*i+j] = (math.exp(-(1/50)*(((i*i)+(j*j)))))/(3.14*50);
                        //Log.d("GValue", "g["+(5*i+j)+"]"+"="+g[5*i+j]);
                        //Log.d("ConvolveLoop", "width="+width);
                        //Log.d("ConvolveLoop", "height="+height);
                        if( (x+(j-15)) <0 || (y+(i-15)) <0 || (x+(j+15))>width || (y+(i+15))>height) {
                            BB=BB;
                            GG=GG;
                            RR=RR;
                        }

                        else {
                            conv = pixels[((y + (i-15)) * width) + (x + (j-15))];
                            //Log.d("conv", "value="+conv);
                            //int compute = (((y + (i-2)) * width) + (x + (j-2)));
                            //if(compute > height*width) {
                            //    Log.d("ConvolveLoop", "pixels=" + (((y + (i - 2)) * width) + (x + (j - 2))));
                            //}
                            BB = (int) (BB + (conv & 0xff) * g[31*i+j]);
                            //Log.d("CONV&0FF","value="+(conv & 0xff));
                            //Log.d("BB", "value="+BB);
                            GG = (int) (GG + (((conv>>8)& 0xff) * g[31*i+j]));
                            RR = (int) (RR + (((conv>>16)& 0xff) * g[31*i+j]));
                            AA = (conv>>24)& 0xff;
                        }


                        //AA = (conv>>24)& 0xff;
                        //p = (int) (p + color);
                    }
                }

                //Log.d("BB", "value="+BB);
                //Log.d("GG", "value="+GG);
                //Log.d("RR", "value="+RR);
                color = (AA & 0xff) << 24 | (RR & 0xff) << 16 | (GG & 0xff) << 8 | (BB & 0xff);
                //System.out.print(color);


                // From Google Developer: int color = (A & 0xff) << 24 | (R & 0xff) << 16 | (G & 0xff) << 16 | (B & 0xff);
                //int p = pixels[y*width+x];
                //int BB = p & 0xff;
                //int BB = -1;
                //int GG = (p>>8)& 0xff;
                //int RR = (p>>16)& 0xff; //set red high
                //int AA = (p>>24)& 0xff;
                //int color = (AA & 0xff) << 24 | (RR & 0xff) << 16 | (GG & 0xff) << 8 | (BB & 0xff);
                pixels[y*width+x] = color;

                //Gaussian Weight Vector



            }
        }
        out.setPixels(pixels,offset,stride,0,0,width,height);

        Log.d("TILTSHIFT_JAVA","hey2");
        return out;
    }
    public static Bitmap tiltshift_cpp(Bitmap in, int a0, int a1, int a2, int a3, float s_far, float s_near){
        return in;
    }
    public static Bitmap tiltshift_neon(Bitmap in, int a0, int a1, int a2, int a3, float s_far, float s_near){
        return in;
    }
    private static native int[] nativeTiltShift(int[] pixels, int imgW, int imgH, int a0, int a1, int a2, int a3, float s_far, float s_near);
    private static native int[] nativeTiltShiftNeon(int[] pixels, int imgW, int imgH, int a0, int a1, int a2, int a3, float s_far, float s_near);

}