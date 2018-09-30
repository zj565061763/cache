package com.sd.lib.cache.store;

import android.text.TextUtils;

import com.sd.lib.cache.DiskInfo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 文件缓存实现类
 */
public class FileCacheStore implements CacheStore
{
    protected final File getCacheFile(String key, DiskInfo info)
    {
        key = transformKey(key);
        if (TextUtils.isEmpty(key))
            throw new NullPointerException();

        return new File(info.getDirectory(), key);
    }

    protected String transformKey(String key)
    {
        return MD5(key);
    }

    @Override
    public boolean putCache(String key, byte[] value, DiskInfo info)
    {
        final File file = getCacheFile(key, info);

        OutputStream out = null;
        try
        {
            out = new BufferedOutputStream(new FileOutputStream(file));
            out.write(value);
            out.flush();
            return true;
        } catch (Exception e)
        {
            info.getExceptionHandler().onException(e);
            return false;
        } finally
        {
            closeQuietly(out);
        }
    }

    @Override
    public byte[] getCache(String key, Class clazz, DiskInfo info)
    {
        final File file = getCacheFile(key, info);

        InputStream in = null;
        ByteArrayOutputStream out = null;
        try
        {
            in = new BufferedInputStream(new FileInputStream(file));
            out = new ByteArrayOutputStream();

            final byte[] buffer = new byte[1024];
            int length = -1;
            while (true)
            {
                length = in.read(buffer);
                if (length < 0)
                    break;
                out.write(buffer, 0, length);
            }
            return out.toByteArray();
        } catch (Exception e)
        {
            info.getExceptionHandler().onException(e);
            return null;
        } finally
        {
            closeQuietly(out);
            closeQuietly(in);
        }
    }

    @Override
    public boolean removeCache(String key, DiskInfo info)
    {
        final File file = getCacheFile(key, info);

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

    private static void closeQuietly(Closeable closeable)
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
