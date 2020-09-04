package com.github.ladderwinner.extra;

import org.junit.Test;
import org.junit.runner.RunWith;
import com.github.ladderwinner.TraceMe;
import com.github.ladderwinner.LWTracer;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DimensionQueueTest {
    LWTracer mLWTracer = mock(LWTracer.class);
    ArgumentCaptor<LWTracer.Callback> mCaptor = ArgumentCaptor.forClass(LWTracer.Callback.class);

    @Test
    public void testEmpty() {
        new DimensionQueue(mLWTracer);
        verify(mLWTracer).addTrackingCallback(mCaptor.capture());

        TraceMe pre = new TraceMe();
        TraceMe post = mCaptor.getValue().onTrack(pre);
        assertThat(post, notNullValue());
        assertThat(pre, is(post));
    }

    @Test
    public void testCallback() {
        DimensionQueue queue = new DimensionQueue(mLWTracer);
        verify(mLWTracer).addTrackingCallback(mCaptor.capture());

        queue.add(1, "test1");
        queue.add(2, "test2");
        TraceMe pre = new TraceMe();
        TraceMe post = mCaptor.getValue().onTrack(pre);
        assertThat(post, notNullValue());
        assertThat(pre, is(post));
        assertThat(CustomDimension.getDimension(post, 1), is("test1"));
        assertThat(CustomDimension.getDimension(post, 2), is("test2"));
    }

    @Test
    public void testCollision() {
        DimensionQueue queue = new DimensionQueue(mLWTracer);
        verify(mLWTracer).addTrackingCallback(mCaptor.capture());

        queue.add(1, "test1");
        TraceMe pre = new TraceMe();
        CustomDimension.setDimension(pre, 1, "don't overwrite me");
        TraceMe post = mCaptor.getValue().onTrack(pre);
        assertThat(post, notNullValue());
        assertThat(pre, is(post));
        assertThat(CustomDimension.getDimension(post, 1), is("don't overwrite me"));
    }

    @Test
    public void testOverwriting() {
        DimensionQueue queue = new DimensionQueue(mLWTracer);
        verify(mLWTracer).addTrackingCallback(mCaptor.capture());

        queue.add(1, "test1");
        queue.add(1, "test3");
        queue.add(2, "test2");
        {
            TraceMe post = mCaptor.getValue().onTrack(new TraceMe());
            assertThat(post, notNullValue());
            assertThat(CustomDimension.getDimension(post, 1), is("test1"));
            assertThat(CustomDimension.getDimension(post, 2), is("test2"));
        }
        {
            TraceMe post = mCaptor.getValue().onTrack(new TraceMe());
            assertThat(post, notNullValue());
            assertThat(CustomDimension.getDimension(post, 1), is("test3"));
            assertThat(CustomDimension.getDimension(post, 2), nullValue());
        }
    }
}
