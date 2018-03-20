package com.fanwe.lib.cache.core.handler;

import com.fanwe.lib.cache.core.IDiskInfo;

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
public class SerializableHandler<T extends Serializable> extends CacheHandler<T>
{
    public SerializableHandler(IDiskInfo diskInfo)
    {
        super(diskInfo);
    }

    @Override
    protected String getKeyPrefix()
    {
        return "serializable_";
    }

    @Override
    protected boolean putCacheImpl(String key, T value, File file)
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
            e.printStackTrace();
        } finally
        {
            closeQuietly(os);
        }
        return false;
    }

    @Override
    protected T getCacheImpl(String key, Class<T> clazz, File file)
    {
        ObjectInputStream is = null;
        try
        {
            is = new ObjectInputStream(new FileInputStream(file));
            return (T) is.readObject();
        } catch (Exception e)
        {
            e.printStackTrace();
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
}
