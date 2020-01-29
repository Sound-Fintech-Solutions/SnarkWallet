package com.snark.wallet;

import org.bitcoinj.core.Sha256Hash;

import org.bitcoinj.core.neutrino.GetCFiltersMessage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExtWallet implements Serializable {

    public Integer legacyIndex = 0;
    public Integer legacyIndexI = 0;
    public Integer p2wpkhIndex = 0;
    public Integer p2wpkhIndexI = 0;
    public Boolean restored = false;
    public Boolean newWallet = false;

   // public ConcurrentHashMap<String, BIP47Channel> bip47Channels = new ConcurrentHashMap<>();

    public Integer lastFilterHeight = 0;

}
