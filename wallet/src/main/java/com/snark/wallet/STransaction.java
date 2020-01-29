package com.snark.wallet;

import android.util.Log;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionBag;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptException;
import org.bitcoinj.script.ScriptPattern;
import org.bitcoinj.wallet.Wallet;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class STransaction {
    public String txId;
    public Coin amount;
    public Integer type; // 0 received, 1 send
    public Date date;
    public String addressTo;
    public Integer confirmations;
    public Long fee;
    public Integer weight;
    public String status = "pending";

    Integer CONFIRM_TO_DONE = 6;

    public STransaction (Transaction tx, Wallet jWallet) {

        this.txId = tx.getTxId().toString();
        this.type = tx.getValueSentFromMe(jWallet) != Coin.ZERO ? 1 : 0;
        if (this.type == 1) {
            this.amount = tx.getValueSentFromMe(jWallet);
        } else {
            this.amount = tx.getValueSentToMe(jWallet);
        }
        this.date = tx.getUpdateTime();
        this.confirmations = tx.getConfidence().getDepthInBlocks();
        if (tx.getConfidence().getDepthInBlocks() >= CONFIRM_TO_DONE) {
            this.status = "confirmed";
        }
        this.fee = tx.getFee() != null ? tx.getFee().value : 0;
        this.weight = tx.bitcoinSerialize().length;

        List<String> addressed = new ArrayList<>();
        /*
        for (TransactionOutput n : tx.getMyOutputs(jWallet)) {
            addressed.add(n.getScriptPubKey().getToAddress(jWallet.getNetworkParameters()).toString());
        }
        this.addressTo = StringUtils.join(addressed, ",");
        */

//        this.address =  tx.getOutputs().get(0).getScriptPubKey().getToAddress(jWallet.getNetworkParameters()).toString();

        //            Log.i("Wallet",  "  ________________________");
//            Log.i("Wallet", "Date and Time: " + tx.getUpdateTime().toString());
//            Log.i("Wallet", "From Address: " + tx.getOutput(0).getAddressFromP2PKHScript(networkParameters));
//            Log.i("Wallet", "To Address: " + tx.getOutput(0).getAddressFromP2PKHScript(networkParameters));
//            Log.i("Wallet", "Amount Sent to me: " + tx.getValueSentToMe(jWallet).toFriendlyString());
//            Log.i("Wallet", "Amount Sent from me: " + tx.getValueSentFromMe(jWallet).toFriendlyString());
//            long fee = (tx.getInputSum().getValue() > 0 ? tx.getInputSum().getValue() - tx.getOutputSum().getValue() : 0);
//            Log.i("Wallet", "Fee: " + Coin.valueOf(fee).toFriendlyString());
//            Log.i("Wallet", "Transaction Depth: " + tx.getConfidence().getDepthInBlocks());
//            Log.i("Wallet", "Transaction Blocks: " + tx.getConfidence().toString());
//            Log.i("Wallet", "Tx Hex: " + tx.getHashAsString());
//            Log.i("Wallet", "Tx: Weight " + tx.toString());
    }
}
