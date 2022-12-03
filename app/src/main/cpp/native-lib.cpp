#include <jni.h>
#include <string>
#include <opencv2/opencv.hpp>

using namespace cv;

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_autolight_1android_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_autolight_1android_control_1light_ControlLightActivity_getLight(JNIEnv *env, jobject thiz, jlong mat_addr_input) {
    int meanOutput;
    Mat &matInput = *(Mat *)mat_addr_input;
    meanOutput = (int)mean(matInput)[0];
    return meanOutput;
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_example_autolight_1android_customize_1standard_CustomizeStandardActivity_getLight(
        JNIEnv *env, jobject thiz, jlong mat_addr_input) {
    int meanOutput;
    Mat &matInput = *(Mat *)mat_addr_input;
    meanOutput = (int)mean(matInput)[0];
    return meanOutput;
}