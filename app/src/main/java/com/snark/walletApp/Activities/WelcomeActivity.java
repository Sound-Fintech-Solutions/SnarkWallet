package com.snark.walletApp.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.snark.wallet.AddressType;
import com.snark.wallet.SnarkWallet;
import com.snark.walletApp.R;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
    }



    public void goToCreateNewWallet (View view) {
        Intent cr = new Intent(this, CreateWalletActivity.class);
        startActivity(cr);
    }

    public void goToResroreWallet (View view) {
        Intent cr = new Intent(this, RestoreWalletActivity.class);
        startActivity(cr);
    }

}
