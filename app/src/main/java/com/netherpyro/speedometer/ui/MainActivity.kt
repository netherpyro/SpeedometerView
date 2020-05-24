package com.netherpyro.speedometer.ui

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.netherpyro.speedometer.ISpeedGeneratorCallback
import com.netherpyro.speedometer.ISpeedGeneratorService
import com.netherpyro.speedometer.R
import com.netherpyro.speedometer.generator.SpeedGeneratorService
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), ServiceConnection {

    companion object {
        private const val TAG = "MainActivity"
    }

    private var service: ISpeedGeneratorService? = null
    private val mainHandler = Handler()

    private val generatorCallback = object : ISpeedGeneratorCallback.Stub() {

        override fun onSpeedValue(value: Double) {
            mainHandler.post { onGeneratorSpeedValue(value) }
        }

        override fun onRpmValue(value: Double) {
            mainHandler.post { onGeneratorRpmValue(value) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val serviceIntent = Intent(applicationContext, SpeedGeneratorService::class.java)
        startService(serviceIntent)
    }

    override fun onStart() {
        super.onStart()

        val serviceIntent = Intent(applicationContext, SpeedGeneratorService::class.java)
        bindService(serviceIntent, this, 0)
    }


    override fun onStop() {
        super.onStop()

        try {
            service?.ungisterCallback()
        } catch (e: RemoteException) {
            Log.e(TAG, "onServiceConnected::unregister callback failed", e)
        }

        unbindService(this)
        service = null
    }

    override fun onDestroy() {
        if (isFinishing) {
            stopService(Intent(applicationContext, SpeedGeneratorService::class.java))
        }

        super.onDestroy()
    }

    override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
        service = ISpeedGeneratorService.Stub.asInterface(binder)

        try {
            speedometerView.maxValue = service?.maxSpeed ?: -1.0
            tachometerView.maxValue = service?.maxRpm ?: -1.0
            service?.registerCallback(generatorCallback)
        } catch (e: RemoteException) {
            Log.e(TAG, "onServiceConnected::register callback failed", e)
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        this.service = null
    }

    fun onGeneratorSpeedValue(value: Double) {
        speedometerView.currentValue = value
    }

    fun onGeneratorRpmValue(value: Double) {
        tachometerView.currentValue = value
    }
}
