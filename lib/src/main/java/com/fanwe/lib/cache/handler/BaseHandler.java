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

import android.text.TextUtils;

import com.fanwe.lib.cache.Disk;
import com.fanwe.lib.cache.DiskInfo;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * 缓存处理基类
 */
public abstract class BaseHandler<T> implements CacheHandler<T>, Disk.CommonCache<T>
{
    private final DiskInfo mDiskInfo;
    private static final Map<String, Object> MAP_MEMORY = new HashMap<>();

    public BaseHandler(DiskInfo diskInfo)
    {
        if (diskInfo == null)
            throw new NullPointerException("diskInfo is null");

        mDiskInfo = diskInfo;
    }

    protected final DiskInfo getDiskInfo()
    {
        return mDiskInfo;
    }

    private final String getRealKey(final String key, boolean encrypt)
    {
        if (TextUtils.isEmpty(key))
            throw new IllegalArgumentException("key is null or empty");

        final String prefix = getKeyPrefix();
        if (TextUtils.isEmpty(prefix))
            throw new IllegalArgumentException("key prefix is null or empty");

        if (encrypt)
            return prefix + MD5(key);
        else
            return prefix + key;
    }

    private File getCacheFile(String key)
    {
        final String realKey = getRealKey(key, true);
        final File dir = getDiskInfo().getDirectory();
        return new File(dir, realKey);
    }

    //---------- CacheHandler start ----------

    @Override
    public final boolean putCache(String key, T value)
    {
        synchronized (Disk.class)
        {
            if (value == null)
            {
                removeCache(key);
                return true;
            }

            final File file = getCacheFile(key);
            final boolean result = putCacheImpl(key, value, file);

            if (result)
                putMemoryIfNeed(key, value);

            return result;
        }
    }

    @Override
    public final T getCache(String key, Class<T> clazz)
    {
        synchronized (Disk.class)
        {
            final Object result = getMemory(key);
            if (result != null)
                return (T) result;

            final File file = getCacheFile(key);
            if (!file.exists())
                return null;

            final T cache = getCacheImpl(key, clazz, file);
            if (cache != null)
                putMemoryIfNeed(key, cache);

            return cache;
        }
    }

    @Override
    public final boolean removeCache(String key)
    {
        synchronized (Disk.class)
        {
            removeMemory(key);

            final File file = getCacheFile(key);
            if (file.exists())
                return file.delete();
            else
                return true;
        }
    }

    //---------- CacheHandler end ----------


    //---------- CommonCache start ----------

    @Override
    public final boolean put(String key, T value)
    {
        return putCache(key, value);
    }

    @Override
    public final T get(String key)
    {
        return get(key, null);
    }

    @Override
    public final T get(String key, T defaultValue)
    {
        final T cache = getCache(key, null);
        return cache == null ? defaultValue : cache;
    }

    //---------- CommonCache end ----------


    //---------- memory start ----------

    private void putMemoryIfNeed(String key, Object value)
    {
        if (getDiskInfo().isMemorySupport())
        {
            final String realKey = getRealKey(key, false);
            MAP_MEMORY.put(realKey, value);
        }
    }

    private Object getMemory(String key)
    {
        if (getDiskInfo().isMemorySupport())
        {
            final String realKey = getRealKey(key, false);
            return MAP_MEMORY.get(realKey);
        }
        return null;
    }

    private void removeMemory(String key)
    {
        if (MAP_MEMORY.isEmpty())
            return;

        final String realKey = getRealKey(key, false);
        MAP_MEMORY.remove(realKey);
    }

    //---------- memory end ----------

    protected abstract String getKeyPrefix();

    protected abstract boolean putCacheImpl(String key, T value, File file);

    protected abstract T getCacheImpl(String key, Class<T> clazz, File file);

    //---------- utils start ----------

    private static String MD5(String value)
    {
        String result;
        try
        {
            final MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(value.getBytes());
            byte[] bytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++)
            {
                String hex = Integer.toHexString(0xFF & bytes[i]);
                if (hex.length() == 1)
                {
                    sb.append('0');
                }
                sb.append(hex);
            }
            result = sb.toString();
        } catch (NoSuchAlgorithmException e)
        {
            result = null;
        }
        return result;
    }

    //---------- utils end ----------
}
