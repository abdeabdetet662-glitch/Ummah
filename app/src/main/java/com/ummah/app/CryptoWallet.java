package com.ummah.app;

import org.bouncycastle.crypto.digests.KeccakDigest;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.FixedPointCombMultiplier;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class CryptoWallet {

    public static byte[] sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(input.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return new byte[32];
        }
    }

    public static String deriveEthereumAddress(String seedPhrase) {
        try {
            byte[] privKeyBytes = sha256(seedPhrase);
            BigInteger privKey = new BigInteger(1, privKeyBytes);

            ECDomainParameters params = new ECDomainParameters(
                    CustomNamedCurves.getByName("secp256k1"));
            ECPoint pubPoint = new FixedPointCombMultiplier().multiply(
                    params.getG(), privKey);
            byte[] pubKeyBytes = pubPoint.getEncoded(false);

            KeccakDigest digest = new KeccakDigest(256);
            digest.update(pubKeyBytes, 1, 64);
            byte[] hash = new byte[32];
            digest.doFinal(hash, 0);

            StringBuilder sb = new StringBuilder("0x");
            for (int i = 12; i < 32; i++) {
                sb.append(String.format("%02x", hash[i]));
            }
            return sb.toString();
        } catch (Exception e) {
            return "0x0000000000000000000000000000000000000000";
        }
    }

    public static String getPrivateKeyHex(String seedPhrase) {
        byte[] pk = sha256(seedPhrase);
        StringBuilder sb = new StringBuilder("0x");
        for (byte b : pk) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
