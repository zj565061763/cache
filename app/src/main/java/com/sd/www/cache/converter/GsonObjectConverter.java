package com.sd.www.cache.converter;

import com.sd.lib.cache.Disk;
import com.google.gson.Gson;

/**
 * Created by Administrator on 2017/8/29.
 */
public class GsonObjectConverter implements Disk.ObjectConverter
{
    private static final Gson GSON = new Gson();

    @Override
    public byte[] objectToByte(Object object)
    {
        // 对象转byte
        return GSON.toJson(object).getBytes();
    }

    @Override
    public <T> T byteToObject(byte[] bytes, Class<T> clazz)
    {
        // byte转对象
        return GSON.fromJson(new String(bytes), clazz);
    }
}
