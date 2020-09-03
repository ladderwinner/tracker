package com.lw.sdk;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import testhelpers.BaseTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


@RunWith(MockitoJUnitRunner.class)
public class TraceMeTest extends BaseTest {
    @Test
    public void testSourcingFromOtherTrackMe() throws Exception {
        TraceMe base = new TraceMe();
        for (QueryParams param : QueryParams.values()) {
            String testValue = UUID.randomUUID().toString();
            base.set(param, testValue);
        }

        TraceMe offSpring = new TraceMe(base);
        for (QueryParams param : QueryParams.values()) {
            assertEquals(base.get(param), offSpring.get(param));
        }
    }

    @Test
    public void testAdd_overwrite() {
        TraceMe a = new TraceMe();
        a.set(QueryParams.URL_PATH, "pathA");
        a.set(QueryParams.EVENT_NAME, "name");
        TraceMe b = new TraceMe();
        b.set(QueryParams.URL_PATH, "pathB");
        a.putAll(b);
        assertEquals("pathB", a.get(QueryParams.URL_PATH));
        assertEquals("pathB", b.get(QueryParams.URL_PATH));
        assertEquals("name", a.get(QueryParams.EVENT_NAME));

        b.putAll(a);
        assertEquals("pathB", a.get(QueryParams.URL_PATH));
        assertEquals("pathB", b.get(QueryParams.URL_PATH));
        assertEquals("name", a.get(QueryParams.EVENT_NAME));
        assertEquals("name", b.get(QueryParams.EVENT_NAME));

    }

    @Test
    public void testSet() throws Exception {
        TraceMe traceMe = new TraceMe();
        traceMe.set(QueryParams.HOURS, "String");
        assertEquals("String", traceMe.get(QueryParams.HOURS));

        traceMe = new TraceMe();
        traceMe.set(QueryParams.HOURS, 1f);
        assertEquals(String.valueOf(1f), traceMe.get(QueryParams.HOURS));

        traceMe = new TraceMe();
        traceMe.set(QueryParams.HOURS, 1L);
        assertEquals(String.valueOf(1L), traceMe.get(QueryParams.HOURS));

        traceMe = new TraceMe();
        traceMe.set(QueryParams.HOURS, 1);
        assertEquals(String.valueOf(1), traceMe.get(QueryParams.HOURS));

        traceMe = new TraceMe();
        traceMe.set(QueryParams.HOURS, null);
        assertNull(traceMe.get(QueryParams.HOURS));
    }

    @Test
    public void testTrySet() throws Exception {
        TraceMe traceMe = new TraceMe();
        traceMe.trySet(QueryParams.HOURS, "A");
        traceMe.trySet(QueryParams.HOURS, "B");
        assertEquals("A", traceMe.get(QueryParams.HOURS));

        traceMe = new TraceMe();
        traceMe.trySet(QueryParams.HOURS, 1f);
        traceMe.trySet(QueryParams.HOURS, 2f);
        assertEquals(String.valueOf(1f), traceMe.get(QueryParams.HOURS));

        traceMe = new TraceMe();
        traceMe.trySet(QueryParams.HOURS, 1L);
        traceMe.trySet(QueryParams.HOURS, 2L);
        assertEquals(String.valueOf(1L), traceMe.get(QueryParams.HOURS));

        traceMe = new TraceMe();
        traceMe.trySet(QueryParams.HOURS, 1);
        traceMe.trySet(QueryParams.HOURS, 2);
        assertEquals(String.valueOf(1), traceMe.get(QueryParams.HOURS));

        traceMe = new TraceMe();
        traceMe.trySet(QueryParams.HOURS, "A");
        traceMe.trySet(QueryParams.HOURS, null);
        assertNotNull(traceMe.get(QueryParams.HOURS));
    }

    @Test
    public void testSetAll() throws Exception {
        TraceMe traceMe = new TraceMe();
        Map<QueryParams, String> testValues = new HashMap<>();
        for (QueryParams param : QueryParams.values()) {
            String testValue = UUID.randomUUID().toString();
            traceMe.set(param, testValue);
            testValues.put(param, testValue);
        }
        assertEquals(QueryParams.values().length, testValues.size());

        for (QueryParams param : QueryParams.values()) {
            assertTrue(traceMe.has(param));
            assertEquals(testValues.get(param), traceMe.get(param));
        }
        for (QueryParams param : QueryParams.values()) {
            traceMe.set(param, null);
            assertFalse(traceMe.has(param));
            assertNull(traceMe.get(param));
        }
    }
}
