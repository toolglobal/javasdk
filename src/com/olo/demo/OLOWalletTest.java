package com.olo.demo;

import org.bouncycastle.util.encoders.Hex;
import org.web3j.crypto.*;

import java.io.File;
import java.math.BigInteger;

public class OLOWalletTest {
    public static void main(String[] args) throws Exception {

        BigInteger privKey = new BigInteger("97ddae0f3a25b92268175400149d65d6887b9cefaf28ea2c078e05cdc15a3c0a", 16);
        BigInteger pubKey = Sign.publicKeyFromPrivate(privKey);
        ECKeyPair keyPair = new ECKeyPair(privKey, pubKey);
        System.out.println("Private key: " + privKey.toString(16));
        System.out.println("Public key: " + pubKey.toString(16));
        System.out.println("Public key (compressed): " + compressPubKey(pubKey));
        System.out.println("EIP55 address is " +  Keys.toChecksumAddress(Keys.getAddress(keyPair)));

        String msg = "Message for signing";
        byte[] msgHash = Hash.sha3(msg.getBytes());
        Sign.SignatureData signature = Sign.signMessage(msgHash, keyPair, false);
        System.out.println("Msg: " + msg);
        System.out.println("Msg hash: " + Hex.toHexString(msgHash));
        System.out.printf("Signature: [v = %d, r = %s, s = %s]\n",
                signature.getV() - 27,
                Hex.toHexString(signature.getR()),
                Hex.toHexString(signature.getS()));

        System.out.println();

        BigInteger pubKeyRecovered = Sign.signedMessageToKey(msg.getBytes(), signature);
        System.out.println("Recovered public key: " + pubKeyRecovered.toString(16));

        boolean validSig = pubKey.equals(pubKeyRecovered);
        System.out.println("Signature valid? " + validSig);

        return;
//        Bip39Wallet wallet;
//        try {
//            wallet = WalletUtils.generateBip39Wallet("111111", new File("."));
//        } catch (Exception e) {
//            throw new Exception("generateBip39Wallet wallet failed");
//        }
//
//        String keyStoreKey = wallet.getFilename();
//        String memorizingWords = wallet.getMnemonic();
//        Credentials credentials = WalletUtils.loadBip39Credentials("111111",
//                wallet.getMnemonic());
//
//        //System.out.println("address is 0x" +  credentials.getAddress());
//        //System.out.println("pubkey is 0x" +  credentials.getEcKeyPair().getPublicKey().toString(16));
//        System.out.println("privkey is 0x" +  credentials.getEcKeyPair().getPrivateKey().toString(16));
//        String compressedPubKey =  compressPubKey(credentials.getEcKeyPair().getPublicKey());
//        System.out.println("compressedPubKey " +  compressedPubKey);
//        String address = Keys.toChecksumAddress(credentials.getAddress());
//        System.out.println("EIP55 address is " +  address);
    }

    public static String compressPubKey(BigInteger pubKey) {
        String pubKeyYPrefix = pubKey.testBit(0) ? "03" : "02";
        String pubKeyHex = pubKey.toString(16);
        String pubKeyX = pubKeyHex.substring(0, 64);
        return pubKeyYPrefix + pubKeyX;
    }
}
