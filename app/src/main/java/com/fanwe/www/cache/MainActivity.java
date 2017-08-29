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


        SDDiskCache.open().putString(key, "hello");
        printString();

        TestModel model = new TestModel();
        SDDiskCache.open().putObject(model);
        printObject();
    }

    private void printString()
    {
        Log.i(TAG, "string:" + SDDiskCache.open().hasString(key) + "," + SDDiskCache.open().getString(key));
    }

    private void printObject()
    {
        Log.i(TAG, "object:" + SDDiskCache.open().hasObject(TestModel.class) + "," + SDDiskCache.open().getObject(TestModel.class));
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        SDDiskCache.open().delete();
        SDDiskCache.openCache().delete();
    }
}
