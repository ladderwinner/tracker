/*
 * Android SDK for LadderWinner
 *
 * @link https://github.com/LadderWinner-org/LadderWinner-android-sdk
 * @license https://github.com/LadderWinner-org/LadderWinner-sdk-android/blob/master/LICENSE BSD-3 Clause
 */

package com.github.ladderwinner;

import android.annotation.SuppressLint;
import android.app.Application;

import org.junit.Test;
import org.junit.runner.RunWith;
import com.github.ladderwinner.dispatcher.DefaultDispatcher;
import com.github.ladderwinner.dispatcher.DefaultDispatcherFactory;
import com.github.ladderwinner.dispatcher.Dispatcher;
import com.github.ladderwinner.dispatcher.DispatcherFactory;
import com.github.ladderwinner.dispatcher.EventCache;
import com.github.ladderwinner.dispatcher.EventDiskCache;
import com.github.ladderwinner.dispatcher.Packet;
import com.github.ladderwinner.dispatcher.PacketFactory;
import com.github.ladderwinner.dispatcher.PacketSender;
import com.github.ladderwinner.extra.LWTraceUtil;
import com.github.ladderwinner.tools.Connectivity;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import testhelpers.BaseTest;
import testhelpers.FullEnvTestRunner;
import testhelpers.LadderWinnerTestApplication;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@Config(emulateSdk = 18, manifest = Config.NONE)
@RunWith(FullEnvTestRunner.class)
public class LadderWinnerTest extends BaseTest {

    @Test
    public void testNewTracker() {
        LadderWinnerTestApplication app = (LadderWinnerTestApplication) Robolectric.application;
        LWTracer LWTracer = app.onCreateTrackerConfig().build(LadderWinner.getInstance(Robolectric.application));
        assertNotNull(LWTracer);
        assertEquals(app.onCreateTrackerConfig().getApiUrl(), LWTracer.getAPIUrl());
        assertEquals(app.onCreateTrackerConfig().getSiteId(), LWTracer.getSiteId());
    }

    @Test
    public void testNormalTracker() {
        LadderWinner ladderWinner = LadderWinner.getInstance(Robolectric.application);
        LWTracer LWTracer = new LWTraceBuilder("http://test/LadderWinner.php", 1, "Default Tracker").build(ladderWinner);
        assertEquals("http://test/LadderWinner.php", LWTracer.getAPIUrl());
        assertEquals(1, LWTracer.getSiteId());
    }

    @Test
    public void testTrackerNaming() {
        // TODO can we somehow detect naming collisions on tracker creation?
        // Would probably requiring us to track created trackers
    }

    @SuppressLint("InlinedApi")
    @Test
    public void testLowMemoryDispatch() {
        LadderWinnerTestApplication app = (LadderWinnerTestApplication) Robolectric.application;
        final PacketSender packetSender = mock(PacketSender.class);
        app.getLadderWinner().setDispatcherFactory(new DefaultDispatcherFactory() {
            @Override
            public Dispatcher build(LWTracer LWTracer) {
                return new DefaultDispatcher(
                        new EventCache(new EventDiskCache(LWTracer)),
                        new Connectivity(LWTracer.getLadderWinner().getContext()),
                        new PacketFactory(LWTracer.getAPIUrl()),
                        packetSender
                );
            }
        });
        LWTracer LWTracer = app.getTracker();
        assertNotNull(LWTracer);
        LWTracer.setDispatchInterval(-1);

        LWTracer.trace(LWTraceUtil.trace().screen("test").build());
        LWTracer.dispatch();
        verify(packetSender, timeout(500).times(1)).send(any(Packet.class));

        LWTracer.trace(LWTraceUtil.trace().screen("test").build());
        verify(packetSender, timeout(500).times(1)).send(any(Packet.class));

        app.onTrimMemory(Application.TRIM_MEMORY_UI_HIDDEN);
        verify(packetSender, timeout(500).atLeast(2)).send(any(Packet.class));
    }

    @Test
    public void testGetSettings() {
        LWTracer LWTracer1 = mock(LWTracer.class);
        when(LWTracer1.getName()).thenReturn("1");
        LWTracer LWTracer2 = mock(LWTracer.class);
        when(LWTracer2.getName()).thenReturn("2");
        LWTracer LWTracer3 = mock(LWTracer.class);
        when(LWTracer3.getName()).thenReturn("1");

        final LadderWinner ladderWinner = LadderWinner.getInstance(Robolectric.application);
        assertEquals(ladderWinner.getTrackerPreferences(LWTracer1), ladderWinner.getTrackerPreferences(LWTracer1));
        assertNotEquals(ladderWinner.getTrackerPreferences(LWTracer1), ladderWinner.getTrackerPreferences(LWTracer2));
        assertEquals(ladderWinner.getTrackerPreferences(LWTracer1), ladderWinner.getTrackerPreferences(LWTracer3));
    }

    @Test
    public void testSetDispatcherFactory() {
        final LadderWinner ladderWinner = LadderWinner.getInstance(Robolectric.application);
        Dispatcher dispatcher = mock(Dispatcher.class);
        DispatcherFactory factory = mock(DispatcherFactory.class);
        when(factory.build(any(LWTracer.class))).thenReturn(dispatcher);
        assertThat(ladderWinner.getDispatcherFactory(), is(not(nullValue())));
        ladderWinner.setDispatcherFactory(factory);
        assertThat(ladderWinner.getDispatcherFactory(), is(factory));
    }

}
