package org.bitcoinj.core.neutrino;

import android.util.Log;

import org.bitcoinj.core.Message;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.ProtocolException;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Utils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Filter;

public class GetCFiltersMessage extends Message {
    public byte FilterType;
    public int StartHeight; // todo uint32
    public Sha256Hash StopHash; // byte[32]
                                               //                                 // max 100 blocks
    public GetCFiltersMessage(NetworkParameters params, int startHeight, Sha256Hash stopHash) throws ProtocolException {
        super(params);
        FilterType = 0; // Change if another bip
        StartHeight = startHeight;
        // todo The height of the block with hash StopHash MUST be greater than or equal to StartHeight, and the difference MUST be strictly less than 100.
        StopHash = stopHash;
    }

    // todo maybe to send reject
    public void parse(){
        Log.i("Wallet", "We don't serve neutrino: get getCFilter message");
        throw new ProtocolException("We don't serve neutrino: got getcfilters message");
    }

//todo
    protected void bitcoinSerializeToStream(OutputStream stream) throws IOException {
        stream.write(FilterType);
        Utils.uint32ToByteStreamLE(StartHeight, stream);
        stream.write(StopHash.getReversedBytes());
    }

}
