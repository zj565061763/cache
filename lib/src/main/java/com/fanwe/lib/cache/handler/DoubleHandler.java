package com.fanwe.lib.cache.handler;

import com.fanwe.lib.cache.DiskInfo;

/**
 * Double处理类
 */
public class DoubleHandler extends ByteConverterHandler<Double>
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
    protected Double byteToValue(byte[] bytes, Class<Double> clazz)
    {
        return Double.valueOf(new String(bytes));
    }

    @Override
    protected String getKeyPrefix()
    {
        return "double_";
    }
}
