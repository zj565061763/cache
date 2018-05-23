package com.fanwe.www.cache.converter;

import com.fanwe.lib.cache.converter.EncryptConverter;
import com.fanwe.www.cache.AESUtil;

/**
 * Created by Administrator on 2017/8/29.
 */

public class GlobalEncryptConverter implements EncryptConverter
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
