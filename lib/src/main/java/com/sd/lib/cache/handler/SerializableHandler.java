package com.sd.lib.cache.handler;

import com.sd.lib.cache.Disk;
import com.sd.lib.cache.DiskInfo;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * 序列化处理类
 */
public class SerializableHandler extends BaseHandler<Serializable> implements Disk.SerializableCache
{
    public SerializableHandler(DiskInfo diskInfo)
    {
        super(diskInfo);
    }

    @Override
    protected String getKeyPrefix()
    {
        return "serializable_";
    }

    @Override
    protected boolean putCacheImpl(String key, Serializable value, File file)
    {
        ObjectOutputStream os = null;
        try
        {
            os = new ObjectOutputStream(new FileOutputStream(file));
            os.writeObject(value);
            os.flush();
            return true;
        } catch (Exception e)
        {
            getDiskInfo().getExceptionHandler().onException(e);
        } finally
        {
            closeQuietly(os);
        }
        return false;
    }

    @Override
    protected Serializable getCacheImpl(String key, Class clazz, File file)
    {
        ObjectInputStream is = null;
        try
        {
            is = new ObjectInputStream(new FileInputStream(file));
            return (Serializable) is.readObject();
        } catch (Exception e)
        {
            getDiskInfo().getExceptionHandler().onException(e);
        } finally
        {
            closeQuietly(is);
        }
        return null;
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

    @Override
    public <T extends Serializable> boolean put(T value)
    {
        final String key = value.getClass().getName();
        return putCache(key, value);
    }

    @Override
    public <T extends Serializable> T get(Class<T> clazz)
    {
        final String key = clazz.getName();
        return (T) getCache(key, clazz);
    }

    @Override
    public <T extends Serializable> boolean remove(Class<T> clazz)
    {
        final String key = clazz.getName();
        return removeCache(key);
    }
}
