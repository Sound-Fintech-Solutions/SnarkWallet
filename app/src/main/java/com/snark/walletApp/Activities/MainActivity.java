package com.snark.walletApp.Activities;

import android.annotation.SuppressLint;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.snark.wallet.AddressType;
import com.snark.wallet.SnarkWallet;
import com.snark.walletApp.R;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    Handler mh = new Handler();
    TextView pr;
    ProgressBar pb;
    Integer prog = 0;

    Boolean didLoad = false;
    AppCompatActivity self;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        self = this;
        pr = findViewById(R.id.progressLabel);
        pb = findViewById(R.id.progressBar);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        pr.setText("Connecting");
        pb.setProgress(0);

        File data = new File (getApplicationInfo().dataDir);

        try {

            SnarkWallet.shared.Init(data);

            if (SnarkWallet.shared.walletExists()) {

                SnarkWallet.shared.loadWallet();
                SnarkWallet.shared.refreshWallet(callBack);

            } else {
                //Go To Create window
                Intent cr = new Intent(this, WelcomeActivity.class);
                startActivity(cr);
                finish();
            }



        } catch (Exception e) {
           // Swallow
        }

    }

    private SnarkWallet.LoadProgressCallBack callBack = new SnarkWallet.LoadProgressCallBack() {
        @Override
        public void doStep(final Integer rate) {
            Handler mainHandler = new Handler(getApplicationContext().getMainLooper());

            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    pb.setProgress(rate);
                    pr.setText(String.valueOf(rate)+"%");
                } // This is your code
            };
            mainHandler.post(myRunnable);


        }

        @Override
        public void end() {
            if (!didLoad) {
                didLoad = true;
                SnarkWallet.shared.saveWallet();
                Intent cr = new Intent(self, HomeActivity.class);
                startActivity(cr);
                self.finish();
            }
        }

        @Override
        public void start() {

        }
    };


}
