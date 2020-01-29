package com.snark.walletApp.Activities;

import android.Manifest;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.snark.wallet.AddressType;
import com.snark.wallet.SnarkWallet;
import com.snark.walletApp.R;

public class QRScannerActiviry extends AppCompatActivity /*implements ZXingScannerView.ResultHandler */{
/*

    ZXingScannerView mScannerView = null;
*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscanner_activiry);
/*


        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 0);

        mScannerView = new ZXingScannerView(this);
        // Set the scanner view as the content view
        setContentView(mScannerView);
*/


    }
/*
    @Override
    public void onResume() {
        super.onResume();
        // Register ourselves as a handler for scan results.
        mScannerView.setResultHandler(this);
        // Start camera on resume
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop camera on pause
        mScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here
        // Prints scan results
        Intent a = new Intent();

        if (rawResult == null) {
            finish();
        }
        a.putExtra("Data", rawResult.toString());
        setResult(RESULT_OK, a);
        finish();
    }*/
}
