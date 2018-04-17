package com.paulzin.smarthouse

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.paulzin.smarthouse.hardware.GpioManager
import com.paulzin.smarthouse.utils.PiUtils.getPiSerial
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class ThingsActivity : AppCompatActivity() {
    val devicesRef = FirebaseDatabase.getInstance().getReference("devices")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        GpioManager.open()

        devicesRef.child(getPiSerial()).child("turnedOn").addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError?) {
                Timber.e(error?.message)
            }

            override fun onDataChange(newStatus: DataSnapshot?) {
                val deviceIsOn = newStatus?.value as Boolean
                GpioManager.setValue(deviceIsOn)
                stateTextView.text = if (deviceIsOn) "On" else "Off"
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        GpioManager.close()
    }
}
