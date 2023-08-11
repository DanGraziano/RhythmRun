//
// Created by David Centeno on 8/7/23.
//
#include <jni.h>
#include <android/input.h>
#include "AudioEngine.h"
#include <iostream>
#include <thread>

static AudioEngine *audioEngine = new AudioEngine();

extern "C" {

    JNIEXPORT void JNICALL
    Java_edu_northeastern_rhythmrun_MetronomeTest_startMetronome(JNIEnv *env, jobject  /* this */) {
                audioEngine->setToneOn(true);
    }

    JNIEXPORT void JNICALL
    Java_edu_northeastern_rhythmrun_MetronomeTest_stopMetronome(JNIEnv *env, jobject  /* this */) {
        audioEngine->setToneOn(false);
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
