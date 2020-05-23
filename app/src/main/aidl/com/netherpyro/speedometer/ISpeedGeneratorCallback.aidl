// ISpeedGeneratorCallback.aidl
package com.netherpyro.speedometer;

interface ISpeedGeneratorCallback {

    void onSpeedValue(double value);
    void onRpmValue(double value);
}
