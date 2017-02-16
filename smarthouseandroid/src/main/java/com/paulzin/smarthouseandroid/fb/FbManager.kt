package com.paulzin.smarthouseandroid.fb

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object FbManager {
    val devicesRef = FirebaseDatabase.getInstance().getReference("devices")!!
    val usersRef = FirebaseDatabase.getInstance().getReference("users")!!
    val userDevicesRef = FirebaseDatabase.getInstance().getReference("userDevices")!!
    val currentUser = FirebaseAuth.getInstance().currentUser

    fun toggleDevice(deviceId: String, newValue: Boolean) {
        val device = FbManager.devicesRef.child(deviceId)
        val userDevice = FbManager.userDevicesRef.child(FbManager.currentUser!!.uid).child(deviceId)

        device.child("lastUserUid").setValue(FbManager.currentUser.uid)
        userDevice.child("lastUserUid").setValue(FbManager.currentUser.uid)

        device.child("turnedOn").setValue(newValue)
        userDevice.child("turnedOn").setValue(newValue)
    }
}