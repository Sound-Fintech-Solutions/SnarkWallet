package com.snark.walletApp.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.snark.wallet.AddressType;
import com.snark.wallet.SnarkWallet;
import com.snark.walletApp.R;

public class Balance extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance);

        try {
            //SnarkWallet.shared.loadWallet();
            //SnarkWallet.shared.test3();
//            SnarkWallet.shared.doTest();
//            SnarkWallet.shared.refreshWallet();
        } catch (Exception e) {
            Log.i("Wallet", e.getMessage());
        }
    }

    public void showTransactions (View view) {
        //Intent cr = new Intent(this, transactions.class);
      //  startActivity(cr);
        try {
            SnarkWallet.shared.getTransactions();
        } catch (Exception e) {
            Log.i("Wallet", e.getMessage());
        }
    }

    public void getContacts (View view) {

        SnarkWallet.shared.getPaymentAccounts();
    }

    public void saveWallet (View view) {
        try {
            SnarkWallet.shared.saveWallet();
            Log.i("Wallet", "Saved");
        } catch (Exception e) {

        }
    }

    public void loadWallet (View view) {
        try {
            SnarkWallet.shared.loadWallet();

            ((TextView)findViewById(R.id.walletKeys) ).setText("Wallet Keys: " + SnarkWallet.shared.getImportedKeysNum());
            ((TextView)findViewById(R.id.balanceText)).setText(SnarkWallet.shared.getBalance());
        } catch (Exception e) {

        }
    }

    public void refreshBalance (View view) {
        try {
           // SnarkWallet.shared.refreshWallet();
            Log.i("Wallet", SnarkWallet.shared.getBalance());
            ((TextView)findViewById(R.id.balanceText)).setText(SnarkWallet.shared.getBalance());
        } catch (Exception e) {
            Log.i("Wallet", e.getMessage());
        }
    }
    public void NewPMContact (View view) {
        try {
           // SnarkWallet.shared.refreshWallet(true);
            // Mine
            SnarkWallet.shared.createPaymentAccount("Snark", "PM8TJY9zANjgrskDkJWvqgjwbbrMKemT2pPkULan1BNcW1MWfZPPVAvNwqGhiwsLr86xfU1KsrzxeotxYsz4v3kUecFfYRJC5UHujXaNahyprdDhwEsu");
            // Alice
//           SnarkWallet.shared.createNewContact("PM8TJTLJbPRGxSbc8EJi42Wrr6QbNSaSSVJ5Y3E4pbCYiTHUskHg13935Ubb7q8tx9GVbh2UuRnBc3WSyJHhUrw8KhprKnn9eDznYGieTzFcwQRya4GA");
        } catch (Exception e) {
            Log.i("Wallet", e.getMessage());
        }
    }

    public void getNewAddress (View view) {
        String addr = SnarkWallet.shared.getReceiveAddress(AddressType.P2PKH, false);
        ((EditText)findViewById(R.id.addrNew) ).setText(addr);
    }


    public void updateRate  (View view) {





        /*Log.i("Wallet", "Get Exchange Pressed");
        SnarkWallet.shared.getExchangeRate("USD", SnarkWallet.shared.getBalanceSat(), new SnarkWallet.RateCalback() {
            @Override
            public void OnSuccess(final String rate) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView tx = (TextView)findViewById(R.id.balanceText);
                        tx.setText(tx.getText() + " (" + rate +")");
                        Log.i("Wallet", "Text Changed");
                    }
                });

            }

            @Override
            public void OnFailed() {

            }
        });*/
    }

    public void sendCoins (View view) {
        //Intent cr = new Intent(this, transactions.class);
        //startActivity(cr);
    }

    public void testButtonHandler (View view) {
        try {

//            Log.i("Wallet", SnarkWallet.shared.getMnemonic());
//            SnarkWallet.shared.getReceiveKey("PM8TJikJ4WXfpEEhNbpVoMi1gxzLpzA1yDSgHjDJVVM25zv9CejcMuquLBBgmz8ZkMMUDHHgwDZx4qYCfMCMePEsMfiUgSLZPkBt82BR54QVDkD2Fnun", 1);

//            SnarkWallet.shared.doTest();

//            SnarkWallet.shared.createPaymentAccount("Snark", "PM8TJe2uUenZcPBpknEZN2GvSnxt8B3RnG3ox3egMziW9aowj5i5DkhSHJPKZgUvA18yHyx99YHa5TiWpTaMRXMe9bdE2Jf7bkrQH7wtadtPf4xhXFty");
//            SnarkWallet.shared.sendMoneyToBIP47("PM8TJe2uUenZcPBpknEZN2GvSnxt8B3RnG3ox3egMziW9aowj5i5DkhSHJPKZgUvA18yHyx99YHa5TiWpTaMRXMe9bdE2Jf7bkrQH7wtadtPf4xhXFty", 1000);
            //SnarkWallet.shared.doTest();


            SnarkWallet.shared.doTest();
        } catch (Exception e) {
            Log.i("Wallet", e.getMessage());
        }
    }
}
