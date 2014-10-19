package com.jorgemf.android.analytics;

import java.net.URI;
import java.util.concurrent.TimeUnit;

public class Settings {

    protected static final String LOG_TAG = "jorgemf.analytics";

    protected static final String APP_KEY = "jorgemf.analytics";

    protected static final long SESSION_TIMEOUT = TimeUnit.MINUTES.toMillis(30);

    protected static final long SYNCHRONIZE_WINDOW = TimeUnit.MINUTES.toMillis(5);

    protected static final String SHARED_PREFERENCES = "jorgemf.analytics.preferences";

    protected static final int MAXIMUM_EVENTS_SEQUENCE = 10;

    protected static final URI HTTP_URL = URI.create("http://server.com/api"); // TODO set proper url and end point

    protected static class Preferences {

        protected static final String USER_ID = "user.id";

        protected static final String SESSION_TIMESTAMP = "session.timestamp";

        protected static final String SESSION_LAST_EVENT = "session.last_event";

        protected static final String EVENTS_SEQUENCE = "events.sequence";

    }

    static class Database {

        protected static final String NAME = "jorgemf.analytics.sqlite";

        protected static final int VERSION = 1;

    }

    protected static class Json {

        protected static final String DATA = "data";

        protected static final String APP_KEY = "appkey";

        protected static final String USER_ID = "u";

        protected static final String SESSION_ID = "s";

        protected static final String SESSIONS = "s";

        protected static final String EVENTS = "e";

        protected static class Event {

            protected static final String ID = "i";

            protected static final String PARENT_ID = "p";

            protected static final String NAME = "n";

            protected static final String COUNT = "c";
        }

    }
}
