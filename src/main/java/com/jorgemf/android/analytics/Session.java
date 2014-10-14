package com.jorgemf.android.analytics;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Session {

    /**
     * start time of the session in seconds
     */
    private long timestamp;

    /**
     * last event time stamp, in milliseconds
     */
    private long lastEventTime;

    protected Session() {
    }

    protected boolean loadSavedSession(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Settings.SHARED_PREFERENCES, Activity.MODE_PRIVATE);
        if (sharedPreferences.contains(Settings.Preferences.SESSION_TIMESTAMP)) {
            timestamp = sharedPreferences.getLong(Settings.Preferences.SESSION_TIMESTAMP, 0);
            lastEventTime = sharedPreferences.getLong(Settings.Preferences.SESSION_LAST_EVENT, 0);
            return timestamp != 0 &&lastEventTime != 0;
        }
        return false;
    }

    protected void saveSession(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Settings.SHARED_PREFERENCES, Activity.MODE_PRIVATE);
        sharedPreferences.edit()
                .putLong(Settings.Preferences.SESSION_LAST_EVENT, lastEventTime)
                .putLong(Settings.Preferences.SESSION_TIMESTAMP, timestamp)
                .apply();
    }

    private void updateLastEventTime() {
        lastEventTime = System.currentTimeMillis();
    }

    protected void start(Context context) {
        timestamp = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        lastEventTime = System.currentTimeMillis();
    }

    protected long getTimestamp() {
        return timestamp;
    }

    protected boolean isTimeout() {
        return lastEventTime + Settings.SESSION_TIMEOUT < System.currentTimeMillis();
    }

}
