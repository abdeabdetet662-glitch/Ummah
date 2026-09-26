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

    // عدة خوادم احتياطية - نجرب واحداً واحداً حتى يعمل
    private static final String[] RPC_URLS = {
        "https://cloudflare-eth.com",
        "https://rpc.ankr.com/eth",
        "https://eth.public-rpc.com",
        "https://1rpc.io/eth",
        "https://ethereum.publicnode.com",
        "https://eth.llamarpc.com"
    };

    public interface BalanceCallback {
        void onBalance(double eth);
        void onError(String message);
    }

    public static void getEthBalance(final String address, final BalanceCallback cb) {
        new Thread(() -> {
            Exception lastError = null;
            for (String rpcUrl : RPC_URLS) {
                try {
                    JSONObject payload = new JSONObject();
                    payload.put("jsonrpc", "2.0");
                    payload.put("method", "eth_getBalance");
                    payload.put("id", 1);

                    JSONArray params = new JSONArray();
                    params.put(address);
                    params.put("latest");
                    payload.put("params", params);

                    URL url = new URL(rpcUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(10000);

                    OutputStream os = conn.getOutputStream();
                    os.write(payload.toString().getBytes("UTF-8"));
                    os.close();

                    int code = conn.getResponseCode();
                    if (code != 200) { lastError = new Exception("HTTP " + code); continue; }

                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);
                    br.close();

                    JSONObject response = new JSONObject(sb.toString());
                    if (response.has("error")) { lastError = new Exception("RPC error"); continue; }

                    String hexBalance = response.getString("result");
                    BigInteger wei = new BigInteger(hexBalance.substring(2), 16);
                    BigDecimal eth = new BigDecimal(wei).divide(
                            new BigDecimal("1000000000000000000"), 6, BigDecimal.ROUND_DOWN);

                    final double ethDouble = eth.doubleValue();
                    new Handler(Looper.getMainLooper()).post(() -> cb.onBalance(ethDouble));
                    return; // نجح، نوقف المحاولات

                } catch (Exception e) {
                    lastError = e;
                    // نجرب الخادم التالي
                }
            }
            // فشلت كل المحاولات
            final String msg = lastError != null ? lastError.getMessage() : "network error";
            new Handler(Looper.getMainLooper()).post(() -> cb.onError(msg));
        }).start();
    }
}
