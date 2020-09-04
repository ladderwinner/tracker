package com.github.ladderwinner.dispatcher;

import com.github.ladderwinner.LWTracer;

public interface DispatcherFactory {
    Dispatcher build(LWTracer LWTracer);
}
