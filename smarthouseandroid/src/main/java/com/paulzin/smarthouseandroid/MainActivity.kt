package com.paulzin.smarthouseandroid

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.paulzin.smarthouseandroid.adapter.DeviceAdapter
import com.paulzin.smarthouseandroid.fb.FbManager.createNewDevice
import com.paulzin.smarthouseandroid.fb.FbManager.fetchDevices
import com.paulzin.smarthouseandroid.fb.FbManager.isSignedIn
import com.paulzin.smarthouseandroid.fb.FbManager.listenForDevicesUpdates
import com.paulzin.smarthouseandroid.fb.FbManager.toggleDevice
import com.paulzin.smarthouseandroid.fb.FetchDevicesListener
import com.paulzin.smarthouseandroid.model.Device
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {
    val REQUEST_SCAN_BARCODE = 1
    val cameraPermission = "android.permission.CAMERA"
    val adapter = DeviceAdapter(
            { deviceId, newValue -> toggleDevice(deviceId, newValue) },
            { device -> openDeviceDetailsActivity(device) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        setTitle(R.string.home_activity_title)

        initRecyclerView()

        if (!isSignedIn()) {
            openSignInActivity()
            return
        }

        addNewDeviceButton.setOnClickListener { tryToScanBarcode() }

        fetchDevices(object: FetchDevicesListener {
            override fun onCanceled() {
                progressBar.visibility = View.GONE
            }

            override fun onDataChange(devicesList: ArrayList<Device>) {
                adapter.setItems(devicesList)
                emptyListLayout.visibility = if (devicesList.size == 0) View.VISIBLE else View.GONE
                progressBar.visibility = View.GONE
            }
        })

        listenForDevicesUpdates(toolbar)
    }

    private fun initRecyclerView() {
        devicesRecyclerView.adapter = adapter
        devicesRecyclerView.layoutManager = LinearLayoutManager(this)
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

    private fun openDeviceDetailsActivity(device: Device) {
        Snackbar.make(toolbar, device.name, Snackbar.LENGTH_SHORT).show()
    }

    private fun startBarcodeScanActivity() {
        startActivityForResult(Intent(this, BarcodeScannerActivity::class.java), REQUEST_SCAN_BARCODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_SCAN_BARCODE) {
                val deviceId = data?.getStringExtra(BarcodeScannerActivity.EXTRA_DEVICE_ID)
                createNewDevice(deviceId, toolbar)
            }
        }
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

    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, SignInActivity::class.java))
        finish()
    }
}
