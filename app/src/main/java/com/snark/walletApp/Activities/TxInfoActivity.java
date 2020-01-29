package com.snark.walletApp.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;
import com.snark.wallet.STransaction;
import com.snark.wallet.SnarkWallet;
import com.snark.walletApp.R;

public class TxInfoActivity extends AppCompatActivity {

    String txId;
    TextView txIdLabel;
    TextView confirmationLabel;
    TextView feeLabel;
    TextView weightLabel;
    TextView statusLabel;
    TextView addressToLabel;
    TextView dateLabel;


    STransaction tx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tx_info);
        try {
            txId = getIntent().getExtras().getString("tx");
        } catch(NullPointerException e) {
            finish();
        }


        Toolbar bar = findViewById(R.id.toolbar);
        setSupportActionBar(bar);
        setTitle("Transaction");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        txIdLabel         = findViewById(R.id.txIdLabel);
        confirmationLabel = findViewById(R.id.confirmationsLabel);
        feeLabel          = findViewById(R.id.feeLabel);
        weightLabel       = findViewById(R.id.weigthLabel);
        statusLabel       = findViewById(R.id.statusLabel);
        addressToLabel    = findViewById(R.id.addressToLabel);
        dateLabel         = findViewById(R.id.dateLabel);


        tx = SnarkWallet.shared.getTransaction(txId);
        txIdLabel.setText(tx.txId);
       // confirmationLabel.setText(tx.confirmations + " " + getString(R.string.confirmations));
        feeLabel.setText(tx.fee + "");
        weightLabel.setText(tx.weight + "");
        statusLabel.setText(tx.status);
        addressToLabel.setText(tx.addressTo);
        dateLabel.setText(tx.date.toString());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}
