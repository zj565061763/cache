package com.sd.www.cache.converter;

import androidx.annotation.NonNull;

import com.sd.lib.cache.Cache;

/**
 * Created by Administrator on 2017/8/29.
 */

public class GlobalEncryptConverter implements Cache.EncryptConverter {
    @NonNull
    @Override
    public byte[] encrypt(@NonNull byte[] bytes) throws Exception {
        return bytes;
    }

    @NonNull
    @Override
    public byte[] decrypt(@NonNull byte[] bytes) throws Exception {
        return bytes;
    }
}
