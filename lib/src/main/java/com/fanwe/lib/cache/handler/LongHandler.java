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
 * Long处理类
 */
public class LongHandler extends ByteConverterHandler<Long>
{
    public LongHandler(DiskInfo diskInfo)
    {
        super(diskInfo);
    }

    @Override
    protected byte[] valueToByte(Long value)
    {
        return value.toString().getBytes();
    }

    @Override
    protected Long byteToValue(byte[] bytes, Class<Long> clazz)
    {
        return Long.valueOf(new String(bytes));
    }

    @Override
    protected String getKeyPrefix()
    {
        return "long_";
    }
}
