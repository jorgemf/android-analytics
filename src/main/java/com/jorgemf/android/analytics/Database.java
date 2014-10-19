package com.jorgemf.android.analytics;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class Database extends SQLiteOpenHelper {

    public Database(Context context) {
        super(context, Settings.Database.NAME, null, Settings.Database.VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        try {
            sqLiteDatabase.execSQL(Table.Event.CREATE_SQL);
        } catch (SQLiteException e) {
            Log.e(Settings.LOG_TAG, e.getMessage(), e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // nothing
    }

    protected String toJson(String userId) {
        SQLiteDatabase dbRead = getReadableDatabase();
        Cursor cursor = dbRead.query(Table.Event.NAME, null, null, null, null, null, Table.Event.SESSION_TIMESTAMP);
        int iId = cursor.getColumnIndex(Table.Event.ID);
        int iSession = cursor.getColumnIndex(Table.Event.SESSION_TIMESTAMP);
        int iParentId = cursor.getColumnIndex(Table.Event.PARENT_ID);
        int iName = cursor.getColumnIndex(Table.Event.NAME);
        int iCount = cursor.getColumnIndex(Table.Event.COUNT);
        StringBuilder sb = new StringBuilder();
        if (cursor.moveToFirst()) {
            sb.append('{').append(Settings.Json.USER_ID).append(":\"").append(userId).append("\",");
            sb.append(Settings.Json.SESSIONS).append(":[{");
            long session = -1;
            do {
                long sessionId = cursor.getLong(iSession);
                if (session != sessionId) {
                    if (session == -1) {
                    } else {
                        sb.append("]},{");
                    }
                    sb.append(Settings.Json.SESSION_ID).append(':').append(sessionId).append(',');
                    sb.append(Settings.Json.EVENTS).append(":[");
                    session = sessionId;
                } else {
                    sb.append(',');
                }
                sb.append('{');
                sb.append(Settings.Json.Event.ID).append(':').append(cursor.getLong(iId)).append(',');
                sb.append(Settings.Json.Event.PARENT_ID).append(':').append(cursor.getLong(iParentId)).append(',');
                sb.append(Settings.Json.Event.NAME).append(':').append(cursor.getString(iName)).append(',');
                sb.append(Settings.Json.Event.COUNT).append(':').append(cursor.getInt(iCount)).append('}');

            } while (cursor.moveToNext());
            sb.append("}]}");
        }

        return sb.toString();
    }

    protected void cleanOldSessions(long timestamp) {
        SQLiteDatabase dbWrite = getWritableDatabase();
        dbWrite.delete(Table.Event.NAME, Table.Event.SESSION_TIMESTAMP + "<?", new String[]{Long.toString(timestamp)});
    }

    protected class Table {

        protected class Event {

            protected static final String TABLE = "event";

            protected static final String ID = BaseColumns._ID;

            protected static final String PARENT_ID = "parent_id";

            protected static final String SESSION_TIMESTAMP = "session";

            protected static final String NAME = "name";

            protected static final String COUNT = "count";

            protected static final String CREATE_SQL = "CREATE TABLE IF NOT EXISTS " + NAME + " ( " +
                    ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    PARENT_ID + " INTEGER, " +
                    SESSION_TIMESTAMP + " INTEGER NOT NULL, " +
                    NAME + " TEXT NOT NULL, " +
                    COUNT + " INTEGER NOT NULL " +
                    " );";

        }
    }
}
