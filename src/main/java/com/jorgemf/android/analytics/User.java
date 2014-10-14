package com.jorgemf.android.analytics;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.Random;

public class User {

    private static User instance;

    private String id;

    private User() {
    }

    public static User instance(Context context) {
        if (instance == null) {
            instance = new User();
            if (instance.load(context)) {
                instance.createId(context);
                instance.save(context);
            }
        }
        return instance;
    }

    private void createId(Context context) {
        Random random = new Random(System.currentTimeMillis());
        // deviceId 64-bit number (as a hex string)
        String deviceId = android.provider.Settings.Secure.getString(
                context.getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID);
        long deviceIdNumber = Long.parseLong(deviceId, 16);
        id = Long.toHexString(random.nextLong() + deviceIdNumber);
    }

    protected boolean load(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Settings.SHARED_PREFERENCES, Activity.MODE_PRIVATE);
        id = sharedPreferences.getString(Settings.Preferences.USER_ID, null);
        return id != null;
    }

    protected void save(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Settings.SHARED_PREFERENCES, Activity.MODE_PRIVATE);
        sharedPreferences.edit()
                .putString(Settings.Preferences.USER_ID, id)
                .apply();
    }

    protected String getId() {
        return id;
    }


}
