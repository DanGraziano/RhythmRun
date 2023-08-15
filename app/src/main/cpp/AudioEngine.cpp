//
// Created by David Centeno on 8/6/23.
//

#include <android/log.h>
#include "AudioEngine.h"
#include <thread>
#include <mutex>
#include <iostream>
#include <chrono>
#include <android/log.h>


// Double-buffering offers a good tradeoff between latency and protection against glitches.
constexpr int32_t kBufferSizeInBursts = 2;

aaudio_data_callback_result_t dataCallback(
        AAudioStream *stream,
        void *userData,
        void *audioData,
        int32_t numFrames) {

    ((Oscillator *) (userData))->render(static_cast<float *>(audioData), numFrames);
    //((Oscillator *)) (userData))->countBuffer(bufferSize)
    return AAUDIO_CALLBACK_RESULT_CONTINUE;
}

// If there is an error starting or creating the audio stream this method is triggered nad will try and restart the stream
// A condition which this may be triggered is if the users switches audio outputs while listening to the metronome.
void errorCallback(AAudioStream *stream,
                   void *userData,
                   aaudio_result_t error){
    if (error == AAUDIO_ERROR_DISCONNECTED){
        std::function<void(void)> restartFunction = std::bind(&AudioEngine::restart,
                                                              static_cast<AudioEngine *>(userData));
        new std::thread(restartFunction);
    }
}

bool AudioEngine::start() {
    //Stream builder
    AAudioStreamBuilder *streamBuilder;
    AAudio_createStreamBuilder(&streamBuilder);
    AAudioStreamBuilder_setFormat(streamBuilder, AAUDIO_FORMAT_PCM_FLOAT);
    AAudioStreamBuilder_setChannelCount(streamBuilder, 1);
    AAudioStreamBuilder_setPerformanceMode(streamBuilder, AAUDIO_PERFORMANCE_MODE_LOW_LATENCY);
    AAudioStreamBuilder_setDataCallback(streamBuilder, ::dataCallback, &oscillator_);
    AAudioStreamBuilder_setErrorCallback(streamBuilder, ::errorCallback, this);

    // ---- Stream Settings ---- //

    // Opens the stream (Audio stream of the device).
    aaudio_result_t result = AAudioStreamBuilder_openStream(streamBuilder, &stream_);
    // Stops the stream and prints error.
    if (result != AAUDIO_OK) {
        __android_log_print(ANDROID_LOG_ERROR, "AudioEngine", "Error opening stream %s",
                            AAudio_convertResultToText(result));
        return false;
    }

    // SAMPLE RATE INFO //
    //Gets the sample rate 48,000
    int32_t sampleRate = AAudioStream_getSampleRate(stream_);

    //Provide Oscillator Sample Rate of 48000
    oscillator_.setSampleRate(sampleRate);
    oscillator_.setBpm(60);
    oscillator_.setInterval(sampleRate);


    //BUFFER INFO //
    // Sets the buffer size Size = 1920;
    AAudioStream_setBufferSizeInFrames(
            stream_, AAudioStream_getFramesPerBurst(stream_) * kBufferSizeInBursts);

    //Gets Buffer size
     bufferSize = AAudioStream_getBufferSizeInFrames(stream_);

    // Starts the stream //
    result = AAudioStream_requestStart(stream_);
    if (result != AAUDIO_OK) {
        __android_log_print(ANDROID_LOG_ERROR, "AudioEngine", "Error starting stream %s",
                            AAudio_convertResultToText(result));
        return false;
    }

    AAudioStreamBuilder_delete(streamBuilder);
    return true;
}

void AudioEngine::restart(){

    static std::mutex restartingLock;
    if (restartingLock.try_lock()){
        stop();
        start();
        restartingLock.unlock();
    }
}

void AudioEngine::stop() {
    if (stream_ != nullptr) {
        AAudioStream_requestStop(stream_);
        AAudioStream_close(stream_);
    }
}

void AudioEngine::setToneOn(bool isToneOn) {
    oscillator_.setWaveOn(isToneOn);
}
