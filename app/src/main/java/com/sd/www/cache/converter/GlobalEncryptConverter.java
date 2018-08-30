package com.sd.www.cache.converter;

import com.sd.lib.cache.Disk;

/**
 * Created by Administrator on 2017/8/29.
 */

public class GlobalEncryptConverter implements Disk.EncryptConverter
{
    @Override
    public byte[] encrypt(byte[] bytes)
    {
        // 加密
        return bytes;
    }

    @Override
    public byte[] decrypt(byte[] bytes)
    {
        // 解密
        return bytes;
    }
}
