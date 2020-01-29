package com.snark.walletApp.Activities;

import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.snark.wallet.AddressType;
import com.snark.wallet.SnarkWallet;
import com.snark.walletApp.R;

public class ReceiveActivity extends AppCompatActivity {

    Spinner addrPicker;
    TextView addressLabel;
    ImageView qrCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);


        Toolbar bar = findViewById(R.id.toolbar);
        setSupportActionBar(bar);
        setTitle("Receive");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);



        addrPicker = findViewById(R.id.addrPicker);
        qrCode = findViewById(R.id.qrCode);
        addressLabel = findViewById(R.id.addressLabel);

        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.address_type));
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        addrPicker.setAdapter(myAdapter);

        addrPicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    changeAddressType(AddressType.P2PKH);
                } else if (i == 1) {
                    changeAddressType(AddressType.P2WPKH);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void changeAddressType (AddressType type) {
        String addr = "";
        switch (type) {
            case P2PKH:
                addr = SnarkWallet.shared.getReceiveAddress(AddressType.P2PKH, false);
                break;
           /* case P2SH:
                addr = SnarkWallet.shared.getReceiveAddress(AddressType.P2SH);
                Log.i("Wallet", "P2SH selected");
                break;*/
            case P2WPKH:
                addr = SnarkWallet.shared.getReceiveAddress(AddressType.P2WPKH, false);
                Log.i("Wallet", "Bech32 selected");
                break;
        }
        addressLabel.setText(addr);


        SnarkWallet.shared.saveWallet();

        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode("bitcoin:"+addr, BarcodeFormat.QR_CODE,200,200);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            qrCode.setImageBitmap(bitmap);
        } catch (Exception e) {

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
