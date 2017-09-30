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

    public static Bitmap tiltshift_java(Bitmap in, int a0, int a1, int a2, int a3, float s_far, float s_near){
        Bitmap out;
        out=in.copy(in.getConfig(),true);

        int width=in.getWidth();
        int height=in.getHeight();

        Log.d("TILTSHIFT_JAVA","hey:"+width+","+height);
        int[] pixels = new int[(width)*(height)];
        int[] result = new int[(width)*(height)];
        int offset=0;
        int stride = width;
        in.getPixels(pixels,offset,stride,0,0,width,height);

        //Defining a kennel matrix, large enough to support up to a sigma value of 6
        double[] g = new double[50*50];

        int far_a0 = a0;
        int far_a1 = a1;
        int near_a2 = a2;
        int near_a3 = a3;
        float sigma_far = s_far;
        float sigma_near = s_near;

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
        int sigma_seven1=0;
        int sigma_seven2=0;

        Color color1 = null;
        Math math = null;

        //Dynamic computation of sigma value for far, near, a1, a2, and a3 regions
        //Pixels for when sigma is less than 0.7 are left alone
        for (int y=0; y<height; y++){
            if(y < far_a0){
                sigma = sigma_far;
                radius = (int)math.ceil(3*sigma);
                size = (2*radius) + 1;
                if(sigma < 0.7f){
                    continue;
                }
            }
            else if( (y < far_a1) && (y >= far_a0) ){
                sigma =  (sigma_far * (far_a1 - y)) / (far_a1-far_a0);
                radius = (int)math.ceil(3*sigma);
                size = (2*radius) + 1;
                if(sigma < 0.7f){
                    sigma_seven1=sigma_seven1+1;
                    continue;
                }
            }
            else if( ( y >= far_a1) && (y<=near_a2) ){
                continue;
            }
            else if( (y > near_a2) && (y <= near_a3) ){
                sigma = (sigma_near * (y - near_a2)) / (near_a3-near_a2);
                radius = (int)math.ceil(3*sigma);
                size = (2*radius) + 1;
                if(sigma < 0.7f){
                    sigma_seven2=sigma_seven2+1;
                    continue;
                }
            }
            else{
                sigma = sigma_near;
                radius = (int)math.ceil(3*sigma);
                size = (2*radius) + 1;
                if(sigma < 0.7f){
                    continue;
                }
            }


            for (int x = 0; x<width; x++){
                //Convolution Algorithm
                p=0;
                BB=0;
                GG=0;
                RR=0;
                AA=0;
                //Log.d("Sigma","value:"+sigma);

                //Evaluating each entry of the Kernel Matrix
                //Computation of convolution is disabled for the region of the image that is in Focus
                for(int i=0; i<size; i++){
                    for(int j=0; j<size; j++) {
                        g[size * i + j] = (math.exp(-(((i - radius) * (i - radius)) + ((j - radius) * (j - radius))) / (2 * (sigma * sigma)))) / (2 * Math.PI * (sigma * sigma));

                        if ((x + (j - radius)) < 0 || (y + (i - radius)) < 0 || (x + (j - radius)) >= width || (y + (i - radius)) >= height) {
                            BB = BB;
                            GG = GG;
                            RR = RR;
                        }
                        else {
                            conv = pixels[((y + (i - radius)) * width) + (x + (j - radius))];
                            BB = (BB + (conv & 0xff) * g[size * i + j]);
                            GG = (GG + (((conv >> 8) & 0xff) * g[size * i + j]));
                            RR = (RR + (((conv >> 16) & 0xff) * g[size * i + j]));
                            AA = (conv >> 24) & 0xff;
                        }
                    }
                }
                color = ( (int)AA & 0xff) << 24 | ( ((int)RR) & 0xff) << 16 | ( ((int)GG) & 0xff) << 8 | ( ((int)BB) & 0xff);
                pixels[y*width+x] = color;
            }
        }
        //Setting the computed pixels into the Bitmap object
        out.setPixels(pixels,offset,stride,0,0,width,height);

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