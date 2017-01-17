package com.paulzin.smarthouse.hardware

import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManagerService
import timber.log.Timber
import java.io.IOException


object GpioManager {
    private val gpioName = "BCM20"
    private val manager = PeripheralManagerService()
    private var gpio : Gpio? = null

    fun open(): Unit {
        try {
            gpio = manager.openGpio(gpioName)
            gpio?.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH)
            gpio?.setActiveType(Gpio.ACTIVE_LOW)
        } catch (e: IOException) {
            Timber.w("Unable to access GPIO", e)
        }
    }

    fun close(): Unit {
        try {
            gpio?.close()
        } catch (e: IOException) {
            Timber.w("Unable to close GPIO", e)
        }
        gpio = null
    }

    fun setValue(value : Boolean): Unit {
        gpio?.value = value
    }
}