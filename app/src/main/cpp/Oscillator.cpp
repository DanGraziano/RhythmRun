//
// Created by David Centeno on 8/6/23.
// Code found here https://developer.android.com/codelabs/making-waves-1-synth#3
// Updated comments for showing understanding of code and better personal reference.

#include "Oscillator.h"
#include <math.h>

#define TWO_PI (3.14159 * 2)
#define AMPLITUDE 0.3
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

    //If the wave has not loaded than let it = 0
    if (!isWaveOn_.load()) phase_ = 0;

    //Iterate through the number of frames
    for (int i = 0; i < numFrames; i++) {

        if (isWaveOn_.load()) {

            // Calculates the next sample value for the sine wave.
            audioData[i] = (float) (sin(phase_) * AMPLITUDE);

            // Increments the phase, handling wrap around.
            phase_ += phaseIncrement_;
            if (phase_ > TWO_PI) phase_ -= TWO_PI;

        } else {
            // Outputs silence by setting sample value to zero.
            // This is important because 0 outputs silence and allows the audio stream to continue.
            audioData[i] = 0;
        }
    }
}
