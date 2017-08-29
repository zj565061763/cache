package com.fanwe.www.cache;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.fanwe.library.cache.SDDiskCache;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "MainActivity";

    private String key = "key";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SDDiskCache.init(this);
        SDDiskCache.setGlobalObjectConverter(new JsonObjectConverter());

        SDDiskCache.openFile().putInt(key, 100);
        printInt();
        SDDiskCache.openFile().removeInt(key);
        printInt();

        SDDiskCache.openFile().putLong(key, 200);
        printLong();
        SDDiskCache.openFile().removeLong(key);
        printLong();

        SDDiskCache.openFile().putFloat(key, 100.123f);
        printFloat();
        SDDiskCache.openFile().removeFloat(key);
        printFloat();

        SDDiskCache.openFile().putDouble(key, 200.123345d);
        printDouble();
        SDDiskCache.openFile().removeDouble(key);
        printDouble();

        SDDiskCache.openFile().putString(key, "hello");
        printString();
        SDDiskCache.openFile().removeString(key);
        printString();
    }

    private void printInt()
    {
        Log.i(TAG, "int:" + SDDiskCache.openFile().getInt(key, 0));
    }

    private void printLong()
    {
        Log.i(TAG, "long:" + SDDiskCache.openFile().getLong(key, 0));
    }

    private void printFloat()
    {
        Log.i(TAG, "float:" + SDDiskCache.openFile().getFloat(key, 0));
    }

    private void printDouble()
    {
        Log.i(TAG, "double:" + SDDiskCache.openFile().getDouble(key, 0));
    }

    private void printString()
    {
        Log.i(TAG, "string:" + SDDiskCache.openFile().getString(key));
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        SDDiskCache.openFile().delete();
        SDDiskCache.openCache().delete();
    }
}
