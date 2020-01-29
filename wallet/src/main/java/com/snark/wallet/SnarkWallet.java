package com.snark.wallet;

import android.util.Log;

import com.google.common.base.Throwables;
import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.ListenableFuture;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.CheckpointManager;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.LegacyAddress;
import org.bitcoinj.core.Message;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Peer;
import org.bitcoinj.core.PeerAddress;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.TransactionWitness;
import org.bitcoinj.core.Utils;
import org.bitcoinj.core.VersionMessage;
import org.bitcoinj.core.listeners.DownloadProgressTracker;
import org.bitcoinj.core.listeners.PeerConnectedEventListener;
import org.bitcoinj.core.listeners.PeerDisconnectedEventListener;
import org.bitcoinj.core.listeners.PreMessageReceivedEventListener;
import org.bitcoinj.core.neutrino.GetCFiltersMessage;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.net.NioClientManager;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptException;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.SPVBlockStore;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.KeyChainGroup;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class SnarkWallet {

    public static final SnarkWallet shared = new SnarkWallet();

    // private HD_Wallet wallet44 = null;
    public ExtWallet sWallet = new ExtWallet();
    private Wallet   jWallet = null;
   // private BIP47Account Account47 = null;
    //----

    private File dataFolder;

    // Change it before release !!!

    NetworkParameters networkParameters = MainNetParams.get();
//    NetworkParameters networkParameters = TestNet3Params.get();
    private String testAccount = "main";

    private String BLOCKSTAIN_STORE_FILE = "block_store.blk" + testAccount;
    private String SWALLET_FILE = "wallet.enc" + testAccount;
    private String JWALLET_FILE = "jWallet.enc" + testAccount;
    private String PAYMENT_ACCOUNTS_FILE = "pa47.enc" + testAccount;





    private BlockStore blockStore = null;
    private PeerGroup peerGroup = null;
    //---


    public Integer feeFast = 15;
    public Integer feeHour = 15;
    public Integer feeMin  = 1;
    private Boolean feeLoaded = false;


    private Integer RESTORE_GAP = 8;


    public void Init (File file) throws Exception {

     //   this.loadTrafficRate();

        dataFolder = file;
        blockStore = new SPVBlockStore(networkParameters, new File(dataFolder, BLOCKSTAIN_STORE_FILE));

        Security.removeProvider("BC");
        Security.insertProviderAt(new org.bouncycastle.jce.provider.BouncyCastleProvider(), 2);
        Security.addProvider(new BouncyCastleProvider());



    }


    public String getBalance () {
        return jWallet.getBalance(Wallet.BalanceType.AVAILABLE).toFriendlyString();
    }
    public Coin getBalanceCoin () {
        return jWallet.getBalance(Wallet.BalanceType.AVAILABLE);
    }


    /*
     *
     *    Loading and saving wallet and creating
     *
     * */

    public Boolean createNewWallet () {
        if (jWallet == null) {

            try {

                KeyChainGroup kg =  KeyChainGroup.builder(networkParameters).fromSeed(new DeterministicSeed(new SecureRandom(), 256, ""), Script.ScriptType.P2WPKH).build();

                jWallet = new Wallet(networkParameters, kg);

                // todo: fix it
                jWallet.importKey(new ECKey());

                File tmp  = new File(dataFolder, JWALLET_FILE);
                File tmp1 = new File(dataFolder, SWALLET_FILE);
                File tmp2 = new File(dataFolder, PAYMENT_ACCOUNTS_FILE);
                File tmp3 = new File(dataFolder, BLOCKSTAIN_STORE_FILE);

                tmp. delete();
                tmp1.delete();
                tmp2.delete();
                tmp3.delete();

                sWallet.newWallet = true;

                Log.i("Wallet", "Wallet Created");
            } catch (Exception e) {
                //Swallow
                Log.i("Wallet", e.getMessage());
            }
            return true;
        }
        return false;
    }


    public void loadWallet () {

        try {
            // Reading the object from a fil
            FileInputStream sWalletTmp = new FileInputStream(new File(dataFolder, SWALLET_FILE));
            ObjectInputStream in = new ObjectInputStream(sWalletTmp);
            sWallet = (ExtWallet) in.readObject();
            in.close();
            sWalletTmp.close();



            jWallet = Wallet.loadFromFile(new File(dataFolder, JWALLET_FILE));


            /*
            File file = new File(dataFolder, PAYMENT_ACCOUNTS_FILE);
            String jsonString;
            jsonString = FileUtils.readFileToString(file, Charset.defaultCharset());
            Gson gson = new Gson();
            Type collectionType = new TypeToken<Collection<BIP47Channel>>(){}.getType();
            List<BIP47Channel> BIP47ChannelList = gson.fromJson(jsonString, collectionType);
            if (BIP47ChannelList != null) {
                for (BIP47Channel BIP47Channel : BIP47ChannelList) {
                    //sWallet.bip47Channels.put(BIP47Channel.getPaymentCode(), BIP47Channel);
                }
            }



            loadClientBip47Account();
               */

            connectToPeerGroup();


            this.assignTransactionsCallback();

            Log.i("Wallet", "Loaded");

        } catch (Exception e) {
            Log.i("Neutrino", "Wallet load error:"+e.getMessage());

        }
    }


    public void saveWallet () {
        try {

//            if (true) return;

            FileOutputStream sWalletTmp = new FileOutputStream(new File(dataFolder, SWALLET_FILE));
            ObjectOutputStream out = new ObjectOutputStream(sWalletTmp);
            out.writeObject(sWallet);
            out.close();
            sWalletTmp.close();


/*

            // Saving bip47 accounts
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(sWallet.bip47Channels.values());
            File file = new File(dataFolder, PAYMENT_ACCOUNTS_FILE);
            FileUtils.writeStringToFile(file, json, Charset.defaultCharset(), false);
            //---

            */
            jWallet.saveToFile(new File(dataFolder, JWALLET_FILE));

            Log.i("Wallet", "Wallet Saved");
        } catch (IOException e) {
            Log.i("Neutrino", "Wallet save error: "+ e);
            e.printStackTrace();
        } catch (Exception e){
            Log.i("Neutrino", "Wallet blockStore error: "+ e);
        }


    }

    public Boolean walletExists () {
        File tmp = new File(dataFolder, JWALLET_FILE);
        File tmp1 = new File(dataFolder, SWALLET_FILE);
        //File tmp2 = new File(dataFolder, PAYMENT_ACCOUNTS_FILE);
        return tmp.exists() && tmp1.exists()/* && tmp2.exists()*/;
    }

    public Boolean walletLoaded () {
        return jWallet == null && sWallet == null;
    }

    public void restoreWallet (List<String> mnemonic) {
        try {

            KeyChainGroup kg =  KeyChainGroup.builder(networkParameters).fromSeed(new DeterministicSeed(mnemonic, null, "", Calendar.getInstance().getTimeInMillis()), Script.ScriptType.P2WPKH).build();

            jWallet = new Wallet(networkParameters, kg);



            int coin = networkParameters == MainNetParams.get() ? 0 : 1;

            for (Integer i = 0; i < RESTORE_GAP; i++) {

                getReceiveAddress(AddressType.P2PKH, false);
                getReceiveAddress(AddressType.P2PKH, true);


                getReceiveAddress(AddressType.P2WPKH, false);
                getReceiveAddress(AddressType.P2WPKH, true);



//                ECKey key = this.getHDKey(HDKeyDerivation.createMasterPrivateKey(jWallet.getKeyChainSeed().getSeedBytes()), 49, coin,0,0,i);
//                Script redeemScript = ScriptBuilder.createP2WPKHOutputScript(key);
//                byte[] p2wpkhHash = Utils.sha256hash160(redeemScript.getProgram());
//                Script scriptPubKey = ScriptBuilder.createP2SHOutputScript(p2wpkhHash);
//
//                Log.i("Wallet",
//                        LegacyAddress.fromScriptHash(networkParameters, scriptPubKey.getPubKeyHash()).toString());
//                jWallet.addWatchedScripts(Arrays.asList(scriptPubKey));
            }

            File tmp  = new File(dataFolder, JWALLET_FILE);
            File tmp1 = new File(dataFolder, SWALLET_FILE);
            File tmp2 = new File(dataFolder, PAYMENT_ACCOUNTS_FILE);
            File tmp3 = new File(dataFolder, BLOCKSTAIN_STORE_FILE);

            tmp. delete();
            tmp1.delete();
            tmp2.delete();
            tmp3.delete();

            Log.i("Wallet", "Wallet Created");
        } catch (Exception e) {
            //Swallow
            Log.i("Wallet", e.getMessage());

        }

    }

    public Boolean deleteWallet () {
        try {

            File tmp  = new File(dataFolder, JWALLET_FILE);
            File tmp1 = new File(dataFolder, SWALLET_FILE);
            File tmp2 = new File(dataFolder, PAYMENT_ACCOUNTS_FILE);
            File tmp3 = new File(dataFolder, BLOCKSTAIN_STORE_FILE);

            tmp. delete();
            tmp1.delete();
            tmp2.delete();
            tmp3.delete();

        } catch (Exception e) {
            return false;
        }

        return true;
    }




    /*
     *
     *   Refresh wallet. E.g. search for new income ot etc.
     *
     */

    public interface LoadProgressCallBack {
        void doStep (Integer rate);
        void end     ();
        void start   ();
    }


    public void refreshWallet (final LoadProgressCallBack callback) throws Exception {

        DownloadProgressTracker l = new DownloadProgressTracker() {
            @Override
            protected void progress(double pct, int blocksSoFar, Date date) {
                super.progress(pct, blocksSoFar, date);
                int pro = (int) pct;


                callback.doStep(pro);
                Log.i("Wallet", "Progress - " + Integer.toString(pro));
            }

            @Override
            protected void doneDownload() {
                super.doneDownload();
               // sWallet.restored = true;
               // SnarkWallet.shared.sWallet.newWallet = false;
                callback.end();
                Log.i("Wallet", "Done Downloaded");
            }

            @Override
            protected void startDownload(int blocks) {
                super.startDownload(blocks);
                callback.start();
                Log.i("Wallet", "Started Download");
            }
        };

        // Wallet wallet = new Wallet();

        peerGroup.startBlockChainDownload(l);

    }



    /*
     *
     *   Working with transactions
     *
     * */

    public List<STransaction> getTransactions () {

        Set<Transaction> all = jWallet.getTransactions(true);
        List<STransaction> out = new ArrayList<>();

        for (Transaction tx : all) {

            out.add(new STransaction(tx, jWallet));

        }


        Collections.sort(out, new Comparator<STransaction>(){
            public int compare(STransaction obj1, STransaction obj2) {
                // ## Ascending order
                return obj2.date.compareTo(obj1.date);
            }
        });
        return out;
    }

    public STransaction getTransaction (String txId) {
        return new STransaction(jWallet.getTransaction(Sha256Hash.wrap(txId)), jWallet);
    }

    private ECKey getHDKey (DeterministicKey mKey, Integer purpose, Integer coin, Integer account, Integer type, Integer id) {

        DeterministicKey purposeKey;
        DeterministicKey coinKey;
        DeterministicKey accountd;
        DeterministicKey wallet;
        DeterministicKey addr;

        purposeKey = HDKeyDerivation.deriveChildKey(mKey, purpose  | ChildNumber.HARDENED_BIT);
        coinKey = HDKeyDerivation.deriveChildKey(purposeKey, coin  | ChildNumber.HARDENED_BIT);
        accountd = HDKeyDerivation.deriveChildKey(coinKey, account | ChildNumber.HARDENED_BIT);
        wallet = HDKeyDerivation.deriveChildKey(accountd, type);
        addr = HDKeyDerivation.deriveChildKey(wallet, id);
        ECKey key = ECKey.fromPrivate(addr.getPrivKeyBytes());

        return key;
    }

    public String getReceiveAddress (AddressType type, Boolean internal) {
        ECKey key;
        DeterministicKey mKey = HDKeyDerivation.createMasterPrivateKey (jWallet.getKeyChainSeed().getSeedBytes());

        Integer net = 0;
        Integer itype = 0;
        String address;
        if (networkParameters == TestNet3Params.get()) {
            net = 1;
        }
        if (internal) {
            itype = 1;
        }
        switch (type) {
            case P2PKH:
                key = this.getHDKey(mKey, 44, net, 0, itype, itype > 0 ? sWallet.legacyIndexI++ : sWallet.legacyIndex++);
                address = Address.fromKey(networkParameters, key, Script.ScriptType.P2PKH).toString();
                break;
            case P2WPKH:
            default:
                key = this.getHDKey(mKey, 84, net, 0, itype, itype > 0 ? sWallet.p2wpkhIndexI++ : sWallet.p2wpkhIndex++);
                address = Address.fromKey(networkParameters, key, Script.ScriptType.P2WPKH).toString();
                break;
        }

        Log.i("Wallet", address);

        jWallet.importKey(key);


        return address;
    }

    public Boolean sendCoins(String to, long amount, long fee){
        try {

            Address addr = Address.fromString(networkParameters, to);

            Coin coin = Coin.valueOf(amount);
            SendRequest sendRequest = SendRequest.to(addr, coin);

            sendRequest.feePerKb = Coin.valueOf(fee * 1000);
            sendRequest.changeAddress = Address.fromString(networkParameters, this.getReceiveAddress(AddressType.P2WPKH, true));
            jWallet.completeTx(sendRequest);


            peerGroup.broadcastTransaction(sendRequest.tx);

            Log.i("Wallet", "Payment sent");
            return true;
        } catch (Exception e) {
            Log.i("Wallet", e.getMessage());
            return false;
        }
    }






    /*
     *
     *   Working with listeners
     *
     * */


    private void assignTransactionsCallback () {
        jWallet.addCoinsReceivedEventListener(new WalletCoinsReceivedEventListener() {
            @Override
            public void onCoinsReceived(Wallet wallet, Transaction transaction, Coin prevBalance, Coin newBalance) {
                Log.i("Wallet", "Transaction received");

//                if (true) return;
                Boolean myTx = false;

                if (transaction.getValueSentFromMe(jWallet).compareTo(Coin.ZERO) != 0) {
                    Log.i("Wallet", "My transactions");
                    myTx = true;
                }


                for (TransactionOutput tx : transaction.getOutputs()) {
                    if (tx.isMine(jWallet)) {
                        Script s = new Script( tx.getScriptBytes() );
                        switch ( s.getScriptType() ) {
                            case P2PKH:
                                getReceiveAddress(AddressType.P2PKH, myTx);
                                return;
                            case P2WPKH:
                                getReceiveAddress(AddressType.P2WPKH, myTx);
                                return;
                        }
                    }
                }

                /*
                if (BIP47Manager.isNotificationTransaction(transaction, Account47, jWallet)) {

                    Log.i("Wallet", "NTX transaction received");

                    BIP47PaymentCode BIP47PaymentCode = BIP47Manager.getPaymentCodeInNotificationTransaction(transaction, Account47);
                    Log.i("Wallet", "NTX transaction received2");
                    if (BIP47PaymentCode == null) {

                        Log.i("Wallet", "Error decoding payment code in tx {} " + transaction);
                    } else {
                        Log.i("Wallet", "NTX transaction received3");
                        Log.i("Wallet", "Payment Code: " + BIP47PaymentCode);

                        sWallet.bip47Channels.put(BIP47PaymentCode.toString(),  new BIP47Channel(BIP47PaymentCode.toString()));

                        boolean needsSaving = savePaymentCode(BIP47PaymentCode);
                        if (needsSaving) {
                            try {
                                //saveWallet();
                            } catch (Exception e) {
                                //Swallow
                            }
                        }
                    }
                } else if (BIP47Manager.isToBIP47Address(transaction, jWallet)) {
                    Log.i("Wallet","New BIP47 payment received to address: "+BIP47Manager.getAddressOfReceived(transaction, jWallet));


                    boolean needsSaving = generateNextPaymentIncomingAddress(getAddressOfReceived(transaction).toString());

                    if (needsSaving) {
                        try {
                            saveWallet();
                        } catch (Exception e) {
                            //Swallow
                        }
                    }
                    String paymentCode = BIP47Manager.getPaymentCodeForAddress(getAddressOfReceived(transaction).toString(), sWallet.bip47Channels);


                    Log.i("Wallet","Received tx for Payment Code: " + paymentCode);
                } else {
                    Coin valueSentToMe = getValueSentToMe(transaction);
                    Log.i("Wallet","Received tx for "+valueSentToMe.toFriendlyString() + ":" + transaction);
                }
                */
            }


        });


    }








    /*
     *
     *   BIP 47 (Payment codes)
     *
     * */

    //  Not tested --
    // Creates new contact, save it to channels and send ntx
    public void createPaymentAccount(String label, String paymentCode) throws InsufficientMoneyException, IOException {
/*
        BIP47Channel tmpChannel = new BIP47Channel(paymentCode, label);

        SendRequest tr = BIP47Manager.makeNotificationTransaction(paymentCode, jWallet, Account47.getPaymentCode());

        jWallet.commitTx(tr.tx);

        peerGroup.broadcastTransaction(tr.tx);

        sWallet.bip47Channels.put(paymentCode, tmpChannel);

        saveWallet();
        */
        Log.i("Wallet", "Notification Transaction sent and wallet saved");
    }

    public void getPaymentAccounts() {
        /*
        for (BIP47Channel contact : sWallet.bip47Channels.values()) {
            Log.i("Wallet", contact.getLabel());
        }
        */
    }
    /*

    public BIP47PaymentAddress getReceiveKey( String senderPaymentCode, int idx) throws AddressFormatException, NotSecp256k1Exception, Exception {

        ECKey accountKey = Account47.keyAt(idx);

        BIP47PaymentAddress pc = new BIP47PaymentAddress(networkParameters, new BIP47PaymentCode(senderPaymentCode), 0, accountKey.getPrivKeyBytes());

        Log.i("Wallet", pc.getReceiveECKey().getPrivateKeyAsWiF(networkParameters));


        return pc;
    }

    // Returns string pc
    public String getClientPaymentCode () {
        if (Account47 != null) {
            return Account47.getStringPaymentCode();
        }
        return "";
    }

    private void loadClientBip47Account() throws Exception {

        if (jWallet.getKeyChainSeed().getSeedBytes() == null) {
            throw new Exception("BIP74 - Empty keychain seed");
        }

        DeterministicKey mKey = HDKeyDerivation.createMasterPrivateKey (jWallet.getKeyChainSeed().getSeedBytes());
        DeterministicKey purposeKey = HDKeyDerivation.deriveChildKey(mKey, 47 | ChildNumber.HARDENED_BIT);

        int cl = 0;

        if (networkParameters == TestNet3Params.get()) {
            cl = 1;
        }
        DeterministicKey coinKey = HDKeyDerivation.deriveChildKey(purposeKey, cl | ChildNumber.HARDENED_BIT);
        // DeterministicKey accountKey = HDKeyDerivation.deriveChildKey(coinKey, ChildNumber.HARDENED_BIT);

        Account47 = new BIP47Account(networkParameters, coinKey, 2 | ChildNumber.HARDENED_BIT);

        Address notificationAddress = Account47.getNotificationAddress();

        Log.i("Wallet", "My payment code is " + Account47.getStringPaymentCode());
        Log.i("Wallet", "My notification code is " + Account47.getNotificationAddress());

//        jWallet.removeWatchedAddress(notificationAddress);
        if (!jWallet.isAddressWatched(notificationAddress)) {
            jWallet.addWatchedAddress(notificationAddress);
        }

    }


    private boolean savePaymentCode(BIP47PaymentCode BIP47PaymentCode) {
        if (sWallet.bip47Channels.containsKey(BIP47PaymentCode.toString())) {
            BIP47Channel BIP47Channel = sWallet.bip47Channels.get(BIP47PaymentCode.toString());
            if (BIP47Channel.getIncomingAddresses().size() != 0) {
                return false;
            } else {
                try {
                    BIP47Channel.generateKeys(this);

                    return true;
                } catch (NotSecp256k1Exception | InvalidKeyException | InvalidKeySpecException | NoSuchAlgorithmException | NoSuchProviderException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }

        BIP47Channel BIP47Channel = new BIP47Channel(BIP47PaymentCode.toString());

        try {
            BIP47Channel.generateKeys(this);
            sWallet.bip47Channels.put(BIP47PaymentCode.toString(), BIP47Channel);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    // Return My 47 Account
    public BIP47Account getClientPaymentAccount() {
        return Account47;
    }

    // Finds contact with pc and sends value to next address
    public void sendCoinsToPaymentAccount (String pc, long value) {

        BIP47Channel receiver = BIP47Manager.getBip47MetaForPaymentCode(pc, sWallet.bip47Channels);

        int idx = receiver.getCurrentOutgoingIndex();
        receiver.incrementOutgoingIndex();

        try {

            BIP47PaymentAddress addr = BIP47Util.getSendAddress(Account47, networkParameters, new BIP47PaymentCode(receiver.getPaymentCode()), idx);
            String addrs = LegacyAddress.fromKey(networkParameters, addr.getSendECKey()).toBase58();


            this.sendCoins(addrs, value, 5);

        } catch (Exception e) {
            Log.i("Wallet", e.getMessage());
        }
    }


    public boolean generateNextPaymentIncomingAddress(String address) {
        for (BIP47Channel BIP47Channel : sWallet.bip47Channels.values()) {
            for (BIP47Address bip47Address : BIP47Channel.getIncomingAddresses()) {
                if (!bip47Address.getAddress().equals(address)) {
                    continue;
                }
                if (bip47Address.isSeen()) {
                    return false;
                }

                int nextIndex = BIP47Channel.getCurrentIncomingIndex() + 1;
                try {
                    ECKey key = BIP47Util.getReceiveAddress(this, BIP47Channel.getPaymentCode(), nextIndex).getReceiveECKey();
                    jWallet.importKey(key);
                    Address newAddress = getAddressOfKey(key);
                    BIP47Channel.addNewIncomingAddress(newAddress.toString(), nextIndex);
                    bip47Address.setSeen(true);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        }
        return false;
    }


    */


    /*
     *
     *   Accessory
     *
     * */

    public String getImportedKeysNum () {
        return String.valueOf(jWallet.getImportedKeys().size());
    }

    public List<ECKey> getImportedKeys () {
        return jWallet.getImportedKeys();
    };

    public List<String> getMnemonic () {
        if (jWallet != null) {
            return jWallet.getKeyChainSeed().getMnemonicCode();
        }
        return new ArrayList<>();
    }

    public interface RateCalback {
        void OnSuccess (String rate);
        void OnFailed  ();
    }
        /*
    public void getExchangeRate (String currency, final Coin btc, final RateCalback callback) {
        // Instantiate the RequestQueue.
        OkHttpClient client = new OkHttpClient();
        String url = "https://api.coingecko.com/api/v3/exchange_rates";
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.i("Wallet", "Failed load exchange rate");
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    String str = response.body().string();
                    try {
                        JSONObject reader = new JSONObject(str);

                        JSONObject rates = reader.getJSONObject("rates");
                        Double rate = rates.getJSONObject("usd").getDouble("value");
                        Double val = rate * btc.getValue() / 100000000;

                        String out = String.valueOf(Math.floor(val)) + " USD";

                        callback.OnSuccess(out);

                    } catch (Exception e) {
                        Log.i("Wallet", e.getMessage());
                    }
                }
            }
        });
    }
    */
    public Address getAddressOfKey(ECKey key) {
        return LegacyAddress.fromKey(networkParameters, key);
    }

    public NetworkParameters getNetworkParameters () {
        return networkParameters;
    }

    public void importKey (ECKey key) {
        jWallet.importKey(key);
    }

    public String getTrafficRate () {
        if (!feeLoaded) {
            return "-";
        }
        double r = (double) feeFast / 200.0;
        if (r > 1)
            r = 1;
        return String.valueOf((int) (r * 10));
    }


    /*
     *
     *   Private Accessory
     *
     * */

    public void Test () {
        jWallet.addWatchedAddress(Address.fromString(networkParameters, "19cjSWJHsMjfdkecFhCjtLAbKMpsNuzeyg"));
        Log.i("Wallet", "Do Test done");
    }

    private Coin getValueSentToMe(Transaction transaction) {
        if (jWallet != null) {
            return transaction.getValueSentToMe(jWallet);
        }
        return Coin.ZERO;
    }


    private void connectToPeerGroup ()  throws Exception {

        if (!sWallet.restored) {
            InputStream checkpoints;

            checkpoints = CheckpointManager.openStream(networkParameters);
            if (checkpoints == null) {
                Log.i("Wallet", "Bad");
            } else {
                Log.i("Wallet", "Good");
            }
          //  CheckpointManager.checkpoint(networkParameters, checkpoints, blockStore, (Calendar.getInstance().getTimeInMillis() - 60 * 60 * 24 * 30 * 5));
            sWallet.restored = true;
        }

        BlockChain chain = new BlockChain(networkParameters, blockStore);

        peerGroup = new PeerGroup(networkParameters, chain);

//
        peerGroup.addAddress(InetAddress.getByName("13.124.219.100"));
//        peerGroup.addAddress(InetAddress.getByName("128.199.174.62"));
//        peerGroup.addAddress(InetAddress.getByName("96.9.244.139"));
//        peerGroup.addAddress(InetAddress.getByName("149.56.19.79"));
//        peerGroup.addAddress(InetAddress.getByName("46.101.112.24"));
//        peerGroup.addAddress(InetAddress.getByName("35.153.87.161"));
//        peerGroup.addAddress(InetAddress.getByName("18.212.159.39"));
//        peerGroup.addAddress(InetAddress.getByName("178.128.165.102"));
//        peerGroup.addAddress(InetAddress.getByName("165.227.7.29"));
//        peerGroup.addAddress(InetAddress.getByName("138.197.221.66"));
//        peerGroup.addAddress(InetAddress.getByName("159.203.119.159"));
//        peerGroup.addAddress(InetAddress.getByName("195.201.208.28"));
//        peerGroup.addAddress(InetAddress.getByName("34.255.115.216"));
//        peerGroup.addAddress(InetAddress.getByName("89.238.166.235"));
//        peerGroup.addAddress(InetAddress.getByName("5.9.87.7"));
//        peerGroup.addAddress(InetAddress.getByName("85.29.220.49"));
//        peerGroup.addAddress(InetAddress.getByName("150.101.247.206"));
//        peerGroup.addAddress(InetAddress.getByName("173.212.227.208"));
//        peerGroup.addAddress(InetAddress.getByName("87.27.157.110"));
//        peerGroup.addAddress(InetAddress.getByName("159.203.58.25"));
//        peerGroup.addAddress(InetAddress.getByName("5.189.154.131"));

//        peerGroup.getMemoryPool().

        peerGroup.setRequiredServices(1 << 6);
        peerGroup.addPeerDiscovery(new DnsDiscovery(networkParameters));
        peerGroup.setBloomFilteringEnabled(false);
        peerGroup.addConnectedEventListener(new PeerConnectedEventListener() {
            @Override
            public void onPeerConnected(Peer peer, int peerCount) {
                Log.i("Wallet", "Peer connected " + peer.getAddress().toString());
            }
        });

        peerGroup.addDisconnectedEventListener(new PeerDisconnectedEventListener() {
            @Override
            public void onPeerDisconnected(Peer peer, int peerCount) {
                Log.i("Wallet", "Peer disconnected " + peer.getAddress().toString());
            }
        });






        jWallet.setAcceptRiskyTransactions(true);
        chain.addWallet(jWallet);

        peerGroup.addWallet(jWallet);

        peerGroup.startAsync();
    }



    private Address getAddressOfReceived(Transaction tx) {

        for (final TransactionOutput output : tx.getOutputs()) {
            try {
                if (output.isMineOrWatched(jWallet)) {
                    final Script script = output.getScriptPubKey();
                    return script.getToAddress(networkParameters, true);
                }
            } catch (final ScriptException x) {
                // swallow
            }
        }

        return null;
    }
/*
    private void loadTrafficRate () {
        OkHttpClient client = new OkHttpClient();
        String url = "https://bitcoinfees.earn.com/api/v1/fees/recommended";
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                loadTrafficRate();
                Log.i("Wallet", "Failed load traffic rate");
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    String str = response.body().string();
                    try {
                        JSONObject reader = new JSONObject(str);

                        feeFast = reader.getInt("fastestFee");
                        feeHour = reader.getInt("hourFee");
                        feeLoaded = true;
                    } catch (Exception e) {
                        Log.i("Wallet", e.getMessage());
                    }
                }
            }
        });
    }

*/





    /*
     *
     *   Testing
     *
     * */

//    public static BIP47PaymentAddress getSendAddress(BIP47AppKit spendWallet, BIP47PaymentCode receiverBIP47PaymentCode, int idx) throws AddressFormatException, NotSecp256k1Exception {
//        ECKey key = spendWallet.getAccount(0).keyAt(0);
//        return getPaymentAddress(spendWallet.getParams(), receiverBIP47PaymentCode, idx, key);
//    }
//
//    public String getCurrentOutgoingAddress(BIP47Channel BIP47Channel) {
//        try {
//            ECKey key = getSendAddress(this, new BIP47PaymentCode(BIP47Channel.getPaymentCode()), BIP47Channel.getCurrentOutgoingIndex()).getSendECKey();
//            return LegacyAddress.fromKey(params, key).toString();
//        } catch (InvalidKeyException | InvalidKeySpecException | NotSecp256k1Exception | NoSuchProviderException | NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }


//    public void doTest () throws Exception {
//        BIP47Manager.makeNotificationTransaction("", jWallet);
    //Log.i("Wallet", String.valueOf(sWallet.bip47Channels.size()));
    // ECKey key =  BIP47Manager.getPaymentAddress(networkParameters, new BIP47PaymentCode("PM8TJikJ4WXfpEEhNbpVoMi1gxzLpzA1yDSgHjDJVVM25zv9CejcMuquLBBgmz8ZkMMUDHHgwDZx4qYCfMCMePEsMfiUgSLZPkBt82BR54QVDkD2Fnun"), 0, Account47.keyAt(0)).getSendECKey();
    //Log.i("Wallet", LegacyAddress.fromKey(networkParameters, key).toBase58());
//        Log.i("Wallet", key.getPublicKeyAsHex());
//        sWallet.bip47Channels.get(0).
    //new ECPublicKeySpec();
//        getFieldSize
//        ScriptPattern.extractKeyFromPayToPubKey()
//        Hex.decode("001407abd8f85edfca803eff968266047c9b9d822ea7");
//        Log.i("Wallet", String.valueOf(ECNamedCurveTable.getParameterSpec("secp256k1").getCurve().getFieldSize()));
//        ECNamedCurveTable.getParameterSpec("secp256k1").getCurve().decodePoint(Hex.decode("03c1c171a053812134ad2b39895cca80f1f1bfc1559ddeeed4805a57d2a5ffda68"));
//        Log.i("Wallet", "test2");
//        for ( BIP47Channel asd : sWallet.bip47Channels.values()) {
//            Log.i("Wallet", asd.getLabel());
//            Log.i("Wallet", asd.getPaymentCode());
//        }
//        try {
//            getReceiveAddress()
//            ECKey asd = BIP47Util.getReceiveAddress(this, "PM8TJRvJox1BAELujpDfE9kL61zFCSsJMeYNQ8KciZRx1tFKJK6XW5S9QECJqJSe9rDPyLgnbfbjMrahwRvTqAqSypcVqz6qkmcc6NPxZcE3x6HxBypJ", 0).getReceiveECKey();
//            Log.i("Wallet",BIP47Util.getReceiveAddress(this, "PM8TJRvJox1BAELujpDfE9kL61zFCSsJMeYNQ8KciZRx1tFKJK6XW5S9QECJqJSe9rDPyLgnbfbjMrahwRvTqAqSypcVqz6qkmcc6NPxZcE3x6HxBypJ", 0).getReceiveECKey().getPrivateKeyAsHex());
//            Log.i("Wallet", Address.fromKey(networkParameters, BIP47Util.getReceiveAddress(this, "PM8TJRvJox1BAELujpDfE9kL61zFCSsJMeYNQ8KciZRx1tFKJK6XW5S9QECJqJSe9rDPyLgnbfbjMrahwRvTqAqSypcVqz6qkmcc6NPxZcE3x6HxBypJ", 1).getReceiveECKey(), Script.ScriptType.P2PKH).toString());
//        } catch (Exception e) {
//
//        }
//        this.sefndMoneyToBIP47("PM8TJY9zANjgrskDkJWvqgjwbbrMKemT2pPkULan1BNcW1MWfZPPVAvNwqGhiwsLr86xfU1KsrzxeotxYsz4v3kUecFfYRJC5UHujXaNahyprdDhwEsu", 2000);
    //  sWallet.bip47Channels.get("")





    public void doTestSendToP2SH_FROM_P2PKH () throws Exception {

        //ECKey key = new ECKey();85

//        jWallet.importKey(key);
//        LegacyAddress tmp = LegacyAddress.fromScriptHash(networkParameters, key.getPubKeyHash());
//        ScriptBuilder.
//        Log.i("Wallet", tmp.toBase58());

        Script sc = ScriptBuilder.createOutputScript(Address.fromString(networkParameters, "2NEb1Qo8q1f7izQYEqCchSwb9SKgXwXsLzs"));


//        jWallet.get

        Transaction tx = new Transaction(networkParameters);

        TransactionOutput tout = new TransactionOutput(networkParameters, null, Coin.valueOf(1000),  sc.getProgram());

        tx.addOutput(tout);

        SendRequest r = SendRequest.forTx(tx);

        r.feePerKb = Coin.valueOf(15000);

        jWallet.completeTx(r);

        peerGroup.broadcastTransaction(r.tx);

        Log.i("Wallet", "Transaction Sent");
//
//        SendRequest.to()



    }

    public void doTest () {
        try {
            //this.doTestSendToP2SH_FROM_P2PKH();
        } catch (Exception e) {
            Log.i("Wallet", e.getMessage());
        }

        ECKey key = jWallet.getImportedKeys().get(2);

//        Log.i("Wallet", LegacyAddress.fromScriptHash(networkParameters, ScriptBuilder.createP2WPKHOutputScript(key).getPubKeyHash()).toString());

//        if (true) return;
        Script sr = ScriptBuilder.createP2SHOutputScript(1 , Arrays.asList(key));
        Log.i("Wallet", LegacyAddress.fromScriptHash(networkParameters, sr.getPubKeyHash()).toString());

        Log.i("Wallet", "step1");
        //Script redeemScript = ScriptBuilder.createMultiSigOutputScript(1, Arrays.asList(key));
//

        Log.i("Wallet", "step1");
        Script sc = ScriptBuilder.createOutputScript(Address.fromString(networkParameters, "2NEb1Qo8q1f7izQYEqCchSwb9SKgXwXsLzs"));
        Transaction tx = new Transaction(networkParameters);
//        TransactionOutput tout = new TransactionOutput(networkParameters, null, Coin.valueOf(1000),  sc.getProgram());
//        tx.addOutput(tout);
        tx.addOutput(Coin.valueOf(1000), sc);

        Script redeemScript = ScriptBuilder.createP2WPKHOutputScript(key);

        TransactionInput txIn = new TransactionInput(networkParameters, null,  redeemScript.getProgram(), new TransactionOutPoint(networkParameters, 0, Sha256Hash.wrap("be81eccbbb61d2ea5eca79393cccca60b16d244580cea414ab021d49f27db35e")));
        tx.addInput(txIn);



        byte[] p2wpkhHash = Utils.sha256hash160(redeemScript.getProgram());
        Script scriptPubKey = ScriptBuilder.createP2SHOutputScript(p2wpkhHash);
        Log.i("Wallet----", LegacyAddress.fromScriptHash(networkParameters, scriptPubKey.getPubKeyHash()).toString());
        Script scriptCode = new ScriptBuilder().data(ScriptBuilder.createP2PKHOutputScript(key).getProgram()).build();

        TransactionSignature txSig = tx.calculateWitnessSignature(0, key,
                scriptCode, Coin.valueOf(20000),
                Transaction.SigHash.ALL, false);


//        Script red = ScriptBuilder.createRedeemScript(1,Arrays.asList(key));//segWitOutputScript(key);
//        Script red = ScriptBuilder.createRedeemScript(1,Arrays.asList(key));
//        Log.i("Wallet", "step3");

        txIn.setWitness(TransactionWitness.redeemP2WPKH(txSig, key));
        txIn.setScriptSig(new ScriptBuilder().data(redeemScript.getProgram()).build());
        //  Log.i("Wallet", "step2");

        // tx.addInput(in);
//        Sha256Hash sighash = tx.hashForSignature(1, red, Transaction.SigHash.ALL, false);
        // Sha256Hash sighash = tx.hashForWitnessSignature(0, red, Coin.valueOf(5000), Transaction.SigHash.ALL, false);


        //ECKey.ECDSASignature ecdsaSignature = key.sign(sighash);

        // TransactionSignature transactionSignarture = new TransactionSignature(ecdsaSignature, Transaction.SigHash.ALL, false);

        //tx.getInput(0).setWitness(TransactionWitness.redeemP2WPKH(tx., key));

        // tx.getInput(0).verify();

        // Use the redeem script we have saved somewhere to start building the transaction
        // Script redeemScript = new Script(hexStringToByteArray("524104711acc7644d34e493eba76984c81d99f1233f06b3242d90e6cd082b26fd0c1186f65de8d3378a6630f2285bd17972372685378683b604c68343fa1b532196c4d410476d6ef11a42010a889ee0c3d75f9cac3a51a3e245744fb9bf1bc8c196eb0f6982e39aad753514248966f4d545a5439ece8e27e13764c92f6230e0244cae5bee54104a45f0da4e6501fa781b6534e601f410a59328691d86d034d13362138f7e9a2927451280544e36c88279ee00c7face2fb707d0210842017e3937ae4584faacf6753ae"));

        // Start building the transaction by adding the unspent inputs we want to use
        // The data is taken from blockchain.info, and can be found here: https://blockchain.info/rawtx/ca1884b8f2e0ba88249a86ec5ddca04f937f12d4fac299af41a9b51643302077

//        ScriptBuilder scriptBuilder = new ScriptBuilder();
//        scriptBuilder.data(new String("a9141ed0f8aca467d828a7548af27fe56040fb113fed87").getBytes()); // Script of this output
//        TransactionInput input = tx.addInput( Sha256Hash.wrap("9cd9373c45be399114f77c5b3e8c5f57b02c15b428a7c108eeb8e3a94b8d9aa6"), 1, scriptBuilder.build());


//        Script redeemScript = ScriptBuilder.createRedeemScript(1, Arrays.asList(key));



        // Sign the first part of the transaction using private key #1
//        Sha256Hash sighash = tx.hashForSignature(0, redeemScript, Transaction.SigHash.ALL, false);
//        ECKey.ECDSASignature ecdsaSignature = key.sign(sighash);
//        TransactionSignature transactionSignarture = new TransactionSignature(ecdsaSignature, Transaction.SigHash.ALL, false);
//
//        // Create p2sh multisig input script
//        Script inputScript = ScriptBuilder.createP2SHMultiSigInputScript(Arrays.asList(transactionSignarture), redeemScript);
//
//
////        ScriptBuilder.create
//
//        input.setScriptSig(redeemScript);

        Log.i("Wallet", "step1");


//        new TransactionInput(Sha256Hash.wrap("9cd9373c45be399114f77c5b3e8c5f57b02c15b428a7c108eeb8e3a94b8d9aa6"), 1, );
//
//        TransactionInput t = new TransactionInput(networkParameters, null, Hex.decode("a9141ed0f8aca467d828a7548af27fe56040fb113fed87"));
//
//
//        Log.i("Wallet", "step2");
//        Sha256Hash sighash = tx.hashForSignature(0, redeemScript, Transaction.SigHash.ALL, false);
//        Log.i("Wallet", "step2");
//        ECKey.ECDSASignature ecdsaSignature = key.sign(sighash);
//        TransactionSignature transactionSignarture = new TransactionSignature(ecdsaSignature, Transaction.SigHash.ALL, false);
//        Log.i("Wallet", "step2");
//        // Create p2sh multisig input script
//        Script inputScript = ScriptBuilder.createP2SHMultiSigInputScript(Arrays.asList(transactionSignarture), redeemScript);
//
//        Log.i("Wallet", "step3");


//        t.setScriptSig(ScriptBuilder.createMultiSigInputScript());
//        t.setScriptSig(inputScript);
//        tx.addInput(t);
//        tx.getInput(0).setScriptSig(segWitRedeemScript(key));


//        Transaction spendTx = new Transaction(networkParameters);
////        Address address = Address.fromBase58(networkParameters, "19EfMrs5WkcvtBBnuEqP6v1yppeWww61Kc");
////        Script outputScript = ScriptBuilder.createOutputScript(address);
//        spendTx.addOutput(sc.getValue(), sc);
////                        System.out.println(spendTx.getOutputs());
//        TransactionInput input = spendTx.addInput(multiSigOutput);

//        Sha256Hash sigHash = tx.hashForSignature(0, sc, Transaction.SigHash.ALL, false);
//        ECKey.ECDSASignature signature = list.get(0).sign(sigHash);
//        ECKey.ECDSASignature signature1 = list.get(1).sign(sigHash);
//        TransactionSignature txSig = new TransactionSignature(signature, Transaction.SigHash.ALL, false);
//        TransactionSignature txSig1 = new TransactionSignature(signature1, Transaction.SigHash.ALL, false);
//
//
//        Script inputScript = ScriptBuilder.createMultiSigInputScript((ImmutableList.of(txSig, txSig1)));
////                        System.out.println(inputScript);
//        input.setScriptSig(inputScript);
//        input.verify(multiSigOutput);

//        tx.



//        redeemScript = ScriptBuilder.createMultiSigOutputScript(1, Arrays.asList(key));
//        multisigAddress = ScriptBuilder.createP2SHOutputScript(redeemScript).getToAddress(networkParameters).toString();
//        inputScript = ScriptBuilder.createP2SHMultiSigInputScript(null, redeemScript);

        // new TransactionOutPoint()




//        TransactionInput t = new TransactionInput(tx.getParams(), tx, inputScript.getProgram(), new TransactionOutPoint(tx.getParams(), 1, Sha256Hash.wrap("9cd9373c45be399114f77c5b3e8c5f57b02c15b428a7c108eeb8e3a94b8d9aa6")), Coin.valueOf(5000));

//        tx.addInput(t);
        //  tx.hashForSignature(0, redeemScript, Transaction.SigHash.SINGLE, true);
//        tx.hashForWitnessSignature(0, redeemScript.getProgram(), Coin.valueOf(5000), (byte)0x80);
        //tx.hashForWitnessSignature(0,)
////        tx.addInput(new TransactionInput(tx.getParams(), tx, segWitRedeemScript(key).getProgram()));
//        tx.addSignedInput(new TransactionOutPoint(), key)
//        try {
//            SendRequest r = SendRequest.forTx(tx);
////            r.feePerKb = Coin.valueOf(10);
//            jWallet.signTransaction(r);
//        } catch (Exception e) {
//            Log.i("Wallet", e.getMessage());
//        }


//        ECKey serverKey = ....;
//        Transaction contract = ....;
//        TransactionOutput multisigOutput = tx.getOutput(0);
//        Script multisigScript = multisigOutput.getScriptPubKey();
//// Is the output what we expect?
////        checkState(multisigScript.isSentToMultiSig());
//        Coin value = multisigOutput.getValue();

// O\
        tx.verify();
        //peerGroup.broadcastTransaction(tx);

        Log.i("Wallet", Hex.toHexString(tx.bitcoinSerialize()));

        Log.i("Wallet", "Broadcasted");
    }

}
