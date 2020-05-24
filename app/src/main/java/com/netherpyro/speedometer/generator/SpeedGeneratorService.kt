package com.netherpyro.speedometer.generator

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Message
import com.netherpyro.speedometer.ISpeedGeneratorCallback
import com.netherpyro.speedometer.ISpeedGeneratorService
import kotlin.math.sin

/**
 * @author mmikhailov on 20.05.2020.
 */
class SpeedGeneratorService : Service() {

    private val maxKmH = 190.0
    private val maxRPM = 8000.0

    private lateinit var thread: SpeedGeneratorThread

    private val binder = object : ISpeedGeneratorService.Stub() {

        override fun getMaxSpeed(): Double = maxKmH

        override fun getMaxRpm(): Double = maxRPM

        override fun registerCallback(callback: ISpeedGeneratorCallback?) {
            thread.requestRegisterCallback(callback)
        }

        override fun ungisterCallback() {
            thread.requestUnregisterCallback()
        }
    }

    override fun onCreate() {
        super.onCreate()

        thread = SpeedGeneratorThread(maxKmH, maxRPM)
        thread.start()
        thread.requestStart()
    }

    override fun onDestroy() {
        thread.requestStop()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    @Suppress("PrivatePropertyName")
    private class SpeedGeneratorThread(
            private val maxKmh: Double,
            private val maxRpm: Double
    ) : HandlerThread("SpeedGeneratorThread"), Handler.Callback {

        private val START = 0
        private val STOP = 1
        private val REGISTER_CALLBACK = 2
        private val UNREGISTER_CALLBACK = 3

        private val frameTime = 16L
        private val frameRad = 2 * Math.PI / frameTime

        private var running = true
        private lateinit var handler: Handler

        private var callback: ISpeedGeneratorCallback? = null

        @Synchronized
        override fun start() {
            super.start()
            handler = Handler(looper, this)
        }

        override fun handleMessage(msg: Message): Boolean {
            if (!isAlive) {
                return false
            }

            return when (msg.what) {
                START -> startGenerate()
                STOP -> stopGenerate()
                REGISTER_CALLBACK -> registerCallback(msg.obj as ISpeedGeneratorCallback)
                UNREGISTER_CALLBACK -> unregisterCallback()
                else -> false
            }
        }

        fun requestStart() {
            handler.sendEmptyMessage(START)
        }

        fun requestStop() {
            handler.sendEmptyMessage(STOP)
        }

        fun requestRegisterCallback(callback: ISpeedGeneratorCallback?) {
            handler.sendMessage(handler.obtainMessage(REGISTER_CALLBACK, callback))
        }

        fun requestUnregisterCallback() {
            handler.sendEmptyMessage(UNREGISTER_CALLBACK)
        }

        fun registerCallback(callback: ISpeedGeneratorCallback?): Boolean {
            this.callback = callback

            return true
        }

        fun unregisterCallback(): Boolean {
            this.callback = null

            return true
        }

        private fun startGenerate(): Boolean {
            Thread(Runnable {
                var currentFrameRad = frameRad
                val amplitude = maxKmh / 2
                while (running) {
                    val value = amplitude + amplitude * sin(0.1 * currentFrameRad)

                    callback?.onSpeedValue(value)

                    currentFrameRad += frameRad
                    Thread.sleep(frameTime)
                }
            }).start()

            Thread(Runnable {
                var currentFrameRad = frameRad
                val amplitude = maxRpm / 2
                while (running) {
                    val value = amplitude + amplitude * sin(0.2 * currentFrameRad)

                    callback?.onRpmValue(value)

                    currentFrameRad += frameRad
                    Thread.sleep(frameTime)
                }
            }).start()

            return true
        }

        private fun stopGenerate(): Boolean {
            running = false

            interrupt()
            quit()

            return true
        }
    }
}