package com.fanwe.library.cache;

import android.content.Context;

/**
 * Created by Administrator on 2017/8/29.
 */

public class SDCache
{
    private static Context mContext;

    public static void init(Context context)
    {
        mContext = context.getApplicationContext();
    }

    static Context getContext()
    {
        return mContext;
    }
}
