package com.fanwe.www.cache.converter;

import com.fanwe.lib.cache.converter.ObjectConverter;
import com.google.gson.Gson;

/**
 * Created by Administrator on 2017/8/29.
 */
public class GsonObjectConverter implements ObjectConverter
{
    private static final Gson GSON = new Gson();

    @Override
    public byte[] objectToByte(Object object)
    {
        // 对象转二进制
        return GSON.toJson(object).getBytes();
    }

    @Override
    public <T> T byteToObject(byte[] bytes, Class<T> clazz)
    {
        // 二进制转对象
        return GSON.fromJson(new String(bytes), clazz);
    }
}
