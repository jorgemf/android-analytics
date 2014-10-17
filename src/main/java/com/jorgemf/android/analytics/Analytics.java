package com.jorgemf.android.analytics;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Analytics {

    private static Analytics instance = new Analytics();

    private Context appContext;

    private Session session;

    private User user;

    private long[][] idNodesSequence;

    private Database database;

    static private final Lock synchronizedLock = new ReentrantLock();

    /**
     * in AndroidManifest.xml inside application tag:
     * <meta-data
     * android:name="jorgemf.analytics"
     * android:value="appkey"/>
     *
     * @see com.jorgemf.android.analytics.Settings#APP_KEY
     */
    private String appKey;

    private long lastSynchronizeTime;

    private Analytics() {
        idNodesSequence = new long[Settings.MAXIMUM_EVENTS_SEQUENCE][Settings.MAXIMUM_EVENTS_SEQUENCE];
        for (int i = 0; i < Settings.MAXIMUM_EVENTS_SEQUENCE; i++) {
            for (int j = 0; j < Settings.MAXIMUM_EVENTS_SEQUENCE; j++) {
                idNodesSequence[i][j] = Event.NO_PARENT;
            }
        }
    }

    public static void onStart(Context context) {
        if (instance.appContext == null) {
            instance.appContext = context.getApplicationContext();
            instance.setAppKey();
            instance.session = new Session();
            instance.user = User.instance(context);
            instance.database = Database.instance(context);
            if (!instance.session.loadSavedSession(context)) {
                instance.session.start(context);
            } else {
                instance.loadSequence(context);
            }
        }
    }

    public static void onStop(Context context) {
        if (instance.appContext == null) {
            Log.e(Settings.LOG_TAG, "Analytics was not started. It cannot be finished.");
        } else {
            instance.saveSequence(context);
            instance.session.saveSession(context);
            instance.synchronizeData();
        }
    }

    public static void restartSession() {
        if (instance.appContext == null) {
            Log.e(Settings.LOG_TAG, "Analytics was not started. New session cannot be started.");
        } else {
            instance.session.start(instance.appContext);
        }
    }

    public static void flush() {
        if (instance.appContext == null) {
            Log.e(Settings.LOG_TAG, "Analytics was not started. It cannot be finished.");
        } else {
            instance.synchronizeData();
        }
    }

    public static void trackEvent(String eventName) {
        if (instance.appContext == null) {
            Log.e(Settings.LOG_TAG, "Analytics was not started. Event is not tracked.");
        } else {
            if (instance.session.isTimeout()) {
                instance.synchronizeData();
                instance.session.start(instance.appContext);
            }
            instance.track(eventName);
        }
    }

    private synchronized void track(String eventName) {
        ArrayList<Event> events = new ArrayList<>();
        long sessionId = session.getTimestamp();
        SQLiteDatabase dbRead = database.getReadableDatabase();
        events.add(new Event(eventName, Event.NO_PARENT, sessionId, dbRead));
        for (int i = 0; i < Settings.MAXIMUM_EVENTS_SEQUENCE - 1; i++) {
            if (idNodesSequence[i][i] != -1) {
                events.add(new Event(eventName, idNodesSequence[i][i], sessionId, dbRead));
            }
        }
        dbRead.close();
        SQLiteDatabase dbWrite = database.getWritableDatabase();
        dbWrite.beginTransaction();
        try {
            for (int i = events.size() - 1; i >= 0; i--) {
                Event event = events.get(i);
                // one per list, one list per sequence
                long id;
                if (event.hasId()) {
                    id = event.getId();
                    dbWrite.update(Database.Table.Event.TABLE, event.getContentValues(), Database.Table.Event.ID + "=?", new String[]{Long.toString(id)});
                } else {
                    id = dbWrite.insert(Database.Table.Event.TABLE, null, event.getContentValues());
                }
                if (i > 0) {
                    System.arraycopy(idNodesSequence[i], 1, idNodesSequence[i], 0, i);
                }
                idNodesSequence[i][i] = id;
            }
            dbWrite.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(Settings.LOG_TAG, "Failed to store events locally: " + e.getMessage());
        }
        dbWrite.endTransaction();
    }

    private void saveSequence(Context context) {
        StringBuilder sequence = new StringBuilder();
        for (int i = 0; i < idNodesSequence.length; i++) {
            for (int j = 0; j <= i; j++) {
                if (idNodesSequence[i][j] != -1) {
                    sequence.append(Long.toHexString(idNodesSequence[i][j])).append(',');
                }
            }
            sequence.append(';');
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(Settings.SHARED_PREFERENCES, Activity.MODE_PRIVATE);
        sharedPreferences.edit().putString(Settings.Preferences.EVENTS_SEQUENCE, sequence.toString());
    }

    private void loadSequence(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Settings.SHARED_PREFERENCES, Activity.MODE_PRIVATE);
        String sequence = sharedPreferences.getString(Settings.Preferences.EVENTS_SEQUENCE, "");
        int start = 0;
        int row = 0;
        int column = 0;
        for (int i = 0; i < sequence.length(); i++) {
            char c = sequence.charAt(i);
            if (c == ',') {
                idNodesSequence[row][column] = Long.parseLong(sequence.substring(start, i), 16);
                column++;
            } else if (c == ';') {
                row++;
                column = 0;
            }
        }
    }

    private void synchronizeData() {
        synchronizeData(false);
    }

    private synchronized void synchronizeData(boolean force) {
        long currentTime = System.currentTimeMillis();
        if (force || lastSynchronizeTime + Settings.SYNCHRONIZE_WINDOW > currentTime) {
            Thread thread = new Thread(new SynchronizeThread(this));
            thread.start();
        }
    }

    private void setAppKey() {
        try {
            ApplicationInfo ai = appContext.getPackageManager().getApplicationInfo(appContext.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            appKey = bundle.getString(Settings.APP_KEY);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(Settings.LOG_TAG, "Failed to load meta-data, NameNotFound: " + e.getMessage());
        } catch (NullPointerException e) {
            Log.e(Settings.LOG_TAG, "Failed to load meta-data, NullPointer: " + e.getMessage());
        }
    }

    private class SynchronizeThread implements Runnable {

        private Analytics analytics;

        protected SynchronizeThread(Analytics analytics) {
            this.analytics = analytics;
        }

        @Override
        public void run() {
            long currentTime = System.currentTimeMillis();
            synchronizedLock.lock();
            try {
                long currentSession = analytics.session.getTimestamp();
                // create json to send it
                String json = database.toJson(analytics.user.getId());
                // make http request
                if (HttpRequest.postData(analytics.appKey, json)) {
                    // update last synchronization
                    analytics.lastSynchronizeTime = currentTime;
                    // clean everything in the database which is older than current session
                    database.cleanOldSessions(session.getTimestamp());
                }
            } catch (Exception e) {
                Log.e(Settings.LOG_TAG, "Failed to synchronize: " + e.getMessage());
            } finally {
                synchronizedLock.unlock();
            }
        }
    }
}
