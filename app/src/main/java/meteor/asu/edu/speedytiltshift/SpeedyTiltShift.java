package meteor.asu.edu.speedytiltshift;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;

/**
 * Created by roblkw on 7/26/17.
 */

public class SpeedyTiltShift {

    static {
        System.loadLibrary("nativetiltshift-lib");
    }

    /* public static Bitmap tiltshift_java(Bitmap in, int a0, int a1, int a2, int a3, float s_far, float s_near){
        Bitmap out;
        out=in.copy(in.getConfig(),true);

        int width=in.getWidth();
        int height=in.getHeight();

        Log.d("TILTSHIFT_JAVA","hey:"+width+","+height);
        int[] pixels = new int[width*height];
        int offset=0;
        int stride = width;
//        in.getPixels(pixels,offset,stride,0,0,width,height);
        for (int y=0; y<height; y++){
            for (int x = 0; x<width; x++){
                // From Google Developer: int color = (A & 0xff) << 24 | (R & 0xff) << 16 | (G & 0xff) << 16 | (B & 0xff);
//                int p = pixels[y*width+x];
//                int BB = p & 0xff;
//                int GG = (p<<8)& 0xff;
//                int RR = 0xff;//(p<<16)& 0xff; //set red high
//                int AA = (p<<24)& 0xff;
//                int color = (AA & 0xff) << 24 | (RR & 0xff) << 16 | (GG & 0xff) << 8 | (BB & 0xff);
//                pixels[y*width+x] = color;
            }
        }
//        out.setPixels(pixels,offset,stride,0,0,width,height);

        Log.d("TILTSHIFT_JAVA","hey2");
        return out;
    }

    */

    public static Bitmap tiltshift_java(Bitmap in, int a0, int a1, int a2, int a3, float s_far, float s_near){
        Bitmap out;
        out=in.copy(in.getConfig(),true);

        int width=in.getWidth();
        int height=in.getHeight();

        Log.d("TILTSHIFT_JAVA","hey:"+width+","+height);
        int[] pixels = new int[(width)*(height)];
        int offset=0;
        int stride = width;
        in.getPixels(pixels,offset,stride,0,0,width,height);

        //Kernel Matrix
        double[] g = new double[50*50];

        int far_a0 = 50;
        int far_a1 = 100;
        int near_a2 = 1350;
        int near_a3 = 1430;
        float sigma_far = 0.7f;
        float sigma_near = 0.7f;

        float sigma;
        int radius;
        int size;
        int conv;
        int p;
        double BB;
        double GG;
        double RR;
        double AA;
        int color;

        Color color1 = null;
        Math math = null;
        //Log.d("red", "value"+color1.red(pixels[1000]));
        //Log.d("green", "value"+color1.green(pixels[1000]));
        //Log.d("blue", "value"+color1.blue(pixels[1000]));
        //Log.d("pixel", "value"+(pixels[1000]>>8 & 0xff));

        for (int y=0; y<height; y++){
            if(y < far_a0){
                sigma = sigma_far;
                radius = (int)math.ceil(3*sigma);
                size = (2*radius) + 1;
            }
            else if( (y < far_a1) && (y >= far_a0) ){
                sigma =  (sigma_far * (far_a1 - y)) / (far_a1-far_a0);
                radius = (int)math.ceil(3*sigma);
                size = (2*radius) + 1;
            }
            else if( ( y >= far_a1) && (y<=near_a2) ){
                continue;
            }
            else if( (y > near_a2) && (y <= near_a3) ){
                sigma = (sigma_near * (y - near_a2)) / (near_a3-near_a2);
                radius = (int)math.ceil(3*sigma);
                size = (2*radius) + 1;
            }
            else{
                sigma = sigma_near;
                radius = (int)math.ceil(3*sigma);
                size = (2*radius) + 1;
            }


            for (int x = 0; x<width; x++){
                //Convolution
                p=0;
                BB=0;
                GG=0;
                RR=0;
                AA=0;
                for(int i=0; i<size; i++){
                    for(int j=0; j<size; j++){
                        g[size*i+j] =  (math.exp( - ((i*i)+(j*j))/ (2*(sigma * sigma)) ))  / (2* Math.PI * (sigma * sigma));
                        //Log.d("G", "value="+g[size*i+j]);
                        //Log.d("GValue", "g["+(5*i+j)+"]"+"="+g[5*i+j]);
                        //Log.d("ConvolveLoop", "width="+width);
                        //Log.d("ConvolveLoop", "height="+height);
                        if( (x+(j-radius)) <0 || (y+(i-radius)) <0 || (x+(j-radius))>= width || (y+(i-radius))>= height) {
                            BB=BB;
                            GG=GG;
                            RR=RR;
                            //Log.d("x", "value="+(x+(j-3)));
                            //Log.d("y", "value="+(y+(i-3)));

                        }

                        else {
                            conv = pixels[((y + (i-radius)) * width) + (x + (j-radius))];
                            //Log.d("conv", "value="+conv);
                            //int compute = (((y + (i-2)) * width) + (x + (j-2)));
                            //if(compute > height*width) {
                            //    Log.d("ConvolveLoop", "pixels=" + (((y + (i - 2)) * width) + (x + (j - 2))));
                            //}
                            BB =  (BB + (conv & 0xff) * g[size*i+j]);
                            //Log.d("CONV&0FF","value="+(conv & 0xff));
                            //Log.d("BB", "value="+BB);
                            GG =  (GG + (((conv>>8)& 0xff) * g[size*i+j]));
                            RR =  (RR + (((conv>>16)& 0xff) * g[size*i+j]));
                            AA = (conv>>24)& 0xff;
                        }


                        //AA = (conv>>24)& 0xff;
                        //p = (int) (p + color);
                    }
                }

                //Log.d("BB", "value="+BB);
                //Log.d("GG", "value="+GG);
                //Log.d("RR", "value="+RR);
                color = ( (int)AA & 0xff) << 24 | ( ((int)RR) & 0xff) << 16 | ( ((int)GG) & 0xff) << 8 | ( ((int)BB) & 0xff);
                //Log.d("Color", "value="+color);
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