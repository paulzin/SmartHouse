package com.paulzin.smarthouseandroid.fb

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object FbManager {
    val devicesRef = FirebaseDatabase.getInstance().getReference("devices")
    val usersRef = FirebaseDatabase.getInstance().getReference("users")
    val userDevicesRef = FirebaseDatabase.getInstance().getReference("userDevices")
    val currentUser = FirebaseAuth.getInstance().currentUser
}