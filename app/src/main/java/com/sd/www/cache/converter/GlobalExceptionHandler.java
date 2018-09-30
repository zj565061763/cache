package com.sd.www.cache.converter;

import android.util.Log;

import com.sd.lib.cache.Cache;

public class GlobalExceptionHandler implements Cache.ExceptionHandler
{
    @Override
    public void onException(Exception e)
    {
        Log.e(GlobalExceptionHandler.class.getSimpleName(), String.valueOf(e));
    }
}
