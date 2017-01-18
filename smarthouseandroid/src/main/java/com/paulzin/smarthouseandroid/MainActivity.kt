package com.paulzin.smarthouseandroid

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.paulzin.smarthouseandroid.model.Device
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_device.view.*


class MainActivity : AppCompatActivity() {
    private val cameraPermission = "android.permission.CAMERA"
    var devicesAdapter: FirebaseRecyclerAdapter<Device, DevicesHolder>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val signedIn = FirebaseAuth.getInstance().currentUser != null
        if (!signedIn) {
            openSignInActivity()
            return
        }
        addNewDeviceButton.setOnClickListener { checkCameraPermission() }
        setSupportActionBar(toolbar)

        devicesRecyclerView.layoutManager = LinearLayoutManager(this)

        val deviceRef = FirebaseDatabase.getInstance().getReference("devices")

        devicesAdapter = object : FirebaseRecyclerAdapter<Device, DevicesHolder>(
                Device::class.java, R.layout.item_device, DevicesHolder::class.java, deviceRef) {
            public override fun populateViewHolder(deviceView: DevicesHolder, device: Device, position: Int) {
                deviceView.bindDevice(device)
            }

            override fun onDataChanged() {
                emptyListLayout.visibility = if (devicesAdapter!!.itemCount === 0) View.VISIBLE else View.INVISIBLE
            }
        }
        devicesRecyclerView.adapter = devicesAdapter
    }

    private fun openSignInActivity() {
        startActivity(Intent(this, SignInActivity::class.java))
        finish()
    }

    private fun startBarcodeScanActivity() {
        startActivity(Intent(this, BarcodeScannerActivity::class.java))
    }

    private fun checkCameraPermission() {
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

    fun changeStatus(status : Boolean) {
        val db = FirebaseDatabase.getInstance().reference
        db.child("status").setValue(status)
    }

    class DevicesHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindDevice(device: Device) {
            with(device) {
                itemView.deviceName.text = name
            }
        }
    }
}
