package com.snark.walletApp.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.snark.wallet.AddressType;
import com.snark.wallet.SnarkWallet;
import com.snark.walletApp.R;

import org.apache.commons.lang3.StringUtils;

public class CreateWalletActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_wallet);

        SnarkWallet.shared.createNewWallet();

        TextView textView = findViewById(R.id.wordsTextView);

        textView.setText(StringUtils.join(SnarkWallet.shared.getMnemonic(), "\n"));


    }


    public void GoNext (View view) {

        SnarkWallet.shared.saveWallet();

        Intent cr = new Intent(this, MainActivity.class);
        startActivity(cr);

        finish();

    }
}
