package com.fanwe.www.cache;

import com.alibaba.fastjson.JSON;
import com.fanwe.library.cache.IObjectConverter;

/**
 * Created by Administrator on 2017/8/29.
 */
public class JsonObjectConverter implements IObjectConverter
{
    @Override
    public String objectToString(Object object)
    {
        return JSON.toJSONString(object);
    }

    @Override
    public <T> T stringToObject(String string, Class<T> clazz)
    {
        return JSON.parseObject(string, clazz);
    }
}
