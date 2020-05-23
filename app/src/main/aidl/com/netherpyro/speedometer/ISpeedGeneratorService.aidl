// ISpeedGeneratorService.aidl
package com.netherpyro.speedometer;

import com.netherpyro.speedometer.ISpeedGeneratorCallback;

interface ISpeedGeneratorService {

    int getMaxSpeed();
    int getMaxRpm();

    void registerCallback(ISpeedGeneratorCallback callback);
    void ungisterCallback();
}
