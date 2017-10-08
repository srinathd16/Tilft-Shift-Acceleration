#include <jni.h>
#include <string>
#include <arm_neon.h>
#include <math.h>
#include <android/log.h>

using namespace std;

extern "C" {
JNIEXPORT jintArray JNICALL

/**Native C++ implementation of the Speedy Tilt Shift algorithm
 * This method is called by SpeedyTiltShift.java (i.e., Java environment)
 * This method uses JNI components such as jint, jfloat, jintArray, etc., to obtain variables from the Java environment
 * Variables obtained as such are,
 *                                  _pixels       - represeting the pixels array of the input Bitmap image
 *                                  s_far, s_near - absolute boundaries that define the gradient in blur
 *                                  a0-a3         - intermediate boundaries that define the gradient in blur between the absolute boundaries
 * This method,
 *              -Initially unwraps the variables and arrays obtained from the Java environment for relevant local usage.
 *              -Later implements a convolution algorithm using a Kernel Weight Vector which is convoled with the pixels horizontally at first
 *               followed by convolving the pixels vertically.
 *              -Finally, returns the convolved pixels as an array back to the Java (caller) function, where the output Bitmap image is returned.
 *
*/
Java_meteor_asu_edu_speedytiltshift_SpeedyTiltShift_nativeTiltShift(JNIEnv *env,
                                                                    jobject This,
                                                                    jintArray pixels_,
                                                                    jint width, jint height,
                                                                    jint a0, jint a1, jint a2, jint a3, jfloat s_far, jfloat s_near
                                                                    ) {
    ///Declaring and obtaining the pixels of the input Bitmap image from the caller Java function, using jint and jint array
    jint *pixels = env->GetIntArrayElements(pixels_, NULL);
    long length = env->GetArrayLength(pixels_);
    jintArray pixelsOut = env->NewIntArray(length);

    ///Defining a kennel matrix, large enough to support up to a sigma value of 6
    double k[70];


    ///Declaring a and sigma variables for local use
    int far_a0 = a0;
    int far_a1 = a1;
    int near_a2 = a2;
    int near_a3 = a3;
    float sigma_far = s_far;
    float sigma_near = s_near;

    float sigma;
    int radius, size, conv, color;
    double BB, GG, RR, AA;
    int sigma_seven1=0;
    int sigma_seven2=0;

    /**Dynamic computation of sigma value for far, near, a1, a2, and a3 regions
    *Pixels for when sigma is less than 0.7 are left alone
    */

    /**This for-loop is responsible for the kernel to traverse horizontally
    *And is also used to compute Sigma values and the Kernel vectors which remain the same for each iteration of this loop
    */
    for (int y=0; y<height; y++){
        //Computing sigma_far
        if(y < far_a0){
            sigma = sigma_far;
            radius = (int)ceil(3*sigma);
            size = (2*radius) + 1;
            if(sigma < 0.7f){
                continue;
            }
        }
        ///Computing sigma between a0 and a1
        else if( (y < far_a1) && (y >= far_a0) ){
            sigma =  (sigma_far * (far_a1 - y)) / (far_a1-far_a0);
            radius = (int)ceil(3*sigma);
            size = (2*radius) + 1;
            if(sigma < 0.7f){
                sigma_seven1=sigma_seven1+1;
                continue;
            }
        }
        ///Skipping the region between a1 and a2 (Focused region)
        else if( ( y >= far_a1) && (y<=near_a2) ){
            continue;
        }
        ///Computing sigma between a2 and a3
        else if( (y > near_a2) && (y <= near_a3) ){
            sigma = (sigma_near * (y - near_a2)) / (near_a3-near_a2);
            radius = (int)ceil(3*sigma);
            size = (2*radius) + 1;
            if(sigma < 0.7f){
                sigma_seven2=sigma_seven2+1;
                continue;
            }
        }
        ///Computing sigma_near
        else{
            sigma = sigma_near;
            radius = (int)ceil(3*sigma);
            size = (2*radius) + 1;
            if(sigma < 0.7f){
                continue;
            }
        }

        ///Computing the Kernel Vector (used interchangeably for horizontal and vertical operations)
        for(int j=0; j<size; j++) {
            k[j] = (exp(-(((j - radius) * (j - radius))) / (2 * (sigma * sigma)))) / pow((2 * M_PI * (sigma * sigma)), 0.5);
            //__android_log_print(ANDROID_LOG_INFO, "Kernel K", "K[j]= %f", k[j]);
        }

        ///This for-loop is responsible for the kernel to traverse vertically
        for (int x = 0; x<width; x++){
            ///Re-initializing the A, R, G, B components of each pixel for exclusive usage while kernel traverses vertically
            BB=0, GG=0, RR=0, AA=0;
            //Log.d("Sigma","value:"+sigma);

            ///Convolving the kernel vector horizontally
            for(int j=0; j<size; j++){
                {
                    ///Convolution is disabled for the region of the image that is in Focus, by retianing the original pixel values
                    if ((x + (j - radius)) < 0 || (x + (j - radius)) >= width){
                        BB = BB, GG = GG, RR = RR;
                    }
                    else {
                        ///Convolution of the neighboring pixels
                        conv = pixels[((y) * width) + (x + (j - radius))];
                        ///Computing the convolved values of A, R, G, B
                        BB = (BB + (conv & 0xff) * k[j]);
                        GG = (GG + (((conv >> 8) & 0xff) * k[j]));
                        RR = (RR + (((conv >> 16) & 0xff) * k[j]));
                        AA = (conv >> 24) & 0xff;
                    }
                }
            }
            ///Computing the resulting convolved pixel value, later flushed into the output Bitmap image
            color = ( (int)AA & 0xff) << 24 | ( ((int)RR) & 0xff) << 16 | ( ((int)GG) & 0xff) << 8 | ( ((int)BB) & 0xff);
            pixels[y*width+x] = color;

            ///Re-initializing the A, R, G, B components of each pixel for exclusive usage while kernel traverses vertically
            ///Convolving the kernel vector vertically
            BB=0, GG=0, RR=0, AA=0;
            for(int i=0; i<size; i++){
                {
                    if ((y + (i - radius)) < 0 || (y + (i - radius)) >= height){
                        BB = BB, GG = GG, RR = RR;
                    }
                    else {
                        ///Convolution of the neighboring pixels
                        conv = pixels[((y+(i - radius)) * width) + (x)];
                        ///Computing the convolved values of A, R, G, B
                        BB = (BB + (conv & 0xff) * k[i]);
                        GG = (GG + (((conv >> 8) & 0xff) * k[i]));
                        RR = (RR + (((conv >> 16) & 0xff) * k[i]));
                        AA = (conv >> 24) & 0xff;
                    }
                }
            }
            ///Computing the resulting convolved pixel value, later flushed into the output Bitmap image
            color = ( (int)AA & 0xff) << 24 | ( ((int)RR) & 0xff) << 16 | ( ((int)GG) & 0xff) << 8 | ( ((int)BB) & 0xff);
            pixels[y*width+x] = color;

            //__android_log_print(ANDROID_LOG_INFO, "Pixels", "pixels[]= %d", pixels[y*width+x]);
        }

    }

    ///Returning the convolved pixels back to the Java caller function
    env->SetIntArrayRegion(pixelsOut, 0, length, pixels);
    env->ReleaseIntArrayElements(pixels_, pixels, 0);
    return pixelsOut;
}

JNIEXPORT jintArray JNICALL
Java_meteor_asu_edu_speedytiltshift_SpeedyTiltShift_nativeTiltShiftNeon(JNIEnv *env,
                                                                        jobject This,
                                                                        jintArray pixels_,
                                                                        jint width, jint height,
                                                                        jint a0, jint a1, jint a2, jint a3, jfloat s_far, jfloat s_near
) {
    jint *pixels = env->GetIntArrayElements(pixels_, NULL);
    long length = env->GetArrayLength(pixels_);
    //jintArray pixelsOut = env->NewIntArray(length);

    __android_log_print(ANDROID_LOG_INFO, "TAG_ROBLKW", "check1");
    //__android_log_print(ANDROID_LOG_INFO, "TAG_ROBLKW", "length  =  %d", length);

    int far_a0 = a0;
    int far_a1 = a1;
    int near_a2 = a2;
    int near_a3 = a3;
    float sigma_far = s_far;
    float sigma_near = s_near;


    float sigma;
    int radius;
    int size;
    float max_sigma;

    if(sigma_far >= sigma_near){
        max_sigma = sigma_far;
    }
    else{
        max_sigma =sigma_near;
    }

    int max_radius=(int) ceil (3*max_sigma);
    int max_size=(2*max_radius)+1;
    int max_size_chk=max_size + 16-max_size % 16;
    if(max_size < 16){
        max_size_chk=16;
    }
    uint16_t k[max_size_chk];
    double AA;
    int color;

    //pixels[] with zeroes padded
    int new_height = (height+max_size_chk);
    int new_width = (width+max_size_chk);
    int new_pixels[new_height*new_width];
    int new_length = new_width*new_height;
    jintArray pixelsOut = env->NewIntArray(new_length);

    __android_log_print(ANDROID_LOG_INFO, "TAG_ROBLKW", "height    =  %d", height);
    __android_log_print(ANDROID_LOG_INFO, "TAG_ROBLKW", "width =  %d", width);
    __android_log_print(ANDROID_LOG_INFO, "TAG_ROBLKW", "new_length    =  %d", new_length);

    int count=0;

    for(int i=0; i<new_height; i++){
        for(int j=0; j<new_width; j++){
            if(j>=0 && j<new_width && i>=0 && i<max_size_chk/2){
                new_pixels[(i*new_width)+j] = 0;
            }

            if(j>=0 && j<max_size_chk/2 && i>=max_size_chk && i<(height+max_size_chk/2)){
                new_pixels[(i*new_width)+j] = 0;
            }

            if(j>=(width+max_size_chk/2) && j<new_width && i>=max_size_chk/2 && i<(height+max_size_chk/2)){
                new_pixels[(i*new_width)+j] = 0;
            }

            if(i>=(height+max_size_chk/2) && i<new_height && j>=0 && j<new_width){
                new_pixels[(i*new_width)+j] = 0;
            }

            if( i>=max_size_chk/2 && i<(height+max_size_chk/2) && j>=max_size_chk/2 && j<(width+max_size_chk/2)){
                new_pixels[(i*new_width)+j] = pixels[(i-max_size_chk/2)*width + (j-max_size_chk/2)];
                count++;

            }
        }

    }
    __android_log_print(ANDROID_LOG_INFO, "TAG_ROBLKW", "count    =  %d", count);
    //uint8_t * arrayInPtr1 = (uint8_t *) new_pixels;
    uint8_t arrayInPtr[4*new_length];

    for(int i=0; i<new_length; i++){
        arrayInPtr[4*i+3]=(uint8_t)(new_pixels[i] % 256 );
        arrayInPtr[4*i+2]=(uint8_t)(new_pixels[i]>>8 % 256 );
        arrayInPtr[4*i+1]=(uint8_t)(new_pixels[i] >>16 % 256 );
        arrayInPtr[4*i]=(uint8_t)(new_pixels[i] >>24 % 256 );
    }
    uint8_t * arrayInPtr1 ;
    arrayInPtr1 = arrayInPtr ;
    //__android_log_print(ANDROID_LOG_INFO, "Kernel K", "pointer: %d", arrayInPtr1);

    uint16_t * kernPtr = (uint16_t *) k;
    uint16x4_t out_R, out_R1, out_R2, out_R3 ;//= vdupq_n_u16(0);
    uint16x4_t out_G, out_G1, out_G2, out_G3;//= vdupq_n_u16(0);
    uint16x4_t out_B, out_B1, out_B2, out_B3;

    for (int y=0; y<height; y++){
        if(y < far_a0){
            sigma = sigma_far;
            radius = (int)ceil(3*sigma);
            size = (2*radius) + 1;
            if(sigma < 0.7f){
                continue;
            }
        }
        else if( (y < far_a1) && (y >= far_a0) ){
            sigma =  (sigma_far * (far_a1 - y)) / (far_a1-far_a0);
            radius = (int)ceil(3*sigma);
            size = (2*radius) + 1;
            if(sigma < 0.7f){
                continue;
            }
        }
        else if( ( y >= far_a1) && (y<=near_a2) ){
            continue;
        }
        else if( (y > near_a2) && (y <= near_a3) ){
            sigma = (sigma_near * (y - near_a2)) / (near_a3-near_a2);
            radius = (int)ceil(3*sigma);
            size = (2*radius) + 1;
            if(sigma < 0.7f){
                continue;
            }
        }
        else{
            sigma = sigma_near;
            radius = (int)ceil(3*sigma);
            size = (2*radius) + 1;
            if(sigma < 0.7f){
                continue;
            }
        }

        arrayInPtr1 = &arrayInPtr[4*(max_size_chk/2+y)*new_width+ 4*(max_size_chk-radius)] ;

        int twotothe8 = pow(2,8);
        for(int j=0; j<size; j++) {

            k[j] = ((exp(-(((j - radius) * (j - radius))) / (2 * (sigma * sigma)))) / pow((2 * M_PI * (sigma * sigma)), 0.5))*twotothe8;
            //__android_log_print(ANDROID_LOG_INFO, "Kernel K", "K[j]= %f", k[j]);
        }
        //__android_log_print(ANDROID_LOG_INFO, "Kernel K", "check2");
        for(int j=size; j<max_size_chk; j++){
            k[j]=0;
        }



        for (int x = 0; x<width; x++){
            //Convolution Algorithm

            arrayInPtr1=arrayInPtr1+ 4*x;
            //int l = &arrayInPtr1 ;
            if(arrayInPtr[4*(max_size_chk/2+y)*new_width+ 4*max_size_chk/2+4*x+1] != ((pixels[y*width+x]>>16)& 0x00ff )){
                __android_log_print(ANDROID_LOG_INFO, "not equal", "Pointed value= %d actual value= %d",arrayInPtr[4*(max_size_chk/2+y)*new_width+ 4*max_size_chk/2+4*x+1], (pixels[y*width+x]>>16)& 0x00ff )  ;
            }

            //Evaluating each entry of the Kernel Matrix
            //Computation of convolution is disabled for the region of the image that is in Focus

            //Vector-1


            uint16x4_t p= vdup_n_u16(0);
                    out_R = vand_u16(out_R,p);
                    out_R1 = vand_u16(out_R1,p);
                    out_R2= vand_u16(out_R2,p);
                    out_R3 = vand_u16(out_R3,p);
                    out_G = vand_u16(out_G,p);
                    out_G1 = vand_u16(out_G1,p);
                    out_G2 = vand_u16(out_G2,p);
                    out_G3 = vand_u16(out_G3,p);
                    out_B = vand_u16(out_B,p);
                    out_B1= vand_u16(out_B1,p);
                    out_B2 = vand_u16(out_B2,p);
                    out_B3 = vand_u16(out_B3,p);

            int res_R =0, res_G=0, res_B=0;

            for(int i=0; i<size; i=i+16){
                uint16x8_t kern1 = vld1q_u16(kernPtr+i);
                uint16x8_t kern2 = vld1q_u16(kernPtr+8+i);
               // float32x4_t kern3 = vld1q_f32(kernPtr+8+i);
                //float32x4_t kern4 = vld1q_f32(kernPtr+12+i);

                uint16x4_t K11  = vget_low_u16(kern1);
                uint16x4_t K12  = vget_high_u16(kern1);
                uint16x4_t K21  = vget_low_u16(kern2);
                uint16x4_t K22  = vget_high_u16(kern2);

                uint8x16x4_t pix = vld4q_u8(arrayInPtr1+4*i);
                int z = arrayInPtr1[(max_size_chk/2+y)*new_width+ max_size_chk/2+x];
                //for(int i=0; i<4*new_length; i++){
                // __android_log_print(ANDROID_LOG_INFO, "arrayinptr[%d]", "arrayinptr[%d]\tptr= %d", i, arrayInPtr[i]);
                // }//
                uint8x16_t R  =  pix.val[1];
                uint8x8_t  R_low = vget_low_u8(R);
                uint8x8_t  R_high = vget_high_u8(R);

                uint16x8_t R1  = vmovl_u8(R_low);
                uint16x8_t R2  = vmovl_u8(R_high);

                uint16x4_t R11  = vget_low_u16(R1);
                uint16x4_t R12  = vget_high_u16(R1);
                uint16x4_t R21  = vget_low_u16(R2);
                uint16x4_t R22  = vget_high_u16(R2);

               // uint32x4_t R111  = vmovl_u16(R11);
                //uint32x4_t R121  = vmovl_u16(R12);
               // uint32x4_t R212  = vmovl_u16(R21);
               // uint32x4_t R222  = vmovl_u16(R22);



                //Green
                uint8x16_t G  =  pix.val[2];
                uint8x8_t  G_low = vget_low_u8(G);
                uint8x8_t  G_high = vget_high_u8(G);

                uint16x8_t G1  = vmovl_u8(G_low);
                uint16x8_t G2  = vmovl_u8(G_high);

                uint16x4_t G11  = vget_low_u16(G1);
                uint16x4_t G12  = vget_high_u16(G1);
               uint16x4_t G21  = vget_low_u16(G2);
                uint16x4_t G22  = vget_high_u16(G2);

                //uint32x4_t G111  = vmovl_u16(G11);
                //uint32x4_t G121  = vmovl_u16(G12);
                //uint32x4_t G212  = vmovl_u16(G21);
                //uint32x4_t G222  = vmovl_u16(G22);



                //Blue
                uint8x16_t B  =  pix.val[3];
                uint8x8_t  B_low = vget_low_u8(B);
                uint8x8_t  B_high = vget_high_u8(B);

                uint16x8_t B1  = vmovl_u8(B_low);
                uint16x8_t B2  = vmovl_u8(B_high);

                uint16x4_t B11  = vget_low_u16(B1);
                uint16x4_t B12  = vget_high_u16(B1);
                uint16x4_t B21  = vget_low_u16(B2);
                uint16x4_t B22  = vget_high_u16(B2);

               // uint32x4_t B111  = vmovl_u16(B11);
               // uint32x4_t B121  = vmovl_u16(B12);
                //uint32x4_t B212  = vmovl_u16(B21);
               // uint32x4_t B222  = vmovl_u16(B22);



                out_R = vmul_u16(K11,R11);
                out_G = vmul_u16(G11,K11);
                out_B = vmul_u16(B11,K11);

                res_R+=vget_lane_u16(out_R,0);
                res_G+=vget_lane_u16(out_G,0);
                res_B+=vget_lane_u16(out_B,0);

                res_R+=vget_lane_u16(out_R,1);
                res_G+=vget_lane_u16(out_G,1);
                res_B+=vget_lane_u16(out_B,1);

                res_R+=vget_lane_u16(out_R,2);
                res_G+=vget_lane_u16(out_G,2);
                res_B+=vget_lane_u16(out_B,2);

                res_R+=vget_lane_u16(out_R,3);
                res_G+=vget_lane_u16(out_G,3);
                res_B+=vget_lane_u16(out_B,3);

                out_R1=vmul_u16(K12,R12);
                out_G1=vmul_u16(G12,K12);
                out_B1=vmul_u16(B12,K12);

                res_R+=vget_lane_u16(out_R1,0);
                res_G+=vget_lane_u16(out_G1,0);
                res_B+=vget_lane_u16(out_B1,0);

                res_R+=vget_lane_u16(out_R1,1);
                res_G+=vget_lane_u16(out_G1,1);
                res_B+=vget_lane_u16(out_B1,1);

                res_R+=vget_lane_u16(out_R1,2);
                res_G+=vget_lane_u16(out_G1,2);
                res_B+=vget_lane_u16(out_B1,2);

                res_R+=vget_lane_u16(out_R1,3);
                res_G+=vget_lane_u16(out_G1,3);
                res_B+=vget_lane_u16(out_B1,3);

                out_R2=vmul_u16(K21,R21);
                out_G2=vmul_u16(G21,K21);
                out_B2=vmul_u16(B21,K21);

                res_R+=vget_lane_u16(out_R2,0);
                res_G+=vget_lane_u16(out_G2,0);
                res_B+=vget_lane_u16(out_B2,0);

                res_R+=vget_lane_u16(out_R2,1);
                res_G+=vget_lane_u16(out_G2,1);
                res_B+=vget_lane_u16(out_B2,1);

                res_R+=vget_lane_u16(out_R2,2);
                res_G+=vget_lane_u16(out_G2,2);
                res_B+=vget_lane_u16(out_B2,2);

                res_R+=vget_lane_u16(out_R2,3);
                res_G+=vget_lane_u16(out_G2,3);
                res_B+=vget_lane_u16(out_B2,3);

                out_R3=vmul_u16(K22,R22);
                out_G3=vmul_u16(G22,K22);
                out_B3=vmul_u16(B22,K22);

                res_R+=vget_lane_u16(out_R3,0);
                res_G+=vget_lane_u16(out_G3,0);
                res_B+=vget_lane_u16(out_B3,0);

                res_R+=vget_lane_u16(out_R3,1);
                res_G+=vget_lane_u16(out_G3,1);
                res_B+=vget_lane_u16(out_B3,1);

                res_R+=vget_lane_u16(out_R3,2);
                res_G+=vget_lane_u16(out_G3,2);
                res_B+=vget_lane_u16(out_B3,2);

                res_R+=vget_lane_u16(out_R3,3);
                res_G+=vget_lane_u16(out_G3,3);
                res_B+=vget_lane_u16(out_B3,3);







            }

            //__android_log_print(ANDROID_LOG_INFO, "Kernel K", "check5");
            /*
            for(int j=0; j<size; j++){
                {
                    if ((x + (j - radius)) < 0 || (x + (j - radius)) >= width){
                        BB = BB;
                        GG = GG;
                        RR = RR;
                    }
                    else {
                        conv = pixels[((y) * width) + (x + (j - radius))];
                        BB = (BB + (conv & 0xff) * k[j]);
                        GG = (GG + (((conv >> 8) & 0xff) * k[j]));
                        RR = (RR + (((conv >> 16) & 0xff) * k[j]));
                        AA = (conv >> 24) & 0xff;
                    }
                }
            }
            */
            AA = 0xff;
            res_R= res_R/twotothe8;
            res_G= res_G/twotothe8;
            res_B= res_B/twotothe8;
            color = ( (int)AA & 0xff) << 24 | ( ((int)res_R) & 0xff) << 16 | ( ((int)res_G) & 0xff) << 8 | ( ((int)res_B) & 0xff);
            pixels[y*width+x] = color;
            //

            //Vector-2
            /*
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
                        conv = pixels[((y+(i - radius)) * width) + (x)];
                        BB = (BB + (conv & 0xff) * k[i]);
                        GG = (GG + (((conv >> 8) & 0xff) * k[i]));
                        RR = (RR + (((conv >> 16) & 0xff) * k[i]));
                        AA = (conv >> 24) & 0xff;
                    }
                }
            }
            color = ( (int)AA & 0xff) << 24 | ( ((int)RR) & 0xff) << 16 | ( ((int)GG) & 0xff) << 8 | ( ((int)BB) & 0xff);
            */
            //pixels[y*width+x] = color;
            //__android_log_print(ANDROID_LOG_INFO, "Pixels", "pixels[]= %d", pixels[y*width+x]);
        }

        //__android_log_print(ANDROID_LOG_INFO, "Kernel K", "check7");
    }

    //__android_log_print(ANDROID_LOG_INFO, "Kernel K", "check8");

    env->SetIntArrayRegion(pixelsOut, 0, length, pixels);
    env->ReleaseIntArrayElements(pixels_, pixels, 0);
    return pixelsOut;

}
}