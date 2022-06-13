package com.sd.www.cache.converter;

import androidx.annotation.NonNull;

import com.sd.lib.cache.Cache;

/**
 * Created by Administrator on 2017/8/29.
 */

public class GlobalEncryptConverter implements Cache.EncryptConverter {
    @Override
    public byte[] encrypt(@NonNull byte[] bytes) {
        // 加密
        return bytes;
    }

    @Override
    public byte[] decrypt(@NonNull byte[] bytes) {
        // 解密
        return bytes;
    }
}
