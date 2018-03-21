package com.fanwe.lib.cache.handler;

import android.text.TextUtils;

import com.fanwe.lib.cache.IDiskInfo;
import com.fanwe.lib.cache.api.ICommonCache;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zhengjun on 2018/3/20.
 */
public abstract class CacheHandler<T> implements ICacheHandler<T>, ICommonCache<T>
{
    private IDiskInfo mDiskInfo;
    private static final Map<String, Object> MAP_MEMORY = new ConcurrentHashMap<>();

    public CacheHandler(IDiskInfo diskInfo)
    {
        if (diskInfo == null)
        {
            throw new NullPointerException("diskInfo is null");
        }
        mDiskInfo = diskInfo;
    }

    protected final IDiskInfo getDiskInfo()
    {
        return mDiskInfo;
    }

    private final String getRealKey(final String key)
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

    //---------- ICacheHandler start ----------

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
            putMemory(key, value);
        }
        return result;
    }

    @Override
    public final T getCache(String key, Class<T> clazz)
    {
        final T result = getMemory(key);
        if (result != null)
        {
            return result;
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

    //---------- ICacheHandler end ----------


    //---------- ICommonCache start ----------

    @Override
    public final boolean put(String key, T value)
    {
        return putCache(key, value);
    }

    @Override
    public final T get(String key)
    {
        return getCache(key, null);
    }

    //---------- ICommonCache end ----------


    //---------- memory start ----------

    private void putMemory(String key, Object value)
    {
        if (getDiskInfo().isMemorySupport())
        {
            final String realKey = getRealKey(key);
            MAP_MEMORY.put(realKey, value);
        }
    }

    private <T> T getMemory(String key)
    {
        if (getDiskInfo().isMemorySupport())
        {
            final String realKey = getRealKey(key);
            return (T) MAP_MEMORY.get(realKey);
        } else
        {
            return null;
        }
    }

    private void removeMemory(String key)
    {
        if (MAP_MEMORY.isEmpty())
        {
            return;
        }
        final String realKey = getRealKey(key);
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
