package testhelpers;


import com.github.ladderwinner.LWTraceBuilder;
import com.github.ladderwinner.extra.LadderWinnerApplication;
import org.robolectric.TestLifecycleApplication;
import org.robolectric.shadows.ShadowLog;

import java.lang.reflect.Method;

public class LadderWinnerTestApplication extends LadderWinnerApplication implements TestLifecycleApplication {

    @Override
    public void onCreate() {
        ShadowLog.stream = System.out;
        super.onCreate();
    }

    @Override
    public void beforeTest(Method method) {

    }

    @Override
    public void prepareTest(Object test) {
    }

    @Override
    public void afterTest(Method method) {

    }

    @Override
    public String getPackageName() {
        return "11";
    }


    @Override
    public LWTraceBuilder onCreateTrackerConfig() {
        return LWTraceBuilder.createDefault("http://example.com", 1);
    }
}
