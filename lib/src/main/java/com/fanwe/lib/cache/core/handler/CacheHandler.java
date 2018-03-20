package com.fanwe.lib.cache.core.handler;

import android.text.TextUtils;

import com.fanwe.lib.cache.core.IDiskInfo;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zhengjun on 2018/3/20.
 */
abstract class CacheHandler<T> implements ICacheHandler<T>
{
    private IDiskInfo mDiskInfo;
    private static final Map<String, Object> MAP_MEMORY = new ConcurrentHashMap<>();

    public CacheHandler(IDiskInfo diskInfo)
    {
        mDiskInfo = diskInfo;
    }

    protected final IDiskInfo getDiskInfo()
    {
        return mDiskInfo;
    }

    protected final void checkEncryptConverter()
    {
        if (getDiskInfo().isEncrypt() && getDiskInfo().getEncryptConverter() == null)
        {
            throw new NullPointerException("you must provide an IEncryptConverter instance before this");
        }
    }

    protected final void checkObjectConverter()
    {
        if (getDiskInfo().getObjectConverter() == null)
        {
            throw new NullPointerException("you must provide an IObjectConverter instance before this");
        }
    }

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

    public final String getRealKey(final String key)
    {
        if (TextUtils.isEmpty(key))
        {
            throw new IllegalArgumentException("key is null or empty");
        }
        final String prefix = getKeyPrefix();
        if (TextUtils.isEmpty(prefix))
        {
            throw new IllegalArgumentException("key prefix is null or empty");
        }

        return prefix + MD5(key);
    }

    private File getCacheFile(String key)
    {
        final String realKey = getRealKey(key);
        final File dir = getDiskInfo().getDirectory();

        return new File(dir, realKey);
    }

    @Override
    public final boolean putCache(String key, T value)
    {
        if (value == null)
        {
            removeCache(key);
            return true;
        }

        final boolean result = putCacheImpl(key, value, getCacheFile(key));
        if (result)
        {
            putMemoryIfNeed(key, value);
        }
        return result;
    }

    @Override
    public final T getCache(String key, Class clazz)
    {
        if (getDiskInfo().isMemorySupport())
        {
            T result = getMemory(key);
            if (result != null) return result;
        }

        final File file = getCacheFile(key);
        if (!file.exists())
        {
            return null;
        }
        return getCacheImpl(key, clazz, file);
    }

    @Override
    public final boolean hasCache(String key)
    {
        return getCacheFile(key).exists();
    }

    @Override
    public final boolean removeCache(String key)
    {
        removeMemory(key);

        final File file = getCacheFile(key);
        if (file.exists())
        {
            return file.delete();
        } else
        {
            return true;
        }
    }

    //---------- memory start ----------

    private void putMemoryIfNeed(String key, Object value)
    {
        if (getDiskInfo().isMemorySupport())
        {
            MAP_MEMORY.put(getRealKey(key), value);
        }
    }

    private <T> T getMemory(String key)
    {
        if (getDiskInfo().isMemorySupport())
        {
            return (T) MAP_MEMORY.get(getRealKey(key));
        } else
        {
            return null;
        }
    }

    private void removeMemory(String key)
    {
        MAP_MEMORY.remove(getRealKey(key));
    }

    //---------- memory end ----------

    protected abstract String getKeyPrefix();

    protected abstract boolean putCacheImpl(String key, T value, File file);

    protected abstract T getCacheImpl(String key, Class clazz, File file);
}
