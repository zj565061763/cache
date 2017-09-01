package com.fanwe.www.cache.converter;

import com.fanwe.library.cache.IObjectConverter;
import com.google.gson.Gson;

/**
 * Created by Administrator on 2017/8/29.
 */
public class GsonObjectConverter implements IObjectConverter
{
    private static final Gson GSON = new Gson();

    @Override
    public String objectToString(Object object)
    {
        return GSON.toJson(object); //对象转json
    }

    @Override
    public <T> T stringToObject(String string, Class<T> clazz)
    {
        return GSON.fromJson(string, clazz); //json转对象
    }
}
