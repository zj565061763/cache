package com.sd.lib.cache.store;

import android.text.TextUtils;

import com.sd.lib.cache.Cache;
import com.sd.lib.cache.CacheInfo;

import java.io.Closeable;
import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 文件缓存
 */
public abstract class DiskCacheStore implements Cache.CacheStore
{
    private final File mDirectory;

    public DiskCacheStore(File directory)
    {
        if (directory == null)
            throw new IllegalArgumentException("directory is null when create: " + getClass().getName());
        mDirectory = directory;
    }

    private File getDirectory()
    {
        if (mDirectory.exists() || mDirectory.mkdirs())
            return mDirectory;

        return null;
    }

    private File getCacheFile(String key, CacheInfo info)
    {
        key = transformKey(key);
        if (TextUtils.isEmpty(key))
            throw new NullPointerException("transformKey() return null when getCacheFile()");

        final File dir = getDirectory();
        if (dir == null)
        {
            info.getExceptionHandler().onException(new RuntimeException("directory is not available:" + mDirectory.getAbsolutePath()));
            return null;
        }

        return new File(dir, key);
    }

    protected String transformKey(String key)
    {
        return MD5(key);
    }

    @Override
    public final boolean putCache(String key, byte[] value, CacheInfo info)
    {
        final File file = getCacheFile(key, info);
        if (file == null)
            return false;

        try
        {
            return putCacheImpl(key, value, file);
        } catch (Exception e)
        {
            info.getExceptionHandler().onException(e);
            return false;
        }
    }

    @Override
    public final byte[] getCache(String key, Class<?> clazz, CacheInfo info)
    {
        final File file = getCacheFile(key, info);
        if (file == null)
            return null;

        try
        {
            return getCacheImpl(key, clazz, file);
        } catch (Exception e)
        {
            info.getExceptionHandler().onException(e);
            return null;
        }
    }

    @Override
    public final boolean removeCache(String key, CacheInfo info)
    {
        final File file = getCacheFile(key, info);
        if (file == null)
            return false;

        try
        {
            return removeCacheImpl(key, file);
        } catch (Exception e)
        {
            info.getExceptionHandler().onException(e);
            return false;
        }
    }

    @Override
    public final boolean containsCache(String key, CacheInfo info)
    {
        final File file = getCacheFile(key, info);
        if (file == null)
            return false;

        return file.exists();
    }

    protected abstract boolean putCacheImpl(String key, byte[] value, File file) throws Exception;

    protected abstract byte[] getCacheImpl(String key, Class<?> clazz, File file) throws Exception;

    protected boolean removeCacheImpl(String key, File file) throws Exception
    {
        return file.exists() ? file.delete() : false;
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

    protected static void closeQuietly(Closeable closeable)
    {
        if (closeable != null)
        {
            try
            {
                closeable.close();
            } catch (Throwable ignored)
            {
            }
        }
    }
}
