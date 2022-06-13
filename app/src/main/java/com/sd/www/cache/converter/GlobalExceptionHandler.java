package com.sd.www.cache.converter;

import android.util.Log;

import androidx.annotation.NonNull;

import com.sd.lib.cache.Cache;
import com.sd.www.cache.MainActivity;

public class GlobalExceptionHandler implements Cache.ExceptionHandler {
    @Override
    public void onException(@NonNull Exception e) {
        Log.e(MainActivity.TAG, String.valueOf(e));
    }
}
