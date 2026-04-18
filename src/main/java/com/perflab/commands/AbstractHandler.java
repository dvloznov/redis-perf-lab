package com.perflab.commands;

import com.perflab.resp.RespValue;

public abstract class AbstractHandler {

    protected abstract boolean shouldHandle(RespValue request);

    protected abstract RespValue handle(RespValue request);

    protected abstract String getCommand();

    protected boolean shouldHandleCommand(String reqCommand) {
        return getCommand().toLowerCase().equals(reqCommand.toLowerCase());
    }

}
