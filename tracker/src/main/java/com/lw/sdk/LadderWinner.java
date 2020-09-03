/*
 * Android SDK for LadderWinner
 *
 * @link https://github.com/LadderWinner-org/LadderWinner-android-sdk
 * @license https://github.com/LadderWinner-org/LadderWinner-sdk-android/blob/master/LICENSE BSD-3 Clause
 */

package com.lw.sdk;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;

import com.lw.sdk.dispatcher.DefaultDispatcherFactory;
import com.lw.sdk.dispatcher.DispatcherFactory;
import com.lw.sdk.tools.BuildInfo;
import com.lw.sdk.tools.Checksum;
import com.lw.sdk.tools.DeviceHelper;
import com.lw.sdk.tools.PropertySource;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;


public class LadderWinner {
    public static final String LOGGER_PREFIX = "LadderWinner:";
    private static final String TAG = LadderWinner.tag(LadderWinner.class);
    private static final String BASE_PREFERENCE_FILE = "com.lw.sdk";

    @SuppressLint("StaticFieldLeak") private static LadderWinner sInstance;

    private final Map<LWTracer, SharedPreferences> mPreferenceMap = new HashMap<>();
    private final Context mContext;
    private final SharedPreferences mBasePreferences;
    private DispatcherFactory mDispatcherFactory = new DefaultDispatcherFactory();

    public static synchronized LadderWinner getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LadderWinner.class) {
                if (sInstance == null) sInstance = new LadderWinner(context);
            }
        }
        return sInstance;
    }

    private LadderWinner(Context context) {
        mContext = context.getApplicationContext();
        mBasePreferences = context.getSharedPreferences(BASE_PREFERENCE_FILE, Context.MODE_PRIVATE);
    }

    public Context getContext() {
        return mContext;
    }

    /**
     * Base preferences, tracker idenpendent.
     */
    public SharedPreferences getPreferences() {
        return mBasePreferences;
    }

    /**
     * @return Tracker specific settings object
     */
    public SharedPreferences getTrackerPreferences(@NonNull LWTracer LWTracer) {
        synchronized (mPreferenceMap) {
            SharedPreferences newPrefs = mPreferenceMap.get(LWTracer);
            if (newPrefs == null) {
                String prefName;
                try {
                    prefName = "com.lw.sdk_" + Checksum.getMD5Checksum(LWTracer.getName());
                } catch (Exception e) {
                    Timber.tag(TAG).e(e);
                    prefName = "com.lw.sdk_" + LWTracer.getName();
                }
                newPrefs = getContext().getSharedPreferences(prefName, Context.MODE_PRIVATE);
                mPreferenceMap.put(LWTracer, newPrefs);
            }
            return newPrefs;
        }
    }

    /**
     * If you want to use your own {@link com.lw.sdk.dispatcher.Dispatcher}
     */
    public void setDispatcherFactory(DispatcherFactory dispatcherFactory) {
        this.mDispatcherFactory = dispatcherFactory;
    }

    public DispatcherFactory getDispatcherFactory() {
        return mDispatcherFactory;
    }

    DeviceHelper getDeviceHelper() {
        return new DeviceHelper(mContext, new PropertySource(), new BuildInfo());
    }

    public static String tag(Class... classes) {
        String[] tags = new String[classes.length];
        for (int i = 0; i < classes.length; i++) {
            tags[i] = classes[i].getSimpleName();
        }
        return tag(tags);
    }

    public static String tag(String... tags) {
        StringBuilder sb = new StringBuilder(LOGGER_PREFIX);
        for (int i = 0; i < tags.length; i++) {
            sb.append(tags[i]);
            if (i < tags.length - 1) sb.append(":");
        }
        return sb.toString();
    }
}
