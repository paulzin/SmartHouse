package com.paulzin.smarthouseandroid

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView


class BarcodeScannerActivity : Activity(), ZXingScannerView.ResultHandler {
    private var mScannerView: ZXingScannerView? = null

    public override fun onCreate(state: Bundle?) {
        super.onCreate(state)

        mScannerView = ZXingScannerView(this)
        setContentView(mScannerView)
    }

    public override fun onResume() {
        super.onResume()
        mScannerView?.setResultHandler(this)
        mScannerView?.startCamera()
    }

    public override fun onPause() {
        super.onPause()
        mScannerView?.stopCamera()
    }

    override fun handleResult(rawResult: Result) {
        mScannerView?.resumeCameraPreview(this)

        val intent = Intent()
        intent.putExtra(EXTRA_DEVICE_ID, rawResult.text)
        setResult(RESULT_OK, intent)
        finish()
    }

    companion object {
        val EXTRA_DEVICE_ID : String = "deviceIdExtra"
    }
}