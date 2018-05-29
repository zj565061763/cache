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

import com.fanwe.lib.cache.Disk;
import com.fanwe.lib.cache.DiskInfo;

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
public class SerializableHandler<T extends Serializable> extends BaseHandler<T>
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
            final Disk.ExceptionHandler handler = getDiskInfo().getExceptionHandler();
            if (handler != null) handler.onException(e);
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
            final Disk.ExceptionHandler handler = getDiskInfo().getExceptionHandler();
            if (handler != null) handler.onException(e);
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
