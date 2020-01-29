package org.bitcoinj.core.neutrino;

import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

import java.math.BigInteger;

import  java.util.Collections;
import java.util.logging.Filter;

import org.bitcoinj.core.Context;
import org.bitcoinj.core.Message;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.ProtocolException;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.UnsafeByteArrayOutputStream;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bouncycastle.crypto.macs.SipHash;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.encoders.Hex;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.UnsignedLong;

import javax.annotation.Nullable;

public class GCSFilter extends Message implements  Serializable{ // PeerFilterProvider






    public byte FilterType;
    public Sha256Hash blockHash;

    private long N;
    public byte[] filterBytes;

    static private int DEFAULT_P = 19; // 20
    static private long DEFAULT_M = 784931;


    //the target false positive rate
    private long M = DEFAULT_M;
    private int P = DEFAULT_P;

    private long F;
    private static BigInteger bigF;
    private byte[] key;

    public GCSFilter(NetworkParameters params, byte[] payload){
        super(params, payload, 0);
        P = DEFAULT_P;
        M = DEFAULT_M;
        if (N > Math.pow(2,32)){
            Log.i("Wallet", "Neutrino: N MUST BE <= 2^32");
            throw new ProtocolException("Neutrino: N MUST BE <= 2^32");
        }
        F = N * M;
        bigF = BigInteger.valueOf(F);

    }

//    todo better to do this
//    public GCSFilter (NetworkParameters params, byte[] _filterBytes, byte[] _key, int _N) {
//        super(params);
//        this(filterBytes, _key, _N, DEFAULT_P, DEFAULT_F);
//    }
    /**
     * Constructor of a compact filter
     * @param _filterBytes {Byte[]} filter to match with
     * @param _key {Byte[16]} The parameter k MUST be set to the first 16 bytes of the hash (in standard little-endian representation) of the block for which the filter is constructed.
     *             This ensures the key is deterministic while still varying from block to block.
     * @param _N {Int} number of elements in the filter
     * @param _P {Int} the bit parameter of the Golomb-Rice coding
     * @param _F {long} Range of element hashes, F = N * M @deprecate
     */
    public GCSFilter (byte[] _filterBytes, byte[] _key, int _N, int _P, long _F, long M) {
        filterBytes = _filterBytes;
        P = _P;
        this.M = M;

        N = _N;
        if (N > Math.pow(2,32))
            throw new ProtocolException("Neutrino: N MUST BE <= 2^32");
        F = N * M;
        bigF = BigInteger.valueOf(F);
        if ((int)N < 0 ){
            Log.i("Wallet", "Error in N");
        }

        if (_key.length != 16)
            throw new ProtocolException("Neutrino: The key MUST BE 16 bytes long");
        ByteBuffer bb = ByteBuffer.wrap(_key);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        key = bb.array();
    }

    /** Deserialize the message
     * */
    protected void parse() throws ProtocolException {
        try {
            FilterType = readBytes(1)[0];
            blockHash = readHash();
            key = Arrays.copyOfRange(blockHash.getReversedBytes(), 0, 16);
            if (key.length != 16)
                throw new ProtocolException("Neutrino: The key MUST BE 16 bytes long");

            long numbytes = readVarInt();
            int c = cursor;
            N = readVarInt();
            filterBytes = readBytes((int)(numbytes-cursor+c));
            length = cursor - offset;
            if (length < 0 )
                throw new ProtocolException("Claimed value length too large");

        } catch (Exception e){
            Log.i("Wallet", "\t\tError in parsing filter" + e);
        }
    }

    public Sha256Hash GetFilterHash(){

        ByteArrayOutputStream buf = new UnsafeByteArrayOutputStream();
        buf.write((int)N); // todo N
        try{
            buf.write(filterBytes);
        } catch (Exception e){
            Log.i("Wallet", "asd");
        }
        return Sha256Hash.twiceOf(buf.toByteArray());
    }
//    // MaxCFilterDataSize is the maximum byte size of a committed filter.
//    // The maximum size is currently defined as 256KiB.
//    MaxCFilterDataSize = 256 * 1024
    /**
     * makes a filter chain header for a filter, given the
     * previous filter chain header.
     * @param prevHeader
     * @return
     */
    public Sha256Hash makeHeader(Sha256Hash prevHeader){
//        byte[] zero = {(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
//                (byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0};
//        byte[] filterBytes = {(byte)105,(byte)124,(byte)64};
        byte[] filterTip = new byte[64];
        byte[] filterHash = GetFilterHash().getBytes();
        System.arraycopy(filterHash, 0, filterTip, 0, 32);
        System.arraycopy(prevHeader.getBytes(), 0,filterTip, 32, 32);

        return Sha256Hash.twiceOf(filterTip);
    }

//[
//["Block Height,Block Hash,Block,[Prev Output Scripts for Block],Previous Basic Header,Basic Filter,Basic Header,Notes"],
//[0,"000000000933ea01ad0ee984209779baaec3ced90fa3f408719526f8d77f4943",
// "0100000000000000000000000000000000000000000000000000000000000000000000003ba3edfd7a7b12b27ac72c3e67768f617fc81bc3888a51323a9fb8aa4b1e5e4adae5494dffff001d1aa4ae180101000000010000000000000000000000000000000000000000000000000000000000000000ffffffff4d04ffff001d0104455468652054696d65732030332f4a616e2f32303039204368616e63656c6c6f72206f6e206272696e6b206f66207365636f6e64206261696c6f757420666f722062616e6b73ffffffff0100f2052a01000000434104678afdb0fe5548271967f1a67130b7105cd6a828e03909a67962e0ea1f61deb649f6bc3f4cef38c4f35504e51ec112de5c384df7ba0b8d578a4c702b6bf11d5fac00000000",
// [],"0000000000000000000000000000000000000000000000000000000000000000",
// "019dfca8","21584579b7eb08997773e5aeff3a7f932700042d0ed2a6129012b7d7ae81b750","Genesis block"],

//[2,"000000006c02c8ea6e4ff69651f7fcde348fb9d557a06e6957b65552002a7820",
// "0100000006128e87be8b1b4dea47a7247d5528d2702c96826c7a648497e773b800000000e241352e3bec0a95a6217e10c3abb54adfa05abb12c126695595580fb92e222032e7494dffff001d00d235340101000000010000000000000000000000000000000000000000000000000000000000000000ffffffff0e0432e7494d010e062f503253482fffffffff0100f2052a010000002321038a7f6ef1c8ca0c588aa53fa860128077c9e6c11e6830f4d7ee4e763a56b7718fac00000000",
// [],"d7bdac13a59d745b1add0d2ce852f1a0442e8945fc1bf3848d3cbffd88c24fe1","0174a170",
// "186afd11ef2b5e7e3504f2e8cbf8df28a1fd251fe53d60dff8b1467d1b386cf0",""],

    //[3,"000000008b896e272758da5297bcd98fdc6d97c9b765ecec401e286dc1fdbe10","0100000020782a005255b657696ea057d5b98f34defcf75196f64f6eeac8026c0000000041ba5afc532aae03151b8aa87b65e1594f97504a768e010c98c0add79216247186e7494dffff001d058dc2b60101000000010000000000000000000000000000000000000000000000000000000000000000ffffffff0e0486e7494d0151062f503253482fffffffff0100f2052a01000000232103f6d9ff4c12959445ca5549c811683bf9c88e637b222dd2e0311154c4c85cf423ac00000000",[],"186afd11ef2b5e7e3504f2e8cbf8df28a1fd251fe53d60dff8b1467d1b386cf0","016cf7a0","8d63aadf5ab7257cb6d2316a57b16f517bff1c6388f124ec4c04af1212729d2a",""],
//[49291,"0000000018b07dca1b28b4b5a119f6d6e71698ce1ed96f143f54179ce177a19c","02000000abfaf47274223ca2fea22797e44498240e482cb4c2f2baea088962f800000000604b5b52c32305b15d7542071d8b04e750a547500005d4010727694b6e72a776e55d0d51ffff001d211806480201000000010000000000000000000000000000000000000000000000000000000000000000ffffffff0d038bc0000102062f503253482fffffffff01a078072a01000000232102971dd6034ed0cf52450b608d196c07d6345184fcb14deb277a6b82d526a6163dac0000000001000000081cefd96060ecb1c4fbe675ad8a4f8bdc61d634c52b3a1c4116dee23749fe80ff000000009300493046022100866859c21f306538152e83f115bcfbf59ab4bb34887a88c03483a5dff9895f96022100a6dfd83caa609bf0516debc2bf65c3df91813a4842650a1858b3f61cfa8af249014730440220296d4b818bb037d0f83f9f7111665f49532dfdcbec1e6b784526e9ac4046eaa602204acf3a5cb2695e8404d80bf49ab04828bcbe6fc31d25a2844ced7a8d24afbdff01ffffffff1cefd96060ecb1c4fbe675ad8a4f8bdc61d634c52b3a1c4116dee23749fe80ff020000009400483045022100e87899175991aa008176cb553c6f2badbb5b741f328c9845fcab89f8b18cae2302200acce689896dc82933015e7230e5230d5cff8a1ffe82d334d60162ac2c5b0c9601493046022100994ad29d1e7b03e41731a4316e5f4992f0d9b6e2efc40a1ccd2c949b461175c502210099b69fdc2db00fbba214f16e286f6a49e2d8a0d5ffc6409d87796add475478d601ffffffff1e4a6d2d280ea06680d6cf8788ac90344a9c67cca9b06005bbd6d3f6945c8272010000009500493046022100a27400ba52fd842ce07398a1de102f710a10c5599545e6c95798934352c2e4df022100f6383b0b14c9f64b6718139f55b6b9494374755b86bae7d63f5d3e583b57255a01493046022100fdf543292f34e1eeb1703b264965339ec4a450ec47585009c606b3edbc5b617b022100a5fbb1c8de8aaaa582988cdb23622838e38de90bebcaab3928d949aa502a65d401ffffffff1e4a6d2d280ea06680d6cf8788ac90344a9c67cca9b06005bbd6d3f6945c8272020000009400493046022100ac626ac3051f875145b4fe4cfe089ea895aac73f65ab837b1ac30f5d875874fa022100bc03e79fa4b7eb707fb735b95ff6613ca33adeaf3a0607cdcead4cfd3b51729801483045022100b720b04a5c5e2f61b7df0fcf334ab6fea167b7aaede5695d3f7c6973496adbf1022043328c4cc1cdc3e5db7bb895ccc37133e960b2fd3ece98350f774596badb387201ffffffff23a8733e349c97d6cd90f520fdd084ba15ce0a395aad03cd51370602bb9e5db3010000004a00483045022100e8556b72c5e9c0da7371913a45861a61c5df434dfd962de7b23848e1a28c86ca02205d41ceda00136267281be0974be132ac4cda1459fe2090ce455619d8b91045e901ffffffff6856d609b881e875a5ee141c235e2a82f6b039f2b9babe82333677a5570285a6000000006a473044022040a1c631554b8b210fbdf2a73f191b2851afb51d5171fb53502a3a040a38d2c0022040d11cf6e7b41fe1b66c3d08f6ada1aee07a047cb77f242b8ecc63812c832c9a012102bcfad931b502761e452962a5976c79158a0f6d307ad31b739611dac6a297c256ffffffff6856d609b881e875a5ee141c235e2a82f6b039f2b9babe82333677a5570285a601000000930048304502205b109df098f7e932fbf71a45869c3f80323974a826ee2770789eae178a21bfc8022100c0e75615e53ee4b6e32b9bb5faa36ac539e9c05fa2ae6b6de5d09c08455c8b9601483045022009fb7d27375c47bea23b24818634df6a54ecf72d52e0c1268fb2a2c84f1885de022100e0ed4f15d62e7f537da0d0f1863498f9c7c0c0a4e00e4679588c8d1a9eb20bb801ffffffffa563c3722b7b39481836d5edfc1461f97335d5d1e9a23ade13680d0e2c1c371f030000006c493046022100ecc38ae2b1565643dc3c0dad5e961a5f0ea09cab28d024f92fa05c922924157e022100ebc166edf6fbe4004c72bfe8cf40130263f98ddff728c8e67b113dbd621906a601210211a4ed241174708c07206601b44a4c1c29e5ad8b1f731c50ca7e1d4b2a06dc1fffffffff02d0223a00000000001976a91445db0b779c0b9fa207f12a8218c94fc77aff504588ac80f0fa02000000000000000000",["5221033423007d8f263819a2e42becaaf5b06f34cb09919e06304349d950668209eaed21021d69e2b68c3960903b702af7829fadcd80bd89b158150c85c4a75b2c8cb9c39452ae","52210279be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f8179821021d69e2b68c3960903b702af7829fadcd80bd89b158150c85c4a75b2c8cb9c39452ae","522102a7ae1e0971fc1689bd66d2a7296da3a1662fd21a53c9e38979e0f090a375c12d21022adb62335f41eb4e27056ac37d462cda5ad783fa8e0e526ed79c752475db285d52ae","52210279be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f8179821022adb62335f41eb4e27056ac37d462cda5ad783fa8e0e526ed79c752475db285d52ae","512103b9d1d0e2b4355ec3cdef7c11a5c0beff9e8b8d8372ab4b4e0aaf30e80173001951ae","76a9149144761ebaccd5b4bbdc2a35453585b5637b2f8588ac","522103f1848b40621c5d48471d9784c8174ca060555891ace6d2b03c58eece946b1a9121020ee5d32b54d429c152fdc7b1db84f2074b0564d35400d89d11870f9273ec140c52ae","76a914f4fa1cc7de742d135ea82c17adf0bb9cf5f4fb8388ac"],"ed47705334f4643892ca46396eb3f4196a5e30880589e4009ef38eae895d4a13","0afbc2920af1b027f31f87b592276eb4c32094bb4d3697021b4c6380","b6d98692cec5145f67585f3434ec3c2b3030182e1cb3ec58b855c5c164dfaaa3","Tx pays to empty output script"],
//[180480,"00000000fd3ceb2404ff07a785c7fdcc76619edc8ed61bd25134eaa22084366a","020000006058aa080a655aa991a444bd7d1f2defd9a3bbe68aabb69030cf3b4e00000000d2e826bfd7ef0beaa891a7eedbc92cd6a544a6cb61c7bdaa436762eb2123ef9790f5f552ffff001d0002c90f0501000000010000000000000000000000000000000000000000000000000000000000000000ffffffff0e0300c102024608062f503253482fffffffff01c0c6072a01000000232102e769e60137a4df6b0df8ebd387cca44c4c57ae74cc0114a8e8317c8f3bfd85e9ac00000000010000000381a0802911a01ffb025c4dea0bc77963e8c1bb46313b71164c53f72f37fe5248010000000151ffffffffc904b267833d215e2128bd9575242232ac2bc311550c7fc1f0ef6f264b40d14c010000000151ffffffffdf0915666649dba81886519c531649b7b02180b4af67d6885e871299e9d5f775000000000151ffffffff0180817dcb00000000232103bb52138972c48a132fc1f637858c5189607dd0f7fe40c4f20f6ad65f2d389ba4ac0000000001000000018da38b434fba82d66052af74fc5e4e94301b114d9bc03f819dc876398404c8b4010000006c493046022100fe738b7580dc5fb5168e51fc61b5aed211125eb71068031009a22d9bbad752c5022100be5086baa384d40bcab0fa586e4f728397388d86e18b66cc417dc4f7fa4f9878012103f233299455134caa2687bdf15cb0becdfb03bd0ff2ff38e65ec6b7834295c34fffffffff022ebc1400000000001976a9147779b7fba1c1e06b717069b80ca170e8b04458a488ac9879c40f000000001976a9142a0307cd925dbb66b534c4db33003dd18c57015788ac0000000001000000026139a62e3422a602de36c873a225c1d3ca5aeee598539ceecb9f0dc8d1ad0f83010000006b483045022100ad9f32b4a0a2ddc19b5a74eba78123e57616f1b3cfd72ce68c03ea35a3dda1f002200dbd22aa6da17213df5e70dfc3b2611d40f70c98ed9626aa5e2cde9d97461f0a012103ddb295d2f1e8319187738fb4b230fdd9aa29d0e01647f69f6d770b9ab24eea90ffffffff983c82c87cf020040d671956525014d5c2b28c6d948c85e1a522362c0059eeae010000006b4830450221009ca544274c786d30a5d5d25e17759201ea16d3aedddf0b9e9721246f7ef6b32e02202cfa5564b6e87dfd9fd98957820e4d4e6238baeb0f65fe305d91506bb13f5f4f012103c99113deac0d5d044e3ac0346abc02501542af8c8d3759f1382c72ff84e704f7ffffffff02c0c62d00000000001976a914ae19d27efe12f5a886dc79af37ad6805db6f922d88ac70ce2000000000001976a9143b8d051d37a07ea1042067e93efe63dbf73920b988ac000000000100000002be566e8cd9933f0c75c4a82c027f7d0c544d5c101d0607ef6ae5d07b98e7f1dc000000006b483045022036a8cdfd5ea7ebc06c2bfb6e4f942bbf9a1caeded41680d11a3a9f5d8284abad022100cacb92a5be3f39e8bc14db1710910ef7b395fa1e18f45d41c28d914fcdde33be012102bf59abf110b5131fae0a3ce1ec379329b4c896a6ae5d443edb68529cc2bc7816ffffffff96cf67645b76ceb23fe922874847456a15feee1655082ff32d25a6bf2c0dfc90000000006a47304402203471ca2001784a5ac0abab583581f2613523da47ec5f53df833c117b5abd81500220618a2847723d57324f2984678db556dbca1a72230fc7e39df04c2239942ba942012102925c9794fd7bb9f8b29e207d5fc491b1150135a21f505041858889fa4edf436fffffffff026c840f00000000001976a914797fb8777d7991d8284d88bfd421ce520f0f843188ac00ca9a3b000000001976a9146d10f3f592699265d10b106eda37c3ce793f7a8588ac00000000",["","","","76a9142903b138c24be9e070b3e73ec495d77a204615e788ac","76a91433a1941fd9a37b9821d376f5a51bd4b52fa50e2888ac","76a914e4374e8155d0865742ca12b8d4d14d41b57d682f88ac","76a914001fa7459a6cfc64bdc178ba7e7a21603bb2568f88ac","76a914f6039952bc2b307aeec5371bfb96b66078ec17f688ac"],"b109139671dbedc2b6fcd499a5480a7461ae458af8ff9411d819aa64ba6995d1","0db414c859a07e8205876354a210a75042d0463404913d61a8e068e58a3ae2aa080026","a0af77e0a7ed20ea78d2def3200cc24f08217dcd51755c7c7feb0e2ba8316c2d","Tx spends from empty output script"],
//[926485,"000000000000015d6077a411a8f5cc95caf775ccf11c54e27df75ce58d187313","0000002060bbab0edbf3ef8a49608ee326f8fd75c473b7e3982095e2d100000000000000c30134f8c9b6d2470488d7a67a888f6fa12f8692e0c3411fbfb92f0f68f67eedae03ca57ef13021acc22dc4105010000000001010000000000000000000000000000000000000000000000000000000000000000ffffffff2f0315230e0004ae03ca57043e3d1e1d0c8796bf579aef0c0000000000122f4e696e6a61506f6f6c2f5345475749542fffffffff038427a112000000001976a914876fbb82ec05caa6af7a3b5e5a983aae6c6cc6d688ac0000000000000000266a24aa21a9ed5c748e121c0fe146d973a4ac26fa4a68b0549d46ee22d25f50a5e46fe1b377ee00000000000000002952534b424c4f434b3acd16772ad61a3c5f00287480b720f6035d5e54c9efc71be94bb5e3727f10909001200000000000000000000000000000000000000000000000000000000000000000000000000100000000010145310e878941a1b2bc2d33797ee4d89d95eaaf2e13488063a2aa9a74490f510a0100000023220020b6744de4f6ec63cc92f7c220cdefeeb1b1bed2b66c8e5706d80ec247d37e65a1ffffffff01002d3101000000001976a9143ebc40e411ed3c76f86711507ab952300890397288ac0400473044022001dd489a5d4e2fbd8a3ade27177f6b49296ba7695c40dbbe650ea83f106415fd02200b23a0602d8ff1bdf79dee118205fc7e9b40672bf31563e5741feb53fb86388501483045022100f88f040e90cc5dc6c6189d04718376ac19ed996bf9e4a3c29c3718d90ffd27180220761711f16c9e3a44f71aab55cbc0634907a1fa8bb635d971a9a01d368727bea10169522103b3623117e988b76aaabe3d63f56a4fc88b228a71e64c4cc551d1204822fe85cb2103dd823066e096f72ed617a41d3ca56717db335b1ea47a1b4c5c9dbdd0963acba621033d7c89bd9da29fa8d44db7906a9778b53121f72191184a9fee785c39180e4be153ae00000000010000000120925534261de4dcebb1ed5ab1b62bfe7a3ef968fb111dc2c910adfebc6e3bdf010000006b483045022100f50198f5ae66211a4f485190abe4dc7accdabe3bc214ebc9ea7069b97097d46e0220316a70a03014887086e335fc1b48358d46cd6bdc9af3b57c109c94af76fc915101210316cff587a01a2736d5e12e53551b18d73780b83c3bfb4fcf209c869b11b6415effffffff0220a10700000000001976a91450333046115eaa0ac9e0216565f945070e44573988ac2e7cd01a000000001976a914c01a7ca16b47be50cbdbc60724f701d52d75156688ac00000000010000000203a25f58630d7a1ea52550365fd2156683f56daf6ca73a4b4bbd097e66516322010000006a47304402204efc3d70e4ca3049c2a425025edf22d5ca355f9ec899dbfbbeeb2268533a0f2b02204780d3739653035af4814ea52e1396d021953f948c29754edd0ee537364603dc012103f7a897e4dbecab2264b21917f90664ea8256189ea725d28740cf7ba5d85b5763ffffffff03a25f58630d7a1ea52550365fd2156683f56daf6ca73a4b4bbd097e66516322000000006a47304402202d96defdc5b4af71d6ba28c9a6042c2d5ee7bc6de565d4db84ef517445626e03022022da80320e9e489c8f41b74833dfb6a54a4eb5087cdb46eb663eef0b25caa526012103f7a897e4dbecab2264b21917f90664ea8256189ea725d28740cf7ba5d85b5763ffffffff0200e1f5050000000017a914b7e6f7ff8658b2d1fb107e3d7be7af4742e6b1b3876f88fc00000000001976a914913bcc2be49cb534c20474c4dee1e9c4c317e7eb88ac0000000001000000043ffd60d3818431c495b89be84afac205d5d1ed663009291c560758bbd0a66df5010000006b483045022100f344607de9df42049688dcae8ff1db34c0c7cd25ec05516e30d2bc8f12ac9b2f022060b648f6a21745ea6d9782e17bcc4277b5808326488a1f40d41e125879723d3a012103f7a897e4dbecab2264b21917f90664ea8256189ea725d28740cf7ba5d85b5763ffffffffa5379401cce30f84731ef1ba65ce27edf2cc7ce57704507ebe8714aa16a96b92010000006a473044022020c37a63bf4d7f564c2192528709b6a38ab8271bd96898c6c2e335e5208661580220435c6f1ad4d9305d2c0a818b2feb5e45d443f2f162c0f61953a14d097fd07064012103f7a897e4dbecab2264b21917f90664ea8256189ea725d28740cf7ba5d85b5763ffffffff70e731e193235ff12c3184510895731a099112ffca4b00246c60003c40f843ce000000006a473044022053760f74c29a879e30a17b5f03a5bb057a5751a39f86fa6ecdedc36a1b7db04c022041d41c9b95f00d2d10a0373322a9025dba66c942196bc9d8adeb0e12d3024728012103f7a897e4dbecab2264b21917f90664ea8256189ea725d28740cf7ba5d85b5763ffffffff66b7a71b3e50379c8e85fc18fe3f1a408fc985f257036c34702ba205cef09f6f000000006a4730440220499bf9e2db3db6e930228d0661395f65431acae466634d098612fd80b08459ee022040e069fc9e3c60009f521cef54c38aadbd1251aee37940e6018aadb10f194d6a012103f7a897e4dbecab2264b21917f90664ea8256189ea725d28740cf7ba5d85b5763ffffffff0200e1f5050000000017a9148fc37ad460fdfbd2b44fe446f6e3071a4f64faa6878f447f0b000000001976a914913bcc2be49cb534c20474c4dee1e9c4c317e7eb88ac00000000",["a914feb8a29635c56d9cd913122f90678756bf23887687","76a914c01a7ca16b47be50cbdbc60724f701d52d75156688ac","76a914913bcc2be49cb534c20474c4dee1e9c4c317e7eb88ac","76a914913bcc2be49cb534c20474c4dee1e9c4c317e7eb88ac","76a914913bcc2be49cb534c20474c4dee1e9c4c317e7eb88ac","76a914913bcc2be49cb534c20474c4dee1e9c4c317e7eb88ac","76a914913bcc2be49cb534c20474c4dee1e9c4c317e7eb88ac","76a914913bcc2be49cb534c20474c4dee1e9c4c317e7eb88ac"],"da49977ba1ee0d620a2c4f8f646b03cd0d230f5c6c994722e3ba884889f0be1a","09027acea61b6cc3fb33f5d52f7d088a6b2f75d234e89ca800","4cd9dd007a325199102f1fc0b7d77ca25ee3c84d46018c4353ecfcb56c0d3e7a","Duplicate pushdata 913bcc2be49cb534c20474c4dee1e9c4c317e7eb"],
//[987876,"0000000000000c00901f2049055e2a437c819d79a3d54fd63e6af796cd7b8a79","000000202694f74969fdb542090e95a56bc8aa2d646e27033850e32f1c5f000000000000f7e53676b3f12d5beb524ed617f2d25f5a93b5f4f52c1ba2678260d72712f8dd0a6dfe5740257e1a4b1768960101000000010000000000000000000000000000000000000000000000000000000000000000ffffffff1603e4120ff9c30a1c216900002f424d4920546573742fffffff0001205fa012000000001e76a914c486de584a735ec2f22da7cd9681614681f92173d83d0aa68688ac00000000",[],"e9d729b72d533c29abe5276d5cf6c152f3723f10efe000b1e0c9ca5265a8beb6","010c0b40","e6137ae5a8424c40da1e5023c16975cc97b09300b4c050e6b1c713add3836c40","Coinbase tx has unparseable output script"],
//[1263442,"000000006f27ddfe1dd680044a34548f41bed47eba9e6f0b310da21423bc5f33","000000201c8d1a529c39a396db2db234d5ec152fa651a2872966daccbde028b400000000083f14492679151dbfaa1a825ef4c18518e780c1f91044180280a7d33f4a98ff5f45765aaddc001d38333b9a02010000000001010000000000000000000000000000000000000000000000000000000000000000ffffffff230352471300fe5f45765afe94690a000963676d696e6572343208000000000000000000ffffffff024423a804000000001976a914f2c25ac3d59f3d674b1d1d0a25c27339aaac0ba688ac0000000000000000266a24aa21a9edcb26cb3052426b9ebb4d19c819ef87c19677bbf3a7c46ef0855bd1b2abe83491012000000000000000000000000000000000000000000000000000000000000000000000000002000000000101d20978463906ba4ff5e7192494b88dd5eb0de85d900ab253af909106faa22cc5010000000004000000014777ff000000000016001446c29eabe8208a33aa1023c741fa79aa92e881ff0347304402207d7ca96134f2bcfdd6b536536fdd39ad17793632016936f777ebb32c22943fda02206014d2fb8a6aa58279797f861042ba604ebd2f8f61e5bddbd9d3be5a245047b201004b632103eeaeba7ce5dc2470221e9517fb498e8d6bd4e73b85b8be655196972eb9ccd5566754b2752103a40b74d43df244799d041f32ce1ad515a6cd99501701540e38750d883ae21d3a68ac00000000",["002027a5000c7917f785d8fc6e5a55adfca8717ecb973ebb7743849ff956d896a7ed"],"a4a4d6c6034da8aa06f01fe71f1fffbd79e032006b07f6c7a2c60a66aa310c01","0385acb4f0fe889ef0","3588f34fbbc11640f9ed40b2a66a4e096215d50389691309c1dac74d4268aa81","Includes witness data"]
//]
    static public void testMatchFilter() {
        byte[] key0 = {(byte)154,  (byte)203,  (byte)4, (byte)66, (byte)240 ,(byte)197 ,(byte)52 ,(byte)30 ,(byte)170 ,(byte)32 ,(byte)155 ,
                (byte)142 , (byte)112, (byte)14, (byte)9, (byte)118};

        byte[] compressed_set  = { (byte)116, (byte) 136, (byte)41, (byte)213, (byte)55, (byte)205, (byte)208, (byte)1, (byte)210,
                (byte)228, (byte)130, (byte)23, (byte)207, (byte)129 ,(byte)103 ,(byte)228 ,(byte)47 ,(byte)160 ,(byte)225 ,(byte)132,
                (byte)133 ,(byte)66 ,(byte)154 ,(byte)154 ,(byte)17 ,(byte)28 ,(byte)5 ,(byte)38 ,(byte)99 ,(byte)207 ,(byte)192 ,(byte)55,
                (byte)61 ,(byte)237 ,(byte)198 ,(byte)226 ,(byte)4 ,(byte)138 ,(byte)206 ,(byte)251 ,(byte)70 ,(byte)210 ,(byte)132 ,(byte)158,
                (byte)154 ,(byte)224};

        GCSFilter gcs = new GCSFilter( compressed_set, key0, 17, 20,17825792, 1048576);
//        gcs.TestHashToRange();

        byte[][] target ={
                "Alex".getBytes(), // Alex = true; Alice = false
                "Betty".getBytes(),
                "Charmaine".getBytes(),
                "Donna".getBytes(),
                "Edith".getBytes(),
                "Faina".getBytes(),
                "Georgia".getBytes(),
                "Hannah".getBytes(),
                "Ilsbeth".getBytes(),
                "Jennifer".getBytes(),
                "Kayla".getBytes(),
                "Lena".getBytes(),
                "Michelle".getBytes(),
                "Natalie".getBytes(),
                "Ophelia".getBytes(),
                "Peggy".getBytes(),
                "Queenie".getBytes(),
//                "Nate".getBytes()
        };

        boolean matchRes = gcs.MatchFilter(target);
        Log.i("Wallet", "res of matching = "+ matchRes);

//        N + filterdata = 09027acea61b6cc3fb33f5d52f7d088a6b2f75d234e89ca800

        Sha256Hash blockHash = Sha256Hash.wrap("000000006f27ddfe1dd680044a34548f41bed47eba9e6f0b310da21423bc5f33");
        byte[] key = Arrays.copyOfRange(blockHash.getReversedBytes(), 0, 16);
        if (key.length != 16)
            throw new ProtocolException("Neutrino: The key MUST BE 16 bytes long");

//        GCSFilter filter = new GCSFilter(Hex.decode("85acb4f0fe889ef0"),key,3,DEFAULT_P,0, DEFAULT_M);
        GCSFilter filter = new GCSFilter(MainNetParams.get(), Hex.decode("00335fbc2314a20d310b6f9eba7ed4be418f54344a0480d61dfedd276f00000000090385acb4f0fe889ef0"));
        try {
        ArrayList<byte[]> target2 = new ArrayList<>();
//        try {
//        Transaction tx =  new Transaction(TestNet3Params.get(),
//                Hex.decode("010000000145310e878941a1b2bc2d33797ee4d89d95eaaf2e13488063a2aa9a74490f510a0100000023220020b6744de4f6ec63cc92f7c220cdefeeb1b1bed2b66c8e5706d80ec247d37e65a1ffffffff01002d3101000000001976a9143ebc40e411ed3c76f86711507ab952300890397288ac00000000"));
//        Transaction tx2 =  new Transaction(TestNet3Params.get(),
//                Hex.decode("010000000120925534261de4dcebb1ed5ab1b62bfe7a3ef968fb111dc2c910adfebc6e3bdf010000006b483045022100f50198f5ae66211a4f485190abe4dc7accdabe3bc214ebc9ea7069b97097d46e0220316a70a03014887086e335fc1b48358d46cd6bdc9af3b57c109c94af76fc915101210316cff587a01a2736d5e12e53551b18d73780b83c3bfb4fcf209c869b11b6415effffffff0220a10700000000001976a91450333046115eaa0ac9e0216565f945070e44573988ac2e7cd01a000000001976a914c01a7ca16b47be50cbdbc60724f701d52d75156688ac00000000"));
//        Transaction tx3 =  new Transaction(TestNet3Params.get(),
//                Hex.decode("06eee51317a76a76c67499c8f782819745b58d28cdb4d8357ef7f7e6d79cc513"));
//
//        Address add = Address.fromString( TestNet3Params.get(),"2NGU4ogScHEHEpReUzi9RB2ha58KAFnkFyk");
//        Address add2 = Address.fromString( TestNet3Params.get(),"2NGU4ogScHEHEpReUzi9RB2ha58KAFnkFyk");
//        Script s = ScriptBuilder.createOutputScript(add);
//        Script s2 = ScriptBuilder.createOutputScript(add2);
//
//
//    target2.add(tx.getHash().getReversedBytes());
//    target2.add(tx.getHash().getBytes());
//    target2.add(tx2.getHash().getReversedBytes());
//    target2.add(tx2.getHash().getBytes());
//    target2.add(tx3.getHash().getReversedBytes());
//    target2.add(tx3.getHash().getBytes());
//
//    target2.add(add.getHash());
//    target2.add(s.getProgram());
//    target2.add(add2.getHash());
//    target2.add(s2.getProgram());
//
//
//    Script script = new Script(Hex.decode("76a9143ebc40e411ed3c76f86711507ab952300890397288ac"));
//    target2.add(script.getProgram());
//} catch (Exception e){
//}
//        Log.i("Wallet", "target size = "+target2.size());
            target2.add(Hex.decode("001446c29eabe8208a33aa1023c741fa79aa92e881ff"));
            target2.add(Hex.decode("002027a5000c7917f785d8fc6e5a55adfca8717ecb973ebb7743849ff956d896a7ed"));
            byte[][] t = new byte[target2.size()][];

            boolean res = filter.MatchFilter(target2.toArray(t));
            Log.i("Wallet", "res of 2 matching = "+ res);
        } catch (Exception e){
            Log.i("Wallet", "Error"+ e);
        }
    }

/**
 * @param item The parameter k MUST be set to the first 16 bytes of the hash (in standard little-endian representation)
 *          of the block for which the filter is constructed. This ensures the key is deterministic while still varying from block to block.
 * **/
    private BigInteger HashToRange(byte[] item) {
        SipHash mac = new SipHash();
        mac.init(new KeyParameter(key));
        mac.update(item, 0, item.length);
        long hashLong = mac.doFinal();

        UnsignedLong ulong = UnsignedLong.fromLongBits(hashLong);
        BigInteger hash = ulong.bigIntegerValue();
        BigInteger a = hash.multiply(bigF).shiftRight(64);

        return a;
    }

    /* From BIP158: MatchAny algorithm
     * func MatchAny(key: [16]byte, filterBytes: []byte, target: []byte, P: uint, N: uint, M: uint)
     *
     * // Map targets to the same range as the set hashes.
     *     let target_hashes = []
     *     for target in targets:
     *         let target_hash = hash_to_range(target, F, k)
     *         target_hashes.append(target_hash)
     *
     *     // Sort targets so matching can be checked in linear time.
     *     target_hashes.sort()
     *
     *     stream = new_bit_stream(filterBytes)
     *
     *     let value = 0
     *     let target_idx = 0
     *     let target_val = target_hashes[target_idx]
     *
     *     loop N times:
     *         let delta = golomb_decode(stream, P)
     *         value += delta
     *
     *         inner loop:
     *             if target_val == value:
     *                 return true
     *
     *             // Move on to the next set value.
     *             else if target_val > value:
     *                 break inner loop
     *
     *             // Move on to the next target value.
     *             else if target_val < value:
     *                 target_idx++
     *
     *                 // If there are no targets left, then there are no matches.
     *                 if target_idx == len(targets):
     *                     break outer loop
     *
     *                 target_val = target_hashes[target_idx]
     *
     *     return false
     */
    /** Returns whether the block filter matches the target items.
     * If this returns false, it means the block is certainly not interesting to
     * us. This method differs from blockFilterMatches in that it expects the
     * filter to already be obtained, rather than fetching the filter from the network.
     * Matching filterBytes (filter) with targets
     * @param target {Byte[][]} array of targets in bytes
     * @return true - if target matches the filter
     */

    static {

    }

    public native boolean intFrom( byte[] arr, int p, byte[][] arr2, int N, byte[] cat);
     public boolean MatchFilter(byte[][] target) throws ProtocolException {
         try {
             System.loadLibrary("helloworld-c");
             return intFrom( filterBytes, P, target, (int)N, key);
         } catch (Exception e){
             Log.i("Neutrino", "Error in matching: " +e);
             throw new  ProtocolException("Error in matching");
         }
    }

     public void TestHashToRange(){

        //data = [65 108 105 99 101]
         //siphash = 2844218534172848917
         //data = [66 101 116 116 121]
         //siphash = 6906042180905048022
         //data = [67 104 97 114 109 97 105 110 101]
         //siphash = 4040857690126615240
         //data = [68 111 110 110 97]
         //siphash = 16128272527414910197
         //data = [69 100 105 116 104]
         //siphash = 13175877981948968740

// All this passed
//         data = [75 97 121 108 97]
//         siphash = 9320909139603028136
//         after mult = 9007149
//         byte[] item = { (byte) 75, (byte) 97, (byte) 121, (byte) 108, (byte) 97};

//         data = [65 108 105 99 101]
//         siphash = 2844218534172848917
//         after mult = 2748476
//         byte[] item = { (byte) 65, (byte) 108, (byte) 105, (byte) 99, (byte) 101};

        byte[] item = { (byte) 78, (byte) 97, (byte) 116, (byte) 101};

         BigInteger res = this.HashToRange(item);
//        HashFunction siphash = Hashing.sipHash24(siphashKey0, siphashKey1);
//        BigInteger hash = new BigInteger(siphash.hashBytes(item).toString());
//        long has = siphash.hashBytes(item).asLong();

//        BigInteger hashtorange = hash.multiply(bigF);
//        byte[] a = hash.multiply(bigF).toByteArray();
//        BigInteger hashtorange2 = hashtorange.shiftRight(64);
//        byte[] aa = hashtorange.shiftRight(64).toByteArray();
//
//        long r =  hashtorange2.longValue();

//        SipHash mac = new SipHash();
//        mac.init(new KeyParameter(key));
//        mac.update(item, 0, item.length);
//        long hashLong = mac.doFinal();
//
//        BigInteger hashBig = new BigInteger(Long.toString(hashLong));
//        BigInteger res = hashBig.multiply(bigF).shiftRight(64);
//        long re =  res.longValue();

//        Log.i("Wallet", "HashToRange = " + hash + "; >> 64 = " + hashtorange.toString());
    }
}