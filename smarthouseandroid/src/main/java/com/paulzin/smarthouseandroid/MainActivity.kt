package com.paulzin.smarthouseandroid

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
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
    val cameraPermission = "android.permission.CAMERA"

    val devicesRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("devices")
    val useresRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")
    val userDevicesRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("usersDevices")

    var devicesAdapter: FirebaseRecyclerAdapter<Device, DevicesHolder>? = null

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

        devicesAdapter = DeviceAdapter(this)

        devicesRecyclerView.layoutManager = LinearLayoutManager(this)
        devicesRecyclerView.adapter = devicesAdapter
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
        startActivity(Intent(this, BarcodeScannerActivity::class.java))
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
        val device = devicesRef.child(deviceId)
        device.child("turnedOn").setValue(newValue)
    }

    inner class DeviceAdapter(val activity : Activity) : FirebaseRecyclerAdapter<Device, DevicesHolder>(
            Device::class.java, R.layout.item_device, DevicesHolder::class.java, userDevicesRef.child(FirebaseAuth.getInstance().currentUser?.uid)) {

        public override fun populateViewHolder(deviceView: DevicesHolder, device: Device, position: Int) {
            deviceView.bindDevice(device, activity)
        }

        override fun onDataChanged() {
            emptyListLayout.visibility = if (itemCount === 0) VISIBLE else INVISIBLE
        }
    }

    public class DevicesHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindDevice(device: Device, activity: Activity) {
            with(device) {
                itemView.deviceName.text = name
                itemView.deviceSwitch.isChecked = turnedOn
                Glide.with(activity).load(imageUrl).into(itemView.deviceImageView)
            }
        }
    }

    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, SignInActivity::class.java))
        finish()
    }
}
