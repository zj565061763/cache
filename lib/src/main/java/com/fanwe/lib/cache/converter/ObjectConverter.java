package com.fanwe.lib.cache.converter;

/**
 * 对象转换器
 */
public interface ObjectConverter
{
    /**
     * 对象转byte
     *
     * @param object
     * @return
     */
    byte[] objectToByte(Object object);

    /**
     * byte转对象
     *
     * @param bytes
     * @param clazz
     * @param <T>
     * @return
     */
    <T> T byteToObject(byte[] bytes, Class<T> clazz);
}
