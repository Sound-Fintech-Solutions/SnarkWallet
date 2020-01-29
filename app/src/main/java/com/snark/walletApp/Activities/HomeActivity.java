package com.snark.walletApp.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.snark.wallet.AddressType;
import com.snark.wallet.SnarkWallet;
import com.snark.walletApp.R;

import java.util.Timer;
import java.util.TimerTask;

public class HomeActivity extends AppCompatActivity {


    TextView btcLabel;
    TextView usdLabel;
    TextView trafficLabel;
    AppCompatActivity self;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        Toolbar bar = findViewById(R.id.toolbar);
        setSupportActionBar(bar);
        setTitle("Home");



        self = this;

        btcLabel     = findViewById(R.id.btcLabel);
        usdLabel     = findViewById(R.id.usdLabel);
        trafficLabel = findViewById(R.id.mempoolScore);





        trafficLabel.setText(SnarkWallet.shared.getTrafficRate());


      //  usdLabel.setText(R.string.loading);

      //  SnarkWallet.shared.getExchangeRate("usd", SnarkWallet.shared.getBalanceCoin(), updateUsd);

        update();


        Timer t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
                                  @Override
                                  public void run() {
                                     runOnUiThread(new Runnable() {
                                         @Override
                                         public void run() {
                                             update();
                                         }
                                     });
                                  }

                              }, 0, 2000);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        // Locate MenuItem with ShareActionProvider
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.history) {
            Intent cr = new Intent(self, HistoryActivity.class);
            startActivity(cr);
        } else if (item.getItemId() == R.id.contacts) {
            Intent cr = new Intent(self, ContactsActivity.class);
            startActivity(cr);
        } else if (item.getItemId() == R.id.test) {
            SnarkWallet.shared.Test();
        } else if (item.getItemId() == R.id.share) {
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
          //  String shareBody = "This is my payment code: " + SnarkWallet.shared.getClientPaymentCode();
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Payment code");
         //   sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(sharingIntent, "Share via"));
        } else if (item.getItemId() == R.id.logOut) {
            logOutPressed();
        }

        return true;
    }

    private SnarkWallet.RateCalback updateUsd = new SnarkWallet.RateCalback() {
        @Override
        public void OnSuccess(final String rate) {
            self.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    usdLabel.setText(rate);
                }
            });

        }

        @Override
        public void OnFailed() {

        }

    };

    private void update () {
        btcLabel.setText(SnarkWallet.shared.getBalance());
    }


    public void goToReceiveActivity (View view) {
//        Log.i("Neutrino", "Balance "  + SnarkWallet.shared.getBalance());
//        Log.i("Neutrino", "mnemonic " + SnarkWallet.shared.getMnemonic() );
        Intent cr = new Intent(self, ReceiveActivity.class);
        startActivity(cr);
    }

    public void goToSendActivity (View view) {
        Intent cr = new Intent(self, SendActivity.class);
        startActivity(cr);
    }

    private void logOutPressed () {/*
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.myDialog));
        builder.setCancelable(true);

        builder.setTitle("Confirmation");
        builder.setMessage("Are you sure you want to delete your wallet?");
        builder.setPositiveButton(android.R.string.yes,
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SnarkWallet.shared.deleteWallet();
                    Intent cr = new Intent(self, MainActivity.class);
                    startActivity(cr);
                    finish();
                }
            });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i("Wallet", "Canceled");
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();*/
    }

}
