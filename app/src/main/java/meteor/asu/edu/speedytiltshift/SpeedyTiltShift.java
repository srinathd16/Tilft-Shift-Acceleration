package meteor.asu.edu.speedytiltshift;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import static android.graphics.Bitmap.createBitmap;

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
     *              -Pixels from the input image are obtained using the getPixels function (a function of Bitmap class)
     *              -A kernel vector is then created based on varying values of sigma. Also alongside, convolution of each pixel is computed
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
        float maximumSigma = Math.max(s_near, s_far);
        int kernelSize = (int) (2*(Math.ceil(3*maximumSigma))+1);
        float[] kernelVector = new float[kernelSize];

        /** sigma          - float variable to hold the resultant value of sigma (computed per row)
         * radius          - integer variable representing the radius of the kernel
         * size            - integer value representing 2*radius+1
         * pixelForConvolution - integer value to hold the pixel upon which convolution will be implemented
         * AA/RR/GG/BB     - float values that hold the ARGB channel values of pixelForConvolution
         * convolutedPixel - integer value to hold the value of the convoluted pixel
         * */
        float sigma;
        int radius;
        int size;
        int pixelForConvolution;
        float BB;
        float GG;
        float RR;
        float AA;
        int convolutedPixel;

        /**Creating an object of Math class, later to be used for its "ceil" and "exp" functions
         * */
        Math math = null;

        /**result - Declaring an array to store the (convoluted) pixels of the output image
         *Assigning it to "pixels", to copy all of its content
         *  Purpose: Easier to retain part of the image which should be in-focus(no blur)
         */
        int[] result;
        result = pixels;

        /**This for-loop marks the start of the convolution algorithm - involving Horizontal Kernel operations
         * Variable "y" will ensure vertical traversal through the "pixels" array (input image)
         * Sigma value is dynamically computed for far, near, a1, a2, and a3 regions - all of which are dependent on the vertical axis of the input image
         * Radius and size of the kernel are computed
         * Regions of the input image between a1 and a2 are kept in-focus
         * */
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

            /**This for-loop computes the (horizontal) kernel vector
             * */
            for(int j=0; j<size; j++) {
                kernelVector[j] = (float) ((math.exp(-(((j - radius) * (j - radius))) / (2 * (sigma * sigma)))) / Math.pow((2 * Math.PI * (sigma * sigma)), 0.5));
            }

            /**This for-loop allows traversal of the input image along its width
             * ARGB values of the "result" pixel are initialized to zero
             * */
            for (int x = 0; x<width; x++){
                //Convolution Algorithm
                BB=0;
                GG=0;
                RR=0;
                AA=0;

                /**Actual convolution of pixelForConvolution happens in this for-loop
                 * Logic under "if" ensures that pixels are unaltered beyond the boundaries of the input image, where kernel vector is expected to extend to
                 * Under "else", pixelForConvolution is identified first and ARGB components of it are individually convoluted
                 * Convoluted ARGB values of pixelConvolution are packed into convolutedPixel and is placed in the "result" array (output image)
                 * */
                for(int j=0; j<size; j++){
                    {
                        if ((x + (j - radius)) < 0 || (x + (j - radius)) >= width) {
                            BB = BB;
                            GG = GG;
                            RR = RR;
                        } else {
                            pixelForConvolution = pixels[((y) * width) + (x + (j - radius))];
                            BB = (BB + (pixelForConvolution & 0xff) * kernelVector[j]);
                            GG = (GG + (((pixelForConvolution >> 8) & 0xff) * kernelVector[j]));
                            RR = (RR + (((pixelForConvolution >> 16) & 0xff) * kernelVector[j]));
                            AA = (pixelForConvolution >> 24) & 0xff;
                        }
                    }
                }
                convolutedPixel = ( (int)AA & 0xff) << 24 | ( ((int)RR) & 0xff) << 16 | ( ((int)GG) & 0xff) << 8 | ( ((int)BB) & 0xff);
                result[y*width+x] = convolutedPixel;
            }

        }

        /**This for-loop marks the start of the convolution algorithm - involving Vertical Kernel operations
         * Variable "y" will ensure vertical traversal through the "pixels" array (input image)
         * Sigma value is dynamically computed for far, near, a1, a2, and a3 regions - all of which are dependent on the vertical axis of the input image
         * Radius and size of the kernel are computed
         * Regions of the input image between a1 and a2 are kept in-focus
         * */
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

            /**This for-loop computes the (vertical) kernel vector
             * */
            for(int j=0; j<size; j++) {
                kernelVector[j] = (float) ((math.exp(-(((j - radius) * (j - radius))) / (2 * (sigma * sigma)))) / Math.pow((2 * Math.PI * (sigma * sigma)), 0.5));
            }

            /**This for-loop allows traversal of the input image along its width
             * ARGB values of the "result" pixel are initialized to zero
             * */
            for (int x = 0; x<width; x++){

                /**Actual convolution of pixelForConvolution happens in this for-loop
                 * Logic under "if" ensures that pixels are unaltered beyond the boundaries of the input image, where kernel vector is expected to extend to
                 * Under "else", pixelForConvolution is identified first and ARGB components of it are individually convoluted
                 * Convoluted ARGB values of pixelConvolution are packed into convolutedPixel and is placed in the "result" array (output image)
                 * */
                BB=0;
                GG=0;
                RR=0;
                AA=0;
                for(int i=0; i<size; i++){
                    {
                        if ((y + (i - radius)) < 0 || (y + (i - radius)) >= height){
                            BB = BB;
                            GG = GG;
                            RR = RR;
                        }
                        else {
                            pixelForConvolution = result[((y+(i - radius)) * width) + (x)];
                            BB = (BB + (pixelForConvolution & 0xff) * kernelVector[i]);
                            GG = (GG + (((pixelForConvolution >> 8) & 0xff) * kernelVector[i]));
                            RR = (RR + (((pixelForConvolution >> 16) & 0xff) * kernelVector[i]));
                            AA = (pixelForConvolution >> 24) & 0xff;
                        }
                    }
                }
                convolutedPixel = ( (int)AA & 0xff) << 24 | ( ((int)RR) & 0xff) << 16 | ( ((int)GG) & 0xff) << 8 | ( ((int)BB) & 0xff);
                pixels[y*width+x] = convolutedPixel;
            }

        }

        /**"result" array is flushed into the Bitmap object "out" and is returned back to MainActivity.java where it gets displayed as the output image
         * */
        out.setPixels(pixels,offset,stride,0,0,width,height);
        return out;
    }

    /**"tiltshift_cpp" is the C++ implementation of the Speedy Tilt Shift algorithm
     * This method obtains variables from the caller function in SpeedyTiltShift.java
     * Variables obtained as such, from MainActivity.java are,
     *                                  Bitmap in     - the input image whose blurring is to be performed is obtained as a Bitmap object
     *                                  s_far, s_near - absolute boundaries that define the gradient in blur
     *                                  a0-a3         - intermediate boundaries that define the gradient in blur between the absolute boundaries
     * This method,
     *              -Initially creates a Bitmap object "out" with the same configuration as that of the input image, to be outputted as the final blurred image
     *              -Pixels from the input image are obtained using the getPixels function (a function of Bitmap class)
     *              -Method "nativeTiltShift" that implements native C++ is called. pixels, width, height, a0-a3, s_far/near are passed as parameters
     *              -A "result" array is maintained to contain the convoluted pixels, obtained from nativeTiltShift, which is returned back to MainActivity.java
     * */
    public static Bitmap tiltshift_cpp(Bitmap in, int a0, int a1, int a2, int a3, float s_far, float s_near){
        Bitmap out;
        out=in.copy(in.getConfig(),true);
        int width = in.getWidth();
        int height = in.getHeight();
        int offset=0;
        int stride = width;

        int[] pixels = new int[width*height];
        in.getPixels(pixels,offset,stride,0,0,width,height);

        int[] result = new int[width*height];
        result = nativeTiltShift(pixels, width, height, a0, a1, a2, a3, s_far, s_near);

        //Log.d("TILTSHIFT_CPP","width: "+width+" height:"+height);

        out.setPixels(result,offset,stride,0,0,width,height);
        return out;
    }

    /**"tiltshift_neon" is the NEON implementation of the Speedy Tilt Shift algorithm
     * This method obtains variables from the caller function in SpeedyTiltShift.java
     * Variables obtained as such, from MainActivity.java are,
     *                                  Bitmap in     - the input image whose blurring is to be performed is obtained as a Bitmap object
     *                                  s_far, s_near - absolute boundaries that define the gradient in blur
     *                                  a0-a3         - intermediate boundaries that define the gradient in blur between the absolute boundaries
     * This method,
     *              -Initially creates a Bitmap object "out" with the same configuration as that of the input image, to be outputted as the final blurred image
     *              -Pixels from the input image are obtained using the getPixels function (a function of Bitmap class)
     *              -Method "nativeTiltShiftNeon" that implements native NEON is called. pixels, width, height, a0-a3, s_far/near are passed as parameters
     *              -A "result" array is maintained to contain the convoluted pixels, obtained from nativeTiltShift, which is returned back to MainActivity.java
     * */
    public static Bitmap tiltshift_neon(Bitmap in, int a0, int a1, int a2, int a3, float s_far, float s_near){
        Bitmap out;
        out=in.copy(in.getConfig(),true);
        int inWidth = in.getWidth();
        int inHeight = in.getHeight();
        int offset=0;
        int stride = inWidth;

        int[] pixels = new int[inWidth*inHeight];
        in.getPixels(pixels,offset,stride,0,0,inWidth,inHeight);

        int[] result = new int[inWidth*inHeight];
        result = nativeTiltShiftNeon(pixels, inWidth, inHeight, a0, a1, a2, a3, s_far, s_near);

        out.setPixels(result,offset,stride,0,0,inWidth,inHeight);

        return out;
    }

    /**Prototypes for nativeTiltShift and nativeTiltShiftNeon
     * */
    private static native int[] nativeTiltShift(int[] pixels, int imgW, int imgH, int a0, int a1, int a2, int a3, float s_far, float s_near);
    private static native int[] nativeTiltShiftNeon(int[] pixels, int imgW, int imgH, int a0, int a1, int a2, int a3, float s_far, float s_near);

}