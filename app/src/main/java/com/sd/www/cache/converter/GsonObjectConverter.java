package com.sd.www.cache.converter;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.sd.lib.cache.Cache;

/**
 * Created by Administrator on 2017/8/29.
 */
public class GsonObjectConverter implements Cache.ObjectConverter {
    private final Gson gson = new Gson();

    @NonNull
    @Override
    public byte[] objectToByte(@NonNull Object value) throws Exception {
        return gson.toJson(value).getBytes();
    }

    @NonNull
    @Override
    public <T> T byteToObject(@NonNull byte[] bytes, @NonNull Class<T> clazz) throws Exception {
        return gson.fromJson(new String(bytes), clazz);
    }
}
