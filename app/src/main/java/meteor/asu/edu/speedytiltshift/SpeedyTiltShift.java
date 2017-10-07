package meteor.asu.edu.speedytiltshift;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import static android.graphics.Bitmap.createBitmap;

/**
 * Created by roblkw on 7/26/17.
 */
/**This file/class is called by MainActivity.java
 * Includes the class SpeedyTilt Shift, containing calls to functions that implement the Speedy Tilt Shift Algorithm in Java, C++, and NEON respectively
 *
 */

/**This class contains three functions,
 *                                      -tiltshift_java - contains the Java implementation of the Speedy Tilt Shift algorithm
 *                                      -tiltshift_cpp - contains the C++ implementation of the Speedy Tilt Shift algorithm
 *                                      -tiltshift_neon - contains the NEON implementation of the Speedy Tilt Shift algorithm
 *
 */
public class SpeedyTiltShift {

    static {
        System.loadLibrary("nativetiltshift-lib");
    }

    /**"tiltshift_java" is the Java implementation of the Speedy Tilt Shift algorithm
     * This method obtains variables from the caller function in SpeedyTiltShift.java
     * Variables obtained as such, from MainActivity.java are,
     *                                  Bitmap in     - the input image whose blurring is to be performed is obtained as a Bitmap object
     *                                  s_far, s_near - absolute boundaries that define the gradient in blur
     *                                  a0-a3         - intermediate boundaries that define the gradient in blur between the absolute boundaries
     * This method,
     *              -Initially creates a Bitmap object "out" with the same configuration as that of the input image, to be outputted as the final blurred image
     *              -Pixels from the input image are obtained using the getPixels function (a function Bitmap class)
     *               followed by convolving the pixels vertically
     *              -A kernel vector is then created based on varying values of sigma. Also alongside, convolved value of each pixel is computed
     *              -All convolved pixels are collated into an array and is flushed into the Bitmap object "out" which is indeed the blurred image
     */
    public static Bitmap tiltshift_java(Bitmap in, int a0, int a1, int a2, int a3, float s_far, float s_near){
        /**Creating a Bitmap object "out" to store the output(blurred) image and assigning it the same configuration as that of the input image (image to be blurred)
         */
        Bitmap out;
        out = in.copy(in.getConfig(), true);

        /**Obtaining dimensions of the input image to be used to build an array of input-image-pixels
         */
        int width=in.getWidth();
        int height=in.getHeight();

        Log.d("TILTSHIFT_JAVA","hey:"+width+","+height);

        /**Following set of variables are required to be supplied as inputs to the "getPixels" function to retrieve pixels from the input image
         * pixels - creating an integer array to store the pixels of the input image
         * offset - integer variable to define the number of pixels to offset the image by
         *          Set to "zero" such that the index is at the first pixel of the input image
         * stride - integet variable to define the number of pixels to skip per row, if the value is less than that of the width of the input image
         *          Setting it to "width" such that no pixels are skipped
         *
         * "getPixels" is a function of Bitmap class, used to extract pixels from the input image
         */
        int[] pixels = new int[(width)*(height)];
        int offset=0;
        int stride = width;
        in.getPixels(pixels,offset,stride,0,0,width,height);

        /**Creating a Kernel vector of type float
         * Kernel vector size is computed using 2(ceil(3*maximum_value_of_sigma))+1
         * maximumSigma and kernelSize are integer type intermediate variables in computing the size of the kernel vector
         * */
        int maximumSigma = (int) Math.max(s_near, s_far);
        int kernelSize = (int) (2*(Math.ceil(3*maximumSigma))+1);
        float[] kernelVector = new float[kernelSize];

        /** sigma          - float variable to hold the resultant value of sigma (computed per row)
         * radius          - integer variable representing the radius of the kernel
         * size            - integer value representing 2*radius+1
         * convolutedPixel -
         * */
        float sigma;
        int radius;
        int size;
        int conv;
        float BB;
        float GG;
        float RR;
        float AA;
        int color;

        Math math = null;

        /**result - Declaring an array to store the (convoluted) pixels of the output image
         */
        int[] result;
        result = pixels;

        //Dynamic computation of sigma value for far, near, a1, a2, and a3 regions
        //Pixels for when sigma is less than 0.7 are left alone
        for (int y=0; y<height; y++){
            if(y < a0){
                sigma = s_far;
                radius = (int)math.ceil(3*sigma);
                size = (2*radius) + 1;
                if(sigma < 0.7f){
                    continue;
                }
            }
            else if( (y < a1) && (y >= a0) ){
                sigma =  (s_far * (a1 - y)) / (a1-a0);
                radius = (int)math.ceil(3*sigma);
                size = (2*radius) + 1;
                if(sigma < 0.7f){
                    continue;
                }
            }
            else if( ( y >= a1) && (y<=a2) ){
                continue;
            }
            else if( (y > a2) && (y <= a3) ){
                sigma = (s_near * (y - a2)) / (a3-a2);
                radius = (int)math.ceil(3*sigma);
                size = (2*radius) + 1;
                if(sigma < 0.7f){
                    continue;
                }
            }
            else{
                sigma = s_near;
                radius = (int)math.ceil(3*sigma);
                size = (2*radius) + 1;
                if(sigma < 0.7f){
                    continue;
                }
            }

            for(int j=0; j<size; j++) {
                kernelVector[j] = (float) ((math.exp(-(((j - radius) * (j - radius))) / (2 * (sigma * sigma)))) / Math.pow((2 * Math.PI * (sigma * sigma)), 0.5));
            }


                    for (int x = 0; x<width; x++){
                //Convolution Algorithm
                BB=0;
                GG=0;
                RR=0;
                AA=0;
                //Log.d("Sigma","value:"+sigma);

                //Evaluating each entry of the Kernel Matrix
                //Computation of convolution is disabled for the region of the image that is in Focus

                //Vector-1
                for(int j=0; j<size; j++){
                    {
                        //kernelVector[j] = (math.exp(-(((j - radius) * (j - radius))) / (2 * (sigma * sigma)))) / Math.pow((2 * Math.PI * (sigma * sigma)),0.5);

                        if ((x + (j - radius)) < 0 || (x + (j - radius)) >= width) {
                            BB = BB;
                            GG = GG;
                            RR = RR;
                        } else {
                            conv = pixels[((y) * width) + (x + (j - radius))];
                            BB = (BB + (conv & 0xff) * kernelVector[j]);
                            GG = (GG + (((conv >> 8) & 0xff) * kernelVector[j]));
                            RR = (RR + (((conv >> 16) & 0xff) * kernelVector[j]));
                            AA = (conv >> 24) & 0xff;
                        }
                    }
                }
                color = ( (int)AA & 0xff) << 24 | ( ((int)RR) & 0xff) << 16 | ( ((int)GG) & 0xff) << 8 | ( ((int)BB) & 0xff);
                result[y*width+x] = color;

                //Vector-2
                BB=0;
                GG=0;
                RR=0;
                AA=0;
                for(int i=0; i<size; i++){
                    {
                        //kernelVector[i] = (math.exp(-(((i - radius) * (i - radius))) / (2 * (sigma * sigma)))) / Math.pow((2 * Math.PI * (sigma * sigma)),0.5);

                        if ((y + (i - radius)) < 0 || (y + (i - radius)) >= height){
                            BB = BB;
                            GG = GG;
                            RR = RR;
                        }
                        else {
                            conv = pixels[((y+(i - radius)) * width) + (x)];
                            BB = (BB + (conv & 0xff) * kernelVector[i]);
                            GG = (GG + (((conv >> 8) & 0xff) * kernelVector[i]));
                            RR = (RR + (((conv >> 16) & 0xff) * kernelVector[i]));
                            AA = (conv >> 24) & 0xff;
                        }
                    }
                }
                color = ( (int)AA & 0xff) << 24 | ( ((int)RR) & 0xff) << 16 | ( ((int)GG) & 0xff) << 8 | ( ((int)BB) & 0xff);
                result[y*width+x] = color;
            }

        }

        //Setting the computed pixels into the Bitmap object
        out.setPixels(result,offset,stride,0,0,width,height);

        return out;
    }
    public static Bitmap tiltshift_cpp(Bitmap in, int a0, int a1, int a2, int a3, float s_far, float s_near){
        Bitmap out;
        out=in.copy(in.getConfig(),true);
        int imgW = in.getWidth();
        int imgH = in.getHeight();
        int offset=0;
        int stride = imgW;

        int[] pixels = new int[(imgW)*(imgH)];
        in.getPixels(pixels,offset,stride,0,0,imgW,imgH);

        nativeTiltShift(pixels, imgW, imgH, a0, a1, a2, a3, s_far, s_near);

        Log.d("TILTSHIFT_CPP","width: "+imgW+" height:"+imgH);

        out.setPixels(pixels,offset,stride,0,0,imgW,imgH);
        return out;
    }
    public static Bitmap tiltshift_neon(Bitmap in, int a0, int a1, int a2, int a3, float s_far, float s_near){
        //Bitmap out;

        //out=in.copy(in.getConfig(),true);


        int imgW = in.getWidth(); int imgW1 = imgW+18;
        int imgH = in.getHeight(); int imgH1 = imgH+18;
        Bitmap out = createBitmap(imgW1, imgH1, in.getConfig());
        Log.d("TILTSHIFT_CPP","width: "+out.getWidth()+" height:"+out.getHeight()+" config:"+out.getConfig());
        out.setHeight(imgH1);
        out.setWidth(imgW1);
        Log.d("TILTSHIFT_CPP","width: "+out.getWidth()+" height:"+out.getHeight()+" config:"+out.getConfig());
        int offset=0;
        int stride = imgW;
        int stride1 = imgW1;

        int[] pixels = new int[(imgW)*(imgH)];
        //int[] pixels1 = new int[(imgW+18)*(imgH+18)];
        in.getPixels(pixels,offset,stride,0,0,imgW,imgH);


        nativeTiltShiftNeon(pixels, imgW, imgH, a0, a1, a2, a3, s_far, s_near);
        //out.setPixels(pixels1,offset,stride,0,0,imgW1,imgH1);
        out.setPixels(pixels,offset,stride,0,0,imgW,imgH);

        return out;
    }
    private static native int[] nativeTiltShift(int[] pixels, int imgW, int imgH, int a0, int a1, int a2, int a3, float s_far, float s_near);
    private static native int[] nativeTiltShiftNeon(int[] pixels, int imgW, int imgH, int a0, int a1, int a2, int a3, float s_far, float s_near);

}