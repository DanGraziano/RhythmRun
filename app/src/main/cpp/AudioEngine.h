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

private:
    Oscillator oscillator_;
    AAudioStream *stream_;
};


#endif //RHYTHMRUN_AUDIOENGINE_H
