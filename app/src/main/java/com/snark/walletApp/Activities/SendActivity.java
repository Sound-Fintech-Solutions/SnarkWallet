package com.snark.walletApp.Activities;

import android.Manifest;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethod;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.snark.wallet.AddressType;
import com.snark.wallet.SnarkWallet;
import com.snark.walletApp.R;

import org.bitcoinj.core.Coin;

public class SendActivity extends AppCompatActivity {

    EditText amountText;
    EditText addressText;

    long amount = 0;
    long fee = 1;

    String address;

    Boolean amountDidConvert = true;

    SeekBar feeBar;

    TextView feeText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);


        Toolbar bar = findViewById(R.id.toolbar);
        setSupportActionBar(bar);
        setTitle("Send");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);





        amountText  = findViewById(R.id.amountInput);
        addressText = findViewById(R.id.addressInput);
        feeBar      = findViewById(R.id.feeBar);
        feeText     = findViewById(R.id.feeLabel);

        feeBar.setMax(SnarkWallet.shared.feeFast);

        feeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

              //  feeText.setText(String.valueOf(progress) +" "+ getString(R.string.fee));
                fee = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        amountText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    if (amount == 0) {
                        amountText.setText("");
                    } else {
                        amountText.setText(String.valueOf(amount));
                    }
                    amountDidConvert = false;
                } else {
                    if (!amountDidConvert) {
                        amount = Long.parseLong(amountText.getText().toString());
                        amountText.setText(Coin.valueOf(amount).toFriendlyString());
                    }
                    amountDidConvert = true;
                }
            }
        });

        amountText.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    if (!amountDidConvert) {
                        try {
                            amount = Integer.valueOf(amountText.getText().toString());
                        } catch (Exception e) {
                            amount = 0;
                        }
                        amountText.setText(Coin.valueOf(amount).toFriendlyString());
                    }
                    amountDidConvert = true;
                    amountText.clearFocus();



                    InputMethodManager imm = (InputMethodManager)getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(findViewById(R.id.sendLayout).getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });





    }

    public void sendButtonPressed (View view) {
        if (SnarkWallet.shared.sendCoins(address, amount, fee)) {
//         if (true) {
            SnarkWallet.shared.saveWallet();
            Intent cr = new Intent(this, TransactionSentActivity.class);
            startActivity(cr);
            finish();
        }
    }

    public void qrCodePressed (View view) {
        Intent cr = new Intent(this, QRScannerActiviry.class);
        startActivityForResult(cr, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && data != null) {
            String a[] = data.getStringExtra("Data").split(":");
            if (a.length == 2) {
                String addr = a[1];
                address = addr;
                addressText.setText(address);
            }
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
