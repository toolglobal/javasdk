package com.olo.service.util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.web3j.crypto.ECDSASignature;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Sign;
import org.web3j.rlp.RlpEncoder;
import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpString;
import org.web3j.rlp.RlpType;
import org.web3j.utils.Numeric;

import com.alibaba.fastjson.JSONObject;

/**
 * 转账
 *
 * @author Hu Dingjiang
 * @date 2019/7/1
 */
public class Transaction {
    private long createdAt;
    private int gasLimit;
    private int gasPrice;
    private int nonce;
    private String sender;
    private TxBody body;
    private byte[] signature;
    private String privkey;

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public int getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(int gasLimit) {
        this.gasLimit = gasLimit;
    }

    public int getGasPrice() {
        return gasPrice;
    }

    public void setGasPrice(int gasPrice) {
        this.gasPrice = gasPrice;
    }

    public int getNonce() {
        return nonce;
    }

    public void setNonce(int nonce) {
        this.nonce = nonce;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public TxBody getBody() {
        return body;
    }

    public void setBody(TxBody body) {
        this.body = body;
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    public String getPrivkey() {
        return privkey;
    }

    public void setPrivkey(String privkey) {
        this.privkey = privkey;
    }

    public static int getOffsetShortList() {
        return OFFSET_SHORT_LIST;
    }

    private static final int OFFSET_SHORT_LIST = 0xc0;

    /**
     * 将transaction对象中约定属性转成RLP格式的byte数组，<br/>
     * 对1的结果进行SHA3生成摘要。<br/>
     * 对2的结果用私钥进行加密，得到签名signature。
     */
    public void Sign() {
        byte[] signHash = toSignHash();
        // 用私钥进行加密
        sign(signHash);
    }


    /**
     * 用私钥加密
     *
     * @param hash
     */
    private void sign(byte[] hash) {
        BigInteger pk = Numeric.toBigIntNoPrefix(privkey);
        byte[] privateKeyByte = pk.toByteArray();
        // 通过私钥生成公私钥对
        ECKeyPair keyPair = ECKeyPair.create(privateKeyByte);
        ECDSASignature sig = keyPair.sign(hash);
        // Now we have to work backwards to figure out the recId needed to recover the
        // signature.
        int recId = -1;
        for (int i = 0; i < 4; i++) {
            BigInteger k = Sign.recoverFromSignature(i, sig, hash);
            if (k != null && k.equals(keyPair.getPublicKey())) {
                recId = i;
                break;
            }
        }
        if (recId == -1) {
            throw new RuntimeException("Could not construct a recoverable key. Are your credentials valid?");
        }

        int headerByte = recId + 27;

        // 1 header + 32 bytes for R + 32 bytes for S
        byte v = (byte) headerByte;
        byte[] r = Numeric.toBytesPadded(sig.r, 32);
        byte[] s = Numeric.toBytesPadded(sig.s, 32);

        final byte fixedV = v >= 27 ? (byte) (v - 27) : v;
        signature = merge(r, s, new byte[]{fixedV});
    }

    /**
     * @param arrays - arrays to merge
     * @return - merged array
     */
    public static byte[] merge(byte[]... arrays) {
        int count = 0;
        for (byte[] array : arrays) {
            count += array.length;
        }

        // Create new array and copy all array contents
        byte[] mergedArray = new byte[count];
        int start = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, mergedArray, start, array.length);
            start += array.length;
        }
        return mergedArray;
    }

    private byte[] toSignHash() {
        byte[] rlpRaw = RLPEncodeToSign();
        byte[] raw = RlpEncoder.encode(RlpString.create(rlpRaw));
        // 对结果进行SHA3生成摘要。
        return Hash.sha3(raw);
    }

    /**
     * 计算当前交易hash值
     *
     * @return
     */
    public String TxHash() {
        byte[] rlpRaw = RLPEncodeTx();
        // 对结果进行SHA3生成摘要。
        byte[] hash = Hash.sha3(rlpRaw);
        return Numeric.toHexString(hash);
    }

    /**
     * 将transaction对象转成RLP格式的byte数组
     *
     * @return
     */
    private byte[] RLPEncodeToSign() {
        List<RlpType> result = new ArrayList<RlpType>();
        result.add(RlpString.create(BigInteger.valueOf(gasPrice)));
        result.add(RlpString.create(BigInteger.valueOf(gasLimit)));
        result.add(RlpString.create(BigInteger.valueOf(nonce)));
        result.add(RlpString.create(Numeric.hexStringToByteArray(sender)));

        List<RlpType> bodyResult = new ArrayList<RlpType>();
        if (body.getTo().length() == 40) {
            byte[] array = new byte[33];
            byte[] temp2 = Numeric.hexStringToByteArray(body.getTo());
            System.arraycopy(temp2, 0, array, 13, temp2.length);
            bodyResult.add(RlpString.create(array));
        } else {
            bodyResult.add(RlpString.create(Numeric.hexStringToByteArray(body.getTo())));
        }
        bodyResult.add(RlpString.create(new BigInteger(body.getValue())));
        bodyResult.add(RlpString.create(Numeric.hexStringToByteArray(body.getLoad())));
        bodyResult.add(RlpString.create(body.getMemo()));
        RlpList bodyRlpList = new RlpList(bodyResult);

        result.add(bodyRlpList);

        RlpList rlpList = new RlpList(result);
        return RlpEncoder.encode(rlpList);
    }

    private byte[] RLPEncodeTx() {

        List<RlpType> result = new ArrayList<RlpType>();

        result.add(RlpString.create(BigInteger.valueOf(createdAt)));
        result.add(RlpString.create(BigInteger.valueOf(gasLimit)));
        result.add(RlpString.create(BigInteger.valueOf(gasPrice)));
        result.add(RlpString.create(BigInteger.valueOf(nonce)));
        result.add(RlpString.create(Numeric.hexStringToByteArray(sender)));

        List<RlpType> bodyResult = new ArrayList<RlpType>();
        if (body.getTo().length() == 40) {
            byte[] array = new byte[33];
            byte[] temp2 = Numeric.hexStringToByteArray(body.getTo());
            System.arraycopy(temp2, 0, array, 13, temp2.length);
            bodyResult.add(RlpString.create(array));
        } else {
            bodyResult.add(RlpString.create(Numeric.hexStringToByteArray(body.getTo())));
        }
        bodyResult.add(RlpString.create(new BigInteger(body.getValue())));
        bodyResult.add(RlpString.create(body.getLoad()));
        bodyResult.add(RlpString.create(body.getMemo()));
        RlpList bodyRlpList = new RlpList(bodyResult);

        result.add(bodyRlpList);
        //
        result.add(RlpString.create(signature));

        RlpList rlpList = new RlpList(result);
        return RlpEncoder.encode(rlpList);
    }

    public String getJson() {
        JSONObject result = new JSONObject();
        try {
            result.put("createdAt", createdAt);
            result.put("gasLimit", gasLimit);
            result.put("gasPrice", gasPrice + "");
            result.put("nonce", nonce);
            result.put("sender", sender);
            result.put("signature", Numeric.toHexString(getSignature()));
            result.put("body", body.getJson());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }
}
