package com.perflab.resp;

public sealed interface RespValue permits
        RespSimpleString, RespError, RespInteger, RespBulkString, RespArray, RespBoolean {
    public String encode();
};
