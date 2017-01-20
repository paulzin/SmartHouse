package com.paulzin.smarthouse.utils

import io.paperdb.Paper
import java.io.BufferedReader
import java.io.InputStreamReader

object PiUtils {
    val PI_SERIAL_KEY = "piSerial"

    fun getPiSerial(): String {
        var raspberryPiSerial = Paper.book().read<String>(PI_SERIAL_KEY)
        if (!raspberryPiSerial.isNullOrBlank()) return raspberryPiSerial

        val process = Runtime.getRuntime().exec("cat /proc/cpuinfo")
        val s = BufferedReader(InputStreamReader(process.inputStream)).use { it.readText() }

        raspberryPiSerial = s.substring(s.indexOf("Serial")).substring(s.indexOf(":")).trim()
        Paper.book().write(PI_SERIAL_KEY, raspberryPiSerial)

        return raspberryPiSerial
    }
}