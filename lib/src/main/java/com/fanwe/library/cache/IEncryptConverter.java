package com.fanwe.library.cache;

/**
 * 加解密转换器
 */
public interface IEncryptConverter
{
    /**
     * 加密字符串
     *
     * @param content
     * @return
     */
    String encrypt(String content);

    /**
     * 解密字符串
     *
     * @param content
     * @return
     */
    String decrypt(String content);
}
