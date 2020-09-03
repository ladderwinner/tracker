package com.lw.sdk.dispatcher;

import com.lw.sdk.LWTracer;
import com.lw.sdk.tools.Connectivity;

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
