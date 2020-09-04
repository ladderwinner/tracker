package com.github.ladderwinner.dispatcher;

import com.github.ladderwinner.LWTracer;
import com.github.ladderwinner.tools.Connectivity;

public class DefaultDispatcherFactory implements DispatcherFactory {
    public Dispatcher build(LWTracer LWTracer) {
        return new DefaultDispatcher(
                new EventCache(new EventDiskCache(LWTracer)),
                new Connectivity(LWTracer.getLadderWinner().getContext()),
                new PacketFactory(LWTracer.getAPIUrl()),
                new DefaultPacketSender()
        );
    }
}
