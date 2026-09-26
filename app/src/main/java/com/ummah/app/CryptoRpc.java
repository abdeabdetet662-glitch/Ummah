package com.ummah.app;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;

public class CryptoRpc {

    // عقدة Ethereum عامة مجانية
    private static final String RPC_URL = "https://eth.llamarpc.com";

    public interface BalanceCallback {
        void onBalance(double eth);
        void onError(String message);
    }

    public static void getEthBalance(final String address, final BalanceCallback cb) {
        new Thread(() -> {
            try {
                JSONObject payload = new JSONObject();
                payload.put("jsonrpc", "2.0");
                payload.put("method", "eth_getBalance");
                payload.put("id", 1);

                JSONArray params = new JSONArray();
                params.put(address);
                params.put("latest");
                payload.put("params", params);

                URL url = new URL(RPC_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(20000);
                conn.setReadTimeout(20000);

                OutputStream os = conn.getOutputStream();
                os.write(payload.toString().getBytes("UTF-8"));
                os.close();

                int code = conn.getResponseCode();
                if (code != 200) { postError(cb, "RPC " + code); return; }

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                JSONObject response = new JSONObject(sb.toString());
                if (response.has("error")) {
                    postError(cb, "RPC error");
                    return;
                }

                String hexBalance = response.getString("result");
                BigInteger wei = new BigInteger(hexBalance.substring(2), 16);
                BigDecimal eth = new BigDecimal(wei).divide(
                        new BigDecimal("1000000000000000000"), 6, BigDecimal.ROUND_DOWN);

                final double ethDouble = eth.doubleValue();
                new Handler(Looper.getMainLooper()).post(() -> cb.onBalance(ethDouble));

            } catch (Exception e) {
                postError(cb, e.getMessage() != null ? e.getMessage() : "network error");
            }
        }).start();
    }

    private static void postError(BalanceCallback cb, String msg) {
        new Handler(Looper.getMainLooper()).post(() -> cb.onError(msg));
    }
}
