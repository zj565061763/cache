package com.fanwe.lib.cache.converter;

/**
 * 加解密转换器
 */
public interface EncryptConverter
{
    /**
     * 加密数据
     *
     * @param bytes
     * @return
     */
    byte[] encrypt(byte[] bytes);

    /**
     * 解密数据
     *
     * @param bytes
     * @return
     */
    byte[] decrypt(byte[] bytes);
}
