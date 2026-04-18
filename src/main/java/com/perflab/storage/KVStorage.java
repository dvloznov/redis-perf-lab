package com.perflab.storage;

import java.util.HashMap;
import java.util.Map;

public abstract class KVStorage {
    public static final Map<String, String> STORAGE = new HashMap<>();

    public static void set(String key, String value) {
        STORAGE.put(key, value);
    }

    public static String get(String key) {
        return STORAGE.get(key);
    }
}
