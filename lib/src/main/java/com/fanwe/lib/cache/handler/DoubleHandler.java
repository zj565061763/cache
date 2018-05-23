/*
 * Copyright (C) 2017 zhengjun, fanwe (http://www.fanwe.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
