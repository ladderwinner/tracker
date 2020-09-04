package com.github.ladderwinner;

import android.content.Context;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import testhelpers.BaseTest;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LWTracerBuilderTest extends BaseTest {
    String mTestUrl = "https://example.com/LadderWinner.php";

    @Test
    public void testApplicationDomain() {
        LadderWinner LadderWinner = mock(LadderWinner.class);
        Context context = mock(Context.class);
        when(LadderWinner.getContext()).thenReturn(context);
        when(context.getPackageName()).thenReturn("some.pkg");

        LWTraceBuilder LWTraceBuilder = new LWTraceBuilder(mTestUrl, 1337, "");
        try {
            LWTraceBuilder.build(LadderWinner);
        } catch (Exception ignore) {}
        assertThat(LWTraceBuilder.getApplicationBaseUrl(), is("https://some.pkg/"));

        LWTraceBuilder.setApplicationBaseUrl("rest://something");
        assertThat(LWTraceBuilder.getApplicationBaseUrl(), is("rest://something"));
    }

    @Test
    public void testSiteId() {
        LWTraceBuilder LWTraceBuilder = new LWTraceBuilder(mTestUrl, 1337, "");
        assertThat(LWTraceBuilder.getSiteId(), is(1337));
    }

    @Test
    public void testGetName() {
        LWTraceBuilder LWTraceBuilder = new LWTraceBuilder(mTestUrl, 1337, "Default Tracker");
        assertThat(LWTraceBuilder.getTrackerName(), is("Default Tracker"));
        LWTraceBuilder.setTrackerName("strawberry");
        assertThat(LWTraceBuilder.getTrackerName(), is("strawberry"));

    }

    @Test
    public void testEquals() {
        LWTraceBuilder LWTraceBuilder1 = new LWTraceBuilder(mTestUrl, 1337, "a");
        LWTraceBuilder LWTraceBuilder2 = new LWTraceBuilder(mTestUrl, 1337, "a");
        LWTraceBuilder LWTraceBuilder3 = new LWTraceBuilder(mTestUrl, 1336, "b");
        assertThat(LWTraceBuilder1, is(LWTraceBuilder2));
        assertThat(LWTraceBuilder1, is(not(LWTraceBuilder3)));
    }

    @Test
    public void testHashCode() {
        LWTraceBuilder LWTraceBuilder = new LWTraceBuilder(mTestUrl, 1337, "Tracker");
        int result = mTestUrl.hashCode();
        result = 31 * result + 1337;
        result = 31 * result + "Tracker".hashCode();
        assertThat(result, is(LWTraceBuilder.hashCode()));
    }
}
