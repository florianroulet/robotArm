#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_florian_robotarm_HomeActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello world !!";
    return env->NewStringUTF(hello.c_str());
}
