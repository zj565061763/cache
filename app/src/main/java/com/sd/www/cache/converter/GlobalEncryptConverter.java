package com.sd.www.cache.converter;

import android.util.Log;

import androidx.annotation.NonNull;

import com.sd.lib.cache.Cache;
import com.sd.www.cache.MainActivity;

/**
 * Created by Administrator on 2017/8/29.
 */

public class GlobalEncryptConverter implements Cache.EncryptConverter {
    @NonNull
    @Override
    public byte[] encrypt(@NonNull byte[] bytes) throws Exception {
        Log.i(MainActivity.TAG, "encrypt +++++ ");
        return bytes;
    }

    @NonNull
    @Override
    public byte[] decrypt(@NonNull byte[] bytes) throws Exception {
        Log.i(MainActivity.TAG, "encrypt ----- ");
        return bytes;
    }
}
