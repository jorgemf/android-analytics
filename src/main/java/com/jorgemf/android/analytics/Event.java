package com.jorgemf.android.analytics;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Event {

    private static final String[] QUERY_PROJECTION_COUNT = {Database.Table.Event.COUNT};

    private static final String[] QUERY_PROJECTION_ID = {Database.Table.Event.ID};

    private static final String QUERY_SELECTION = Database.Table.Event.NAME + "=? AND "
            + Database.Table.Event.PARENT_ID + "=? AND "
            + Database.Table.Event.SESSION_TIMESTAMP + "=? ";

    public static final long NO_PARENT = -1;

    private long id;

    private String name;

    private long parentId;

    private long sessionId;

    private int count;

    protected Event(long id, String name, long parentId, long sessionId, int count) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
        this.sessionId = sessionId;
        this.count = count;
    }

    protected Event(String name, long parentId, long sessionId, SQLiteDatabase db) {
        this.id = -1;
        this.name = name;
        this.parentId = parentId;
        Cursor c = db.query(
                Database.Table.Event.TABLE,
                QUERY_PROJECTION_COUNT,
                QUERY_SELECTION,
                new String[]{name, Long.toString(parentId), Long.toString(sessionId)},
                null, null, null);
        if (c.moveToFirst()) {
            this.count = c.getInt(0) + 1;
        } else {
            this.count = 1;
        }
        c.close();
    }

    protected boolean hasId() {
        return id >= 0;
    }

    protected long getId() {
        return this.id;
    }

    protected ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        if (hasId()) {
            values.put(Database.Table.Event.ID, id);
        }
        values.put(Database.Table.Event.NAME, name);
        values.put(Database.Table.Event.PARENT_ID, parentId);
        values.put(Database.Table.Event.SESSION_TIMESTAMP, sessionId);
        values.put(Database.Table.Event.COUNT, count);
        return values;
    }

    protected static int getId(String name, int parentId, String sessionId, SQLiteDatabase db) {
        int id = -1;
        Cursor c = db.query(
                Database.Table.Event.TABLE,
                QUERY_PROJECTION_ID,
                QUERY_SELECTION,
                new String[]{name, Integer.toString(parentId), sessionId},
                null, null, null);
        if (c.moveToFirst()) {
            id = c.getInt(0);
        }
        c.close();
        return id;
    }
}
