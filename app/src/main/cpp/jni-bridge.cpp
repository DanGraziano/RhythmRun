//
// Created by David Centeno on 8/7/23.
//
#include <jni.h>
#include <android/input.h>
#include "AudioEngine.h"

static AudioEngine *audioEngine = new AudioEngine();

extern "C" {

    JNIEXPORT void JNICALL
    Java_edu_northeastern_rhythmrun_MetronomeTest_touchEvent(JNIEnv *env, jobject obj, jint action) {
        switch (action) {
            case AMOTION_EVENT_ACTION_DOWN:
                audioEngine->setToneOn(true);
                break;
            case AMOTION_EVENT_ACTION_UP:
                audioEngine->setToneOn(false);
                break;
            default:
                break;
        }
    }

    JNIEXPORT void JNICALL
    Java_edu_northeastern_rhythmrun_MetronomeTest_startEngine(JNIEnv *env, jobject /* this */) {
    audioEngine->start();
    }

    JNIEXPORT void JNICALL
    Java_edu_northeastern_rhythmrun_MetronomeTest_stopEngine(JNIEnv *env, jobject /* this */) {
    audioEngine->stop();
    }

}
