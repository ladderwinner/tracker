package testhelpers;

import org.junit.runner.RunWith;
import com.lw.sdk.LadderWinner;
import com.lw.sdk.LWTracer;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

@Config(emulateSdk = 18, manifest = Config.NONE)
@RunWith(FullEnvTestRunner.class)
public abstract class DefaultTestCase extends BaseTest {
    public LWTracer createTracker() {
        LadderWinnerTestApplication app = (LadderWinnerTestApplication) Robolectric.application;
        final LWTracer LWTracer = app.onCreateTrackerConfig().build(LadderWinner.getInstance(Robolectric.application));
        LWTracer.getPreferences().edit().clear().apply();
        return LWTracer;
    }

    public LadderWinner getLadderWinner() {
        return LadderWinner.getInstance(Robolectric.application);
    }

}
