package com.perflab.commands;

import java.util.Set;

import com.perflab.resp.RespError;
import com.perflab.resp.RespValue;

public abstract class CommandDispatcher {
    private static final Set<AbstractHandler> HANDLERS = Set.of(
            new PingHandler(),
            new EchoHandler(),
            new SetHandler(),
            new GetHandler(),
            new ConfigHandler());

    public static RespValue handle(RespValue command) {
        var res = HANDLERS.stream().filter(handler -> handler.shouldHandle(command)).findAny();
        if (res.isEmpty()) {
            return new RespError("Unknown command");
        }

        return res.get().handle(command);
    }

}
