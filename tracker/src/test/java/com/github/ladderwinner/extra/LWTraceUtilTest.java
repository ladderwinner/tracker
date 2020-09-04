package com.github.ladderwinner.extra;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import com.github.ladderwinner.LadderWinner;
import com.github.ladderwinner.QueryParams;
import com.github.ladderwinner.TraceMe;
import com.github.ladderwinner.LWTracer;
import com.github.ladderwinner.dispatcher.DispatchMode;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static com.github.ladderwinner.extra.LWTraceUtil.trace;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@SuppressWarnings("deprecation")
public class LWTraceUtilTest {
    ArgumentCaptor<TraceMe> mCaptor = ArgumentCaptor.forClass(TraceMe.class);
    @Mock
    LWTracer mLWTracer;
    @Mock
    LadderWinner mLadderWinner;
    @Mock Context mContext;
    @Mock PackageManager mPackageManager;
    @Mock LadderWinnerApplication mLadderWinnerApplication;

    @Before
    public void setup() throws PackageManager.NameNotFoundException {
        MockitoAnnotations.initMocks(this);
        when(mLWTracer.getLadderWinner()).thenReturn(mLadderWinner);
        when(mLadderWinner.getContext()).thenReturn(mContext);
        when(mContext.getPackageManager()).thenReturn(mPackageManager);
        when(mContext.getPackageName()).thenReturn("packageName");
        when(mLadderWinnerApplication.getTracker()).thenReturn(mLWTracer);
        PackageInfo packageInfo = new PackageInfo();
        packageInfo.versionCode = 123;
        //noinspection WrongConstant
        when(mPackageManager.getPackageInfo(anyString(), anyInt())).thenReturn(packageInfo);
    }

    @Test
    public void testBaseEvent() {
        LWTraceUtil.trace().screen("/path").with(mLadderWinnerApplication);
        verify(mLadderWinnerApplication).getTracker();
        verify(mLWTracer).trace(any(TraceMe.class));
    }

    @Test
    public void testBaseEvent_track_safely() {
        final LWTraceUtil.BaseEvent badTrackMe = new LWTraceUtil.BaseEvent(null) {
            @Override
            public TraceMe build() {
                throw new IllegalArgumentException();
            }
        };
        assertThat(badTrackMe.safelyWith(mLWTracer), is(false));
        assertThat(badTrackMe.safelyWith(mLadderWinnerApplication), is(false));
        verify(mLWTracer, never()).trace(any(TraceMe.class));

        final LWTraceUtil.BaseEvent goodTrackMe = new LWTraceUtil.BaseEvent(null) {
            @Override
            public TraceMe build() {
                return new TraceMe();
            }
        };
        assertThat(goodTrackMe.safelyWith(mLWTracer), is(true));
        verify(mLWTracer, times(1)).trace(any(TraceMe.class));
        assertThat(goodTrackMe.safelyWith(mLadderWinnerApplication), is(true));
        verify(mLWTracer, times(2)).trace(any(TraceMe.class));
    }

    @Test
    public void testOutlink() throws Exception {
        URL valid = new URL("https://foo.bar");
        LWTraceUtil.trace().outlink(valid).with(mLWTracer);
        verify(mLWTracer).trace(mCaptor.capture());
        assertEquals(valid.toExternalForm(), mCaptor.getValue().get(QueryParams.LINK));
        assertEquals(valid.toExternalForm(), mCaptor.getValue().get(QueryParams.URL_PATH));

        valid = new URL("https://foo.bar");
        LWTraceUtil.trace().outlink(valid).with(mLWTracer);
        verify(mLWTracer, times(2)).trace(mCaptor.capture());
        assertEquals(valid.toExternalForm(), mCaptor.getValue().get(QueryParams.LINK));
        assertEquals(valid.toExternalForm(), mCaptor.getValue().get(QueryParams.URL_PATH));

        valid = new URL("ftp://foo.bar");
        LWTraceUtil.trace().outlink(valid).with(mLWTracer);
        verify(mLWTracer, times(3)).trace(mCaptor.capture());
        assertEquals(valid.toExternalForm(), mCaptor.getValue().get(QueryParams.LINK));
        assertEquals(valid.toExternalForm(), mCaptor.getValue().get(QueryParams.URL_PATH));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOutlink_invalid_url() throws MalformedURLException {
        LWTraceUtil.trace().outlink(new URL("file://mount/sdcard/something")).build();
    }

    @Test
    public void testDownloadTrackChecksum() throws Exception {
        DownloadTracker downloadTracker = mock(DownloadTracker.class);
        LWTraceUtil.trace().download(downloadTracker).identifier(new DownloadTracker.Extra.ApkChecksum(mContext)).with(mLWTracer);
        verify(downloadTracker).trackOnce(any(TraceMe.class), any(DownloadTracker.Extra.ApkChecksum.class));
    }

    @Test
    public void testDownloadTrackForced() throws Exception {
        DownloadTracker downloadTracker = mock(DownloadTracker.class);
        LWTraceUtil.trace().download(downloadTracker).force().with(mLWTracer);
        verify(downloadTracker).trackNewAppDownload(any(TraceMe.class), any(DownloadTracker.Extra.None.class));
    }

    @Test
    public void testDownloadCustomVersion() throws Exception {
        DownloadTracker downloadTracker = mock(DownloadTracker.class);
        String version = UUID.randomUUID().toString();

        LWTraceUtil.trace().download(downloadTracker).version(version).with(mLWTracer);
        verify(downloadTracker).setVersion(version);
        verify(downloadTracker).trackOnce(any(TraceMe.class), any(DownloadTracker.Extra.class));
    }

    @Test
    public void testVisitCustomVariables_merge_base() throws Exception {
        CustomVariables varsA = new CustomVariables().put(1, "visit1", "A");
        CustomVariables varsB = new CustomVariables().put(2, "visit2", "B");
        CustomVariables combined = new CustomVariables().put(1, "visit1", "A").put(2, "visit2", "B");

        LWTraceUtil.trace(varsA.toVisitVariables())
                .visitVariables(varsB)
                .screen("/path")
                .with(mLWTracer);

        verify(mLWTracer).trace(mCaptor.capture());
        assertEquals(combined.toString(), mCaptor.getValue().get(QueryParams.VISIT_SCOPE_CUSTOM_VARIABLES));
        assertEquals("/path", mCaptor.getValue().get(QueryParams.URL_PATH));
    }

    @Test
    public void testVisitCustomVariables_merge_singles() throws Exception {
        CustomVariables varsA = new CustomVariables().put(1, "visit1", "A");
        CustomVariables varsB = new CustomVariables().put(2, "visit2", "B");
        CustomVariables combined = new CustomVariables().put(1, "visit1", "A").put(2, "visit2", "B");

        LWTraceUtil.trace()
                .visitVariables(varsA)
                .visitVariables(varsB)
                .screen("/path")
                .with(mLWTracer);

        verify(mLWTracer).trace(mCaptor.capture());
        assertEquals(combined.toString(), mCaptor.getValue().get(QueryParams.VISIT_SCOPE_CUSTOM_VARIABLES));
        assertEquals("/path", mCaptor.getValue().get(QueryParams.URL_PATH));
    }

    @Test
    public void testVisitCustomVariables_add() throws Exception {
        CustomVariables _vars = new CustomVariables();
        _vars.put(1, "visit1", "A");
        _vars.put(2, "visit2", "B");

        LWTraceUtil.trace()
                .visitVariables(1, "visit1", "A")
                .visitVariables(2, "visit2", "B")
                .screen("/path")
                .with(mLWTracer);

        verify(mLWTracer).trace(mCaptor.capture());
        assertEquals(_vars.toString(), mCaptor.getValue().get(QueryParams.VISIT_SCOPE_CUSTOM_VARIABLES));
        assertEquals("/path", mCaptor.getValue().get(QueryParams.URL_PATH));
    }

    @Test
    public void testSetScreenCustomVariable() throws Exception {
        LWTraceUtil.trace()
                .screen("")
                .variable(1, "2", "3")
                .with(mLWTracer);

        verify(mLWTracer).trace(mCaptor.capture());
        assertEquals("{'1':['2','3']}".replaceAll("'", "\""), mCaptor.getValue().get(QueryParams.SCREEN_SCOPE_CUSTOM_VARIABLES));
    }

    @Test
    public void testSetScreenCustomDimension() throws Exception {
        LWTraceUtil.trace()
                .screen("")
                .dimension(1, "dim1")
                .dimension(2, "dim2")
                .dimension(3, "dim3")
                .dimension(3, null)
                .dimension(4, null)
                .with(mLWTracer);

        verify(mLWTracer).trace(mCaptor.capture());
        assertEquals("dim1", CustomDimension.getDimension(mCaptor.getValue(), 1));
        assertEquals("dim2", CustomDimension.getDimension(mCaptor.getValue(), 2));
        assertNull(CustomDimension.getDimension(mCaptor.getValue(), 3));
        assertNull(CustomDimension.getDimension(mCaptor.getValue(), 4));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetScreem_empty_path() throws Exception {
        LWTraceUtil.trace().screen((String) null).build();
    }

    @Test
    public void testCustomDimension_trackHelperAny() {
        LWTraceUtil.trace()
                .dimension(1, "visit")
                .dimension(2, "screen")
                .event("category", "action")
                .with(mLWTracer);

        verify(mLWTracer).trace(mCaptor.capture());
        assertEquals("visit", CustomDimension.getDimension(mCaptor.getValue(), 1));
        assertEquals("screen", CustomDimension.getDimension(mCaptor.getValue(), 2));
        assertEquals("category", mCaptor.getValue().get(QueryParams.EVENT_CATEGORY));
        assertEquals("action", mCaptor.getValue().get(QueryParams.EVENT_ACTION));
    }

    @Test
    public void testCustomDimension_override() {
        LWTraceUtil.trace()
                .dimension(1, "visit")
                .dimension(2, "screen")
                .screen("/path")
                .dimension(1, null)
                .with(mLWTracer);

        verify(mLWTracer).trace(mCaptor.capture());
        assertNull(CustomDimension.getDimension(mCaptor.getValue(), 1));
        assertEquals("screen", CustomDimension.getDimension(mCaptor.getValue(), 2));
        assertEquals("/path", mCaptor.getValue().get(QueryParams.URL_PATH));
    }

    @Test
    public void testTrackScreenView() throws Exception {
        LWTraceUtil.trace().screen("/test/test").title("title").with(mLWTracer);
        verify(mLWTracer).trace(mCaptor.capture());
        assertTrue(mCaptor.getValue().get(QueryParams.URL_PATH).endsWith("/test/test"));
    }

    @Test
    public void testTrackScreenWithTitleView() throws Exception {
        LWTraceUtil.trace().screen("/test/test").title("Test title").with(mLWTracer);
        verify(mLWTracer).trace(mCaptor.capture());
        assertTrue(mCaptor.getValue().get(QueryParams.URL_PATH).endsWith("/test/test"));
        assertEquals(mCaptor.getValue().get(QueryParams.ACTION_NAME), "Test title");
    }

    @Test
    public void testTrackScreenWithCampaignView() {
        LWTraceUtil.trace().screen("/test/test").campaign("campaign_name", "campaign_keyword").with(mLWTracer);
        verify(mLWTracer).trace(mCaptor.capture());
        assertTrue(mCaptor.getValue().get(QueryParams.URL_PATH).endsWith("/test/test"));
        assertEquals(mCaptor.getValue().get(QueryParams.CAMPAIGN_NAME), "campaign_name");
        assertEquals(mCaptor.getValue().get(QueryParams.CAMPAIGN_KEYWORD), "campaign_keyword");
    }

    @Test
    public void testTrackEvent() throws Exception {
        LWTraceUtil.trace().event("category", "test action").with(mLWTracer);
        verify(mLWTracer).trace(mCaptor.capture());
        TraceMe tracked = mCaptor.getValue();
        assertEquals(tracked.get(QueryParams.EVENT_CATEGORY), "category");
        assertEquals(tracked.get(QueryParams.EVENT_ACTION), "test action");
    }

    @Test
    public void testTrackEventName() throws Exception {
        String name = "test name2";
        LWTraceUtil.trace().event("category", "test action").name(name).with(mLWTracer);
        verify(mLWTracer).trace(mCaptor.capture());
        TraceMe tracked = mCaptor.getValue();
        assertEquals(tracked.get(QueryParams.EVENT_CATEGORY), "category");
        assertEquals(tracked.get(QueryParams.EVENT_ACTION), "test action");
        assertEquals(tracked.get(QueryParams.EVENT_NAME), name);
    }

    @Test
    public void testTrackEventNameAndValue() throws Exception {
        String name = "test name3";
        LWTraceUtil.trace().event("category", "test action").name(name).value(1f).with(mLWTracer);
        verify(mLWTracer).trace(mCaptor.capture());
        TraceMe tracked = mCaptor.getValue();
        assertEquals(tracked.get(QueryParams.EVENT_CATEGORY), "category");
        assertEquals(tracked.get(QueryParams.EVENT_ACTION), "test action");
        assertEquals(tracked.get(QueryParams.EVENT_NAME), name);
        assertEquals(String.valueOf(tracked.get(QueryParams.EVENT_VALUE)), String.valueOf(1f));
    }

    @Test
    public void testTrackEventNameAndValueWithpath() throws Exception {
        LWTraceUtil.trace().event("category", "test action").name("test name3").path("/path").value(1f).with(mLWTracer);
        verify(mLWTracer).trace(mCaptor.capture());
        TraceMe tracked = mCaptor.getValue();
        assertEquals(tracked.get(QueryParams.EVENT_CATEGORY), "category");
        assertEquals(tracked.get(QueryParams.EVENT_ACTION), "test action");
        assertEquals(tracked.get(QueryParams.EVENT_NAME), "test name3");
        assertEquals(tracked.get(QueryParams.URL_PATH), "/path");
        assertEquals(String.valueOf(tracked.get(QueryParams.EVENT_VALUE)), String.valueOf(1f));
    }

    @Test
    public void testTrackGoal() throws Exception {
        LWTraceUtil.trace().goal(1).with(mLWTracer);
        verify(mLWTracer).trace(mCaptor.capture());

        assertNull(mCaptor.getValue().get(QueryParams.REVENUE));
        assertEquals(mCaptor.getValue().get(QueryParams.GOAL_ID), "1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTrackGoal_invalid_id() throws Exception {
        LWTraceUtil.trace().goal(-1).revenue(100f).build();
    }

    @Test
    public void testTrackSiteSearch() throws Exception {
        LWTraceUtil.trace().search("keyword").category("category").count(1337).with(mLWTracer);
        verify(mLWTracer).trace(mCaptor.capture());

        assertEquals(mCaptor.getValue().get(QueryParams.SEARCH_KEYWORD), "keyword");
        assertEquals(mCaptor.getValue().get(QueryParams.SEARCH_CATEGORY), "category");
        assertEquals(mCaptor.getValue().get(QueryParams.SEARCH_NUMBER_OF_HITS), String.valueOf(1337));

        LWTraceUtil.trace().search("keyword2").with(mLWTracer);
        verify(mLWTracer, times(2)).trace(mCaptor.capture());

        assertEquals(mCaptor.getValue().get(QueryParams.SEARCH_KEYWORD), "keyword2");
        assertNull(mCaptor.getValue().get(QueryParams.SEARCH_CATEGORY));
        assertNull(mCaptor.getValue().get(QueryParams.SEARCH_NUMBER_OF_HITS));
    }

    @Test
    public void testTrackGoalRevenue() throws Exception {
        LWTraceUtil.trace().goal(1).revenue(100f).with(mLWTracer);
        verify(mLWTracer).trace(mCaptor.capture());

        assertEquals("1", mCaptor.getValue().get(QueryParams.GOAL_ID));
        assertTrue(100f == Float.valueOf(mCaptor.getValue().get(QueryParams.REVENUE)));
    }

    @Test
    public void testTrackContentImpression() throws Exception {
        String name = "test name2";
        LWTraceUtil.trace().impression(name).piece("test").target("test2").with(mLWTracer);
        verify(mLWTracer).trace(mCaptor.capture());

        assertEquals(mCaptor.getValue().get(QueryParams.CONTENT_NAME), name);
        assertEquals(mCaptor.getValue().get(QueryParams.CONTENT_PIECE), "test");
        assertEquals(mCaptor.getValue().get(QueryParams.CONTENT_TARGET), "test2");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTrackContentImpression_invalid_name_empty() throws Exception {
        LWTraceUtil.trace().impression("").build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTrackContentImpression_invalid_name_null() throws Exception {
        LWTraceUtil.trace().impression(null).build();
    }

    @Test
    public void testTrackContentInteraction_invalid_name_empty() throws Exception {
        int errorCount = 0;
        try {
            LWTraceUtil.trace().interaction("", "test").piece("test").target("test2").build();
        } catch (IllegalArgumentException e) { errorCount++; }
        try {
            LWTraceUtil.trace().interaction("test", "").piece("test").target("test2").build();
        } catch (IllegalArgumentException e) { errorCount++; }
        try {
            LWTraceUtil.trace().interaction("", "").piece("test").target("test2").build();
        } catch (IllegalArgumentException e) { errorCount++; }
        assertThat(errorCount, is(3));
    }

    @Test
    public void testTrackContentInteraction_invalid_name_null() throws Exception {
        int errorCount = 0;
        try {
            LWTraceUtil.trace().interaction(null, "test").piece("test").target("test2").build();
        } catch (IllegalArgumentException e) { errorCount++; }
        try {
            LWTraceUtil.trace().interaction("test", null).piece("test").target("test2").build();
        } catch (IllegalArgumentException e) { errorCount++; }
        try {
            LWTraceUtil.trace().interaction(null, null).piece("test").target("test2").build();
        } catch (IllegalArgumentException e) { errorCount++; }
        assertThat(errorCount, is(3));
    }

    @Test
    public void testTrackEcommerceCartUpdate() throws Exception {
        Locale.setDefault(Locale.US);
        EcommerceItems items = new EcommerceItems();
        items.addItem(new EcommerceItems.Item("fake_sku").name("fake_product").category("fake_category").price(200).quantity(2));
        items.addItem(new EcommerceItems.Item("fake_sku_2").name("fake_product_2").category("fake_category_2").price(400).quantity(3));
        LWTraceUtil.trace().cartUpdate(50000).items(items).with(mLWTracer);
        verify(mLWTracer).trace(mCaptor.capture());

        assertEquals(mCaptor.getValue().get(QueryParams.GOAL_ID), "0");
        assertEquals(mCaptor.getValue().get(QueryParams.REVENUE), "500.00");

        String ecommerceItemsJson = mCaptor.getValue().get(QueryParams.ECOMMERCE_ITEMS);

        new JSONArray(ecommerceItemsJson); // will throw exception if not valid json

        assertTrue(ecommerceItemsJson.contains("[\"fake_sku\",\"fake_product\",\"fake_category\",\"2.00\",\"2\"]"));
        assertTrue(ecommerceItemsJson.contains("[\"fake_sku_2\",\"fake_product_2\",\"fake_category_2\",\"4.00\",\"3\"]"));
    }

    @Test
    public void testTrackEcommerceOrder() throws Exception {
        Locale.setDefault(Locale.US);
        EcommerceItems items = new EcommerceItems();
        items.addItem(new EcommerceItems.Item("fake_sku").name("fake_product").category("fake_category").price(200).quantity(2));
        items.addItem(new EcommerceItems.Item("fake_sku_2").name("fake_product_2").category("fake_category_2").price(400).quantity(3));
        LWTraceUtil.trace().order("orderId", 10020).subTotal(7002).tax(2000).shipping(1000).discount(0).items(items).with(mLWTracer);
        verify(mLWTracer).trace(mCaptor.capture());
        TraceMe tracked = mCaptor.getValue();
        assertEquals(tracked.get(QueryParams.GOAL_ID), "0");
        assertEquals(tracked.get(QueryParams.ORDER_ID), "orderId");
        assertEquals(tracked.get(QueryParams.REVENUE), "100.20");
        assertEquals(tracked.get(QueryParams.SUBTOTAL), "70.02");
        assertEquals(tracked.get(QueryParams.TAX), "20.00");
        assertEquals(tracked.get(QueryParams.SHIPPING), "10.00");
        assertEquals(tracked.get(QueryParams.DISCOUNT), "0.00");

        String ecommerceItemsJson = tracked.get(QueryParams.ECOMMERCE_ITEMS);

        new JSONArray(ecommerceItemsJson); // will throw exception if not valid json

        assertTrue(ecommerceItemsJson.contains("[\"fake_sku\",\"fake_product\",\"fake_category\",\"2.00\",\"2\"]"));
        assertTrue(ecommerceItemsJson.contains("[\"fake_sku_2\",\"fake_product_2\",\"fake_category_2\",\"4.00\",\"3\"]"));
    }

    @Test
    public void testTrackException() throws Exception {
        Exception catchedException;
        try {
            throw new Exception("Test");
        } catch (Exception e) {
            catchedException = e;
        }
        assertNotNull(catchedException);
        LWTraceUtil.trace().exception(catchedException).description("<Null> exception").fatal(false).with(mLWTracer);
        verify(mLWTracer).trace(mCaptor.capture());
        assertEquals(mCaptor.getValue().get(QueryParams.EVENT_CATEGORY), "Exception");
        StackTraceElement traceElement = catchedException.getStackTrace()[0];
        assertNotNull(traceElement);
        assertEquals(mCaptor.getValue().get(QueryParams.EVENT_ACTION), "com.github.ladderwinner.extra.TrackHelperTest" + "/" + "testTrackException" + ":" + traceElement.getLineNumber());
        assertEquals(mCaptor.getValue().get(QueryParams.EVENT_NAME), "<Null> exception");
    }

    @Test
    public void testExceptionHandler() throws Exception {
        assertFalse(Thread.getDefaultUncaughtExceptionHandler() instanceof LadderWinnerExceptionHandler);
        LWTraceUtil.trace().uncaughtExceptions().with(mLWTracer);
        assertTrue(Thread.getDefaultUncaughtExceptionHandler() instanceof LadderWinnerExceptionHandler);
        try {
            //noinspection NumericOverflow
            int i = 1 / 0;
            assertNotEquals(i, 0);
        } catch (Exception e) {
            (Thread.getDefaultUncaughtExceptionHandler()).uncaughtException(Thread.currentThread(), e);
        }
        verify(mLWTracer).trace(mCaptor.capture());
        TraceMe tracked = mCaptor.getValue();
        assertEquals(tracked.get(QueryParams.EVENT_CATEGORY), "Exception");
        assertTrue(tracked.get(QueryParams.EVENT_ACTION).startsWith("com.github.ladderwinner.extra.TrackHelperTest/testExceptionHandler:"));
        assertEquals(tracked.get(QueryParams.EVENT_NAME), "/ by zero");
        assertEquals(tracked.get(QueryParams.EVENT_VALUE), "1");

        verify(mLWTracer).setDispatchMode(DispatchMode.EXCEPTION);
        verify(mLWTracer).dispatchBlocking();

        boolean exception = false;
        try {
            LWTraceUtil.trace().uncaughtExceptions().with(mLWTracer);
        } catch (RuntimeException e) {
            exception = true;
        }
        assertTrue(exception);
    }
}
