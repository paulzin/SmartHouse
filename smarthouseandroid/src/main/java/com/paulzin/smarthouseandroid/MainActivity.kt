package com.paulzin.smarthouseandroid

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import com.google.firebase.auth.FirebaseAuth
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
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    val REQUEST_SCAN_BARCODE = 1
    val cameraPermission = "android.permission.CAMERA"

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

        devicesRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun fetchDevices() {
        FbManager.userDevicesRef.child(FbManager.currentUser!!.uid).addValueEventListener(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(devices: DataSnapshot?) {
                val devicesList = devices?.children!!.map { it?.getValue(Device::class.java)!! }
                val adapter = DeviceAdapter(devicesList,
                        { deviceId, newValue -> toggleDevice(deviceId, newValue) })
                devicesRecyclerView.adapter = adapter
                emptyListLayout.visibility = if (devicesList.size === 0) VISIBLE else INVISIBLE
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.add_new_device -> tryToScanBarcode()
            R.id.remove_devices -> removeDevices()
            R.id.sign_out -> signOut()
        }
        return true
    }

    private fun removeDevices() {
        FbManager.userDevicesRef.removeValue()
        FbManager.devicesRef.removeValue()
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
        newDevice.currentUid = FbManager.currentUser!!.uid
        newDevice.deviceId = deviceId!!

        FbManager.devicesRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChild(deviceId)) {
                    Snackbar.make(toolbar, "Device with id $deviceId is already added", Snackbar.LENGTH_LONG).show()
                } else {
                    Snackbar.make(toolbar, "Device id: " + deviceId, Snackbar.LENGTH_LONG).show()
                    FbManager.devicesRef.child(deviceId).setValue(newDevice)
                    FbManager.userDevicesRef.child(newDevice.currentUid).child(deviceId).setValue(newDevice)
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

        device.child("turnedOn").setValue(newValue)
        userDevice.child("turnedOn").setValue(newValue)
    }

    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, SignInActivity::class.java))
        finish()
    }
}
