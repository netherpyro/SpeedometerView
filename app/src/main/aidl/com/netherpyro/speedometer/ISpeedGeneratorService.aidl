// ISpeedGeneratorService.aidl
package com.netherpyro.speedometer;

import com.netherpyro.speedometer.ISpeedGeneratorCallback;

interface ISpeedGeneratorService {

    double getMaxSpeed();
    double getMaxRpm();

    void registerCallback(ISpeedGeneratorCallback callback);
    void ungisterCallback();
}
