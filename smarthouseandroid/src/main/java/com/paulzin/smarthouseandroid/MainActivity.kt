package com.paulzin.smarthouseandroid

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        deviceSwitch.setOnCheckedChangeListener { button, value -> changeStatus(value)}
    }

    fun changeStatus(status : Boolean) {
        val db = FirebaseDatabase.getInstance().reference
        db.child("status").setValue(status)
    }
}
