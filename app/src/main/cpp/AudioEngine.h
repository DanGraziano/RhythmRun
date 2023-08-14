//
// Created by David Centeno on 8/6/23.
//

#ifndef RHYTHMRUN_AUDIOENGINE_H
#define RHYTHMRUN_AUDIOENGINE_H
#include <aaudio/AAudio.h>
#include "Oscillator.h"

class AudioEngine {
public:
    bool start();
    void stop();
    void restart();
    void setToneOn(bool isToneOn);
    int mBpm;

private:
    Oscillator oscillator_;
    AAudioStream *stream_;
    int bpm = 120;
    float sampleRate = 0.0;
    int interval = 60.0 / bpm * sampleRate;
    int bufferSize = 0;
};


#endif //RHYTHMRUN_AUDIOENGINE_H
