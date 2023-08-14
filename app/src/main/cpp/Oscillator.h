//
// Created by David Centeno on 8/6/23.
//

#ifndef RHYTHMRUN_OSCILLATOR_H
#define RHYTHMRUN_OSCILLATOR_H

#include <atomic>
#include <stdint.h>
#include <math.h>


#define TWO_PI (3.14159 * 2)

class Oscillator {
public:
    void setWaveOn(bool isWaveOn);
    void setSampleRate(int32_t sampleRate);
    void render(float *audioData, int32_t numFrames);
    void setInterval(double sampleRate);
    void setBpm(int mBpm);
    void countBuffer(int bufferSize);

private:
    std::atomic<bool> isWaveOn_{false};
    double phase_ = 0.0;
    double phaseIncrement_ = 0.0;
    double interval = 0.0;
    int bpm = 0;
    int totalSamples = 0;
    int sampleSize = 0;
    int32_t cycle = 0;

};
#endif //RHYTHMRUN_OSCILLATOR_H
