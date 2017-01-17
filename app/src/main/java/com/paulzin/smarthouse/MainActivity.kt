package com.paulzin.smarthouse

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.firebase.database.*
import com.paulzin.smarthouse.hardware.GpioManager
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    val db: DatabaseReference = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        GpioManager.open()

        db.child("status").addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                Timber.e(p0?.message)
            }

            override fun onDataChange(status: DataSnapshot?) {
                GpioManager.setValue(status?.value as Boolean)
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        GpioManager.close()
    }
}
