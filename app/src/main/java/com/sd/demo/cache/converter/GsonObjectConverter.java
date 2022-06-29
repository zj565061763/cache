package com.sd.demo.cache.converter;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.sd.lib.cache.Cache;
import com.sd.demo.cache.MainActivity;

/**
 * Created by Administrator on 2017/8/29.
 */
public class GsonObjectConverter implements Cache.ObjectConverter {
    private final Gson gson = new Gson();

    @NonNull
    @Override
    public byte[] objectToByte(@NonNull Object value) throws Exception {
        Log.i(MainActivity.TAG, "objectToByte +++++ ");
        return gson.toJson(value).getBytes();
    }

    @NonNull
    @Override
    public <T> T byteToObject(@NonNull byte[] bytes, @NonNull Class<T> clazz) throws Exception {
        Log.i(MainActivity.TAG, "byteToObject ----- " + clazz);
        return gson.fromJson(new String(bytes), clazz);
    }
}
