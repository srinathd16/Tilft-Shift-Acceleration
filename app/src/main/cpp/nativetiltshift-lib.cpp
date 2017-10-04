#include <jni.h>
#include <string>
#include <arm_neon.h>
#include <math.h>
#include <android/log.h>

extern "C" {
JNIEXPORT jintArray JNICALL
Java_meteor_asu_edu_speedytiltshift_SpeedyTiltShift_nativeTiltShift(JNIEnv *env,
                                                                    jobject This,
                                                                    jintArray pixels_,
                                                                    jint width, jint height,
                                                                    jint a0, jint a1, jint a2, jint a3, jfloat s_far, jfloat s_near
                                                                    ) {
    int32x4_t sum_vec = vdupq_n_s32(0);
    jint *pixels = env->GetIntArrayElements(pixels_, NULL);
    long length = env->GetArrayLength(pixels_);
    jintArray pixelsOut = env->NewIntArray(length);

    //Defining a kennel matrix, large enough to support up to a sigma value of 6
    double k[70];

/*
    for(int i=0; i<50; i++){
        k[i]=0;
    }

    for(int x=0; x<length; x++){
            __android_log_print(ANDROID_LOG_INFO, "Kernel K", "K[j]= %d", pixels[x]);
    }
*/

    //__android_log_print(ANDROID_LOG_INFO, "Params", "a0= %d, a1 = %d, a2 = %d, a3 = %d, s_far = %f, s_near = %f", a0, a1, a2, a3, s_far, s_near);


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


    //Dynamic computation of sigma value for far, near, a1, a2, and a3 regions
    //Pixels for when sigma is less than 0.7 are left alone
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
                sigma_seven1=sigma_seven1+1;
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
                sigma_seven2=sigma_seven2+1;
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

        for(int j=0; j<size; j++) {
            k[j] = (exp(-(((j - radius) * (j - radius))) / (2 * (sigma * sigma)))) / pow((2 * M_PI * (sigma * sigma)), 0.5);
            //__android_log_print(ANDROID_LOG_INFO, "Kernel K", "K[j]= %f", k[j]);
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

            //Vector-1
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
            color = ( (int)AA & 0xff) << 24 | ( ((int)RR) & 0xff) << 16 | ( ((int)GG) & 0xff) << 8 | ( ((int)BB) & 0xff);
            pixels[y*width+x] = color;

            //Vector-2
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
            pixels[y*width+x] = color;
            //__android_log_print(ANDROID_LOG_INFO, "Pixels", "pixels[]= %d", pixels[y*width+x]);
        }

    }

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
    int32x4_t sum_vec = vdupq_n_s32(0);
    jint *pixels = env->GetIntArrayElements(pixels_, NULL);
    long length = env->GetArrayLength(pixels_);
    jintArray pixelsOut = env->NewIntArray(length);

    //__android_log_print(ANDROID_LOG_INFO, "TAG_ROBLKW", "width*height	=	%d", width*height);
    //__android_log_print(ANDROID_LOG_INFO, "TAG_ROBLKW", "length	=	%d", length);

    uint8_t arrayIn[10];
    uint8_t arrayOut[10];
    for(int i=0; i<10; i++){
        arrayOut[i]=0;
        arrayIn[i]=i%10;
        __android_log_print(ANDROID_LOG_INFO, "TAG_ROBLKW", "arrayOut[%d]	=	%d", i, arrayOut[i]);
        __android_log_print(ANDROID_LOG_INFO, "TAG_ROBLKW", "arrayIn[%d]	=	%d", i, arrayIn[i]);
    }

    uint8_t * arrayInPtr = (uint8_t *) arrayIn;
    uint8_t * arrayOutPtr = (uint8_t *) arrayOut;
    uint8x8_t r, g, b;
    uint8x8x4_t deinterleaved;

    deinterleaved = vld4_u8(arrayInPtr);
    //for (int i=0; i<5; i++) {

        r = deinterleaved.val[1];
        __android_log_print(ANDROID_LOG_INFO, "TAG_ROBLKW", "R[0]	=	%d, R[0]	=	%d, R[0]	=	%d, R[0]	=	%d, R[0]	=	%d", vget_lane_u8(r, 0), vget_lane_u8(r, 1), vget_lane_u8(r, 2),vget_lane_u8(r, 3), vget_lane_u8(r, 4));
    //}

/*
    uint8_t	arrayIn[1000];
    uint8_t	arrayOut[1000];
    for(int i=0; i<1000; i++){
        arrayOut[i]=0;
    }
    int ilength	=	1000;

    for	(int y=0;	y<ilength;	y++)	{
        arrayIn[y]=	y%128; } //

    for	(int y=0;	y<ilength;	y++) {
        __android_log_print(ANDROID_LOG_INFO, "TAG_ROBLKW", "arrayIn[%d]	=	%d", y, arrayIn[y]);
    }
    uint8_t	*	arrayInPtr =	(uint8_t	*)arrayIn;
    uint8_t	*	arrayOutPtr =	(uint8_t	*)arrayOut;
    uint8x16_t	G;
    for	(int i=0;i<3;i++){
        uint8x16x4_t	vecs =	vld4q_u8(arrayInPtr);
        uint8x16_t	R	=	vecs.val[1];
        //__android_log_print(ANDROID_LOG_INFO,	"TAG_ROBLKW",	"R[0]	=	%d, R[1]	=	%d , R[2]	=	%d, R[4]	=	%d",	vgetq_lane_u8(R,0), vgetq_lane_u8(R,1), vgetq_lane_u8(R,2), vgetq_lane_u8(R,3));
        	G	=	vecs.val[2];
        //__android_log_print(ANDROID_LOG_INFO,	"TAG_ROBLKW",	"G[0]	=	%d",	vgetq_lane_u8(G,0));
        //uint8x16_t	B	=	vecs.val[3];
        uint8x16_t	result	=	vaddq_u8(R,G);

        vst1q_u8(arrayOutPtr+16*i,result);
        //__android_log_print(ANDROID_LOG_INFO,	"TAG_ROBLKW",	"arrayOut[0]	=	%d",	arrayOut[0]); __android_log_print(ANDROID_LOG_INFO,	"TAG_ROBLKW",	"arrayOut[1]	=	%d",	arrayOut[1]); __android_log_print(ANDROID_LOG_INFO,	"TAG_ROBLKW",	"arrayOut[2]	=	%d",	arrayOut[2]); __android_log_print(ANDROID_LOG_INFO,	"TAG_ROBLKW",	"arrayOut[3]	=	%d",	arrayOut[3]); __android_log_print(ANDROID_LOG_INFO,	"TAG_ROBLKW",	"arrayOut[4]	=	%d",	arrayOut[4]); __android_log_print(ANDROID_LOG_INFO,	"TAG_ROBLKW",	"arrayOut[5]	=	%d",	arrayOut[5]); __android_log_print(ANDROID_LOG_INFO,	"TAG_ROBLKW",	"arrayOut[14]	=	%d",	arrayOut[14]); __android_log_print(ANDROID_LOG_INFO,	"TAG_ROBLKW",	"arrayOut[15]	=	%d",	arrayOut[15]); __android_log_print(ANDROID_LOG_INFO,	"TAG_ROBLKW",	"arrayOut[16]	=	%d",	arrayOut[16]);
        arrayInPtr +=64;
    }

    arrayOutPtr = 0;
    for(int i=0; i<1000; i++){
        __android_log_print(ANDROID_LOG_INFO,	"TAG_ROBLKW",	"arrayOut[%d]	=	%d",i,	arrayOut[i]);
    }

*/
/*
    //Defining a kennel matrix, large enough to support up to a sigma value of 6
    double k[70];

    int far_a0 = a0;
    int far_a1 = a1;
    int near_a2 = a2;
    int near_a3 = a3;
    float sigma_far = s_far;
    float sigma_near = s_near;

    //Pixel Arrays
    uint8x16x4_t pixels_vector;
    uint8x16_t R;
    uint8x16_t G;
    uint8x16_t B;
    uint8_t * pixelsIn = (uint8_t *) pixels;

    float sigma;
    int radius;
    int size;
    int conv;

    int color;
    int sigma_seven1=0;
    int sigma_seven2=0;


/*
    //pixels[] with zeroes padded
    int new_height = (height+size);
    int new_width = (width+size);
    int new_pixels[new_height*new_width];

    int new_length = new_width*new_height;
    jintArray pixelsOut = env->NewIntArray(new_length);

    for(int i=0; i<new_height; i++){
        for(int j=0; j<new_width; j++){
            if(j>=0 && j<new_width){
                new_pixels[i*(new_width+j)] = 0;
            }

            if(j>=0 && i<size/2 && i>=size/2 && i<(height+size/2)){
                new_pixels[i*(new_width+j)] = 0;
            }

            if(j>=(width+size/2) && j<new_width && i>=size/2 && i<(height+size/2)){
                new_pixels[i*(new_width+j)] = 0;
            }

            if(i>=(height+size/2) && i<=new_height && j>=0 && j<new_width){
                new_pixels[i*(new_width+j)] = 0;
            }

            else{
                new_pixels[i*(new_width+j)] = pixels[(i-size/2)*width + (j-size/2)];
            }
        }

    }

/*
    //Dynamic computation of sigma value for far, near, a1, a2, and a3 regions
    //Pixels for when sigma is less than 0.7 are left alone
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
                sigma_seven1=sigma_seven1+1;
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
                sigma_seven2=sigma_seven2+1;
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

        for(int j=0; j<size; j++) {
            k[j] = (exp(-(((j - radius) * (j - radius))) / (2 * (sigma * sigma)))) / pow((2 * M_PI * (sigma * sigma)), 0.5);
            //__android_log_print(ANDROID_LOG_INFO, "Kernel K", "K[j]= %f", k[j]);
        }
        uint64_t * kIn = (uint64_t *) k;

        for (int x = 0; x<width; x+16){
            //Convolution Algorithm
            B=0;
            G=0;
            R=0;
            //Log.d("Sigma","value:"+sigma);

            pixels_vector = vld4q_u8(pixelsIn);
            R = pixels_vector.val[1];
            G = pixels_vector.val[2];
            B = pixels_vector.val[3];
            //Evaluating each entry of the Kernel Matrix
            //Computation of convolution is disabled for the region of the image that is in Focus

            //Vector-1
            for(int j=0; j<size; j++){
                {
                    if ((x + (j - radius)) < 0 || (x + (j - radius)) >= width){
                        B = B;
                        G = G;
                        R = R;
                    }
                    else {
                        kIn = kIn+25;
                        vmlaq_u8(R, (uint8x16_t) kIn, );
                        conv = pixels[((y) * width) + (x + (j - radius))];
                        BB = (BB + (conv & 0xff) * k[j]);
                        GG = (GG + (((conv >> 8) & 0xff) * k[j]));
                        RR = (RR + (((conv >> 16) & 0xff) * k[j]));
                        AA = (conv >> 24) & 0xff;
                    }
                }
            }
            color = ( (int)AA & 0xff) << 24 | ( ((int)RR) & 0xff) << 16 | ( ((int)GG) & 0xff) << 8 | ( ((int)BB) & 0xff);
            pixels[y*width+x] = color;

            //Vector-2
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
            pixels[y*width+x] = color;
            //__android_log_print(ANDROID_LOG_INFO, "Pixels", "pixels[]= %d", pixels[y*width+x]);
        }

    }
*/
    env->SetIntArrayRegion(pixelsOut, 0, length, pixels);
    env->ReleaseIntArrayElements(pixels_, pixels, 0);
    return pixelsOut;
}
}