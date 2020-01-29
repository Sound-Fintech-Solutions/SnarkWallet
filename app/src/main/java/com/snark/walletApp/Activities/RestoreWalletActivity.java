package com.snark.walletApp.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.snark.wallet.AddressType;
import com.snark.wallet.SnarkWallet;
import com.snark.walletApp.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RestoreWalletActivity extends AppCompatActivity {

    EditText mnemonicTextView;
    Button enterButton;

    boolean started = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restore_wallet);
        mnemonicTextView = findViewById(R.id.mnemonicTextView);
        enterButton      = findViewById(R.id.enterButton);
    }


    public void enterButtonPressed (View view) {
        if (started) {
            return;
        }
        started = true;
        view.setVisibility(View.INVISIBLE);
        List<String> a = new ArrayList<String>();

        a = Arrays.asList(mnemonicTextView.getText().toString().split(" "));
        SnarkWallet.shared.restoreWallet(a);
        SnarkWallet.shared.saveWallet();
        Intent cr= new Intent(getApplicationContext(), MainActivity.class);
        startActivity(cr);
    }
}
