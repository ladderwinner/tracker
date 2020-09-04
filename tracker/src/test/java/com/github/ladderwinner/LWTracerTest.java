package com.github.ladderwinner;

import android.content.Context;
import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;
import com.github.ladderwinner.dispatcher.DispatchMode;
import com.github.ladderwinner.dispatcher.Dispatcher;
import com.github.ladderwinner.dispatcher.DispatcherFactory;
import com.github.ladderwinner.extra.LWTraceUtil;
import com.github.ladderwinner.tools.DeviceHelper;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import testhelpers.TestHelper;
import testhelpers.TestPreferences;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static com.github.ladderwinner.QueryParams.FIRST_VISIT_TIMESTAMP;
import static com.github.ladderwinner.QueryParams.SESSION_START;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@SuppressWarnings("PointlessArithmeticExpression")
public class LWTracerTest {
    ArgumentCaptor<TraceMe> mCaptor = ArgumentCaptor.forClass(TraceMe.class);
    @Mock
    LadderWinner mLadderWinner;
    @Mock Context mContext;
    @Mock Dispatcher mDispatcher;
    @Mock DispatcherFactory mDispatcherFactory;
    @Mock DeviceHelper mDeviceHelper;
    SharedPreferences mTrackerPreferences = new TestPreferences();
    SharedPreferences mPreferences = new TestPreferences();
    private final String mApiUrl = "http://example.com";
    private final int mSiteId = 11;
    private final String mTrackerName = "Default Tracker";
    @Mock
    LWTraceBuilder mLWTraceBuilder;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        when(mLadderWinner.getContext()).thenReturn(mContext);
        when(mLadderWinner.getTrackerPreferences(any(LWTracer.class))).thenReturn(mTrackerPreferences);
        when(mLadderWinner.getPreferences()).thenReturn(mPreferences);
        when(mLadderWinner.getDispatcherFactory()).thenReturn(mDispatcherFactory);
        when(mDispatcherFactory.build(any(LWTracer.class))).thenReturn(mDispatcher);
        when(mLadderWinner.getDeviceHelper()).thenReturn(mDeviceHelper);
        when(mDeviceHelper.getResolution()).thenReturn(new int[]{480, 800});
        when(mDeviceHelper.getUserAgent()).thenReturn("aUserAgent");
        when(mDeviceHelper.getUserLanguage()).thenReturn("en");

        when(mLWTraceBuilder.getApiUrl()).thenReturn(mApiUrl);
        when(mLWTraceBuilder.getSiteId()).thenReturn(mSiteId);
        when(mLWTraceBuilder.getTrackerName()).thenReturn(mTrackerName);
        when(mLWTraceBuilder.getApplicationBaseUrl()).thenReturn("http://this.is.our.package/");

        mTrackerPreferences.edit().clear();
        mPreferences.edit().clear();
    }

    @Test
    public void testGetPreferences() {
        LWTracer LWTracer1 = new LWTracer(mLadderWinner, mLWTraceBuilder);
        verify(mLadderWinner).getTrackerPreferences(LWTracer1);
    }

    /**
     * https://github.com/LadderWinner-org/LadderWinner-sdk-android/issues/92
     */
    @Test
    public void testLastScreenUrl() {
        LWTracer LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);

        LWTracer.trace(new TraceMe());
        verify(mDispatcher).submit(mCaptor.capture());
        assertEquals("http://this.is.our.package/", mCaptor.getValue().get(QueryParams.URL_PATH));

        LWTracer.trace(new TraceMe().set(QueryParams.URL_PATH, "http://some.thing.com/foo/bar"));
        verify(mDispatcher, times(2)).submit(mCaptor.capture());
        assertEquals("http://some.thing.com/foo/bar", mCaptor.getValue().get(QueryParams.URL_PATH));

        LWTracer.trace(new TraceMe().set(QueryParams.URL_PATH, "http://some.other/thing"));
        verify(mDispatcher, times(3)).submit(mCaptor.capture());
        assertEquals("http://some.other/thing", mCaptor.getValue().get(QueryParams.URL_PATH));

        LWTracer.trace(new TraceMe());
        verify(mDispatcher, times(4)).submit(mCaptor.capture());
        assertEquals("http://some.other/thing", mCaptor.getValue().get(QueryParams.URL_PATH));

        LWTracer.trace(new TraceMe().set(QueryParams.URL_PATH, "thang"));
        verify(mDispatcher, times(5)).submit(mCaptor.capture());
        assertEquals("http://this.is.our.package/thang", mCaptor.getValue().get(QueryParams.URL_PATH));
    }

    @Test
    public void testSetDispatchInterval() {
        LWTracer LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        LWTracer.setDispatchInterval(1);
        verify(mDispatcher).setDispatchInterval(1);
        LWTracer.getDispatchInterval();
        verify(mDispatcher).getDispatchInterval();
    }

    @Test
    public void testSetDispatchTimeout() {
        LWTracer LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        int timeout = 1337;
        LWTracer.setDispatchTimeout(timeout);
        verify(mDispatcher).setConnectionTimeOut(timeout);
        LWTracer.getDispatchTimeout();
        verify(mDispatcher).getConnectionTimeOut();
    }

    @Test
    public void testGetOfflineCacheAge_defaultValue() {
        LWTracer LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        assertEquals(24 * 60 * 60 * 1000, LWTracer.getOfflineCacheAge());
    }

    @Test
    public void testSetOfflineCacheAge() {
        LWTracer LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        LWTracer.setOfflineCacheAge(80085);
        assertEquals(80085, LWTracer.getOfflineCacheAge());
    }

    @Test
    public void testGetOfflineCacheSize_defaultValue() {
        LWTracer LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        assertEquals(4 * 1024 * 1024, LWTracer.getOfflineCacheSize());
    }

    @Test
    public void testSetOfflineCacheSize() {
        LWTracer LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        LWTracer.setOfflineCacheSize(16 * 1000 * 1000);
        assertEquals(16 * 1000 * 1000, LWTracer.getOfflineCacheSize());
    }

    @Test
    public void testDispatchMode_default() {
        mTrackerPreferences.edit().clear();
        LWTracer LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        assertEquals(DispatchMode.ALWAYS, LWTracer.getDispatchMode());
        verify(mDispatcher, times(1)).setDispatchMode(DispatchMode.ALWAYS);
    }

    @Test
    public void testDispatchMode_change() {
        LWTracer LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        LWTracer.setDispatchMode(DispatchMode.WIFI_ONLY);
        assertEquals(DispatchMode.WIFI_ONLY, LWTracer.getDispatchMode());
        verify(mDispatcher, times(1)).setDispatchMode(DispatchMode.WIFI_ONLY);
    }

    @Test
    public void testDispatchMode_fallback() {
        LWTracer LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        LWTracer.getPreferences().edit().putString(LWTracer.PREF_KEY_DISPATCHER_MODE, "lol").apply();
        assertEquals(DispatchMode.ALWAYS, LWTracer.getDispatchMode());
        verify(mDispatcher, times(1)).setDispatchMode(DispatchMode.ALWAYS);
    }

    @Test
    public void testSetDispatchMode_propagation() {
        mTrackerPreferences.edit().clear();
        LWTracer LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        verify(mDispatcher, times(1)).setDispatchMode(any());
    }

    @Test
    public void testSetDispatchMode_propagation_change() {
        mTrackerPreferences.edit().clear();
        LWTracer LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        LWTracer.setDispatchMode(DispatchMode.WIFI_ONLY);
        LWTracer.setDispatchMode(DispatchMode.WIFI_ONLY);
        assertEquals(DispatchMode.WIFI_ONLY, LWTracer.getDispatchMode());
        verify(mDispatcher, times(2)).setDispatchMode(DispatchMode.WIFI_ONLY);
        verify(mDispatcher, times(3)).setDispatchMode(any());
    }

    @Test
    public void testSetDispatchMode_exception() {
        LWTracer LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        LWTracer.setDispatchMode(DispatchMode.WIFI_ONLY); // This is persisted
        LWTracer.setDispatchMode(DispatchMode.EXCEPTION); // This isn't
        assertEquals(DispatchMode.EXCEPTION, LWTracer.getDispatchMode());
        verify(mDispatcher, times(1)).setDispatchMode(DispatchMode.EXCEPTION);

        LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        assertEquals(DispatchMode.WIFI_ONLY, LWTracer.getDispatchMode());
    }

    @Test
    public void testsetDispatchGzip() {
        LWTracer LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        LWTracer.setDispatchGzipped(true);
        verify(mDispatcher).setDispatchGzipped(true);
    }

    @Test
    public void testOptOut_set() {
        LWTracer LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        LWTracer.setOptOut(true);
        verify(mDispatcher).clear();
        assertTrue(LWTracer.isOptOut());
        LWTracer.setOptOut(false);
        assertFalse(LWTracer.isOptOut());
    }

    @Test
    public void testOptOut_init() {
        mTrackerPreferences.edit().putBoolean(LWTracer.PREF_KEY_TRACKER_OPTOUT, false).apply();
        LWTracer LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        assertFalse(LWTracer.isOptOut());
        mTrackerPreferences.edit().putBoolean(LWTracer.PREF_KEY_TRACKER_OPTOUT, true).apply();
        LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        assertTrue(LWTracer.isOptOut());
    }

    @Test
    public void testDispatch() {
        LWTracer LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        LWTracer.dispatch();
        verify(mDispatcher).forceDispatch();
        LWTracer.dispatch();
        verify(mDispatcher, times(2)).forceDispatch();
    }

    @Test
    public void testDispatch_optOut() {
        LWTracer LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        LWTracer.setOptOut(true);
        LWTracer.dispatch();
        verify(mDispatcher, never()).forceDispatch();
        LWTracer.setOptOut(false);
        LWTracer.dispatch();
        verify(mDispatcher).forceDispatch();
    }

    @Test
    public void testGetSiteId() {
        when(mLWTraceBuilder.getSiteId()).thenReturn(11);
        assertEquals(new LWTracer(mLadderWinner, mLWTraceBuilder).getSiteId(), 11);
    }

    @Test
    public void testGetLadderWinner() {
        LWTracer LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        assertEquals(mLadderWinner, LWTracer.getLadderWinner());
    }

    @Test
    public void testSetURL() {
        when(mLWTraceBuilder.getApplicationBaseUrl()).thenReturn("http://test.com/");
        LWTracer LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);

        TraceMe traceMe = new TraceMe();
        LWTracer.trace(traceMe);
        assertEquals("http://test.com/", traceMe.get(QueryParams.URL_PATH));

        traceMe.set(QueryParams.URL_PATH, "me");
        LWTracer.trace(traceMe);
        assertEquals("http://test.com/me", traceMe.get(QueryParams.URL_PATH));

        // override protocol
        traceMe.set(QueryParams.URL_PATH, "https://my.com/secure");
        LWTracer.trace(traceMe);
        assertEquals("https://my.com/secure", traceMe.get(QueryParams.URL_PATH));
    }

    @Test
    public void testApplicationDomain() {
        when(mLWTraceBuilder.getApplicationBaseUrl()).thenReturn("http://my-domain.com");
        LWTracer LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);

        LWTraceUtil.trace().screen("test/test").title("Test title").with(LWTracer);
        verify(mDispatcher).submit(mCaptor.capture());
        validateDefaultQuery(mCaptor.getValue());
        assertTrue(mCaptor.getValue().get(QueryParams.URL_PATH).equals("http://my-domain.com/test/test"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVisitorId_invalid_short() {
        LWTracer LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        String tooShortVisitorId = "0123456789ab";
        LWTracer.setVisitorId(tooShortVisitorId);
        assertNotEquals(tooShortVisitorId, LWTracer.getVisitorId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVisitorId_invalid_long() {
        LWTracer LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        String tooLongVisitorId = "0123456789abcdefghi";
        LWTracer.setVisitorId(tooLongVisitorId);
        assertNotEquals(tooLongVisitorId, LWTracer.getVisitorId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVisitorId_invalid_charset() {
        LWTracer LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        String invalidCharacterVisitorId = "01234-6789-ghief";
        LWTracer.setVisitorId(invalidCharacterVisitorId);
        assertNotEquals(invalidCharacterVisitorId, LWTracer.getVisitorId());
    }

    @Test
    public void testVisitorId_init() {
        LWTracer LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        assertThat(LWTracer.getVisitorId(), is(notNullValue()));
    }

    @Test
    public void testVisitorId_restore() {
        LWTracer LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        assertThat(LWTracer.getVisitorId(), is(notNullValue()));
        String visitorId = LWTracer.getVisitorId();

        LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        assertThat(LWTracer.getVisitorId(), is(visitorId));
    }

    @Test
    public void testVisitorId_dispatch() {
        LWTracer LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        String visitorId = "0123456789abcdef";
        LWTracer.setVisitorId(visitorId);
        assertEquals(visitorId, LWTracer.getVisitorId());

        LWTracer.trace(new TraceMe());
        verify(mDispatcher).submit(mCaptor.capture());
        assertEquals(visitorId, mCaptor.getValue().get(QueryParams.VISITOR_ID));

        LWTracer.trace(new TraceMe());
        verify(mDispatcher, times(2)).submit(mCaptor.capture());
        assertEquals(visitorId, mCaptor.getValue().get(QueryParams.VISITOR_ID));
    }

    @Test
    public void testUserID_init() {
        LWTracer LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        assertNull(LWTracer.getDefaultTrackMe().get(QueryParams.USER_ID));
        assertNull(LWTracer.getUserId());
    }

    @Test
    public void testUserID_restore() {
        LWTracer LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        assertNull(LWTracer.getUserId());
        LWTracer.setUserId("cake");
        assertThat(LWTracer.getUserId(), is("cake"));

        LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        assertThat(LWTracer.getUserId(), is("cake"));
        assertThat(LWTracer.getDefaultTrackMe().get(QueryParams.USER_ID), is("cake"));
    }

    @Test
    public void testUserID_invalid() {
        LWTracer LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        assertNull(LWTracer.getUserId());

        LWTracer.setUserId("test");
        assertEquals(LWTracer.getUserId(), "test");

        LWTracer.setUserId("");
        assertEquals(LWTracer.getUserId(), "test");

        LWTracer.setUserId(null);
        assertNull(LWTracer.getUserId());

        String uuid = UUID.randomUUID().toString();
        LWTracer.setUserId(uuid);
        assertEquals(uuid, LWTracer.getUserId());
        assertEquals(uuid, LWTracer.getUserId());
    }

    @Test
    public void testUserID_dispatch() {
        LWTracer LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        String uuid = UUID.randomUUID().toString();
        LWTracer.setUserId(uuid);

        LWTracer.trace(new TraceMe());
        verify(mDispatcher).submit(mCaptor.capture());
        assertEquals(uuid, mCaptor.getValue().get(QueryParams.USER_ID));

        LWTracer.trace(new TraceMe());
        verify(mDispatcher, times(2)).submit(mCaptor.capture());
        assertEquals(uuid, mCaptor.getValue().get(QueryParams.USER_ID));
    }

    @Test
    public void testGetResolution() {
        LWTracer LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        TraceMe traceMe = new TraceMe();
        LWTracer.trace(traceMe);
        verify(mDispatcher).submit(mCaptor.capture());
        assertEquals("480x800", mCaptor.getValue().get(QueryParams.SCREEN_RESOLUTION));
    }

    @Test
    public void testSetNewSession() {
        LWTracer LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        TraceMe traceMe = new TraceMe();
        LWTracer.trace(traceMe);
        verify(mDispatcher).submit(mCaptor.capture());
        assertEquals("1", mCaptor.getValue().get(QueryParams.SESSION_START));

        LWTracer.startNewSession();
        LWTraceUtil.trace().screen("").with(LWTracer);
        verify(mDispatcher, times(2)).submit(mCaptor.capture());
        assertEquals("1", mCaptor.getValue().get(QueryParams.SESSION_START));
    }

    @Test
    public void testSetNewSessionRaceCondition() {
        for (int retry = 0; retry < 5; retry++) {
            final List<TraceMe> traceMes = Collections.synchronizedList(new ArrayList<TraceMe>());
            doAnswer(invocation -> {
                traceMes.add(invocation.getArgument(0));
                return null;
            }).when(mDispatcher).submit(any(TraceMe.class));
            final LWTracer LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
            LWTracer.setDispatchInterval(0);
            int count = 20;
            for (int i = 0; i < count; i++) {
                new Thread(() -> {
                    TestHelper.sleep(10);
                    LWTraceUtil.trace().screen("Test").with(LWTracer);
                }).start();
            }
            TestHelper.sleep(500);
            assertEquals(count, traceMes.size());
            int found = 0;
            for (TraceMe traceMe : traceMes) {
                if (traceMe.get(QueryParams.SESSION_START) != null) found++;
            }
            assertEquals(1, found);
        }
    }

    @Test
    public void testSetSessionTimeout() {
        LWTracer LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        LWTracer.setSessionTimeout(10000);

        LWTraceUtil.trace().screen("test1").with(LWTracer);
        assertThat(LWTracer.getLastEventX().get(QueryParams.SESSION_START), notNullValue());

        LWTraceUtil.trace().screen("test2").with(LWTracer);
        assertThat(LWTracer.getLastEventX().get(QueryParams.SESSION_START), nullValue());

        LWTracer.setSessionTimeout(0);
        TestHelper.sleep(1);
        LWTraceUtil.trace().screen("test3").with(LWTracer);
        assertThat(LWTracer.getLastEventX().get(QueryParams.SESSION_START), notNullValue());

        LWTracer.setSessionTimeout(10000);
        assertEquals(LWTracer.getSessionTimeout(), 10000);
        LWTraceUtil.trace().screen("test3").with(LWTracer);
        assertThat(LWTracer.getLastEventX().get(QueryParams.SESSION_START), nullValue());
    }

    @Test
    public void testCheckSessionTimeout() {
        LWTracer LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        LWTracer.setSessionTimeout(0);
        LWTraceUtil.trace().screen("test").with(LWTracer);
        verify(mDispatcher).submit(mCaptor.capture());
        assertEquals("1", mCaptor.getValue().get(QueryParams.SESSION_START));
        TestHelper.sleep(1);
        LWTraceUtil.trace().screen("test").with(LWTracer);
        verify(mDispatcher, times(2)).submit(mCaptor.capture());
        assertEquals("1", mCaptor.getValue().get(QueryParams.SESSION_START));
        LWTracer.setSessionTimeout(60000);
        LWTraceUtil.trace().screen("test").with(LWTracer);
        verify(mDispatcher, times(3)).submit(mCaptor.capture());
        assertEquals(null, mCaptor.getValue().get(QueryParams.SESSION_START));
    }

    @Test
    public void testTrackerEquals() {
        LWTracer LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        LWTraceBuilder builder2 = mock(LWTraceBuilder.class);
        when(builder2.getApiUrl()).thenReturn("http://localhost");
        when(builder2.getSiteId()).thenReturn(100);
        when(builder2.getTrackerName()).thenReturn("Default Tracker");
        LWTracer LWTracer2 = new LWTracer(mLadderWinner, builder2);

        LWTraceBuilder builder3 = mock(LWTraceBuilder.class);
        when(builder3.getApiUrl()).thenReturn("http://example.com");
        when(builder3.getSiteId()).thenReturn(11);
        when(builder3.getTrackerName()).thenReturn("Default Tracker");
        LWTracer LWTracer3 = new LWTracer(mLadderWinner, builder3);

        assertNotNull(LWTracer);
        assertFalse(LWTracer.equals(LWTracer2));
        assertTrue(LWTracer.equals(LWTracer3));
    }

    @Test
    public void testTrackerHashCode() {
        assertEquals(new LWTracer(mLadderWinner, mLWTraceBuilder).hashCode(), new LWTracer(mLadderWinner, mLWTraceBuilder).hashCode());
    }

    @Test
    public void testUrlPathCorrection() {
        when(mLWTraceBuilder.getApplicationBaseUrl()).thenReturn("https://package/");
        LWTracer LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        String[] paths = new String[]{null, "", "/",};
        for (String path : paths) {
            TraceMe traceMe = new TraceMe();
            traceMe.set(QueryParams.URL_PATH, path);
            LWTracer.trace(traceMe);
            assertEquals("https://package/", traceMe.get(QueryParams.URL_PATH));
        }
    }

    @Test
    public void testSetUserAgent() {
        LWTracer LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        TraceMe traceMe = new TraceMe();
        LWTracer.trace(traceMe);
        assertEquals("aUserAgent", traceMe.get(QueryParams.USER_AGENT));

        // Custom developer specified useragent
        traceMe = new TraceMe();
        String customUserAgent = "Mozilla/5.0 (Linux; U; Android 2.2.1; en-us; Nexus One Build/FRG83) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0";
        traceMe.set(QueryParams.USER_AGENT, customUserAgent);
        LWTracer.trace(traceMe);
        assertEquals(customUserAgent, traceMe.get(QueryParams.USER_AGENT));

        // Modifying default TrackMe, no USER_AGENT
        traceMe = new TraceMe();
        LWTracer.getDefaultTrackMe().set(QueryParams.USER_AGENT, null);
        LWTracer.trace(traceMe);
        assertEquals(null, traceMe.get(QueryParams.USER_AGENT));
    }

    @Test
    public void testFirstVisitTimeStamp() {
        LWTracer LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        assertEquals(-1, LWTracer.getPreferences().getLong(LWTracer.PREF_KEY_TRACKER_FIRSTVISIT, -1));

        LWTraceUtil.trace().event("TestCategory", "TestAction").with(LWTracer);
        verify(mDispatcher).submit(mCaptor.capture());
        TraceMe traceMe1 = mCaptor.getValue();
        TestHelper.sleep(10);
        // make sure we are tracking in seconds
        assertTrue(Math.abs((System.currentTimeMillis() / 1000) - Long.parseLong(traceMe1.get(FIRST_VISIT_TIMESTAMP))) < 2);

        LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        LWTraceUtil.trace().event("TestCategory", "TestAction").with(LWTracer);
        verify(mDispatcher, times(2)).submit(mCaptor.capture());
        TraceMe traceMe2 = mCaptor.getValue();
        assertEquals(Long.parseLong(traceMe1.get(FIRST_VISIT_TIMESTAMP)), Long.parseLong(traceMe2.get(FIRST_VISIT_TIMESTAMP)));
        assertEquals(LWTracer.getPreferences().getLong(LWTracer.PREF_KEY_TRACKER_FIRSTVISIT, -1), Long.parseLong(traceMe1.get(FIRST_VISIT_TIMESTAMP)));
    }

    @Test
    public void testTotalVisitCount() {
        LWTracer LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        assertEquals(-1, LWTracer.getPreferences().getInt(LWTracer.PREF_KEY_TRACKER_VISITCOUNT, -1));
        assertNull(LWTracer.getDefaultTrackMe().get(QueryParams.TOTAL_NUMBER_OF_VISITS));

        LWTraceUtil.trace().event("TestCategory", "TestAction").with(LWTracer);
        verify(mDispatcher).submit(mCaptor.capture());
        assertEquals(1, Integer.parseInt(mCaptor.getValue().get(QueryParams.TOTAL_NUMBER_OF_VISITS)));

        LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        assertEquals(1, LWTracer.getPreferences().getLong(LWTracer.PREF_KEY_TRACKER_VISITCOUNT, -1));
        assertNull(LWTracer.getDefaultTrackMe().get(QueryParams.TOTAL_NUMBER_OF_VISITS));
        LWTraceUtil.trace().event("TestCategory", "TestAction").with(LWTracer);
        verify(mDispatcher, times(2)).submit(mCaptor.capture());
        assertEquals(2, Integer.parseInt(mCaptor.getValue().get(QueryParams.TOTAL_NUMBER_OF_VISITS)));
        assertEquals(2, LWTracer.getPreferences().getLong(LWTracer.PREF_KEY_TRACKER_VISITCOUNT, -1));
    }

    @Test
    public void testVisitCountMultipleThreads() throws Exception {
        LWTracer LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        int threadCount = 1000;
        final CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                TestHelper.sleep(new Random().nextInt(20 - 0) + 0);
                LWTraceUtil.trace().event("TestCategory", "TestAction").with(new LWTracer(mLadderWinner, mLWTraceBuilder));
                countDownLatch.countDown();
            }).start();
        }
        countDownLatch.await();
        assertEquals(threadCount, mTrackerPreferences.getLong(LWTracer.PREF_KEY_TRACKER_VISITCOUNT, 0));
    }

    @Test
    public void testSessionStartRaceCondition() throws Exception {
        final List<TraceMe> traceMes = Collections.synchronizedList(new ArrayList<TraceMe>());
        doAnswer(invocation -> {
            traceMes.add(invocation.getArgument(0));
            return null;
        }).when(mDispatcher).submit(any(TraceMe.class));
        when(mDispatcher.getConnectionTimeOut()).thenReturn(1000);
        for (int i = 0; i < 1000; i++) {
            traceMes.clear();
            final LWTracer LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
            final CountDownLatch countDownLatch = new CountDownLatch(10);
            for (int j = 0; j < 10; j++) {
                new Thread(() -> {
                    try {
                        TestHelper.sleep(new Random().nextInt(4 - 0) + 0);
                        TraceMe traceMe = new TraceMe()
                                .set(QueryParams.EVENT_ACTION, UUID.randomUUID().toString())
                                .set(QueryParams.EVENT_CATEGORY, UUID.randomUUID().toString())
                                .set(QueryParams.EVENT_NAME, UUID.randomUUID().toString())
                                .set(QueryParams.EVENT_VALUE, 1);
                        LWTracer.trace(traceMe);
                        countDownLatch.countDown();
                    } catch (Exception e) {
                        e.printStackTrace();
                        assertFalse(true);
                    }
                }).start();
            }
            countDownLatch.await();
            for (TraceMe out : traceMes) {
                if (traceMes.indexOf(out) == 0) {
                    assertTrue(i + "#" + out.toMap().size(), out.get(QueryParams.LANGUAGE) != null);
                    assertTrue(out.get(QueryParams.FIRST_VISIT_TIMESTAMP) != null);
                    assertTrue(out.get(SESSION_START) != null);
                } else {
                    assertTrue(out.get(QueryParams.LANGUAGE) == null);
                    assertTrue(out.get(QueryParams.FIRST_VISIT_TIMESTAMP) == null);
                    assertTrue(out.get(SESSION_START) == null);
                }
            }
        }
    }

    @Test
    public void testFirstVisitMultipleThreads() throws Exception {
        LWTracer LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        int threadCount = 100;
        final CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        final List<Long> firstVisitTimes = Collections.synchronizedList(new ArrayList<Long>());
        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                TestHelper.sleep(new Random().nextInt(20 - 0) + 0);
                LWTraceUtil.trace().event("TestCategory", "TestAction").with(LWTracer);
                long firstVisit = Long.valueOf(LWTracer.getDefaultTrackMe().get(FIRST_VISIT_TIMESTAMP));
                firstVisitTimes.add(firstVisit);
                countDownLatch.countDown();
            }).start();
        }
        countDownLatch.await();
        for (Long firstVisit : firstVisitTimes) assertEquals(firstVisitTimes.get(0), firstVisit);
    }

    @Test
    public void testPreviousVisits() {
        LWTracer LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        final List<Long> previousVisitTimes = new ArrayList<>();
        for (int i = 0; i < 5; i++) {


            LWTraceUtil.trace().event("TestCategory", "TestAction").with(LWTracer);
            String previousVisit = LWTracer.getDefaultTrackMe().get(QueryParams.PREVIOUS_VISIT_TIMESTAMP);
            if (previousVisit != null)
                previousVisitTimes.add(Long.parseLong(previousVisit));
            TestHelper.sleep(1010);

        }
        assertFalse(previousVisitTimes.contains(0L));
        Long lastTime = 0L;
        for (Long time : previousVisitTimes) {
            assertTrue(lastTime < time);
            lastTime = time;
        }
    }

    @Test
    public void testPreviousVisit() {
        LWTracer LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        // No timestamp yet
        assertEquals(-1, LWTracer.getPreferences().getLong(LWTracer.PREF_KEY_TRACKER_PREVIOUSVISIT, -1));
        LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        LWTraceUtil.trace().event("TestCategory", "TestAction").with(LWTracer);
        verify(mDispatcher).submit(mCaptor.capture());
        long _startTime = System.currentTimeMillis() / 1000;
        // There was no previous visit
        assertNull(mCaptor.getValue().get(QueryParams.PREVIOUS_VISIT_TIMESTAMP));
        TestHelper.sleep(1000);

        // After the first visit we now have a timestamp for the previous visit
        long previousVisit = LWTracer.getPreferences().getLong(LWTracer.PREF_KEY_TRACKER_PREVIOUSVISIT, -1);
        assertTrue(previousVisit - _startTime < 2000);
        assertNotEquals(-1, previousVisit);
        LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        LWTraceUtil.trace().event("TestCategory", "TestAction").with(LWTracer);
        verify(mDispatcher, times(2)).submit(mCaptor.capture());
        // Transmitted timestamp is the one from the first visit visit
        assertEquals(previousVisit, Long.parseLong(mCaptor.getValue().get(QueryParams.PREVIOUS_VISIT_TIMESTAMP)));

        TestHelper.sleep(1000);
        LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        LWTraceUtil.trace().event("TestCategory", "TestAction").with(LWTracer);
        verify(mDispatcher, times(3)).submit(mCaptor.capture());
        // Now the timestamp changed as this is the 3rd visit.
        assertNotEquals(previousVisit, Long.parseLong(mCaptor.getValue().get(QueryParams.PREVIOUS_VISIT_TIMESTAMP)));
        TestHelper.sleep(1000);

        previousVisit = LWTracer.getPreferences().getLong(LWTracer.PREF_KEY_TRACKER_PREVIOUSVISIT, -1);
        LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        LWTraceUtil.trace().event("TestCategory", "TestAction").with(LWTracer);
        verify(mDispatcher, times(4)).submit(mCaptor.capture());
        // Just make sure the timestamp in the 4th visit is from the 3rd visit
        assertEquals(previousVisit, Long.parseLong(mCaptor.getValue().get(QueryParams.PREVIOUS_VISIT_TIMESTAMP)));

        // Test setting a custom timestamp
        TraceMe custom = new TraceMe();
        custom.set(QueryParams.PREVIOUS_VISIT_TIMESTAMP, 1000L);
        LWTracer.trace(custom);
        verify(mDispatcher, times(5)).submit(mCaptor.capture());
        assertEquals(1000L, Long.parseLong(mCaptor.getValue().get(QueryParams.PREVIOUS_VISIT_TIMESTAMP)));
    }

    @Test
    public void testTrackingCallback() {
        LWTracer LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        LWTracer.Callback callback = mock(LWTracer.Callback.class);

        TraceMe pre = new TraceMe();
        LWTracer.trace(pre);
        verify(mDispatcher).submit(pre);
        verify(callback, never()).onTrack(mCaptor.capture());

        reset(mDispatcher, callback);
        LWTracer.addTrackingCallback(callback);
        LWTracer.trace(new TraceMe());
        verify(callback).onTrack(mCaptor.capture());
        verify(mDispatcher, never()).submit(any());

        reset(mDispatcher, callback);
        TraceMe orig = new TraceMe();
        TraceMe replaced = new TraceMe().set("some", "thing");
        when(callback.onTrack(orig)).thenReturn(replaced);
        LWTracer.trace(orig);
        verify(callback).onTrack(orig);
        verify(mDispatcher).submit(replaced);

        reset(mDispatcher, callback);
        TraceMe post = new TraceMe();
        LWTracer.removeTrackingCallback(callback);
        LWTracer.trace(post);
        verify(callback, never()).onTrack(any());
        verify(mDispatcher).submit(post);
    }

    @Test
    public void testTrackingCallbacks() {
        LWTracer LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        LWTracer.Callback callback1 = mock(LWTracer.Callback.class);
        LWTracer.Callback callback2 = mock(LWTracer.Callback.class);

        TraceMe orig = new TraceMe();
        TraceMe replaced = new TraceMe();
        when(callback1.onTrack(orig)).thenReturn(replaced);
        when(callback2.onTrack(replaced)).thenReturn(replaced);

        LWTracer.addTrackingCallback(callback1);
        LWTracer.addTrackingCallback(callback1);
        LWTracer.addTrackingCallback(callback2);
        LWTracer.trace(orig);
        verify(callback1).onTrack(orig);
        verify(callback2).onTrack(replaced);
        verify(mDispatcher).submit(replaced);

        LWTracer.removeTrackingCallback(callback1);
        LWTracer.trace(orig);

        verify(callback2).onTrack(orig);
    }

    private static void validateDefaultQuery(TraceMe params) {
        assertEquals(params.get(QueryParams.SITE_ID), "11");
        assertEquals(params.get(QueryParams.RECORD), "1");
        assertEquals(params.get(QueryParams.SEND_IMAGE), "0");
        assertEquals(params.get(QueryParams.VISITOR_ID).length(), 16);
        assertTrue(params.get(QueryParams.URL_PATH).startsWith("http://"));
        assertTrue(Integer.parseInt(params.get(QueryParams.RANDOM_NUMBER)) > 0);
    }

    @Test
    public void testCustomDispatcherFactory() {
        Dispatcher dispatcher = mock(Dispatcher.class);
        DispatcherFactory factory = mock(DispatcherFactory.class);
        when(factory.build(any(LWTracer.class))).thenReturn(dispatcher);
        when(mLadderWinner.getDispatcherFactory()).thenReturn(factory);
        LWTracer LWTracer = new LWTracer(mLadderWinner, mLWTraceBuilder);
        verify(factory).build(LWTracer);
    }
}
