package com.aicraft.quantum.remote;

import android.content.Context;
import android.content.SharedPreferences;

public final class DeviceStore {
    private static final String PREFS = "aicraft_remote";
    private final SharedPreferences p;

    public DeviceStore(Context context) {
        p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public String host() { return p.getString("host", ""); }
    public String port() { return p.getString("port", "4772"); }
    public String token() { return p.getString("token", ""); }
    public String mac() { return p.getString("mac", ""); }
    public String model() { return p.getString("model", ""); }

    public void saveConnection(String host, String port, String token) {
        p.edit().putString("host", host).putString("port", port).putString("token", token).apply();
    }

    public void saveMac(String mac) { p.edit().putString("mac", mac).apply(); }
    public void saveModel(String model) { p.edit().putString("model", model).apply(); }
}
