// Created by David Centeno on 8/6/23.
// Code found here https://developer.android.com/codelabs/making-waves-1-synth#3
// Updated comments for showing understanding of code and better personal reference.

#include "Oscillator.h"
#include <math.h>
#include <chrono>
#include <thread>
#include <Android/log.h>

#define TWO_PI (3.14159 * 2)
#define AMPLITUDE 0.7
#define FREQUENCY 440.0


//Sets the sample rate of the audio data.
void Oscillator::setSampleRate(int32_t sampleRate) {
    phaseIncrement_ = (TWO_PI * FREQUENCY) / (double) sampleRate;
}

//stores the value of isWaveOn
void Oscillator::setWaveOn(bool isWaveOn) {
    //Makes use of .store method to store value in memory location vs variable.
    isWaveOn_.store(isWaveOn);
}

//adds floating point sine wave values into audioData array each time it is called
void Oscillator::render(float *audioData, int32_t numFrames) {
    cycle = 0;
    //If the wave has not loaded than let it = 0
    if (!isWaveOn_.load()) phase_ = 0;


    //Iterate through the number of frames
    for (int i = 0; i < numFrames * 2; i++) {
        if (isWaveOn_.load()) {
           // TEST A
//                // Calculates the next sample value for the sine wave.
//                audioData[i] = (float) (sin(phase_) * AMPLITUDE);
//
//                // Increments the phase, handling wrap around.
//                phase_ += phaseIncrement_;
//                if (phase_ > TWO_PI) phase_ -= TWO_PI;
//
//                if (numFrames == 960){
//                    cycle += numFrames;
//                    __android_log_print(ANDROID_LOG_INFO, " cycle", "Cycle %i", cycle);
//
//                }
//            if (cycle >= 192000){
//                audioData[i] = 0;
//                cycle = 0;
//
//                __android_log_print(ANDROID_LOG_INFO, " CLICK", "CLICK");

        // TEST B
            audioData[i] = 0;
            if (numFrames == 960){
                    cycle += numFrames;
                    __android_log_print(ANDROID_LOG_INFO, " cycle", "Cycle %i", cycle);
                    sampleSize = cycle % int (interval);
            }
            if (cycle == 48000){
                audioData[i] = (float) (sin(phase_) * AMPLITUDE);
                phase_ += phaseIncrement_;
                if (phase_ > TWO_PI) phase_ -= TWO_PI;
                __android_log_print(ANDROID_LOG_INFO, "Metronome","Click");

                if(cycle == (cycle + 48000)){
                    cycle = 0;
                }
            }

        } else {
            // Outputs silence by setting sample value to zero.
            // This is important because 0 outputs silence and allows the audio stream to continue.
            audioData[i] = 0;
        }
    }
}

void Oscillator::setInterval(double sampleRate) {
    //sampleRate = 48,000
    //bpm is set with the setBpm method
    double mSampleRate = sampleRate;
    double interval = 60.0/ bpm * mSampleRate;
}

void Oscillator::setBpm(int mBpm){
    bpm = mBpm;
}


void Oscillator::countBuffer(int bufferSize){
    totalSamples += bufferSize;
    int samplesRemaining = totalSamples % (int) interval; // How many samples are left before down beat

    if ((samplesRemaining + bufferSize) >= (int) interval){
        __android_log_print(ANDROID_LOG_INFO, "countBuffer","click" );
    }
}