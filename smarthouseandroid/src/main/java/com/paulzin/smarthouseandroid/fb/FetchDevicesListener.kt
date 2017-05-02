package com.paulzin.smarthouseandroid.fb

import com.paulzin.smarthouseandroid.model.Device
import java.util.*

interface FetchDevicesListener {
    fun onCanceled()
    fun onDataChange(devicesList: ArrayList<Device>)
}