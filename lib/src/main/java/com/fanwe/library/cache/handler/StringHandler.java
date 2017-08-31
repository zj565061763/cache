package com.fanwe.library.cache.handler;

import java.io.File;

/**
 * Created by zhengjun on 2017/8/31.
 */

public class StringHandler extends ObjectHandler<String>
{
    private static final String STRING = "string_";

    public StringHandler(File directory)
    {
        super(directory);
    }

    @Override
    public String getKeyPrefix()
    {
        return STRING;
    }

    @Override
    protected boolean onPutObject(String key, String object, File file)
    {
        return false;
    }

    @Override
    protected String onGetObject(String key, File file)
    {
        return null;
    }
}
