package com.perflab.commands;

import java.util.List;

import com.perflab.resp.RespArray;
import com.perflab.resp.RespBulkString;
import com.perflab.resp.RespValue;

public class ConfigHandler extends AbstractHandler {

    @Override
    protected boolean shouldHandle(RespValue request) {
        if (!(request instanceof RespArray)) {
            return false;
        }
        RespArray arrayRequest = (RespArray) request;
        if (arrayRequest.values().size() != 3) {
            return false;
        }

        RespValue firstCommand = arrayRequest.values().get(0);
        if (!(firstCommand instanceof RespBulkString)) {
            return false;
        }

        RespBulkString firstCommandAsString = (RespBulkString) firstCommand;

        if (!shouldHandleCommand(firstCommandAsString.value())) {
            return false;
        }

        return true;
    }

    @Override
    protected RespValue handle(RespValue request) {
        return new RespArray(List.of());
    }

    @Override
    protected String getCommand() {
        return "CONFIG";
    }

}
