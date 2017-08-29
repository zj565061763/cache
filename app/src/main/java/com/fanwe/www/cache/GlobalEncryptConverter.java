package com.fanwe.www.cache;

import com.fanwe.library.cache.IEncryptConverter;

/**
 * Created by Administrator on 2017/8/29.
 */

public class GlobalEncryptConverter implements IEncryptConverter
{
    @Override
    public String encrypt(String content)
    {
        return AESUtil.encrypt(content); //加密
    }

    @Override
    public String decrypt(String content)
    {
        return AESUtil.decrypt(content); //解密
    }
}
