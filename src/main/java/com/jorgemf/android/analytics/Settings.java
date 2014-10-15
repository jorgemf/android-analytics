package com.jorgemf.android.analytics;

import java.util.concurrent.TimeUnit;

public class Settings {

    public static final String LOG_TAG = "jorgemf.analytics";

    public static final String APP_KEY = "jorgemf.analytics";

    public static final long SESSION_TIMEOUT = TimeUnit.MINUTES.toMillis(30);

    public static final long SYNCHRONIZE_WINDOW = TimeUnit.MINUTES.toMillis(5);

    public static final String SHARED_PREFERENCES = "jorgemf.analytics.preferences";

    public static final int MAXIMUM_EVENTS_SEQUENCE = 10;

    static class Preferences {

        public static final String USER_ID = "user.id";

        public static final String SESSION_TIMESTAMP = "session.timestamp";

        public static final String SESSION_LAST_EVENT = "session.last_event";

        public static final String EVENTS_SEQUENCE = "events.sequence";

    }

    static class Database {

        public static final String NAME = "jorgemf.analytics.sqlite";

        public static final int VERSION = 1;

    }
}
