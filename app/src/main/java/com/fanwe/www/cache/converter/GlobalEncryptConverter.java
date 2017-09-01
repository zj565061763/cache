package com.fanwe.www.cache.converter;

import com.fanwe.library.cache.IEncryptConverter;
import com.fanwe.www.cache.AESUtil;

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
