package com.sd.lib.cache.handler;

import com.sd.lib.cache.DiskInfo;

/**
 * Double处理类
 */
public class DoubleHandler extends BaseCacheHandler<Double>
{
    public DoubleHandler(DiskInfo diskInfo)
    {
        super(diskInfo);
    }

    @Override
    protected byte[] valueToByte(Double value)
    {
        return value.toString().getBytes();
    }

    @Override
    protected Double byteToValue(byte[] bytes, Class clazz)
    {
        return Double.valueOf(new String(bytes));
    }

    @Override
    protected String getKeyPrefix()
    {
        return "double_";
    }
}
