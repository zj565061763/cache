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
package com.fanwe.lib.cache.api;

import com.fanwe.lib.cache.DiskInfo;
import com.fanwe.lib.cache.handler.SerializableHandler;

import java.io.Serializable;

/**
 * 序列化缓存
 */
public class SimpleSerializableCache implements SerializableCache
{
    private final DiskInfo mDiskInfo;

    public SimpleSerializableCache(DiskInfo diskInfo)
    {
        mDiskInfo = diskInfo;
    }

    @Override
    public <T extends Serializable> boolean put(T value)
    {
        if (value == null)
            return false;

        final String key = value.getClass().getName();
        return new SerializableHandler<T>(mDiskInfo).putCache(key, value);
    }

    @Override
    public <T extends Serializable> T get(Class<T> clazz)
    {
        final String key = clazz.getName();
        return new SerializableHandler<T>(mDiskInfo).getCache(key, clazz);
    }

    @Override
    public <T extends Serializable> boolean remove(Class<T> clazz)
    {
        final String key = clazz.getName();
        return new SerializableHandler<T>(mDiskInfo).removeCache(key);
    }
}
