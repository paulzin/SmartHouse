package com.paulzin.smarthouseandroid.fb

import android.support.design.widget.Snackbar
import android.support.v7.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.paulzin.smarthouseandroid.model.Device
import com.paulzin.smarthouseandroid.model.User
import java.util.*

object FbManager {
    val auth = FirebaseAuth.getInstance()
    val devicesRef = FirebaseDatabase.getInstance().getReference("devices")
    val usersRef = FirebaseDatabase.getInstance().getReference("users")
    val userDevicesRef = FirebaseDatabase.getInstance().getReference("userDevices")
    val currentUser = FirebaseAuth.getInstance().currentUser

    var userDevicesMap : MutableMap<String, Device> = HashMap()

    fun createNewDevice(deviceId: String?, toolbar: Toolbar) {
        if (deviceId.isNullOrEmpty()) return

        val newDevice = Device()
        newDevice.lastUserUid = FbManager.currentUser!!.uid
        newDevice.deviceId = deviceId!!

        FbManager.userDevicesRef.child(FbManager.currentUser!!.uid).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChild(deviceId)) {
                    Snackbar.make(toolbar, "Duplicate: device with ID $deviceId already exists", Snackbar.LENGTH_LONG).show()
                } else {
                    FbManager.devicesRef.addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onCancelled(p0: DatabaseError?) {
                        }

                        override fun onDataChange(data: DataSnapshot) {
                            if (data.hasChild(deviceId)) {
                                newDevice.turnedOn = data.getValue(Device::class.java).turnedOn
                            }
                            Snackbar.make(toolbar, "Added new device with ID: " + deviceId, Snackbar.LENGTH_LONG).show()
                            FbManager.devicesRef.child(deviceId).setValue(newDevice)
                            FbManager.userDevicesRef.child(newDevice.lastUserUid).child(deviceId).setValue(newDevice)
                        }
                    })
                }
            }
        })
    }

    fun fetchDevices(listener: FetchDevicesListener?) {
        FbManager.userDevicesRef.child(FbManager.currentUser!!.uid).addValueEventListener(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                listener?.onCanceled()
            }

            override fun onDataChange(devices: DataSnapshot?) {
                for (item in devices?.children!!) {
                    userDevicesMap[item.key] = item.getValue(Device::class.java)
                }
                listener?.onDataChange(ArrayList(userDevicesMap.values))
            }
        })
    }

    fun listenForDevicesUpdates(toolbar: Toolbar) {
        FbManager.devicesRef.addChildEventListener(object: ChildEventListener {
            override fun onCancelled(p0: DatabaseError?) {
            }

            override fun onChildMoved(p0: DataSnapshot?, p1: String?) {
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot?, p1: String?) {
                val device = dataSnapshot?.getValue(Device::class.java)!!
                val uid = device.lastUserUid

                if (FbManager.currentUser!!.uid == uid
                        || !userDevicesMap.containsKey(dataSnapshot!!.key))
                    return

                FbManager.usersRef.child(uid).addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onCancelled(p0: DatabaseError?) {
                    }

                    override fun onDataChange(data: DataSnapshot?) {
                        val user = data?.getValue(User::class.java)
                        Snackbar.make(toolbar,
                                "${user?.name} just turned ${device.name} "
                                        + if (device.turnedOn) "on" else "off",
                                Snackbar.LENGTH_LONG).show()
                    }
                })

                userDevicesMap[dataSnapshot!!.key] = device
                updateUserDevice(dataSnapshot!!.key, device)
            }

            override fun onChildAdded(p0: DataSnapshot?, p1: String?) {
            }

            override fun onChildRemoved(p0: DataSnapshot?) {
            }
        })
    }

    private fun updateUserDevice(key: String?, device: Device) {
        FbManager.userDevicesRef.child(FbManager.currentUser!!.uid).child(key).setValue(device)
    }

    fun toggleDevice(deviceId: String, newValue: Boolean) {
        val device = FbManager.devicesRef.child(deviceId)
        val userDevice = FbManager.userDevicesRef.child(FbManager.currentUser!!.uid).child(deviceId)

        device.child("lastUserUid").setValue(FbManager.currentUser.uid)
        userDevice.child("lastUserUid").setValue(FbManager.currentUser.uid)

        device.child("turnedOn").setValue(newValue)
        userDevice.child("turnedOn").setValue(newValue)
    }

    fun isSignedIn() : Boolean = auth.currentUser != null
}