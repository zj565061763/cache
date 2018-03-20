package com.fanwe.lib.cache.core.converter;

/**
 * 对象转换器
 */
public interface IObjectConverter
{
    /**
     * 对象转字符串
     *
     * @param object
     * @return
     */
    String objectToString(Object object);

    /**
     * 字符串转对象
     *
     * @param string
     * @param clazz
     * @param <T>
     * @return
     */
    <T> T stringToObject(String string, Class<T> clazz);
}
