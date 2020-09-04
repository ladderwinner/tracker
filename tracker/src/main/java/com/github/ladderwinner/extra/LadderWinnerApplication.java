/*
 * Android SDK for LadderWinner
 *
 * @link https://github.com/LadderWinner-org/LadderWinner-android-sdk
 * @license https://github.com/LadderWinner-org/LadderWinner-sdk-android/blob/master/LICENSE BSD-3 Clause
 */

package com.github.ladderwinner.extra;

import android.app.Application;

import com.github.ladderwinner.LadderWinner;
import com.github.ladderwinner.LWTracer;
import com.github.ladderwinner.LWTraceBuilder;

public abstract class LadderWinnerApplication extends Application {
    private LWTracer mLadderWinnerLWTracer;

    public LadderWinner getLadderWinner() {
        return LadderWinner.getInstance(this);
    }

    /**
     * Gives you an all purpose thread-safe persisted Tracker.
     *
     * @return a shared Tracker
     */
    public synchronized LWTracer getTracker() {
        if (mLadderWinnerLWTracer == null) mLadderWinnerLWTracer = onCreateTrackerConfig().build(getLadderWinner());
        return mLadderWinnerLWTracer;
    }

    /**
     * See {@link LWTraceBuilder}.
     * You may be interested in {@link LWTraceBuilder#createDefault(String, int)}
     *
     * @return the tracker configuration you want to use.
     */
    public abstract LWTraceBuilder onCreateTrackerConfig();

    @Override
    public void onLowMemory() {
        if (mLadderWinnerLWTracer != null) mLadderWinnerLWTracer.dispatch();
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        if ((level == TRIM_MEMORY_UI_HIDDEN || level == TRIM_MEMORY_COMPLETE) && mLadderWinnerLWTracer != null) {
            mLadderWinnerLWTracer.dispatch();
        }
        super.onTrimMemory(level);
    }

}
