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

        SDDiskCache.open().setObjectConverter(new JsonObjectConverter()).putString(keyString, "hello");
        String valueString = SDDiskCache.open().setObjectConverter(new JsonObjectConverter()).getString(keyString);

        Log.i(TAG, "String:" + valueString);
    }
}
