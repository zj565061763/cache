package com.sd.lib.cache.handler;

import com.sd.lib.cache.DiskInfo;

/**
 * Integer处理类
 */
public class IntegerHandler extends ByteConverterHandler<Integer>
{
    public IntegerHandler(DiskInfo diskInfo)
    {
        super(diskInfo);
    }

    @Override
    protected byte[] valueToByte(Integer value)
    {
        return value.toString().getBytes();
    }

    @Override
    protected Integer byteToValue(byte[] bytes, Class clazz)
    {
        return Integer.valueOf(new String(bytes));
    }

    @Override
    protected String getKeyPrefix()
    {
        return "integer_";
    }
}
