package com.jorgemf.android.analytics;

import java.net.URI;
import java.util.concurrent.TimeUnit;

public class Settings {

    public static final String LOG_TAG = "jorgemf.analytics";

    public static final String APP_KEY = "jorgemf.analytics";

    public static final long SESSION_TIMEOUT = TimeUnit.MINUTES.toMillis(30);

    public static final long SYNCHRONIZE_WINDOW = TimeUnit.MINUTES.toMillis(5);

    public static final String SHARED_PREFERENCES = "jorgemf.analytics.preferences";

    public static final int MAXIMUM_EVENTS_SEQUENCE = 10;

    public static final URI HTTP_URL = URI.create("http://server.com/api"); // TODO set proper url and end point

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

    static class Json {

        public static final String DATA = "data";

        public static final String APP_KEY = "appkey";

        public static final String USER_ID = "u";

        public static final String SESSION_ID = "s";

        public static final String SESSIONS = "s";

        public static final String EVENTS = "e";

        static class Event {

            public static final String ID = "i";

            public static final String PARENT_ID = "p";

            public static final String NAME = "n";

            public static final String COUNT = "c";
        }

    }
}
