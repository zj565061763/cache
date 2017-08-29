package com.fanwe.www.cache;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.fanwe.library.cache.SDDiskCache;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "MainActivity";

    private String keyInt = "keyInt";
    private String keyLong = "keyLong";
    private String keyFloat = "keyFloat";
    private String keyDouble = "keyDouble";
    private String keyString = "keyString";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SDDiskCache.init(this);
        SDDiskCache.setGlobalObjectConverter(new JsonObjectConverter());

        SDDiskCache.open().putInt(keyInt, 100);
        Log.i(TAG, "int:" + SDDiskCache.open().getInt(keyInt));

        SDDiskCache.open().putLong(keyLong, 200);
        Log.i(TAG, "long:" + SDDiskCache.open().getLong(keyLong));

        SDDiskCache.open().putFloat(keyFloat, 100.123f);
        Log.i(TAG, "float:" + SDDiskCache.open().getFloat(keyFloat));

        SDDiskCache.open().putDouble(keyDouble, 200.123345d);
        Log.i(TAG, "double:" + SDDiskCache.open().getDouble(keyDouble));

        SDDiskCache.open().putString(keyString, "hello");
        Log.i(TAG, "string:" + SDDiskCache.open().getString(keyString));
    }
}
