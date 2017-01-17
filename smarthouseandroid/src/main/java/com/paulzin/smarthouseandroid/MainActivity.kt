package com.paulzin.smarthouseandroid

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private val cameraPermission = "android.permission.CAMERA"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        addNewDeviceButton.setOnClickListener { checkCameraPermission() }
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
}
