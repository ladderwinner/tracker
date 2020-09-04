package com.github.ladderwinner.extra;

import android.annotation.TargetApi;
import android.app.Application;
import android.os.Build;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.github.ladderwinner.LadderWinner;
import com.github.ladderwinner.QueryParams;
import com.github.ladderwinner.LWTracer;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Collections;

import testhelpers.DefaultTestCase;
import testhelpers.FullEnvTestRunner;
import testhelpers.QueryHashMap;
import testhelpers.TestActivity;

import static org.junit.Assert.assertEquals;

@Config(emulateSdk = 18, manifest = Config.NONE)
@RunWith(FullEnvTestRunner.class)
public class LadderWinnerApplicationTest extends DefaultTestCase {
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Test
    public void testAutoBindActivities() {
        Application app = Robolectric.application;
        LWTracer LWTracer = createTracker();
        LWTracer.setDryRunTarget(Collections.synchronizedList(new ArrayList<>()));
        //auto attach tracking screen view
        LWTraceUtil.trace().screens(app).with(LWTracer);

        // emulate default trackScreenView
        Robolectric.buildActivity(TestActivity.class).create().start().resume().visible().get();

        assertEquals(TestActivity.getTestTitle(), new QueryHashMap(LWTracer.getLastEventX()).get(QueryParams.ACTION_NAME));
    }

    @Test
    public void testApplicationGetTracker() {
        LadderWinnerApplication LadderWinnerApplication = (LadderWinnerApplication) Robolectric.application;
        assertEquals(LadderWinnerApplication.getTracker(), LadderWinnerApplication.getTracker());
    }

    @Test
    public void testApplication() {
        LadderWinnerApplication LadderWinnerApplication = (LadderWinnerApplication) Robolectric.application;
        Assert.assertEquals(LadderWinnerApplication.getLadderWinner(), LadderWinner.getInstance(LadderWinnerApplication));
    }
}
