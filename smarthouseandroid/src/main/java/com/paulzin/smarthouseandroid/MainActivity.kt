package com.paulzin.smarthouseandroid

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.paulzin.smarthouseandroid.adapter.DeviceAdapter
import com.paulzin.smarthouseandroid.fb.FbManager
import com.paulzin.smarthouseandroid.model.Device
import com.paulzin.smarthouseandroid.model.User
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {
    val REQUEST_SCAN_BARCODE = 1
    val cameraPermission = "android.permission.CAMERA"
    var userDevicesMap : MutableMap<String, Device> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val signedIn = FirebaseAuth.getInstance().currentUser != null
        if (!signedIn) {
            openSignInActivity()
            return
        }

        addNewDeviceButton.setOnClickListener { tryToScanBarcode() }
        fetchDevices()
        listenForDevicesUpdates()

        devicesRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun fetchDevices() {
        FbManager.userDevicesRef.child(FbManager.currentUser!!.uid).addValueEventListener(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                progressBar.visibility = GONE
            }

            override fun onDataChange(devices: DataSnapshot?) {
                for (item in devices?.children!!) {
                    userDevicesMap[item.key] = item.getValue(Device::class.java)
                }

                val devicesList = ArrayList(userDevicesMap.values)
                val adapter = DeviceAdapter(devicesList,
                        { deviceId, newValue -> toggleDevice(deviceId, newValue) })
                devicesRecyclerView.adapter = adapter
                emptyListLayout.visibility = if (devicesList.size === 0) VISIBLE else GONE
                progressBar.visibility = GONE
            }
        })
    }

    private fun listenForDevicesUpdates() {
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.add_new_device -> tryToScanBarcode()
            R.id.sign_out -> signOut()
        }
        return true
    }

    private fun openSignInActivity() {
        startActivity(Intent(this, SignInActivity::class.java))
        finish()
    }

    private fun startBarcodeScanActivity() {
        startActivityForResult(Intent(this, BarcodeScannerActivity::class.java), REQUEST_SCAN_BARCODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_SCAN_BARCODE) {
                val deviceId = data?.getStringExtra(BarcodeScannerActivity.EXTRA_DEVICE_ID)
                createNewDevice(deviceId)
            }
        }
    }

    private fun createNewDevice(deviceId: String?) {
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

    private fun tryToScanBarcode() {
        Dexter.withActivity(this)
                .withPermission(cameraPermission)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse) {
                        startBarcodeScanActivity()
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    }

                    override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest, token: PermissionToken) {
                    }
                }).check()
    }

    fun toggleDevice(deviceId: String, newValue: Boolean) {
        val device = FbManager.devicesRef.child(deviceId)
        val userDevice = FbManager.userDevicesRef.child(FbManager.currentUser!!.uid).child(deviceId)

        device.child("lastUserUid").setValue(FbManager.currentUser.uid)
        userDevice.child("lastUserUid").setValue(FbManager.currentUser.uid)

        device.child("turnedOn").setValue(newValue)
        userDevice.child("turnedOn").setValue(newValue)
    }

    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, SignInActivity::class.java))
        finish()
    }
}
