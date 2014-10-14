package com.jorgemf.android.analytics;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;

import java.util.LinkedList;

public class Analytics {

    private static Analytics instance = new Analytics();

    private Context appContext;

    private Session session;

    private User user;

    private LinkedList<Integer> idNodesSequence;

    private Database database;

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
        idNodesSequence = new LinkedList<>();
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
            }
        }
    }

    public static void onStop(Context context) {
        if (instance.appContext == null) {
            Log.e(Settings.LOG_TAG, "Analytics was not started. It cannot be finished.");
        } else {
            // TODO save last events chain
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
        int parentId = -1;
        if (!idNodesSequence.isEmpty()) {
            parentId = idNodesSequence.getLast();
        }
        SQLiteDatabase db = database.getWritableDatabase();
        db.beginTransaction();
        for () {
            // one per list, one list per sequence
            long id;
            Event event = new Event(eventName, parentId, session.getTimestamp(), database.getReadableDatabase());
            if (event.hasId()) {

                db.update(Database.Table.Event.TABLE, null, event.getContentValues());
            } else {
                id = db.insert(Database.Table.Event.TABLE, null, event.getContentValues());
            }
            db.endTransaction();
            idNodesSequence.add(id);
            while (idNodesSequence.size() > Settings.MAXIMUM_EVENTS_SEQUENCE) {
                // TODO this is not like this
                idNodesSequence.removeFirst();
            }
        }
        // TODO
    }

    private void synchronizeData() {
        synchronizeData(false);
    }

    private synchronized void synchronizeData(boolean force) {
        long currentTime = System.currentTimeMillis();
        if (force || lastSynchronizeTime + Settings.SYNCHRONIZE_WINDOW > currentTime) {
            // TODO
            // block a lock
            // launch a thread
            // update lastSynchronizeTime from thread
            lastSynchronizeTime = currentTime;
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
}
