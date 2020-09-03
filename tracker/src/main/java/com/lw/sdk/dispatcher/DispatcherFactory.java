package com.lw.sdk.dispatcher;

import com.lw.sdk.LWTracer;

public interface DispatcherFactory {
    Dispatcher build(LWTracer LWTracer);
}
