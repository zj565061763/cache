package com.fanwe.lib.cache.handler;

import com.fanwe.lib.cache.DiskInfo;

/**
 * Float处理类
 */
public class FloatHandler extends ByteConverterHandler<Float>
{
    public FloatHandler(DiskInfo diskInfo)
    {
        super(diskInfo);
    }

    @Override
    protected byte[] valueToByte(Float value)
    {
        return value.toString().getBytes();
    }

    @Override
    protected Float byteToValue(byte[] bytes, Class<Float> clazz)
    {
        return Float.valueOf(new String(bytes));
    }

    @Override
    protected String getKeyPrefix()
    {
        return "float_";
    }
}
