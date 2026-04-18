package com.perflab.commands;

import com.perflab.resp.RespArray;
import com.perflab.resp.RespBulkString;
import com.perflab.resp.RespValue;
import com.perflab.storage.KVStorage;

public class GetHandler extends AbstractHandler {

    @Override
    protected boolean shouldHandle(RespValue request) {
        if (!(request instanceof RespArray)) {
            return false;
        }
        RespArray arrayRequest = (RespArray) request;
        if (arrayRequest.values().size() != 2) {
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
        String key = ((RespBulkString) ((RespArray) request).values().get(1)).value();
        String value = KVStorage.get(key);
        return new RespBulkString(value);
    }

    @Override
    protected String getCommand() {
        return "GET";
    }

}
