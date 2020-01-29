package com.snark.walletApp.Activities;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.snark.wallet.STransaction;
import com.snark.wallet.SnarkWallet;
import com.snark.walletApp.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HistoryActivity extends AppCompatActivity {

    ListView listView;

    List<STransaction> all = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Toolbar bar = findViewById(R.id.toolbar);
        setSupportActionBar(bar);
        setTitle("History");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        listView = findViewById(R.id.historyList);

        List list = new ArrayList();

        all = SnarkWallet.shared.getTransactions();

        listView.setAdapter(new CustomAdapter());


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent cr = new Intent(getApplicationContext(), TxInfoActivity.class);
                cr.putExtra("tx", all.get(position).txId);
                startActivity(cr);
            }
        });

        if (all.size() == 0) {
            findViewById(R.id.noTxYet).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.noTxYet).setVisibility(View.INVISIBLE);
        }
    }

    class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return all.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {



            View txView =  getLayoutInflater().inflate(R.layout.transaction_item, parent, false);

//            View txView = getLayoutInflater().inflate(R.layout.transaction_item, parent);

            ((TextView)txView.findViewById(R.id.name)).setText(all.get(position).addressTo);
            ((TextView)txView.findViewById(R.id.amount)).setText(all.get(position).amount.toFriendlyString());
            if (all.get(position).type == 1) {
              //  ((TextView)txView.findViewById(R.id.sent)).setText(R.string.sent);
            } else {
             //   ((TextView)txView.findViewById(R.id.sent)).setText(R.string.received);
            }

            return txView;
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
