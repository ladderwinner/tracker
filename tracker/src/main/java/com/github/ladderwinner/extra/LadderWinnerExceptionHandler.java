/*
 * Android SDK for LadderWinner
 *
 * @link https://github.com/LadderWinner-org/LadderWinner-android-sdk
 * @license https://github.com/LadderWinner-org/LadderWinner-sdk-android/blob/master/LICENSE BSD-3 Clause
 */

package com.github.ladderwinner.extra;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.ladderwinner.LadderWinner;
import com.github.ladderwinner.TraceMe;
import com.github.ladderwinner.LWTracer;
import com.github.ladderwinner.dispatcher.DispatchMode;

import timber.log.Timber;

/**
 * An exception handler that wraps the existing exception handler and dispatches event to a {@link LWTracer}.
 * <p>
 * Also see documentation for {@link LWTraceUtil#uncaughtExceptions()}
 */
public class LadderWinnerExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = LadderWinner.tag(LadderWinnerExceptionHandler.class);
    private final LWTracer mLWTracer;
    private final TraceMe mTraceMe;
    private final Thread.UncaughtExceptionHandler mDefaultExceptionHandler;

    public LadderWinnerExceptionHandler(@NonNull LWTracer LWTracer, @Nullable TraceMe traceMe) {
        mLWTracer = LWTracer;
        mTraceMe = traceMe;
        mDefaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    public LWTracer getTracker() {
        return mLWTracer;
    }

    /**
     * This will give you the previous exception handler that is now wrapped.
     */
    public Thread.UncaughtExceptionHandler getDefaultExceptionHandler() {
        return mDefaultExceptionHandler;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        try {
            String excInfo = ex.getMessage();

            LWTracer LWTracer = getTracker();

            // Force the tracker into offline mode to ensure events are written to disk
            LWTracer.setDispatchMode(DispatchMode.EXCEPTION);

            LWTraceUtil.trace(mTraceMe).exception(ex).description(excInfo).fatal(true).with(LWTracer);

            // Immediately dispatch as the app might be dying after rethrowing the exception and block until the dispatch is completed
            LWTracer.dispatchBlocking();
        } catch (Exception e) {
            Timber.tag(TAG).e(e, "Couldn't track uncaught exception");
        } finally {
            // re-throw critical exception further to the os (important)
            if (getDefaultExceptionHandler() != null && getDefaultExceptionHandler() != this) {
                getDefaultExceptionHandler().uncaughtException(thread, ex);
            }
        }
    }
}
