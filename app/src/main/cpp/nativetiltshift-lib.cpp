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
    double k[50];

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

    //
    uint8_t	arrayIn[1000];
    uint8_t	arrayOut[1000];
    int ilength	=	1000; for	(int y=0;	y<ilength;	y++)	{
        arrayIn[y]=	y%128; } //
    __android_log_print(ANDROID_LOG_INFO,	"TAG_ROBLKW",	"arrayIn[5]	=	%d",	arrayIn[5]);
    uint8_t	*	arrayInPtr =	(uint8_t	*)arrayIn;
    for	(int i=0;i<3;i++){
        uint8x16x4_t	vecs =	vld4q_u8(arrayInPtr);
        uint8x16_t	R	=	vecs.val[1];
        __android_log_print(ANDROID_LOG_INFO,	"TAG_ROBLKW",	"R[0]	=	%d, R[1]	=	%d , R[2]	=	%d, R[4]	=	%d",	vgetq_lane_u8(R,0), vgetq_lane_u8(R,1), vgetq_lane_u8(R,2), vgetq_lane_u8(R,3));
        uint8x16_t	G	=	vecs.val[2];
        //__android_log_print(ANDROID_LOG_INFO,	"TAG_ROBLKW",	"G[0]	=	%d",	vgetq_lane_u8(G,0));
        uint8x16_t	B	=	vecs.val[3]; uint8x16_t	result	=	vaddq_u8(R,G);
        vst1q_u16((unsigned	short	*)arrayOut,result);
        __android_log_print(ANDROID_LOG_INFO,	"TAG_ROBLKW",	"arrayOut[0]	=	%d",	arrayOut[0]); __android_log_print(ANDROID_LOG_INFO,	"TAG_ROBLKW",	"arrayOut[1]	=	%d",	arrayOut[1]); __android_log_print(ANDROID_LOG_INFO,	"TAG_ROBLKW",	"arrayOut[2]	=	%d",	arrayOut[2]); __android_log_print(ANDROID_LOG_INFO,	"TAG_ROBLKW",	"arrayOut[3]	=	%d",	arrayOut[3]); __android_log_print(ANDROID_LOG_INFO,	"TAG_ROBLKW",	"arrayOut[4]	=	%d",	arrayOut[4]); __android_log_print(ANDROID_LOG_INFO,	"TAG_ROBLKW",	"arrayOut[5]	=	%d",	arrayOut[5]); __android_log_print(ANDROID_LOG_INFO,	"TAG_ROBLKW",	"arrayOut[14]	=	%d",	arrayOut[14]); __android_log_print(ANDROID_LOG_INFO,	"TAG_ROBLKW",	"arrayOut[15]	=	%d",	arrayOut[15]); __android_log_print(ANDROID_LOG_INFO,	"TAG_ROBLKW",	"arrayOut[16]	=	%d",	arrayOut[16]);
        arrayInPtr +=64;
    }
//
    env->SetIntArrayRegion(pixelsOut, 0, ilength, pixels);
    env->ReleaseIntArrayElements(pixels_, pixels, 0);
    return pixelsOut;
}
}