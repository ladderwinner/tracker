package com.lw.sdk.extra;

import org.junit.Test;
import org.junit.runner.RunWith;
import com.lw.sdk.TraceMe;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.UUID;

import testhelpers.BaseTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class CustomDimensionTest extends BaseTest {

    @Test
    public void testSetCustomDimensions() throws Exception {
        TraceMe traceMe = new TraceMe();
        CustomDimension.setDimension(traceMe, 0, "foo");
        CustomDimension.setDimension(traceMe, 1, "foo");
        CustomDimension.setDimension(traceMe, 2, "bar");
        CustomDimension.setDimension(traceMe, 3, "empty");
        CustomDimension.setDimension(traceMe, 3, null);
        CustomDimension.setDimension(traceMe, 4, "");


        assertEquals("foo", traceMe.get("dimension1"));
        assertEquals("bar", traceMe.get("dimension2"));
        assertNull(traceMe.get("dimension0"));
        assertNull(traceMe.get("dimension3"));
        assertNull(traceMe.get("dimension4"));
    }

    @Test
    public void testSet_truncate() throws Exception {
        TraceMe traceMe = new TraceMe();
        CustomDimension.setDimension(traceMe, 1, new String(new char[1000]));
        assertEquals(255, traceMe.get("dimension1").length());
    }

    @Test
    public void testSet_badId() throws Exception {
        TraceMe traceMe = new TraceMe();
        CustomDimension.setDimension(traceMe, 0, UUID.randomUUID().toString());
        assertTrue(traceMe.isEmpty());
    }

    @Test
    public void testSet_removal() throws Exception {
        TraceMe traceMe = new TraceMe();
        CustomDimension.setDimension(traceMe, 1, UUID.randomUUID().toString());
        assertFalse(traceMe.isEmpty());
        CustomDimension.setDimension(traceMe, 1, null);
        assertTrue(traceMe.isEmpty());
    }

    @Test
    public void testSet_empty() throws Exception {
        TraceMe traceMe = new TraceMe();
        CustomDimension.setDimension(traceMe, 1, UUID.randomUUID().toString());
        assertFalse(traceMe.isEmpty());
        CustomDimension.setDimension(traceMe, 1, "");
        assertTrue(traceMe.isEmpty());
    }
}
