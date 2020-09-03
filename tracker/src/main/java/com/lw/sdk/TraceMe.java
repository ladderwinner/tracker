/*
 * Android SDK for LadderWinner
 *
 * @link https://github.com/LadderWinner-org/LadderWinner-android-sdk
 * @license https://github.com/LadderWinner-org/LadderWinner-sdk-android/blob/master/LICENSE BSD-3 Clause
 */

package com.lw.sdk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * This objects represents one query to LadderWinner.
 * For each event send to LadderWinner a TrackMe gets created, either explicitly by you or implicitly by the Tracker.
 */
public class TraceMe {
    private static final int DEFAULT_QUERY_CAPACITY = 14;
    private final HashMap<String, String> mQueryParams = new HashMap<>(DEFAULT_QUERY_CAPACITY);

    public TraceMe() { }

    public TraceMe(TraceMe traceMe) {
        mQueryParams.putAll(traceMe.mQueryParams);
    }

    /**
     * Adds TrackMe to this TrackMe, overriding values if necessary.
     */
    public TraceMe putAll(@NonNull TraceMe traceMe) {
        mQueryParams.putAll(traceMe.toMap());
        return this;
    }

    /**
     * Consider using {@link QueryParams} instead of raw strings
     */
    public synchronized TraceMe set(@NonNull String key, String value) {
        if (value == null) mQueryParams.remove(key);
        else if (value.length() > 0) mQueryParams.put(key, value);
        return this;
    }

    /**
     * Consider using {@link QueryParams} instead of raw strings
     */
    @Nullable
    public synchronized String get(@NonNull String queryParams) {
        return mQueryParams.get(queryParams);
    }

    /**
     * You can set any additional Tracking API Parameters within the SDK.
     * This includes for example the local time (parameters h, m and s).
     * <pre>
     * set(QueryParams.HOURS, "10");
     * set(QueryParams.MINUTES, "45");
     * set(QueryParams.SECONDS, "30");
     * </pre>
     *
     * @param key   query params name
     * @param value value
     * @return tracker instance
     */
    public synchronized TraceMe set(@NonNull QueryParams key, String value) {
        set(key.toString(), value);
        return this;
    }

    public synchronized TraceMe set(@NonNull QueryParams key, int value) {
        set(key, Integer.toString(value));
        return this;
    }

    public synchronized TraceMe set(@NonNull QueryParams key, float value) {
        set(key, Float.toString(value));
        return this;
    }

    public synchronized TraceMe set(@NonNull QueryParams key, long value) {
        set(key, Long.toString(value));
        return this;
    }

    public synchronized boolean has(@NonNull QueryParams queryParams) {
        return mQueryParams.containsKey(queryParams.toString());
    }

    /**
     * Only sets the value if it doesn't exist.
     *
     * @param key   type
     * @param value value
     * @return this (for chaining)
     */
    public synchronized TraceMe trySet(@NonNull QueryParams key, int value) {
        return trySet(key, String.valueOf(value));
    }

    /**
     * Only sets the value if it doesn't exist.
     *
     * @param key   type
     * @param value value
     * @return this (for chaining)
     */
    public synchronized TraceMe trySet(@NonNull QueryParams key, float value) {
        return trySet(key, String.valueOf(value));
    }

    public synchronized TraceMe trySet(@NonNull QueryParams key, long value) {
        return trySet(key, String.valueOf(value));
    }

    /**
     * Only sets the value if it doesn't exist.
     *
     * @param key   type
     * @param value value
     * @return this (for chaining)
     */
    public synchronized TraceMe trySet(@NonNull QueryParams key, String value) {
        if (!has(key)) set(key, value);
        return this;
    }

    /**
     * The tracker calls this to get the final data that will be transmitted
     *
     * @return the parameter map, but without the base URL
     */
    public synchronized Map<String, String> toMap() {
        return new HashMap<>(mQueryParams);
    }

    public synchronized String get(@NonNull QueryParams queryParams) {
        return mQueryParams.get(queryParams.toString());
    }

    public synchronized boolean isEmpty() {
        return mQueryParams.isEmpty();
    }
}
